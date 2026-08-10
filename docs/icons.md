# StepCount – Material Design 3 Icons Reference

**Document Version:** 1.1  
**Scope:** Android UI / Jetpack Compose & Google Material Symbols  
**Library:** `androidx.compose.material:material-icons-extended`  
**Icon Catalog Source:** [Google Fonts Icons](https://fonts.google.com/icons)  

This document provides the complete, sequential mapping of all UI icons used across the StepCount application with direct links to [Google Fonts Icons](https://fonts.google.com/icons) for quick preview, SVG download, and Compose code reference.

> [!NOTE]
> All 49 vector `.svg` icons have also been downloaded locally into the [docs/icons/svg/](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons/svg) folder for direct asset import into Android Studio (`res/drawable`) or offline vector design work.

---

## 1. Dependency Configuration

Ensure the extended Material Icons library is declared in your project:

### [gradle/libs.versions.toml](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/android/StepCount/gradle/libs.versions.toml)
```toml
[libraries]
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
```

### [app/build.gradle.kts](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/android/StepCount/app/build.gradle.kts)
```kotlin
dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
}
```

---

## 2. Icon Mappings with Google Fonts Reference Links

### 2.1 Bottom Navigation & Top App Bars

| UI Location | Purpose | Material Symbol | Compose Code | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- |
| **Bottom Nav: Home** | Dashboard landing | `home` | `Icons.Rounded.Home` | [home on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:home) |
| **Bottom Nav: History** | Step records list | `history` | `Icons.Rounded.History` | [history on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:history) |
| **Bottom Nav: Leaderboard** | User rankings | `leaderboard` | `Icons.Rounded.Leaderboard` | [leaderboard on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:leaderboard) |
| **Bottom Nav: Profile** | User account & stats | `person` | `Icons.Rounded.Person` | [person on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:person) |
| **Top App Bar: Settings** | Open settings activity | `settings` | `Icons.Rounded.Settings` | [settings on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:settings) |
| **Top App Bar: Notifications**| View goal alerts | `notifications` | `Icons.Rounded.Notifications` | [notifications on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:notifications) |
| **Top App Bar: Back** | Up / Back navigation | `arrow_back` | `Icons.AutoMirrored.Rounded.ArrowBack` | [arrow_back on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:arrow_back) |

---

### 2.2 Fitness Metrics & Dashboard Screen

| Metric / Component | Purpose | Material Symbol | Compose Code | Default Tint | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Step Counter** | Primary step tracking | `directions_walk` | `Icons.Rounded.DirectionsWalk` | Primary Blue | [directions_walk on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:directions_walk) |
| **Calories Burned** | Energy expenditure (kcal) | `local_fire_department` | `Icons.Rounded.LocalFireDepartment` | `#FF6D00` (Orange) | [local_fire_department on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:local_fire_department) |
| **Distance Traveled** | Walking distance (km) | `straighten` | `Icons.Rounded.Straighten` | Secondary Teal | [straighten on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:straighten) |
| **Active Route** | Alternative distance path | `route` | `Icons.Rounded.Route` | Secondary Teal | [route on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:route) |
| **Active Minutes** | Duration of movement | `timer` | `Icons.Rounded.Timer` | Tertiary Purple | [timer on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:timer) |
| **Daily Goal Target** | Target milestone indicator | `flag` | `Icons.Rounded.Flag` | Primary Blue | [flag on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:flag) |
| **Goal Accuracy Track**| Alternative goal target | `track_changes` | `Icons.Rounded.TrackChanges` | Primary Blue | [track_changes on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:track_changes) |
| **Motivation Quote** | Daily quote header | `format_quote` | `Icons.Rounded.FormatQuote` | OnContainer | [format_quote on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:format_quote) |
| **Inspirational Tip** | Alternative tip icon | `lightbulb` | `Icons.Rounded.Lightbulb` | Amber | [lightbulb on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:lightbulb) |
| **Refresh Action** | Fetch latest steps/quote | `refresh` | `Icons.Rounded.Refresh` | Surface Variant | [refresh on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:refresh) |
| **Goal Reached** | Celebration banner | `celebration` | `Icons.Rounded.Celebration` | `#4CAF50` (Green) | [celebration on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:celebration) |
| **Trophy Milestone** | Daily milestone cup | `emoji_events` | `Icons.Rounded.EmojiEvents` | `#FFB300` (Gold) | [emoji_events on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:emoji_events) |

---

### 2.3 Authentication Screen (`AuthActivity`)

| Input Field / Action | Purpose | Material Symbol | Compose Code | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- |
| **Email Field** | Email leading icon | `email` | `Icons.Rounded.Email` | [email on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:email) |
| **Password Field** | Password leading icon | `lock` | `Icons.Rounded.Lock` | [lock on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:lock) |
| **Show Password** | Trailing visibility toggle | `visibility` | `Icons.Rounded.Visibility` | [visibility on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:visibility) |
| **Hide Password** | Trailing visibility toggle | `visibility_off` | `Icons.Rounded.VisibilityOff` | [visibility_off on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:visibility_off) |
| **Full Name Field** | Name leading icon | `account_circle` | `Icons.Rounded.AccountCircle` | [account_circle on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:account_circle) |
| **Validation Success** | Real-time valid indicator | `check_circle` | `Icons.Rounded.CheckCircle` | [check_circle on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:check_circle) |
| **Guest Mode Button** | Offline tracking access | `person_off` | `Icons.Rounded.PersonOff` | [person_off on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:person_off) |

---

### 2.4 History Screen (`HistoryActivity`)

| Component / Action | Purpose | Material Symbol | Compose Code | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- |
| **Date Header** | Historical day indicator | `calendar_today` | `Icons.Rounded.CalendarToday` | [calendar_today on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:calendar_today) |
| **Goal Met Status** | 100%+ goal achieved tag | `check_circle` | `Icons.Rounded.CheckCircle` | [check_circle on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:check_circle) |
| **Goal In-Progress** | Partial goal achieved tag | `pending` | `Icons.Rounded.Pending` | [pending on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:pending) |
| **Synced Status** | Uploaded to backend | `cloud_done` | `Icons.Rounded.CloudDone` | [cloud_done on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:cloud_done) |
| **Pending Sync** | Local-only / waiting sync | `cloud_sync` | `Icons.Rounded.CloudSync` | [cloud_sync on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:cloud_sync) |
| **Edit Record** | Modify step entry | `edit` | `Icons.Rounded.Edit` | [edit on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:edit) |
| **Delete Record** | Remove step entry | `delete` | `Icons.Rounded.Delete` | [delete on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:delete) |
| **Empty History** | Empty state illustration | `folder_open` | `Icons.Rounded.FolderOpen` | [folder_open on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:folder_open) |

---

### 2.5 Leaderboard Screen (`LeaderboardActivity`)

| Element | Purpose | Material Symbol | Compose Code | Tint Recommendation | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Podium Medal (Gold/Silver/Bronze)** | Top 3 rank podium | `military_tech` | `Icons.Rounded.MilitaryTech` | `#FFD700` / `#C0C0C0` / `#CD7F32` | [military_tech on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:military_tech) |
| **Leaderboard Crown / Cup** | Top ranking standings | `emoji_events` | `Icons.Rounded.EmojiEvents` | Primary Blue | [emoji_events on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:emoji_events) |
| **Goal Percentage Star** | Achievement score tag | `star` | `Icons.Rounded.Star` | `#FFB300` (Amber) | [star on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:star) |
| **Cached Notice** | Offline data warning | `info` | `Icons.Rounded.Info` | Error Red | [info on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:info) |

---

### 2.6 Profile & Settings Screens

| Feature / Setting | Purpose | Material Symbol | Compose Code | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- |
| **Change Avatar** | Camera overlay badge | `photo_camera` | `Icons.Rounded.PhotoCamera` | [photo_camera on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:photo_camera) |
| **Streak Milestone** | Longest walking streak | `whatshot` | `Icons.Rounded.Whatshot` | [whatshot on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:whatshot) |
| **Dark Theme** | Appearance mode toggle | `dark_mode` | `Icons.Rounded.DarkMode` | [dark_mode on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:dark_mode) |
| **Notifications** | Reminder toggle | `notifications_active` | `Icons.Rounded.NotificationsActive` | [notifications_active on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:notifications_active) |
| **Manual Sync** | Trigger instant upload | `sync` | `Icons.Rounded.Sync` | [sync on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:sync) |
| **Clear Cache** | Erase local history | `delete_sweep` | `Icons.Rounded.DeleteSweep` | [delete_sweep on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:delete_sweep) |
| **External Web link** | Privacy Policy & Terms | `open_in_new` | `Icons.AutoMirrored.Rounded.OpenInNew` | [open_in_new on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:open_in_new) |
| **Log Out** | Sign out of Firebase | `logout` | `Icons.AutoMirrored.Rounded.Logout` | [logout on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:logout) |
| **Delete Account** | Danger zone action | `warning` | `Icons.Rounded.Warning` | [warning on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:warning) |

---

### 2.7 Alerts, Modals & System Status

| State / Dialog | Purpose | Material Symbol | Compose Code | Google Fonts Reference Link |
| :--- | :--- | :--- | :--- | :--- |
| **Offline Mode** | No connection banner | `cloud_off` | `Icons.Rounded.CloudOff` | [cloud_off on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:cloud_off) |
| **Missing Sensor** | Hardware fallback alert | `sensors_off` | `Icons.Rounded.SensorsOff` | [sensors_off on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:sensors_off) |
| **Confirm Action** | Dialog question icon | `help_outline` | `Icons.Rounded.HelpOutline` | [help_outline on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:help_outline) |
| **Success Toast** | Confirmation snackbar | `done_all` | `Icons.Rounded.DoneAll` | [done_all on Google Fonts](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:done_all) |

---

## 3. Jetpack Compose Implementation Examples

### Metric Card with Custom Tint
```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Calories",
            tint = Color(0xFFFF6D00),
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(text = value, style = MaterialTheme.typography.titleMedium)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

### Bottom Navigation Bar Setup
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun StepCountBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedRoute == "dashboard",
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Rounded.DirectionsWalk, contentDescription = "Dashboard") },
            label = { Text("Today") }
        )
        NavigationBarItem(
            selected = selectedRoute == "history",
            onClick = { onNavigate("history") },
            icon = { Icon(Icons.Rounded.History, contentDescription = "History") },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = selectedRoute == "leaderboard",
            onClick = { onNavigate("leaderboard") },
            icon = { Icon(Icons.Rounded.Leaderboard, contentDescription = "Leaderboard") },
            label = { Text("Rankings") }
        )
        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Rounded.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
```
