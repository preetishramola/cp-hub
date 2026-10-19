I built CP-Hub to enourage the competitive programming culture in my college . It's basically a platform where users integrate the coding profiles and we list them among other users 
they can see there rank and there is a blog section too for any discussion over a contest ..

## Configuration

All secrets come from environment variables. See `.env.example` for the full list:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (32+ characters) and `CORS_ALLOWED_ORIGINS`.
The server listens on `PORT` (default 8080).

## Deploy

The included `Dockerfile` builds and runs the app, so any Docker host (e.g. Render) works.
Set the environment variables above in the host's dashboard.
