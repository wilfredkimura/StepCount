# StepCount – Firebase Authentication Integration Tutorial

A complete, step-by-step guide to setting up and integrating **Firebase Authentication** across both the **Android (Kotlin) app** and the **FastAPI Backend (with NeonDB PostgreSQL)**.

---

## 1. Overview of the Authentication Flow

```
┌────────────────────────────────┐
│   1. Android App (Compose UI)  │
│   User enters Email & Password │
└──────────────┬─────────────────┘
               │ (1) Authenticate
               ▼
┌────────────────────────────────┐
│    2. Firebase Auth Cloud      │
│   Verifies creds & returns     │
│   Firebase ID Token (JWT)      │
└──────────────┬─────────────────┘
               │ (2) Fresh ID Token
               ▼
┌────────────────────────────────┐
│   3. Android Retrofit Client   │
│   Attaches ID Token in Header: │
│   Authorization: Bearer <token>│
└──────────────┬─────────────────┘
               │ (3) POST /api/auth/firebase-login
               ▼
┌────────────────────────────────┐
│    4. FastAPI Backend Server   │
│   Verifies Token via Firebase  │
│   Admin SDK (serviceAccount)   │
└──────────────┬─────────────────┘
               │ (4) Sync User Record
               ▼
┌────────────────────────────────┐
│  5. NeonDB PostgreSQL Database │
│  Stores / updates user profile │
│  (id=UID, email, name, goal)   │
└────────────────────────────────┘
```

---

## 2. Firebase Console Setup

### Step 2.1: Create a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or select an existing project).
3. Name your project (e.g., `StepCount`) and click **Continue**.
4. (Optional) Enable or disable Google Analytics, then click **Create project**.

---

### Step 2.2: Enable Email/Password Sign-In
1. In the left navigation sidebar, go to **Build** $\rightarrow$ **Authentication**.
2. Click **Get Started**.
3. In the **Sign-in method** tab, click **Email/Password**.
4. Toggle **Enable** to ON and click **Save**.

---

### Step 2.3: Register Android App & Download `google-services.json`
1. Go to **Project Settings** (gear icon next to Project Overview) $\rightarrow$ **General**.
2. In the **Your apps** section, click the **Android** icon.
3. Fill in the app details:
   - **Android package name**: `com.example.stepcount` (must match `applicationId` in `app/build.gradle.kts`).
   - **App nickname**: `StepCount`.
4. Click **Register app**.
5. Download the **`google-services.json`** file.
6. **Move the file into your Android project**:
   - Destination path: `android/StepCount/app/google-services.json`

> [!NOTE]
> `google-services.json` is already included in `.gitignore` to prevent committing your client credentials to public repositories.

---

### Step 2.4: Generate Backend Service Account Key (`serviceAccountKey.json`)
The FastAPI backend needs admin privileges to verify incoming user tokens.

1. In the Firebase Console, go to **Project Settings** $\rightarrow$ **Service accounts** tab.
2. Ensure **Python** is selected under the code snippet.
3. Click **Generate new private key**, then confirm by clicking **Generate key**.
4. A JSON file will download (e.g., `stepcount-firebase-adminsdk-xxxxx.json`).
5. **Rename and place the file in the backend folder**:
   - Rename to: `serviceAccountKey.json`
   - Destination path: `backend/serviceAccountKey.json`

> [!CAUTION]
> Never commit `serviceAccountKey.json` to Git! It contains your private master admin credentials. It is already protected in `.gitignore`.

---

## 3. Configuring the FastAPI Backend

### Step 3.1: Configure `backend/.env`
Open or create `backend/.env` and specify the path to your service account key:

```env
ENVIRONMENT=development
DATABASE_URL=postgresql+asyncpg://neondb_owner:YOUR_PASSWORD@ep-super-wildflower-axeau.us-east-2.aws.neon.tech/neondb?ssl=require
FIREBASE_CREDENTIALS_PATH=serviceAccountKey.json
CORS_ORIGINS=["*"]
```

### Step 3.2: How the Backend Verifies Tokens
In `backend/app/services/firebase_service.py`, Firebase Admin SDK initializes with your key:

```python
import firebase_admin
from firebase_admin import credentials, auth

cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

# Token verification
decoded_token = auth.verify_id_token(id_token)
user_uid = decoded_token["uid"]
user_email = decoded_token.get("email")
```

The `get_current_user` dependency in `backend/app/core/security.py` automatically checks this on all protected endpoints (`/api/steps`, `/api/profile`, `/api/leaderboard`, etc.) and provisions the user in NeonDB!

---

## 4. Configuring the Android Application

### Step 4.1: Google Services Plugin in Gradle
The Android project is already configured with Firebase dependencies:

- In `android/StepCount/build.gradle.kts`:
  ```kotlin
  plugins {
      alias(libs.plugins.google.gms.services) apply false
  }
  ```
- In `android/StepCount/app/build.gradle.kts`:
  ```kotlin
  plugins {
      alias(libs.plugins.google.gms.services)
  }
  dependencies {
      implementation(platform(libs.firebase.bom))
      implementation(libs.firebase.auth.ktx)
  }
  ```

### Step 4.2: How Android Handles Auth & Tokens
1. **User Sign Up & Login**:
   `FirebaseAuthService.kt` calls:
   ```kotlin
   // Registration
   val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
   
   // Login
   val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
   ```

2. **Automatic Token Attachment**:
   Every HTTP request made via Retrofit passes through `AuthTokenInterceptor.kt`, which retrieves a fresh token and attaches it:
   ```kotlin
   val idToken = authService.getFreshIdToken()
   if (idToken != null) {
       val authenticatedRequest = originalRequest.newBuilder()
           .header("Authorization", "Bearer $idToken")
           .build()
       return chain.proceed(authenticatedRequest)
   }
   ```

---

## 5. Deployment on Render (Cloud Production)

When hosting the backend on Render's free tier:

1. Open your **Render Dashboard** $\rightarrow$ select your StepCount backend web service.
2. Go to **Environment** $\rightarrow$ **Add Environment Variable**.
3. Add:
   - **Key**: `FIREBASE_CREDENTIALS_JSON`
   - **Value**: *(Open your local `serviceAccountKey.json`, copy the entire JSON text, and paste it here)*
4. Click **Save Changes**. The backend automatically detects `FIREBASE_CREDENTIALS_JSON` and initializes Firebase Admin in the cloud without needing physical files!

---

## 6. Verification & Testing

### Test 1: Verify Backend with Swagger UI
1. Run backend locally:
   ```powershell
   cd backend
   .\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000
   ```
2. Open `http://127.0.0.1:8000/docs`.
3. In development mode, use `Bearer test_token_myuid` to test endpoints immediately.

### Test 2: Test from Android Device / Emulator
1. Build and run the Android app in Android Studio.
2. On the **Auth Screen**, enter an email (e.g. `runner@stepcount.com`) and password (minimum 6 characters).
3. Click **Sign Up** or **Log In**.
4. Check your Firebase Console $\rightarrow$ **Authentication** $\rightarrow$ **Users** tab to see your new user created in real-time!
5. Check your NeonDB PostgreSQL database $\rightarrow$ the user record and daily step records are automatically synced!
