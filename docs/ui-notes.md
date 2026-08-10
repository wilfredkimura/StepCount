```markdown
# StepCount – UI Design Document

This document describes the user interface elements and features of every activity in the StepCount application.

---

## 1. SplashScreenActivity

**Purpose:** App launch screen and initial routing.

### UI Elements
- App logo (centered)
- App name: **StepCount**
- Short tagline (e.g. “Track your steps. Reach your goals.”)
- Loading indicator (optional)

### Features
- Displays for 2–3 seconds
- Checks if the user is already logged in
- Navigates to `DashboardActivity` if logged in
- Navigates to `AuthActivity` if not logged in

---

## 2. AuthActivity

**Purpose:** User authentication and guest access.

### UI Elements
- App logo
- Tab layout or toggle: **Login** | **Register**
- Email input field
- Password input field
- Confirm Password field (Register only)
- Name input field (Register only)
- **Login / Register** button
- **Continue as Guest** button
- “Forgot Password?” link (optional)
- Error message text

### Features
- Firebase Email/Password authentication
- Form validation (empty fields, invalid email, weak password)
- Guest mode (local tracking only, no sync or leaderboard)
- Navigates to `DashboardActivity` on success

---

## 3. DashboardActivity

**Purpose:** Main screen showing live step progress.

### UI Elements
- Top App Bar with app name and profile/settings icons
- Circular progress indicator (daily goal progress)
- Large step count text (e.g. “8,432 steps”)
- Daily goal text (e.g. “Goal: 10,000 steps”)
- Percentage completed
- Motivational quote card
- Quick action buttons:
  - History
  - Leaderboard
  - Profile
- Bottom Navigation Bar
- Pull-to-refresh support
- Offline indicator (when applicable)

### Features
- Live step counting using `TYPE_STEP_COUNTER`
- Real-time progress ring update
- Congratulatory message when daily goal is reached
- Displays motivational quote from backend
- Manual refresh option

---

## 4. HistoryActivity

**Purpose:** View and manage past daily step records.

### UI Elements
- Top App Bar with title “History” and back button
- RecyclerView list of daily records
- Each list item shows:
  - Date
  - Steps taken
  - Goal for that day
  - Progress percentage or status (Goal Met / Not Met)
- Empty state illustration + message (“No history yet”)
- Swipe-to-delete or delete icon per item
- Filter options (optional): This Week / This Month / All

### Features
- Full CRUD support (Read, Update notes, Delete)
- Offline access via Room
- Confirmation dialog before deleting a record

---

## 5. LeaderboardActivity

**Purpose:** Display user rankings.

### UI Elements
- Top App Bar with title “Leaderboard”
- Tab or Chip group for period selection:
  - Today
  - This Week
  - All Time
- Toggle for ranking type:
  - Most Steps
  - Goal Achievement %
- RecyclerView of ranked users
- Each item shows:
  - Rank number
  - User name
  - Steps (or percentage)
  - Highlight for current user
- Current user rank card at the top or bottom
- Offline banner (“Showing last updated rankings”)
- Pull-to-refresh

### Features
- On-demand ranking from backend
- Local caching in Room
- Works offline with last known data
- Supports two ranking criteria

---

## 6. ProfileActivity

**Purpose:** View user profile information.

### UI Elements
- Top App Bar with title “Profile”
- Profile avatar or initials
- User name
- Email address
- Daily step goal
- Member since date (optional)
- **Edit Profile** button
- **Settings** button
- **Logout** button

### Features
- Displays data from Room / backend
- Navigation to SettingsActivity
- Logout functionality

---

## 7. SettingsActivity

**Purpose:** App preferences and account management.

### UI Elements & Sections

#### Account Section
- Daily Step Goal (Slider or number input)
- Logout button

#### Preferences Section
- Dark Mode toggle (Light / Dark / System)
- Units toggle (Steps only / Steps + Distance)
- Notifications toggle (Daily goal reminders)

#### Data Section
- Manual Sync button (“Sync Now”)
- Clear Local History button  
  → Shows confirmation dialog: **“Are you sure?”**

#### Information Section
- Guest Mode Info (explanation of limitations)
- About / App Version
- Privacy Policy (opens link or in-app page)
- Delete Account button  
  → Shows strong confirmation dialog

### Features
- Changes are saved immediately or on confirm
- Clear Local History requires confirmation
- Delete Account requires confirmation
- Manual sync triggers upload of pending Room data
- Dark mode applies across the entire app

---

## Common UI Components Across the App

- Material Design 3 components
- Bottom Navigation (Dashboard, History, Leaderboard, Profile)
- Consistent color scheme (energetic blues and greens)
- Dark mode support
- Loading indicators and progress bars
- Snackbars for feedback messages
- Empty states with illustrations
- Confirmation dialogs for destructive actions

---

## Navigation Summary

| Screen              | Can Navigate To                          |
|---------------------|------------------------------------------|
| SplashScreen        | AuthActivity / DashboardActivity         |
| AuthActivity        | DashboardActivity                        |
| DashboardActivity   | History, Leaderboard, Profile            |
| HistoryActivity     | DashboardActivity                        |
| LeaderboardActivity | DashboardActivity                        |
| ProfileActivity     | SettingsActivity, AuthActivity (logout)  |
| SettingsActivity    | AuthActivity (logout / delete account)   |
```
