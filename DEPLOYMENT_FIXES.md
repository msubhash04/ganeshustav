# Deployment fixes applied

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
