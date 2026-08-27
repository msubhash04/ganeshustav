# Deployment fixes applied

## 1. Tenant Inspection Mode ("View as President") — new feature
Developer can now drill into any single committee's full dashboard from
the Committee Directory (`Eye` icon on each row):

- **Read-Only Mode**: view every screen a President sees. Every non-GET
  request is rejected with 403 by `InspectionModeFilter`, regardless of
  which controller it hits.
- **Admin Override Mode**: additionally grants `ROLE_PRESIDENT` for the
  duration of the session (`JwtAuthFilter`), so the Developer can create/
  edit/delete on the committee's behalf. Every successful mutating
  request is written to `inspection_audit_logs`.
- **Excluded in both modes**: `/api/members` (staff management) and
  `/api/committees` (lock/unlock, regenerate code) — a Developer
  inspecting a committee can never touch those, even in Admin Override.
- Session start/end is always logged; per-action logs only happen in
  Admin Override mode (Read-Only can't mutate anything, so there's
  nothing to log beyond the session itself).
- Implemented as a short-lived (30 min) second JWT carrying
  `inspectedCommitteeId` + `inspectionMode` claims, resolved by
  `TenantContext.requireCommittee()` — every existing domain service
  (Donations, Expenses, Loans, Sponsorships, Auction, Festival Years)
  needed zero changes, since they all already went through that one
  method.
- New endpoints: `POST /api/developer/inspect/{committeeId}`,
  `POST /api/developer/inspect/exit`, `GET /api/developer/inspect/history`.

## 0. Security fixes (latest)
Three issues fixed:

1. **Public self-registration removed.** `POST /api/auth/register` accepted
   a committee's "Ganesh Unique Code" (`tenantCode`) as authorization to
   create a TREASURER/SECRETARY/VOLUNTEER login for that committee. But
   that exact code is displayed publicly on the `/public/transparency/{tenantCode}`
   donor page (`PublicController`) — so anyone who saw a committee's public
   transparency link could self-register as staff and get full CRUD access
   to that committee's donations/expenses. The endpoint is removed
   entirely; staff accounts are now created only via the existing,
   properly-scoped `POST /api/members` (authenticated PRESIDENT, own
   committee only via `TenantContext` — this always existed and needed no
   changes).
2. **Added a "change password" flow.** `POST /api/auth/change-password`
   (any authenticated role) plus a **Change Password** button in the
   sidebar. Seeded/admin passwords (`admin`, `ganeshdev`) should be changed
   here immediately instead of via raw SQL.
3. **Removed the hardcoded JWT secret fallback from source control.**
   `application.properties` no longer ships a real base64 secret as the
   default for `app.jwt.secret`. If `JWT_SECRET` isn't set, `JwtUtil` now
   generates a random secret at startup (logged as a loud warning) that's
   fine for local dev but invalidates all tokens on every restart —
   forcing you to set a real `JWT_SECRET` before deploying anywhere real,
   instead of silently reusing a value that was sitting in the repo.

**Action required:** if you already deployed with the old default secret,
rotate it now (`openssl rand -base64 64`) on Render's `JWT_SECRET` env var.


## 1. Frontend was ignoring `VITE_API_BASE_URL` (the actual bug causing the 405)
`frontend/src/api/axiosClient.js` had `baseURL: '/api'` hardcoded, so it never
read the `VITE_API_BASE_URL` env var you set in Vercel. In production this
meant every request went to `https://ganeshustav.vercel.app/api/...` instead
of your Render backend, and Vercel returned 405 (no such route exists there).

Fixed to:
```js
baseURL: `${import.meta.env.VITE_API_BASE_URL || ''}/api`
```
Local dev is unaffected — if `VITE_API_BASE_URL` isn't set, it falls back to
`/api`, which still goes through the Vite dev proxy to `localhost:8090`
defined in `vite.config.js`.

**You must trigger a fresh Vercel deployment** after this change (and after
confirming the `VITE_API_BASE_URL` env var is set for Production) — Vite env
vars are baked in at build time, not read at runtime.

## 2. JWT secret was a hardcoded placeholder committed to the repo
`backend/src/main/resources/application.properties` had a real secret value
sitting in source control. JWT auth itself was already fully implemented
(`JwtUtil`, `JwtAuthFilter`, login endpoint) — nothing new was built there,
just secured.

Now reads from an env var with a generated fallback for local dev:
```
app.jwt.secret=${JWT_SECRET:<generated-64-byte-base64-secret>}
```

**Action required:** On Render, set an environment variable:
```
JWT_SECRET=<generate your own, e.g. `openssl rand -base64 64`>
```
Do NOT reuse the fallback value in the properties file for production —
generate a fresh one and keep it only in Render's env var settings.

## 3. CORS only allowed `localhost:5173`
Your deployed Vercel frontend would have been blocked by CORS even after
fix #1. Now configurable via env var, defaulting to include your Vercel URL:
```
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173,https://ganeshustav.vercel.app}
```
**Action required:** On Render, set:
```
CORS_ALLOWED_ORIGINS=https://ganeshustav.vercel.app
```
(comma-separate if you add a custom domain later).

## 4. Database config was hardcoded to local MySQL (root/root)
Would have connected to nothing on Render. Now reads from env vars:
```
spring.datasource.url=${DB_URL:...}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
```
**Action required:** On Render, set:
```
DB_URL=jdbc:mysql://<your-aiven-host>:<port>/<your-db-name>?useSSL=true&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
DB_USERNAME=<your-aiven-username>
DB_PASSWORD=<your-aiven-password>
```
Get these exact values from your Aiven service overview page. Aiven requires
SSL, so make sure `useSSL=true` (not `false`) in the URL.

## 5. Server port
Render assigns a port via the `PORT` env var at runtime. Changed:
```
server.port=${PORT:8090}
```
Render sets this automatically — no action needed from you here.

---

## Summary: env vars to set on Render (backend)
| Key | Value |
|---|---|
| `JWT_SECRET` | your own generated secret (`openssl rand -base64 64`) |
| `CORS_ALLOWED_ORIGINS` | `https://ganeshustav.vercel.app` |
| `DB_URL` | your Aiven JDBC URL |
| `DB_USERNAME` | your Aiven DB username |
| `DB_PASSWORD` | your Aiven DB password |

## Summary: env vars to set on Vercel (frontend)
| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://ganeshustav.onrender.com` |

After setting these, redeploy **both** the frontend (Vercel) and backend
(Render) for the changes to take effect.
