```markdown
# StepCount – Detailed Technical Document  
**Android + FastAPI + PostgreSQL + Firebase Authentication**

---

## 1. User Authentication (10 Marks)

The application supports full user authentication with the following features:

- **Register** – New users can create an account using email and password.
- **Login** – Existing users can log in securely.
- **Logout** – Users can securely log out.
- **Guest Mode** – Users can use the app without creating an account. Guest users can track steps locally but cannot sync data or view the leaderboard.

### Authentication Implementation

- **Primary**: **Firebase Authentication** (Email/Password provider).
  - Handles user registration, login, password hashing, and token generation.
  - Returns a Firebase ID Token upon successful authentication.
- **Backend Integration**: The Android app sends the Firebase ID Token to the FastAPI backend.
  - FastAPI verifies the token using the Firebase Admin SDK.
  - Backend then creates or updates the user record in PostgreSQL.
- **Offline Support**: Basic user profile is also stored in the local Room Database so the app remains usable when offline.

### Authentication Flow
1. User registers or logs in through Firebase Auth in the Auth Activity.
2. Firebase returns a Firebase ID Token.
3. Android app sends the ID Token to FastAPI (`POST /api/auth/firebase-login`).
4. FastAPI verifies the token and synchronizes the user in PostgreSQL.
5. Android app stores the user profile and token information in Room.
6. Subsequent API calls include the Firebase ID Token in the Authorization header.

---

## 2. Multiple Activities (5 Marks)

The application contains **seven activities**:

| Activity                  | Purpose                                      |
|--------------------------|----------------------------------------------|
| SplashScreenActivity     | App logo + check login status                |
| AuthActivity             | Register / Login (Firebase Auth) + Guest mode|
| DashboardActivity        | Live step count, progress, motivation quote  |
| HistoryActivity          | List of past daily step records (CRUD)       |
| LeaderboardActivity      | User rankings from the backend               |
| ProfileActivity          | View profile information                     |
| SettingsActivity         | App settings and preferences                 |

---

## 3. Navigation (5 Marks)

### Explicit Intents
Used for all internal navigation between the activities.

| From                  | To                     | Purpose                              |
|-----------------------|------------------------|--------------------------------------|
| SplashScreenActivity  | AuthActivity           | If user is not logged in             |
| SplashScreenActivity  | DashboardActivity      | If user is already logged in         |
| AuthActivity          | DashboardActivity      | After successful login/register or Guest mode |
| DashboardActivity     | HistoryActivity        | View past step records               |
| DashboardActivity     | LeaderboardActivity    | View rankings                        |
| DashboardActivity     | ProfileActivity        | View profile                         |
| ProfileActivity       | SettingsActivity       | Open settings                        |
| SettingsActivity      | AuthActivity           | After logout or delete account       |
| HistoryActivity       | DashboardActivity      | Back navigation                      |

### Implicit Intents
- **Share** today’s steps or a motivational quote (`ACTION_SEND`)
- **Send Email** to share progress or contact support (`ACTION_SENDTO`)

---

## 4. Local Database (15 Marks)

**Technology**: Room Database

The application uses Room as an **offline-first** local database. All critical data is first saved locally and later synchronized with the FastAPI backend when the device is online.

### Tables in Room Database

The database contains **three related tables**:

#### 1. `user_profile`
Stores the currently logged-in user’s information.

```kotlin
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String,          // Firebase UID
    val email: String,
    val name: String,
    val dailyGoal: Int = 8000
)
```

#### 2. `daily_steps`
Stores the user’s daily step history.

```kotlin
@Entity(
    tableName = "daily_steps",
    foreignKeys = [ForeignKey(
        entity = UserProfile::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DailySteps(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val date: String,              // Format: yyyy-MM-dd
    val steps: Long,
    val goal: Int,                 // Stores the goal that was active on that specific day (for historical accuracy)
    val synced: Boolean = false    // Tracks whether the record has been uploaded to the backend
)
```

#### 3. `leaderboard_cache`
Stores a local cache of the leaderboard so rankings can still be viewed offline.

```kotlin
@Entity(tableName = "leaderboard_cache")
data class LeaderboardCache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,            // Firebase UID of the ranked user
    val name: String,
    val steps: Long,
    val rank: Int,
    val period: String,            // "today", "week", or "all_time"
    val lastUpdated: Long          // Timestamp of last sync
)
```

### Relationships
- `user_profile` (1) → (Many) `daily_steps`
- `leaderboard_cache` is independent but linked by `userId`

### CRUD Operations Supported
- **Create**: Insert new daily step records, user profile, or leaderboard entries
- **Read**: Retrieve today’s steps, full history, user profile, and cached leaderboard
- **Update**: Update daily goal, mark records as synced, refresh leaderboard cache
- **Delete**: Delete specific daily records or clear old leaderboard cache

### Design Principles
- Room is the primary source of truth while offline.
- Data is always written to Room first.
- When the device is online, pending records (`synced = false`) are uploaded to the FastAPI backend.
- The leaderboard is fetched from the backend and cached locally for offline viewing.

---

## 5. Remote Data (15 Marks)

**Technology**: Retrofit + OkHttp

The Android app communicates **only** with the custom FastAPI backend.

### API Endpoints

**Base URL:** `https://your-backend-url.com/api`  
**Authentication Header:** `Authorization: Bearer <firebase_id_token>`

#### Authentication

| Method | Endpoint                        | Description                                                                 | Auth Required |
|--------|---------------------------------|-----------------------------------------------------------------------------|---------------|
| POST   | `/auth/firebase-login`          | Verify Firebase ID Token. Handles both **Register** and **Login**. Creates the user in PostgreSQL if they don’t exist yet. | No            |
| POST   | `/auth/logout`                  | Optional. Logs the user out on the backend (token invalidation / logout event). | Yes           |

> **Note:**  
> - **Register** and **Login** are primarily handled by Firebase Authentication on the Android side.  
> - After successful Firebase authentication, the app calls `/auth/firebase-login` to sync the user with the backend.  
> - **Logout** is mainly handled on the Android side (`FirebaseAuth.signOut()`), with an optional backend call.

#### Steps

| Method | Endpoint                        | Description                                      | Auth Required |
|--------|---------------------------------|--------------------------------------------------|---------------|
| POST   | `/steps`                        | Upload or update today’s steps                   | Yes           |
| GET    | `/steps/today`                  | Get today’s step record for the current user     | Yes           |
| GET    | `/steps/history`                | Get the current user’s full step history         | Yes           |
| PUT    | `/steps/{date}`                 | Update steps for a specific date                 | Yes           |
| DELETE | `/steps/{date}`                 | Delete steps for a specific date                 | Yes           |

#### Leaderboard

| Method | Endpoint                              | Description                                      | Auth Required |
|--------|---------------------------------------|--------------------------------------------------|---------------|
| GET    | `/leaderboard`                        | Get leaderboard (default: today, most steps)     | Yes           |
| GET    | `/leaderboard?period=today`           | Leaderboard for today                            | Yes           |
| GET    | `/leaderboard?period=week`            | Leaderboard for this week                        | Yes           |
| GET    | `/leaderboard?period=all_time`        | All-time leaderboard                             | Yes           |
| GET    | `/leaderboard?type=steps`             | Rank by most steps                               | Yes           |
| GET    | `/leaderboard?type=goal`              | Rank by goal achievement percentage              | Yes           |
| GET    | `/leaderboard/me`                     | Get current user’s rank and position             | Yes           |

#### Profile

| Method | Endpoint                        | Description                                      | Auth Required |
|--------|---------------------------------|--------------------------------------------------|---------------|
| GET    | `/profile`                      | Get current user’s profile                       | Yes           |
| PUT    | `/profile`                      | Update user profile (name, daily goal, etc.)     | Yes           |

#### Motivation

| Method | Endpoint                        | Description                                      | Auth Required |
|--------|---------------------------------|--------------------------------------------------|---------------|
| GET    | `/motivation`                   | Get a motivational quote (fetched from Quotable API) | Yes        |

### Open-Source External API Used
- **Quotable API** (`https://api.quotable.io`)
  - Free, open-source, no API key required
  - Used by the FastAPI backend to provide motivational quotes
  - Android app never calls Quotable directly

### Data Flow
```
Android App → Firebase Authentication
                ↓
         FastAPI Backend → PostgreSQL
                ↓
            Quotable API (optional)
```

---

## 6. Sensor Integration (10 Marks)

**Primary Sensor**: `Sensor.TYPE_STEP_COUNTER`

- Hardware (or low-power) step counter that returns total steps since last device reboot.
- Extremely accurate and battery-efficient.

### How the sensor influences application behaviour
- Live step count is displayed on the Dashboard and updates the progress ring in real time.
- User can also manually refresh the page to get the latest step count.
- Reaching the daily goal triggers a congratulatory message and visual feedback.
- Daily total is calculated as:  
  `Today’s Steps = Current Sensor Value − Daily Baseline`
- If the step counter sensor is missing, the app falls back to accelerometer-based peak detection and clearly informs the user.

**Permission**: `ACTIVITY_RECOGNITION` (requested at runtime on Android 10+).

---

## 7. Good User Interface (10 Marks)

- **Material Design 3** components throughout
- Responsive layouts using ConstraintLayout and modern Material components
- Custom icons and system icons
- High-quality images / illustrations on Splash and empty states
- Bottom Navigation + Overflow Menu
- Consistent colour scheme (energetic blues and greens)
- Dark mode support
- Progress indicators, circular progress for daily goal, and clear visual hierarchy

---

## 8. Error Handling (5 Marks)

The application handles the following cases gracefully:

| Error Type              | Handling Strategy                                      |
|-------------------------|--------------------------------------------------------|
| Empty input             | Field validation + error messages on Register/Login    |
| Network failures        | Offline mode + clear Snackbar (“Working offline”)      |
| Database errors         | Try-catch around Room operations + user feedback       |
| Invalid login           | Firebase error messages + backend verification errors  |
| Missing sensor          | Fallback to accelerometer + informative message        |
| Backend unavailable     | Local Room continues to work; data queued for later sync |
| Firebase token expired  | Automatic redirect to Login screen                     |

---

## 9. Testing (5 Marks)

The following areas will be thoroughly tested:

- **Navigation** – All explicit and implicit intents
- **CRUD operations** – Create, read, update, delete on Room tables
- **API communication** – Successful calls, network errors, invalid tokens, offline behaviour
- **Sensor functionality** – Step counting accuracy, baseline calculation, missing sensor fallback
- **Authentication flow** – Firebase Register, Login, Logout, Guest mode, token verification with backend
- **Offline-first behaviour** – App remains usable when backend is down
- **Leaderboard & Sync** – Data correctly uploaded and ranked when online
- **Settings functionality** – Goal change, dark mode, clear history, etc.

Manual testing + Logcat verification will be performed. Key flows will also be demonstrated live during the project presentation.

---

## Summary of Technology Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Frontend           | Kotlin + Android Studio             |
| Authentication     | Firebase Authentication             |
| Local Database     | Room                                |
| Networking         | Retrofit + OkHttp                   |
| Backend            | FastAPI (Python)                    |
| Backend Database   | PostgreSQL                          |
| External API       | Quotable (via backend)              |
| Sensor             | TYPE_STEP_COUNTER                   |
| UI                 | Material Design 3                   |

---

## SettingsActivity Features

The **SettingsActivity** contains the following options:

| Setting                        | Description                                      | Type                  |
|--------------------------------|--------------------------------------------------|-----------------------|
| **Daily Step Goal**            | Change the daily step target                     | Number input / Slider |
| **Dark Mode**                  | Switch between Light / Dark / System default     | Toggle                |
| **Units**                      | Show Steps only or Steps + Distance (km)         | Toggle                |
| **Notifications**              | Enable/disable daily goal reminders              | Toggle                |
| **Manual Sync**                | Force sync of local steps to the backend         | Button                |
| **Clear Local History**        | Delete all locally stored step history           | Button + Confirmation dialog (“Are you sure?”) |
| **Guest Mode Info**            | Explanation of Guest mode limitations            | Info text             |
| **About / Version**            | Display app version and short description        | Text                  |
| **Delete Account**             | Request permanent account deletion               | Button + Confirmation |
| **Privacy Policy**             | Open or display the privacy policy               | Link / Button         |

---

## Additional Features

### Leaderboard Ranking Logic

The leaderboard supports two ranking criteria:

1. **Most Steps** (Primary)
2. **Goal Achievement Percentage** (Secondary)

Supported periods:
- Today
- This Week
- All Time

#### Is the Leaderboard Real-time?

**No**, it is not fully real-time.

| Method                        | How it works                                      | Update Frequency          | Recommendation |
|-------------------------------|---------------------------------------------------|---------------------------|----------------|
| **On-Demand Calculation**     | Leaderboard is calculated when the user opens the Leaderboard screen | Every time the screen is opened (if online) | **Best for this project** |
| **Periodic Refresh**          | Backend recalculates and caches rankings every few minutes | Every 5–15 minutes        | Good alternative |
| **True Real-time**            | Updates instantly on every step                   | Continuous                | Too complex / overkill |

#### How It Works
1. User opens the Leaderboard screen
2. Android app calls `GET /api/leaderboard`
3. FastAPI runs the ranking query on PostgreSQL
4. Results are returned to the app
5. App displays the rankings and caches them in Room (`leaderboard_cache`)

**When offline:**
- The app shows the last cached leaderboard from Room
- A message is displayed: “Showing last updated rankings (Offline)”

#### Optional Improvements
- Pull-to-Refresh
- Short-term cache (2–5 minutes)
- Display last updated timestamp

---

This design fully satisfies all functional and technical requirements of the IBL23305 Semester Capstone Project while remaining realistic and implementable within a student timeframe.
```