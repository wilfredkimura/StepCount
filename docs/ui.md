# StepCount – UI/UX Design System & Screen Layout Specifications

**App Name:** StepCount  
**Platform:** Android (Material Design 3 & Jetpack Compose)  
**Target SDK:** 36 (Min SDK 24)  
**Document Version:** 2.1  
**Icon Specification:** [Material Design 3 Icons Reference (docs/icons.md)](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons.md)  

---

## 1. Visual Design System

### 1.1 Color Palette
The color scheme is designed to feel energetic, motivating, and modern using athletic blues, emerald greens, and sleek neutral surfaces.

| Token Name | Light Mode Hex | Dark Mode Hex | Usage |
| :--- | :--- | :--- | :--- |
| `primary` | `#00668B` | `#7CD0FF` | Main action buttons, active tabs, progress ring fill |
| `onPrimary` | `#FFFFFF` | `#00344A` | Text/icons on primary color |
| `primaryContainer` | `#C3E8FF` | `#004C6A` | High-emphasis card backgrounds, active badges |
| `onPrimaryContainer` | `#001E2D` | `#C3E8FF` | Text on primary containers |
| `secondary` | `#006D5B` | `#4FDCBC` | Goal reached accents, motivational elements |
| `secondaryContainer` | `#71F8D8` | `#005143` | Milestone cards, secondary progress badges |
| `tertiary` | `#585C7E` | `#C1C3EB` | Neutral accents, leaderboard rankings |
| `background` | `#FBFCFE` | `#191C1E` | Screen background |
| `surface` | `#FBFCFE` | `#191C1E` | App bar, bottom navigation surface |
| `surfaceVariant` | `#DCE3E9` | `#40484C` | Cards, input fields, unselected state surfaces |
| `error` | `#BA1A1A` | `#FFB4AB` | Validation errors, destructive warnings |
| `success` | `#1E8E3E` | `#57DC7A` | Goal completed, sync success indicator |
| `warning` | `#F9A825` | `#FFD54F` | Offline state indicator, missing sensor fallback |

### 1.2 Typography Hierarchy (Inter / Roboto / System)
- **Display Large (`34sp`, Bold):** Main Step Count numbers on Dashboard.
- **Headline Medium (`22sp`, Semi-Bold):** Screen titles, modal headings.
- **Title Medium (`16sp`, Medium):** Card titles, list item primary text, tab labels.
- **Body Medium (`14sp`, Regular):** Descriptions, quotes, subtitles, input field labels.
- **Label Small (`11sp`, Medium):** Status tags, offline chips, timestamps.

### 1.3 Common Touch Targets & Spacing
- Standard padding: `16dp` outer margin, `8dp` inner gutter.
- Minimum interactive touch target: `48dp x 48dp`.
- Corner Radius: Cards `16dp`, Buttons `12dp`, Chips `8dp`, Dialogs `20dp`.

---

## 2. Navigation Architecture & Flow

```
                     ┌───────────────────────┐
                     │  SplashScreenActivity │
                     └───────────┬───────────┘
                                 │
                 ┌───────────────┴───────────────┐
        (Not Authenticated)               (Authenticated)
                 │                               │
                 ▼                               ▼
      ┌────────────────────┐          ┌───────────────────────┐
      │    AuthActivity    ├─────────►│   DashboardActivity   │◄──┐
      │ (Login / Register) │ (Success)└───┬───────┬───────┬───┘   │
      └────────────────────┘              │       │       │       │
                                          │       │       │       │
                 ┌────────────────────────┘       │       └───────┼────────────────────────┐
                 │ (Bottom Nav: History)          │ (Bottom Nav:  │ (Bottom Nav: Profile)  │
                 ▼                                │  Leaderboard) ▼                        │
      ┌────────────────────┐                      ▼     ┌────────────────────┐             │
      │  HistoryActivity   │           ┌──────────────┐ │  ProfileActivity   ├─(Settings)──┤
      │ (Daily Step CRUD)  ├──────────►│ Leaderboard  │ └─────────┬──────────┘             │
      └────────────────────┘ (Back)    │   Activity   │           │ (Logout)               ▼
                                       └──────┬───────┘           │             ┌─────────────────────┐
                                              │ (Back)            └────────────►│  SettingsActivity   │
                                              ▼                                 │(Prefs, Sync, Delete)│
                                     [DashboardActivity]                        └──────────┬──────────┘
                                                                                           │ (Logout / Delete)
                                                                                           ▼
                                                                                   [AuthActivity]
```

---

## 3. Detailed Screen Layouts & Vector Icon Wireframes

---

### Screen 1: `SplashScreenActivity`

**Purpose:** Initial landing screen that displays branding while checking Firebase Auth status and initializing Room database.

#### UI Elements:
- Central app icon: `[ic:directions_walk]` (Vector drawable / Compose ImageVector).
- App Title: **StepCount**.
- Tagline: *"Track your steps. Reach your goals."*
- Circular progress spinner at bottom (visible during authentication check).
- Version tag at bottom.

#### Layout Sketch:
```
+-------------------------------------------------------------+
|                                                             |
|                                                             |
|                                                             |
|                                                             |
|                                                             |
|                                                             |
|                   [ic:directions_walk]                      |
|                   << STEPCOUNT LOGO >>                      |
|                                                             |
|                         StepCount                           |
|               Track your steps. Reach your goals.           |
|                                                             |
|                                                             |
|                                                             |
|                                                             |
|                                                             |
|                                                             |
|                       ( o ) Checking auth...                |
|                                                             |
|                                                             |
|                                                             |
|                         v1.0.0-release                      |
+-------------------------------------------------------------+
```

---

### Screen 2: `AuthActivity`

**Purpose:** Handles user login, new account registration, and guest mode. Includes real-time form validation with Material Symbols.

#### State 2A: Login Mode
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
|  [ic:directions_walk] StepCount                             |
|  Welcome back! Keep moving forward.                         |
|                                                             |
|  +---------------------------+---------------------------+  |
|  |     [ LOGIN (Active) ]    |         REGISTER          |  |
|  +---------------------------+---------------------------+  |
|                                                             |
|  Email Address                                              |
|  +-------------------------------------------------------+  |
|  | [ic:email]  user@example.com                          |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  Password                                                   |
|  +-------------------------------------------------------+  |
|  | [ic:lock]   ••••••••••••              [ic:visibility] |  |
|  +-------------------------------------------------------+  |
|                                      Forgot Password?       |
|                                                             |
|  +-------------------------------------------------------+  |
|  |                  [  LOG IN  ]                         |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  ------------------------- OR ----------------------------  |
|                                                             |
|  +-------------------------------------------------------+  |
|  |       [ic:person_off] Continue as Guest (Offline)     |  |
|  +-------------------------------------------------------+  |
|  * Guest mode stores data locally; syncing is disabled.     |
|                                                             |
+-------------------------------------------------------------+
```

#### State 2B: Register Mode (With Live Validation)
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
|  [ic:directions_walk] StepCount                             |
|  Create your account to compete on the leaderboard!         |
|                                                             |
|  +---------------------------+---------------------------+  |
|  |           LOGIN           |   [ REGISTER (Active) ]   |  |
|  +---------------------------+---------------------------+  |
|                                                             |
|  Full Name                                                  |
|  +-------------------------------------------------------+  |
|  | [ic:account_circle]  Alex Johnson                     |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  Email Address                                              |
|  +-------------------------------------------------------+  |
|  | [ic:email]           alex.johnson@domain.com          |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  Password (min. 6 chars)                                    |
|  +-------------------------------------------------------+  |
|  | [ic:lock]            ••••••••         [ic:visibility] |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  Confirm Password                                           |
|  +-------------------------------------------------------+  |
|  | [ic:lock]            ••••••••        [ic:check_circle]|  |
|  +-------------------------------------------------------+  |
|                                                             |
|  +-------------------------------------------------------+  |
|  |              [  CREATE ACCOUNT  ]                     |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  By signing up, you agree to our Terms and Privacy Policy.  |
+-------------------------------------------------------------+
```

---

### Screen 3: `DashboardActivity`

**Purpose:** The central operational hub. Displays live steps via `Sensor.TYPE_STEP_COUNTER`, daily goal progress, motivational quote from backend, offline status banner, and quick navigation.

#### Layout Sketch:
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
| [ic:directions_walk StepCount]     [ic:notifications] [ic:settings]
| Today, Monday Aug 10                                        |
|                                                             |
| +---------------------------------------------------------+ |
| | [ic:cloud_off] Offline Mode — Sync pending              | |
| +---------------------------------------------------------+ |
|                                                             |
|                  . - ~ ~ ~ ~ ~ - .                          |
|              . '                   ' .                      |
|            /     ==================    \                    |
|           /    /                    \   \                   |
|          |    |  [ic:directions_walk]|   |                  |
|          |    |     8,432 STEPS      |   |                  |
|          |    |                      |   |                  |
|           \    \   Goal: 10,000     /   /                   |
|            \     ==================    /                    |
|              . '    ( 84.3% )      ' .                      |
|                  ' - . _ _ _ _ . - '                        |
|                                                             |
| [ic:local_fire_department] 345 kcal                         |
| [ic:straighten] 6.2 km                                      |
| [ic:timer] 72 active mins                                   |
|                                                             |
| +---------------------------------------------------------+ |
| | [ic:format_quote] Daily Motivation         [ic:refresh] | |
| | "The secret of getting ahead is getting started."       | |
| | — Mark Twain                                            | |
| +---------------------------------------------------------+ |
|                                                             |
| Quick Actions:                                              |
| +-------------------------+ +-----------------------------+ |
| | [ic:history] History    | | [ic:leaderboard] Leaderboard| |
| +-------------------------+ +-----------------------------+ |
|                                                             |
+-------------------------------------------------------------+
| [ic:home Home] [ic:history History] [ic:leaderboard Rank] [ic:person Profile] |
+-------------------------------------------------------------+
```

#### Goal Reached State (Visual Cue):
```
+-------------------------------------------------------------+
| [ic:celebration] GOAL ACHIEVED! 10,240 / 10,000 Steps       |
| You smashed today's goal! Keep the streak alive! [ic:whatshot]|
+-------------------------------------------------------------+
```

---

### Screen 4: `HistoryActivity`

**Purpose:** Comprehensive list of past daily step counts from Room DB with filter chips, full CRUD management, swipe-to-delete, and goal completion indicators.

#### Layout Sketch:
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
| [ic:arrow_back] Step History             [ic:delete_sweep Clear]
|                                                             |
| Filter: [ (•) All Time ]  [ ( ) This Week ]  [ ( ) This Month] |
|                                                             |
| +---------------------------------------------------------+ |
| | [ic:calendar_today] Yesterday, Aug 9    [ic:check_circle] | |
| | [ic:directions_walk] 10,450 / 10,000 steps (104%)       | |
| | [ic:local_fire_department] 420 kcal • [ic:straighten] 7.8 km|
| | [ic:cloud_done] Synced              [ic:edit] [ic:delete] | |
| +---------------------------------------------------------+ |
|                                                             |
| +---------------------------------------------------------+ |
| | [ic:calendar_today] Saturday, Aug 8     [ic:pending]      | |
| | [ic:directions_walk] 8,210 / 10,000 steps (82%)         | |
| | [ic:local_fire_department] 310 kcal • [ic:straighten] 5.9 km|
| | [ic:cloud_done] Synced              [ic:edit] [ic:delete] | |
| +---------------------------------------------------------+ |
|                                                             |
| +---------------------------------------------------------+ |
| | [ic:calendar_today] Friday, Aug 7       [ic:check_circle] | |
| | [ic:directions_walk] 11,100 / 10,000 steps (111%)       | |
| | [ic:local_fire_department] 455 kcal • [ic:straighten] 8.1 km|
| | [ic:cloud_sync] Pending Sync        [ic:edit] [ic:delete] | |
| +---------------------------------------------------------+ |
|                                                             |
+-------------------------------------------------------------+
| [ic:home Home] [ic:history History*] [ic:leaderboard Rank] [ic:person Profile] |
+-------------------------------------------------------------+
```

#### Empty State (When no records exist):
```
+-------------------------------------------------------------+
|                                                             |
|                     [ic:folder_open]                        |
|                      No History Yet                         |
|      Start walking today and your daily step records        |
|             will automatically appear here.                 |
|                                                             |
|            [ [ic:directions_walk] Go to Dashboard ]         |
|                                                             |
+-------------------------------------------------------------+
```

---

### Screen 5: `LeaderboardActivity`

**Purpose:** Real-time social ranking comparing user step performance against others via FastAPI backend with local Room caching for seamless offline viewing.

#### Layout Sketch:
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
| [ic:arrow_back] Leaderboard                       [ic:sync] |
|                                                             |
| Period:  [ [Today] ]    [ This Week ]    [ All Time ]       |
| Rank By: [ (•) Total Steps ]       [ ( ) Goal % Completed ] |
|                                                             |
| +---------------------------------------------------------+ |
| | [ic:info] Cached: Showing data from 10 mins ago         | |
| +---------------------------------------------------------+ |
|                                                             |
|             [ic:emoji_events] PODIUM STANDINGS              |
|                                                             |
|       [ic:military_tech #2]  [ic:military_tech #1]  [ic:military_tech #3]
|            Sarah K.               David M.               Alex J.    |
|            12,410 st              15,820 st              11,900 st  |
|                                                             |
| =========================================================== |
|                                                             |
|  #4  [ic:account_circle] Maya Lin         10,850 st  [ic:star] 98%
| ----------------------------------------------------------- |
|  #5  [ic:account_circle] Chris Evans       9,400 st  [ic:star] 85%
| ----------------------------------------------------------- |
|  #6  [ic:account_circle] You (Alex J.)    8,432 st  [ic:star] 84%
|      >>> (Your ranking is highlighted in primary blue) <<<  |
| ----------------------------------------------------------- |
|  #7  [ic:account_circle] Elena Rostova     7,910 st  [ic:star] 79%
|                                                             |
| +---------------------------------------------------------+ |
| | YOUR RANK: #6 of 142 Users  •  Top 5%                   | |
| +---------------------------------------------------------+ |
|                                                             |
+-------------------------------------------------------------+
| [ic:home Home] [ic:history History] [ic:leaderboard Rank*] [ic:person Profile] |
+-------------------------------------------------------------+
```

---

### Screen 6: `ProfileActivity`

**Purpose:** Displays user profile details, cumulative walking statistics, current daily target, and primary profile actions.

#### Layout Sketch:
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
| Profile                                       [ic:settings] |
|                                                             |
|                        +---------+                          |
|                        |  (AJ)   | [ic:photo_camera Change] |
|                        | Avatar  |                          |
|                        +---------+                          |
|                      Alex Johnson                           |
|                   alex.j@example.com                        |
|                 Member since July 2026                      |
|                                                             |
|  Lifetime Performance:                                      |
|  +---------------------------+ +--------------------------+ |
|  | [ic:directions_walk] Steps| | [ic:local_fire_dept] Kcal| |
|  |   482,910                 | |   19,316 kcal            | |
|  +---------------------------+ +--------------------------+ |
|  | [ic:straighten] Distance  | | [ic:whatshot] Best Streak| |
|  |   362.4 km                | |   14 Days                | |
|  +---------------------------+ +--------------------------+ |
|                                                             |
|  Daily Goal Target:                                         |
|  +-------------------------------------------------------+  |
|  | [ic:flag] 10,000 steps/day           [ic:edit Edit Goal] |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  +-------------------------------------------------------+  |
|  |               [ [ic:settings] App Settings ]          |  |
|  +-------------------------------------------------------+  |
|  +-------------------------------------------------------+  |
|  |               [ [ic:logout] Log Out ]                 |  |
|  +-------------------------------------------------------+  |
|                                                             |
+-------------------------------------------------------------+
| [ic:home Home] [ic:history History] [ic:leaderboard Rank] [ic:person Profile*]|
+-------------------------------------------------------------+
```

---

### Screen 7: `SettingsActivity`

**Purpose:** Manages preferences, theme modes, step counting units, manual sync triggers, and critical data management actions.

#### Layout Sketch:
```
+-------------------------------------------------------------+
| 09:41                                            [status_bar]
|                                                             |
| [ic:arrow_back] Settings                                    |
|                                                             |
| ACCOUNT PREFERENCES                                         |
| ----------------------------------------------------------- |
| Daily Step Goal                                             |
| [=========●====================]  10,000 Steps              |
|                                                             |
| DISPLAY & NOTIFICATIONS                                     |
| ----------------------------------------------------------- |
| [ic:dark_mode] Dark Theme          [ System Default  ▼ ]    |
| [ic:straighten] Step Units         [ (•) Steps  ( ) Steps+Km]
| [ic:notifications_active] Milestone Reminders  [ ON  [🔘] ] |
| Inactivity Alerts                              [ OFF [⚪] ] |
|                                                             |
| DATA & SYNCHRONIZATION                                      |
| ----------------------------------------------------------- |
| Backend Sync Status                [ic:cloud_done] Synced   |
| [ [ic:sync] Sync Pending Data Now ]                         |
| [ [ic:delete_sweep] Clear Local History Cache ]             |
|                                                             |
| ABOUT & LEGAL                                               |
| ----------------------------------------------------------- |
| App Version                        1.0.0 (Build 42)         |
| Privacy Policy & Terms             [ [ic:open_in_new] View ]|
| Guest Mode Limitations             [ [ic:info] Details ]    |
|                                                             |
| DANGER ZONE                                                 |
| ----------------------------------------------------------- |
| [ [ic:logout] Log Out Account ]                             |
| [ [ic:warning] Delete Account & All Data ]                  |
|                                                             |
+-------------------------------------------------------------+
```

---

## 4. Modal Dialogs & Feedback Overlay Wireframes

### 4.1 Delete Record Confirmation Dialog
```
+---------------------------------------------------+
|  [ic:delete] Delete History Record?               |
|                                                   |
|  Are you sure you want to delete the step record  |
|  for Friday, Aug 7, 2026 (11,100 steps)?          |
|  This action cannot be undone.                    |
|                                                   |
|                [ CANCEL ]   [ DELETE (Red) ]      |
+---------------------------------------------------+
```

### 4.2 Edit Daily Goal Modal Sheet
```
+---------------------------------------------------+
|  [ic:flag] Set Daily Step Goal                    |
|                                                   |
|  Choose a daily target that keeps you active:     |
|                                                   |
|        [ - 500 ]    10,000 Steps    [ + 500 ]     |
|                                                   |
|  Presets: [ 6,000 ]  [ 8,000 ]  [ 10,000 ]  [ 12,000 ] |
|                                                   |
|  Estimated calories burned: ~400 kcal/day         |
|                                                   |
|  +---------------------------------------------+  |
|  |              [ SAVE GOAL ]                  |  |
|  +---------------------------------------------+  |
+---------------------------------------------------+
```

### 4.3 Missing Hardware Sensor Fallback Notice
```
+---------------------------------------------------+
|  [ic:sensors_off] Step Sensor Not Detected        |
|                                                   |
|  Your device does not have a dedicated hardware   |
|  step counter. StepCount has switched to          |
|  accelerometer-based motion tracking.             |
|                                                   |
|  * Note: Keep the app running in background for   |
|    the most accurate step counting.               |
|                                                   |
|                   [ UNDERSTOOD ]                  |
+---------------------------------------------------+
```

### 4.4 Account Deletion Danger Modal
```
+---------------------------------------------------+
|  [ic:warning] Permanently Delete Account?         |
|                                                   |
|  This will permanently erase your user profile,   |
|  all historical step data, and leaderboard rank   |
|  from both this device and our servers.           |
|                                                   |
|  Type "DELETE" to confirm:                        |
|  +---------------------------------------------+  |
|  | DELETE|                                     |  |
|  +---------------------------------------------+  |
|                                                   |
|            [ CANCEL ]   [ PERMANENTLY DELETE ]    |
+---------------------------------------------------+
```

---

## 5. UI Accessibility & Responsive Constraints

1. **Contrast Ratio:** All body text meets WCAG AA standard (minimum 4.5:1 against background).
2. **Dynamic Scaling:** All typography defined using `sp` units to scale gracefully with system accessibility text size changes.
3. **Screen Adaptation:** 
   - Uses Android `ConstraintLayout` and Jetpack Compose adaptive layouts.
   - On small screens (e.g. 4.7" devices), circular progress scales proportionally while cards remain scrollable inside a `NestedScrollView`.
   - On tablets / foldables, Dashboard splits into a 2-column layout (Step Ring on left, History/Leaderboard cards on right).
4. **Haptic Feedback:** Vibrations on goal completion and critical action confirmations (deletion, sync trigger).
