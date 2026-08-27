# 🐘 Ganesh Utsav Expense Tracker

A full-stack web app for managing finances of a community Ganesh Festival (Ganpati) celebration.

**Stack:** React (Vite) + Tailwind CSS · Spring Boot (Java 17) · MySQL

---

## 1. Project Structure

```
ganesh-utsav/
├── backend/          Spring Boot REST API
├── frontend/         React + Vite + Tailwind app
└── database/
    └── schema.sql    MySQL schema + seed admin login
```

---

## 2. Database Setup (MySQL)

1. Install MySQL 8+ and make sure it's running.
2. Create the database and tables:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   This creates `ganesh_utsav_db` and seeds one committee login:
   - **username:** `admin`
   - **password:** `Admin@123`

   > Change this password immediately after your first login using the **Change Password** button in the sidebar (or `POST /api/auth/change-password`).

You don't strictly need to run this script by hand — with `spring.jpa.hibernate.ddl-auto=update` (already set), Spring Boot will create/update the tables automatically on first run. But you'll still need to insert at least one committee member manually or via `/api/auth/register` (see below) so you have a login.

---

## 3. Backend Setup (Spring Boot)

**Requirements:** Java 17+, Maven 3.8+

1. Open `backend/src/main/resources/application.properties` and set your real MySQL username/password:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```
2. **Important:** change `app.jwt.secret` to a long random string before deploying anywhere real.
3. Run it:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The API starts on **http://localhost:8080**.

4. If you didn't run `schema.sql`, you'll need at least one login seeded directly in the database (see step 2) — there's no public endpoint that creates accounts. This is deliberate: an earlier version of this API had a public `/api/auth/register` endpoint that accepted a committee's "Ganesh Unique Code" as proof of authorization, but that same code is shown publicly on the `/public/transparency/{tenantCode}` page, so it never should have doubled as a login credential. That endpoint has been removed. Once you have one PRESIDENT login (seeded via SQL, or created by a Developer account through `/api/committees`), all further staff accounts are added from the app itself: **Committee → Add Member** (`POST /api/members`, PRESIDENT-only, scoped to their own committee).

### Key API endpoints
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Get JWT token |
| POST | `/api/auth/change-password` | Required | Change your own password |
| GET | `/api/dashboard/summary` | Required | Totals, charts, recent transactions |
| GET/POST/PUT/DELETE | `/api/donations` | Required | Collections CRUD + search |
| GET/POST/PUT/DELETE | `/api/expenses` | Required | Expenses CRUD (multipart for bill upload) |
| GET | `/api/reports/pdf` `/api/reports/excel` | Required | Download balance sheet |
| GET | `/api/public/transparency` | **Public, no auth** | Aggregate totals only, no donor PII |
| GET/POST | `/api/members` | Required | Committee management |

---

## 4. Frontend Setup (React + Vite + Tailwind)

**Requirements:** Node.js 18+

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173** and proxies all `/api/*` calls to `http://localhost:8080` (see `vite.config.js`) — so make sure the backend is running first.

Login with the seeded `admin` / `Admin@123` (or whatever account you registered).

### Pages
- `/` — Dashboard (summary cards including carry-forward & grand total, pie + bar charts, recent transactions)
- `/festival-setup` — Festival year setup: carry-forward balance, start date, duration (President creates/edits; everyone can view)
- `/collections` — Donations: add, search/filter, printable receipts
- `/expenses` — Expenses: add/edit/delete, category filter, day number, bill upload; a note is required for Miscellaneous/Gift Distribution entries
- `/auction` — Auction / Velampata: item, winner, bid amount, payment status/mode, optional festival day
- `/loans` — Post-festival micro-lending (**President only**): create loans, record repayments with automatic reducing-balance interest calculation, see the full interest/principal split per payment
- `/reports` — Download PDF/Excel balance sheet for any date range
- `/members` — Committee management
- `/sponsorships` — **President only.** Sponsorship Management, with three sections in one page (tabs):
  - **Categories** — CRUD master page for sponsorship categories (e.g. "Vigraha Dhata" / Idol Sponsor, "Laddu Dhata" / Laddu Sponsor)
  - **General Sponsors** — add/edit/delete sponsors, each assigned a category via a dropdown that reads live from the Categories tab
  - **Annadanam Sponsors** — a dedicated, separate table/interface for food-distribution sponsors, tracked per festival day since Annadanam is a daily activity
- `/public` — **No login required.** Share this link with donors for transparency (totals + category breakdown only, no names/phones).

### Roles & permissions (multi-tenant)
The platform now supports **multiple, fully isolated Ganesh Committees**. Five roles exist:
- **Developer (Super Admin)** — global, platform-level access. Only the Developer can register new committees (each gets a unique, regenerable "Ganesh Unique Code"), view the cross-committee Developer Dashboard, and lock/unlock a committee's access. A Developer has no committee of their own and cannot edit any single committee's day-to-day data.
- **President (Committee Admin)** — full CRUD within their own committee only: Festival Setup, Sponsorship Categories, Annadanam Sponsors, Micro-Lending, and staff management.
- **Treasurer / Secretary / Volunteer (Staff)** — operational access within their own committee only: logging donations, recording expenses, managing auctions.

**No committee can ever see or modify another committee's data.** This is enforced server-side on every request (never from anything the client sends) — see `docs/MULTI_TENANCY_ARCHITECTURE.md` for the full design, including the database schema, a complete RBAC matrix, and the exact request-flow security model.

**Developer seed account** (created directly in `database/schema.sql` — there is no API endpoint that can create a Developer account, by design):
- **username:** `ganeshdev`
- **password:** `GaneshDev@2026`

Change this password after first login using the **Change Password** button in the sidebar.

### How the loan interest calculation works
Interest accrues monthly on the **reducing balance** (not the original principal). Every time a repayment is recorded:
1. The system counts whole months since the loan's last interest date (loan start, or the previous repayment).
2. `interest due = current principal × (monthly rate / 100) × months elapsed`
3. If the payment covers the interest, the excess reduces the principal. If it doesn't, the whole payment is treated as interest and the principal is untouched.
4. The "last interest date" moves to the new payment date, so the next round of interest is calculated only on the new, lower principal.

Example matching the spec: ₹10,000 at 2%/month. After 6 months, a ₹6,200 payment is made → interest = ₹1,200 → ₹5,000 goes to principal → new principal = ₹5,000. Future interest accrues only on that ₹5,000.

---

## 5. Customizing the Festival Name & Branding

- The header text "Ganesh Utsav" is set via the `festivalName` prop passed into `<Layout>` in each page (defaults to `"Ganesh Utsav"`). Change the default in `frontend/src/components/layout/Layout.jsx`, or thread a setting through if you want it editable from the UI.
- Colors (saffron / maroon / gold) are defined as reusable tokens in `frontend/tailwind.config.js` — tweak the hex values there to adjust the whole theme at once.

---

## 6. Production Notes / Next Steps

This is a solid, working foundation — a few things worth doing before real deployment:
- **Set a real `JWT_SECRET` environment variable** before deploying (see Section 3) — without it, the backend generates a random secret at startup that changes on every restart, invalidating all sessions.
- **Add pagination** to the donations/expenses tables once you have hundreds of entries.
- **Serve uploaded bill files** via a dedicated static resource handler or object storage (S3-compatible) instead of the local `uploads/bills` folder, especially if you deploy on ephemeral hosting.
- **HTTPS everywhere** in production — JWTs over plain HTTP are not secure.
