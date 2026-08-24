# StepCount Keep-Alive Cron Service

A lightweight, serverless scheduled ping job designed to run on **Vercel Cron** to prevent the StepCount FastAPI backend (hosted on Render / cold-start cloud hosts) from spinning down due to inactivity.

---

## Why This Exists

Free-tier cloud hosts (such as Render) put web instances to sleep after 15 minutes of inactivity. When a mobile user opens the app to register or sync steps, a sleeping server incurs a **50-second cold start delay**, causing network timeouts or dropped registration events.

This service sends an automated HTTP ping to `/health` every **10 minutes**, keeping the backend instance permanently warm and responsive 24/7.

---

## Deployment to Vercel (1-Click Setup)

### Option A: Via Vercel Web Dashboard

1. Push this project folder to your GitHub / GitLab repository.
2. Log into [Vercel Dashboard](https://vercel.com) and click **Add New Project**.
3. Select the repository.
4. Under **Project Settings**:
   - **Framework Preset:** Other
   - **Root Directory:** Edit $\rightarrow$ select `vercel-cron` $\rightarrow$ click Continue.
5. Under **Environment Variables**, add:
   - **Name:** `BACKEND_URL`
   - **Value:** `https://your-render-backend.onrender.com/health` (or your root URL)
6. Click **Deploy**.

Vercel will automatically detect `vercel.json` and schedule the cron job to run every 10 minutes.

---

### Option B: Via Vercel CLI

```bash
cd vercel-cron
npx vercel
```

Set `BACKEND_URL` in the project settings on Vercel Dashboard once deployed.

---

## Verifying Execution

- In the Vercel Dashboard, navigate to **Deployments $\rightarrow$ Project Settings $\rightarrow$ Cron Jobs** to view real-time logs, execution timestamps, and HTTP response times.
