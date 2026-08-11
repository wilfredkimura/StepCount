# StepCount FastAPI Backend

Asynchronous REST API for the **StepCount** Android application built with **FastAPI**, **NeonDB PostgreSQL**, and **Firebase Authentication**.

---

## 1. Local Development Setup (with `venv`)

### 1.1 Create and Activate Virtual Environment
```powershell
# Open terminal in the backend directory
cd backend

# Create virtual environment
python -m venv venv

# Activate on Windows (PowerShell)
.\venv\Scripts\Activate.ps1

# (If PowerShell script execution is restricted, run: Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass)
```

### 1.2 Install Dependencies
```bash
pip install -r requirements.txt
```

### 1.3 Configure Environment (`.env`)
Create a `.env` file in the `backend/` directory (copy from `.env.example`):
```env
ENVIRONMENT=development
DATABASE_URL=postgresql+asyncpg://neondb_owner:YOUR_PASSWORD@ep-super-wildflower-axeau.us-east-2.aws.neon.tech/neondb?ssl=require
FIREBASE_CREDENTIALS_PATH=serviceAccountKey.json
CORS_ORIGINS=["*"]
```

### 1.4 Run the Backend Server
```bash
uvicorn app.main:app --reload --port 8000
```
- Interactive API Documentation (Swagger UI): `http://127.0.0.1:8000/docs`
- Alternative Documentation (ReDoc): `http://127.0.0.1:8000/redoc`
- Health Check: `http://127.0.0.1:8000/health`

---

## 2. Deploying on Render (Free Tier)

Render natively runs FastAPI applications without Docker on its free tier:

1. **Create a New Web Service on Render**:
   - Connect your GitHub repository.
   - Set **Root Directory**: `backend`
   - Set **Environment**: `Python 3`
   - Set **Build Command**: `pip install -r requirements.txt`
   - Set **Start Command**: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`

2. **Add Environment Variables in Render Dashboard**:
   - `DATABASE_URL`: `postgresql+asyncpg://neondb_owner:YOUR_PASSWORD@ep-super-wildflower-axeau.us-east-2.aws.neon.tech/neondb?ssl=require`
   - `ENVIRONMENT`: `production`
   - `FIREBASE_CREDENTIALS_JSON`: *(Paste the entire contents of your Firebase `serviceAccountKey.json` here as a raw JSON string)*

3. **Deploy**:
   - Click **Create Web Service**. Render will install dependencies and start your API.

---

## 3. Running Automated Tests

```bash
# Run pytest with test coverage report
pytest -v --cov=app --cov-report=term-missing

# Run code style and typing checks
ruff check .
mypy app
```
