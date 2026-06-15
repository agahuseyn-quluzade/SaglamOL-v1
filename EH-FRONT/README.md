# SaglamOL Frontend

Next.js 15 App Router frontend for the SaglamOL e-health insurance platform.

## Stack

- Next.js 15, React 19, TypeScript 5
- Zustand for auth/theme/sidebar state
- TanStack Query for server state
- Axios API client with token refresh interceptor
- React Hook Form and Zod for validation
- Recharts for dashboard charts
- Vanilla CSS design tokens

## Run

```bash
npm install
npm run dev
```

The app expects the gateway at `NEXT_PUBLIC_API_URL=http://localhost:8080`.

## Routes

- `/login`, `/register`, `/password-reset/request`, `/password-reset/confirm`
- `/patient/*`
- `/hospital/*`
- `/insurance/*`
- `/admin/*`

The portal routes currently use mock data but are wired around the endpoint map from `frontend_architecture.md`.
