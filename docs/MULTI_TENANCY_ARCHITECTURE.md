# Ganesh Utsav Management Application — Multi-Tenancy & Access Hierarchy

This document covers the four things requested for the Multi-Tenancy (Multi-Committee Isolation) and Super Admin (Developer) module: the updated database schema, the RBAC matrix, the API request/security flow, and the Developer Dashboard layout. It reflects what is actually implemented in the codebase, not an aspirational design — every table, column, and endpoint named below exists in `backend/` and `database/schema.sql`.

No previously defined feature was removed. Carry-Forward Balance, Day-wise Expenses, Annadanam/General Sponsorships, Velampata/Auction, and Reducing-Balance Micro-Lending all continue to work exactly as before — they now simply live *underneath* a Committee, instead of being global.

---

## 1. Multi-Tenant Database Schema

### 1.1 Entity-Relationship Overview

```
                              ┌─────────────────────┐
                              │      committees      │   ← TENANT ROOT
                              │  (Ganesh Committees)  │
                              ├─────────────────────┤
                              │ id (PK)               │
                              │ tenant_code (UNIQUE)  │  "Ganesh Unique Code"
                              │ name, city, state      │
                              │ address                │
                              │ active                 │  locked by Developer post-festival
                              │ created_by_developer_id│──┐
                              └───────────┬────────────┘  │
                                          │ 1                │ (FK, nullable)
              ┌───────────────────────────┼─────────────────┼──────────────────────────┐
              │                           │                 │                          │
              ▼ N                         ▼ N               │                          ▼ N
     ┌─────────────────┐        ┌──────────────────┐        │                 ┌──────────────────┐
     │     members       │        │  festival_years   │        │                 │ sponsorship_       │
     │ (Users)           │        │ (Years)            │        │                 │ categories          │
     ├─────────────────┤        ├──────────────────┤        │                 ├──────────────────┤
     │ id (PK)            │        │ id (PK)             │        │                 │ id (PK)             │
     │ committee_id (FK,  │◄───────│ committee_id (FK)   │        │                 │ committee_id (FK)   │
     │   NULL for         │  NULL  │ label, year         │        │                 │ name  (UNIQUE per   │
     │   DEVELOPER only)  │  only  │ start_date          │        │                 │        committee)   │
     │ role (ENUM incl.   │  for   │ duration_days       │        │                 │ active              │
     │   DEVELOPER)       │  DEV   │ carry_forward_bal.  │        │                 └─────────┬────────┘
     │ username, password │        │ active              │        │                           │ 1
     └─────────────────┘        └─────────┬──────────┘        │                           ▼ N
                                            │ 1                  │                 ┌──────────────────┐
                       ┌────────────────────┼────────────────────┼─────────────────┤ general_sponsors    │
                       │                    │                    │                 ├──────────────────┤
                       ▼ N                  ▼ N                  │                 │ id (PK)             │
              ┌─────────────────┐  ┌──────────────────┐        │                 │ committee_id (FK)   │
              │    donations      │  │    expenses        │        │                 │ category_id (FK)    │
              │   (Income)        │  │                    │        │                 │ festival_year_id    │
              ├─────────────────┤  ├──────────────────┤        │                 │ sponsor_name         │
              │ id (PK)            │  │ id (PK)             │        │                 │ contribution_amount │
              │ committee_id (FK)  │  │ committee_id (FK)   │        │                 └──────────────────┘
              │ festival_year_id   │  │ festival_year_id    │        │
              │ donor_name         │  │ description         │        │                 ┌──────────────────┐
              │ amount             │  │ category, amount    │        └─────────────────┤ annadanam_sponsors  │
              │ receipt_number     │  │ day_number, note     │                          ├──────────────────┤
              └─────────────────┘  └──────────────────┘                          │ id (PK)             │
                                                                                    │ committee_id (FK)   │
                       ┌────────────────────┬────────────────────┐                 │ festival_year_id    │
                       ▼ N                  ▼ N                  ▼ N                │ day_number, meal_slot│
              ┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐      └──────────────────┘
              │  auction_items    │  │      loans         │  │  loan_repayments   │
              │  (Velampata)      │  │                    │  ├──────────────────┤
              ├─────────────────┤  ├──────────────────┤  │ id (PK)             │
              │ id (PK)            │  │ id (PK)             │  │ loan_id (FK)  ─────►│  (no direct committee_id -
              │ committee_id (FK)  │  │ committee_id (FK)   │  │ payment_amount      │   scoped via parent Loan)
              │ festival_year_id   │  │ original_principal  │  │ interest_portion    │
              │ item_name          │  │ current_principal    │  │ principal_portion   │
              │ winner_name        │  │ monthly_interest_%   │  │ remaining_principal │
              │ bid_amount         │  │ status               │  └──────────────────┘
              │ payment_status     │  └──────────────────┘
              └─────────────────┘
```

### 1.2 The core design decision: relate on `id`, not `tenant_code`

Every tenant-scoped table stores a `committee_id BIGINT NOT NULL` foreign key pointing at `committees.id` — the immutable surrogate primary key. **Nothing joins on `tenant_code`.** This matters because the Developer Dashboard supports *regenerating* the Ganesh Unique Code (a security/admin action if a code ever leaks). If any other table stored `tenant_code` directly, regenerating it would either break every foreign key relationship or require a cascading update across every transactional table in the system. By keying on `id` instead, `tenant_code` is free to change at any time with zero downstream impact — it's purely a human-facing label.

### 1.3 Table reference

| Table | Tenant column | Notes |
|---|---|---|
| `committees` | *(is the tenant root)* | `tenant_code`, `name`, `city`, `state`, `address`, `active` (lock flag) |
| `members` | `committee_id` **nullable** | `NULL` only for `role = 'DEVELOPER'`. Role enum: `DEVELOPER, PRESIDENT, TREASURER, SECRETARY, VOLUNTEER` |
| `festival_years` | `committee_id` NOT NULL | `label` unique per committee (not globally) |
| `donations` | `committee_id` NOT NULL | plus `festival_year_id` |
| `expenses` | `committee_id` NOT NULL | plus `festival_year_id`, `day_number`, `note` |
| `auction_items` | `committee_id` NOT NULL | plus `festival_year_id`, `day_number` |
| `loans` | `committee_id` NOT NULL | plus `festival_year_id` |
| `loan_repayments` | *(none — derived via `loan_id`)* | a repayment is only ever reached through its parent Loan, which is already committee-scoped, so a redundant FK was intentionally omitted |
| `sponsorship_categories` | `committee_id` NOT NULL | `name` unique **per committee**, via `UNIQUE(committee_id, name)` — two different committees can both have a "Laddu Dhata" category |
| `general_sponsors` | `committee_id` NOT NULL | plus `category_id`, `festival_year_id` |
| `annadanam_sponsors` | `committee_id` NOT NULL | plus `festival_year_id`, `day_number` |

Full DDL lives in `database/schema.sql`.

---

## 2. Role-Based Access Control (RBAC) Matrix

| Module / Action | Developer (Super Admin) | President | Treasurer / Secretary / Volunteer |
|---|:---:|:---:|:---:|
| **Create a new Ganesh Committee** | ✅ (only role that can) | ❌ | ❌ |
| **Generate / regenerate Ganesh Unique Code** | ✅ | ❌ | ❌ |
| **Lock / unlock a committee** | ✅ | ❌ | ❌ |
| **View Committee Directory (all committees)** | ✅ | ❌ | ❌ |
| **View Developer global dashboard (all-committee totals)** | ✅ | ❌ | ❌ |
| **View/edit own committee's data** | ❌ (Developer has no committee) | ✅ (own committee only) | ✅ operational only |
| **View another committee's data** | ✅ (via Committee Directory, aggregate only) | ❌ never | ❌ never |
| **Festival Year: create / edit dates & duration / carry-forward** | ❌ | ✅ | ❌ (view only) |
| **Collections (Donations): add / edit / delete** | ❌ | ✅ | ✅ |
| **Expenses: add / edit / delete** | ❌ | ✅ | ✅ |
| **Sponsorship Categories: CRUD** | ❌ | ✅ | ❌ (view only) |
| **General Sponsors: CRUD** | ❌ | ✅ | ❌ (view only) |
| **Annadanam Sponsors: CRUD** | ❌ | ✅ | ❌ (view only) |
| **Auction / Velampata: CRUD** | ❌ | ✅ | ✅ |
| **Micro-Lending: create loan / record repayment** | ❌ | ✅ only | ❌ |
| **Committee staff (Members): add / deactivate / remove** | ❌ | ✅ own committee only | ❌ |
| **Reports (PDF/Excel export)** | ❌ | ✅ | ✅ |
| **Public Transparency page** | *(n/a — public, no login)* | *(n/a — public, no login)* | *(n/a — public, no login)* |

Two design notes worth calling out:

- **The Developer intentionally cannot directly edit any committee's day-to-day data** (donations, expenses, loans, etc.). Their role is platform oversight — creating committees and viewing aggregate/monitoring data — not operating a specific committee. This mirrors the spec: "Only the Developer can create new Ganesh Committees... can monitor, view, and manage all Ganesh Committees," which is about tenant lifecycle management, not day-to-day bookkeeping.
- **Staff (Treasurer/Secretary/Volunteer) have operational access only** — they can log collections and expenses but cannot touch Festival Setup, Sponsorship Categories, or Micro-Lending, which remain President-only, matching "Committee Member (Staff)... operational access... logging collections, recording daily expenses" from the spec.

---

## 3. Multi-Tenant API Request Flow & Security Model

### 3.1 The core security principle

**The backend never trusts a client-supplied committee ID for authorization decisions — ever.** Every tenant-scoping check is re-derived server-side, on every single request, from the authenticated user's own account record in the database. This is what makes cross-tenant data leakage structurally impossible rather than just "unlikely":

```
1. Login:      POST /api/auth/login  { username, password }
               → backend loads the Member row, including its committee
               → backend issues a JWT containing:
                   • subject:    username
                   • role:       e.g. "PRESIDENT"
                   • tenantCode: e.g. "GU-MH-PUN-0001"   (DISPLAY ONLY)

2. Every subsequent request:
               Authorization: Bearer <jwt>

3. JwtAuthFilter (server-side, on every request):
               → extracts username from the JWT
               → re-loads the Member fresh from the database (with committee
                 eagerly joined)
               → rejects the request if the member is deactivated, OR if
                 their committee has been locked by the Developer
               → sets the loaded Member object as the Spring Security
                 principal (not just a username string)

4. Every service method (Donations, Expenses, Loans, etc.):
               Long committeeId = tenantContext.requireCommitteeId();
               → reads the committee ID directly off the authenticated
                 Member loaded in step 3 — NEVER from a request parameter,
                 path variable, or JWT claim
               → every repository query is scoped with WHERE committee_id
                 = :committeeId

5. Update/delete of a specific record (e.g. PUT /api/donations/42):
               → the record is loaded by its id first
               → tenantContext.assertOwnedByCurrentTenant(record.getCommittee())
                 is called BEFORE any mutation - throws 403 if the record
                 belongs to a different committee than the caller's own
```

### 3.2 Why the JWT's `tenantCode` claim is not a security mechanism

The JWT does carry a `tenantCode` claim, but purely so the frontend can display "which committee am I logged into" without an extra round-trip. If a malicious or buggy client tampered with that claim, it would have **zero effect** — no server-side code path ever reads the tenant from the JWT payload for an authorization decision. The only two ways a `committee_id` value used in a query is ever determined are:

1. **Looked up fresh from the database**, via the Member row tied to the JWT's `username` claim (which *is* cryptographically verified, since the JWT is signed).
2. For the Developer role specifically, explicit path parameters like `/api/committees/{id}` — but these are only reachable because the *endpoint itself* is gated by `@PreAuthorize("hasRole('DEVELOPER')")`, and Developer-facing endpoints are the only ones designed to operate across tenants at all.

### 3.3 Example: how a President's request to another committee's data is rejected

```
GET /api/donations/9001          (donation #9001 belongs to Committee B)
Authorization: Bearer <valid JWT for a President of Committee A>

→ DonationController → DonationService.getById(9001)
→ loads Donation #9001 from DB → its committee_id = B's id
→ tenantContext.assertOwnedByCurrentTenant(donation.getCommittee())
    compares B's id against the caller's own committee (A's id, read from
    the authenticated Member — never from anything the client sent)
→ mismatch → AccessDeniedException → HTTP 403
```

No committee ID appears anywhere in that request URL or body for the backend to have manipulated in the first place — the isolation is enforced entirely server-side.

### 3.4 Public (unauthenticated) endpoints are the one exception, by design

`GET /api/public/transparency/{tenantCode}` is intentionally public and *does* take a tenant identifier from the URL, because it's meant to be shared with donors who aren't logged in at all. This is safe because:
- It's **read-only** and returns only aggregate totals (no donor names, phone numbers, or addresses)
- `tenantCode` here identifies *which committee's public page to show*, not an authorization bypass — there's nothing sensitive being protected by tenant isolation at this endpoint in the first place

---

## 4. Developer (Super Admin) Dashboard — Layout & Components

### 4.1 Page structure

The Developer Dashboard is a **standalone shell**, separate from the committee-scoped `Layout` component used by President/Staff — a Developer has no committee, so none of the normal sidebar items (Collections, Expenses, Festival Setup, etc.) would make sense or would even load successfully for them.

```
┌──────────────────────────────────────────────────────────────────┐
│  🐘 Ganesh Utsav Platform · Developer Super Admin      [👤] [⎋]   │  ← header
├──────────────────────────────────────────────────────────────────┤
│  Global Overview                          [+ Manage Committees]   │
│                                                                    │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐                    │
│  │ Total        │ │ Active       │ │ Active Utsavs│                    │  ← Section A:
│  │ Registered   │ │ Committees   │ │ (This Year)  │                    │    Global Overview
│  │ Committees   │ │              │ │              │                    │    Widgets
│  └────────────┘ └────────────┘ └────────────┘                    │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐                    │
│  │ Total        │ │ Total        │ │ Total Lent   │                    │
│  │ Collections  │ │ Expenses     │ │ Money        │                    │
│  │ (All Comm.)  │ │ (All Comm.)  │ │ (All Comm.)  │                    │
│  └────────────┘ └────────────┘ └────────────┘                    │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│  ← Committee Directory                     [+ Register Committee] │
├──────────────────────────────────────────────────────────────────┤
│  [Search name/code___] [City____] [State____] [🔍 Filter]         │  ← Section B:
│                                                                    │    Committee
│  Committee          Code            City/State   Members  Status  │    Directory
│  ───────────────────────────────────────────────────────────────  │    (searchable,
│  Shivaji Nagar...   GU-MH-PUN-0001  Pune, MH      6        Active │    filterable)
│  Ganesh Mandal...   GU-KA-BLR-0002  Bangalore,KA  4        Active │
│  ...                                              [↻][🔒/🔓]      │  ← Tenant Management
└──────────────────────────────────────────────────────────────────┘    Actions (per row)
```

### 4.2 Component breakdown

| Component | Backend endpoint | Purpose |
|---|---|---|
| Global Overview Widgets (6 cards) | `GET /api/developer/dashboard/overview` | Total Registered Committees, Active Committees, Active Utsavs This Year, Total Collections (all), Total Expenses (all), Total Lent Money (all) |
| Committee Directory table | `GET /api/committees?query=&city=&state=` | Searchable/filterable list; each row shows name, Ganesh Unique Code, city/state, member count, registration date, active/locked status |
| Register New Committee (modal) | `POST /api/committees` | Creates the committee **and** its initial President account in one atomic call; auto-generates the Ganesh Unique Code |
| Regenerate Code (row action) | `POST /api/committees/{id}/regenerate-code` | Issues a new Ganesh Unique Code; old code stops resolving immediately |
| Lock / Unlock (row action) | `PUT /api/committees/{id}/lock` / `/unlock` | Locking immediately invalidates that committee's members' active sessions (enforced in `JwtAuthFilter`, not just at next login) |

### 4.3 What's intentionally *not* in the Developer Dashboard

Per-committee activity logs (a detailed audit trail beyond the current member count / totals) and a "Developer views a specific committee's live data as read-only" impersonation mode are natural extensions of this design but are **not yet implemented** — the current build gives the Developer creation, directory/search, code management, and lock/unlock, plus global aggregate totals, which covers everything explicitly specified. Building a full per-tenant audit log or an impersonation/read-as-tenant view would be a reasonable next phase if needed.
