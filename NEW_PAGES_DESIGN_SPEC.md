# FIVUCSAS Client Apps - New Pages Design Specification

**Date:** January 31, 2026
**Purpose:** Additional screens needed to complete the application
**Current Screen Count:** 5 Android + 7 Desktop = 12 total
**Target Screen Count:** 16 Android + 10 Desktop = 26 total

---

## Table of Contents

1. [Current Screen Inventory](#current-screen-inventory)
2. [Gap Analysis](#gap-analysis)
3. [New Android Screens (11 new)](#new-android-screens)
4. [New Desktop Screens (3 new)](#new-desktop-screens)
5. [Updated Navigation Graph](#updated-navigation-graph)
6. [Design System Guidelines](#design-system-guidelines)
7. [Implementation Priority](#implementation-priority)

---

## Current Screen Inventory

### Android App (5 screens)

| # | Screen | File | Purpose |
|---|--------|------|---------|
| 1 | Login | `LoginScreen.kt` (130 lines) | Email + password login |
| 2 | Register | `RegisterScreen.kt` (155 lines) | 4-field registration form |
| 3 | Home | `HomeScreen.kt` (241 lines) | Greeting, enroll/verify buttons |
| 4 | Biometric Enroll | `BiometricEnrollScreen.kt` (400 lines) | CameraX face capture for enrollment |
| 5 | Biometric Verify | `BiometricVerifyScreen.kt` (499 lines) | CameraX face capture for verification |

### Desktop App (7 screens)

| # | Screen | File | Purpose |
|---|--------|------|---------|
| 1 | Kiosk Welcome | `WelcomeScreen.kt` (199 lines) | Landing page with Enroll/Verify buttons |
| 2 | Kiosk Enroll | `EnrollScreen.kt` (220 lines) | 5-field form + photo capture |
| 3 | Kiosk Verify | `VerifyScreen.kt` (217 lines) | Capture + result display |
| 4 | Admin Users | `UsersTab.kt` (482 lines) | User CRUD table |
| 5 | Admin Analytics | `AnalyticsTab.kt` (303 lines) | Stats cards + charts |
| 6 | Admin Security | `SecurityTab.kt` (51 lines) | Placeholder only |
| 7 | Admin Settings | `SettingsTab.kt` (934 lines) | 6-section settings |

### What Is Missing

The Android app is **critically thin** - it only has auth (login/register) and biometric (enroll/verify) with a basic home screen. A production-quality biometric identity app needs user account management, activity history, onboarding, settings, and informational pages.

The Desktop app is missing its Security tab implementation and a dedicated Reports/Export screen.

---

## Gap Analysis

| Category | What Exists | What Is Missing |
|----------|-------------|-----------------|
| **Onboarding** | None | Splash screen, welcome tutorial, permission setup |
| **Authentication** | Login, Register | Forgot password, OTP verification, session expired |
| **User Profile** | Name shown on Home | Profile view/edit, avatar, account details |
| **Dashboard** | Basic Home with 2 buttons | Activity summary, stats, recent history, quick actions |
| **Biometric** | Enroll + Verify | Enrollment status, enrollment history, re-enrollment |
| **History/Activity** | None | Verification log, activity timeline |
| **Settings** | None on Android | App preferences, theme, notifications, security |
| **Help/Support** | None | FAQ, user guide, contact support |
| **Notifications** | None | Alert list, activity notifications |
| **Reports** | None on Desktop | PDF/CSV export, date-range reports |

---

## New Android Screens

### Screen 1: Splash & Onboarding Screen

**File:** `androidApp/.../ui/screen/SplashScreen.kt`
**Priority:** HIGH
**Purpose:** First impression, token check, and first-time onboarding

#### Design Specification

```
+------------------------------------------+
|                                          |
|                                          |
|           [FIVUCSAS Logo]               |
|           (120dp circular)               |
|                                          |
|           F I V U C S A S               |
|      Secure Identity Verification       |
|                                          |
|         [Loading Spinner]               |
|       Checking authentication...         |
|                                          |
|                                          |
+------------------------------------------+
```

**Behavior:**
- Show for 1.5 seconds minimum (brand awareness)
- Check if valid JWT token exists in secure storage
  - Token valid -> navigate to Dashboard
  - Token expired -> attempt refresh, if fail -> Login
  - No token -> first launch? -> Onboarding, otherwise -> Login
- Animated logo (fade-in + scale from 0.8 to 1.0)
- Background: gradient matching app primary colors

#### Onboarding Flow (first launch only)

```
+------------------------------------------+
|                                          |
|        [Illustration/Lottie]            |
|         (280dp height area)              |
|                                          |
|    "Secure Biometric Authentication"     |
|                                          |
|    Verify your identity instantly        |
|    using facial recognition              |
|                                          |
|           o  .  .   (page dots)          |
|                                          |
|       [ Get Started ]  (primary btn)     |
|        Skip  (text button)               |
+------------------------------------------+
```

**3 Onboarding Pages:**
1. **Welcome** - App introduction, biometric security concept
2. **How It Works** - 3-step process: Register -> Enroll Face -> Verify Anytime
3. **Permissions** - Camera access needed, privacy assurance

**Components needed:**
- `HorizontalPager` (Compose) for swipeable pages
- Page indicator dots
- "Get Started" button on last page
- "Skip" text button on all pages
- Store `isFirstLaunch = false` in SharedPreferences after completion

---

### Screen 2: Forgot Password Screen

**File:** `androidApp/.../ui/screen/ForgotPasswordScreen.kt`
**Priority:** HIGH
**Purpose:** Password recovery via email

#### Design Specification

```
+------------------------------------------+
|  [<-]   Reset Password                   |
|------------------------------------------|
|                                          |
|           [Lock Icon 64dp]              |
|                                          |
|        Forgot Your Password?             |
|                                          |
|   Enter your email address and we'll    |
|   send you a reset link.                |
|                                          |
|   +----------------------------------+  |
|   | Email Address          [@]       |  |
|   +----------------------------------+  |
|                                          |
|   [ Send Reset Link ]  (full width)     |
|                                          |
|   Remember your password? Log in         |
|                                          |
+------------------------------------------+
```

**After submission - Confirmation state:**

```
+------------------------------------------+
|  [<-]   Reset Password                   |
|------------------------------------------|
|                                          |
|        [Email Sent Icon 64dp]           |
|         (green checkmark)                |
|                                          |
|          Check Your Email                |
|                                          |
|   We've sent a password reset link      |
|   to john@example.com                    |
|                                          |
|   Didn't receive it?                     |
|   [ Resend Email ]  (outlined btn)      |
|                                          |
|   [ Back to Login ]  (primary btn)      |
|                                          |
+------------------------------------------+
```

**States:**
- **Input state:** Email field + submit button
- **Loading state:** Button shows spinner, field disabled
- **Success state:** Confirmation message with resend option
- **Error state:** Inline error under email field ("Email not found")

**Validation:**
- Email format validation before submit
- Rate limit: disable resend for 60 seconds (show countdown)

---

### Screen 3: Dashboard Screen (Enhanced Home)

**File:** `androidApp/.../ui/screen/DashboardScreen.kt`
**Priority:** HIGH
**Purpose:** Replace basic Home with a comprehensive dashboard

#### Design Specification

```
+------------------------------------------+
|  FIVUCSAS              [Bell] [Avatar]   |
|------------------------------------------|
|                                          |
|  Good Morning, John                      |
|  Welcome back                            |
|                                          |
|  +--------------------------------------+|
|  |  ENROLLMENT STATUS                   ||
|  |  [Face Icon]  Enrolled               ||
|  |  Last verified: 2 hours ago          ||
|  |  Confidence: 94%                     ||
|  +--------------------------------------+|
|                                          |
|  Quick Actions                           |
|  +----------------+ +----------------+   |
|  | [Camera Icon]  | | [Shield Icon]  |   |
|  |    Enroll      | |    Verify      |   |
|  |    Face        | |    Identity    |   |
|  +----------------+ +----------------+   |
|  +----------------+ +----------------+   |
|  | [History Icon] | | [Person Icon]  |   |
|  |   Activity     | |   Profile      |   |
|  |   History      | |   Settings     |   |
|  +----------------+ +----------------+   |
|                                          |
|  Recent Activity                         |
|  +--------------------------------------+|
|  | [check] Verification successful      ||
|  |         Today, 10:30 AM  94%         ||
|  +--------------------------------------+|
|  | [check] Verification successful      ||
|  |         Yesterday, 3:15 PM  91%      ||
|  +--------------------------------------+|
|  | [x]     Verification failed          ||
|  |         Yesterday, 3:14 PM  62%      ||
|  +--------------------------------------+|
|  | [+]     Face enrolled                ||
|  |         Jan 28, 2026  Quality: 88%   ||
|  +--------------------------------------+|
|                                          |
|  [ View All Activity ]  (text button)   |
|                                          |
+------------------------------------------+
|  [Home]    [History]    [Profile]        |
+------------------------------------------+
```

**Sections:**
1. **Header:** App name, notification bell (with badge count), avatar thumbnail
2. **Greeting:** Time-based greeting + user name
3. **Enrollment Status Card:**
   - Icon + status text (Enrolled / Not Enrolled / Expired)
   - Last verification timestamp
   - Confidence score from last verification
   - Color: Green (enrolled), Orange (expiring), Red (not enrolled)
4. **Quick Actions Grid:** 2x2 cards with icons
   - Enroll Face -> BiometricEnrollScreen
   - Verify Identity -> BiometricVerifyScreen
   - Activity History -> ActivityHistoryScreen
   - Profile -> ProfileScreen
5. **Recent Activity List:** Last 4 events
   - Each item: icon + description + timestamp + score
   - "View All" link -> ActivityHistoryScreen
6. **Bottom Navigation Bar:** Home | History | Profile (3 tabs)

**Data Sources:**
- User info from auth state
- Enrollment status from Biometric API
- Recent activity from verification history API
- Notification count from notifications API

---

### Screen 4: Profile Screen

**File:** `androidApp/.../ui/screen/ProfileScreen.kt`
**Priority:** HIGH
**Purpose:** View and edit user account information

#### Design Specification

```
+------------------------------------------+
|  [<-]   My Profile              [Edit]   |
|------------------------------------------|
|                                          |
|           [Avatar Circle 96dp]          |
|           (First letter or photo)        |
|           [ Change Photo ]              |
|                                          |
|           John Doe                       |
|           john@example.com              |
|           Member since Jan 2026         |
|                                          |
|  +--------------------------------------+|
|  | Personal Information            [>]  ||
|  |  Name: John Doe                      ||
|  |  Email: john@example.com             ||
|  |  Phone: +1 234 567 8900             ||
|  |  ID Number: NIC-12345678            ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Biometric Status               [>]  ||
|  |  [Green dot] Face Enrolled           ||
|  |  Quality Score: 88%                  ||
|  |  Enrolled: Jan 28, 2026             ||
|  |  Expires: Jul 28, 2026             ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Security                       [>]  ||
|  |  Last login: Today, 9:00 AM         ||
|  |  Login IP: 192.168.1.100           ||
|  |  Sessions: 1 active                 ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Account Actions                      ||
|  |  [ Change Password ]                ||
|  |  [ Re-Enroll Face ]                 ||
|  |  [ Delete Account ] (red text)      ||
|  +--------------------------------------+|
|                                          |
+------------------------------------------+
|  [Home]    [History]    [Profile]        |
+------------------------------------------+
```

**Sections:**
1. **Avatar & Basic Info:** Circular avatar (letter-based or photo), name, email, member date
2. **Personal Information Card:** Expandable card with user details, edit icon
3. **Biometric Status Card:** Enrollment status, quality score, dates
4. **Security Card:** Last login info, active sessions
5. **Account Actions:** Change password, re-enroll, delete account (with confirmation dialog)

**Edit Mode:**
- Tapping "Edit" makes name and phone fields editable
- Save/Cancel buttons appear
- Email is read-only (shown as disabled field)

---

### Screen 5: Edit Profile Screen

**File:** `androidApp/.../ui/screen/EditProfileScreen.kt`
**Priority:** MEDIUM
**Purpose:** Edit user personal information

#### Design Specification

```
+------------------------------------------+
|  [<-]   Edit Profile           [Save]    |
|------------------------------------------|
|                                          |
|           [Avatar Circle 80dp]          |
|           [ Change Photo ]              |
|                                          |
|   +----------------------------------+  |
|   | First Name              [Person] |  |
|   | John                             |  |
|   +----------------------------------+  |
|                                          |
|   +----------------------------------+  |
|   | Last Name               [Person] |  |
|   | Doe                              |  |
|   +----------------------------------+  |
|                                          |
|   +----------------------------------+  |
|   | Email (read-only)        [Lock]  |  |
|   | john@example.com                 |  |
|   +----------------------------------+  |
|                                          |
|   +----------------------------------+  |
|   | Phone Number             [Phone] |  |
|   | +1 234 567 8900                  |  |
|   +----------------------------------+  |
|                                          |
|   +----------------------------------+  |
|   | ID Number                [Badge] |  |
|   | NIC-12345678                     |  |
|   +----------------------------------+  |
|                                          |
|   [ Save Changes ]  (full width btn)    |
|                                          |
+------------------------------------------+
```

**Validation:**
- First/Last name: required, min 2 characters
- Phone: optional, format validation
- ID Number: read-only after initial registration
- Show unsaved changes warning on back-press

---

### Screen 6: Activity History Screen

**File:** `androidApp/.../ui/screen/ActivityHistoryScreen.kt`
**Priority:** HIGH
**Purpose:** Full history of verification and enrollment events

#### Design Specification

```
+------------------------------------------+
|  [<-]   Activity History                 |
|------------------------------------------|
|                                          |
|  [All] [Verifications] [Enrollments]    |
|  (filter chips - horizontally scrollable)|
|                                          |
|  Today                                   |
|  +--------------------------------------+|
|  | [Shield-Check]                       ||
|  | Verification Successful              ||
|  | 10:30 AM  |  Confidence: 94%        ||
|  | Device: Android  |  IP: 192.168.1.5 ||
|  +--------------------------------------+|
|  | [Shield-Check]                       ||
|  | Verification Successful              ||
|  | 09:15 AM  |  Confidence: 91%        ||
|  +--------------------------------------+|
|                                          |
|  Yesterday                               |
|  +--------------------------------------+|
|  | [Shield-X]                           ||
|  | Verification Failed                  ||
|  | 3:14 PM  |  Confidence: 62%         ||
|  | Reason: Low confidence score         ||
|  +--------------------------------------+|
|  | [Shield-Check]                       ||
|  | Verification Successful              ||
|  | 3:15 PM  |  Confidence: 91%         ||
|  +--------------------------------------+|
|                                          |
|  January 28, 2026                        |
|  +--------------------------------------+|
|  | [User-Plus]                          ||
|  | Face Enrollment Completed            ||
|  | 2:00 PM  |  Quality: 88%            ||
|  +--------------------------------------+|
|                                          |
|  (Load more at bottom)                   |
|                                          |
+------------------------------------------+
|  [Home]    [History]    [Profile]        |
+------------------------------------------+
```

**Features:**
- **Filter Chips:** All | Verifications | Enrollments (toggle active filter)
- **Grouped by Date:** Section headers for Today, Yesterday, specific dates
- **Event Cards:** Each event shows:
  - Icon (color-coded: green check, red X, blue plus)
  - Event type and result
  - Timestamp
  - Confidence/quality score
  - Device and IP (optional)
  - Failure reason (for failed events)
- **Pagination:** Load more on scroll to bottom
- **Empty State:** "No activity yet" with illustration if no events
- **Pull-to-Refresh:** Swipe down to refresh list

**Event Types:**
- `VERIFICATION_SUCCESS` - green shield-check icon
- `VERIFICATION_FAILED` - red shield-x icon
- `ENROLLMENT_COMPLETED` - blue user-plus icon
- `ENROLLMENT_FAILED` - orange user-x icon
- `LOGIN` - gray login icon
- `PASSWORD_CHANGED` - yellow key icon

---

### Screen 7: Notifications Screen

**File:** `androidApp/.../ui/screen/NotificationsScreen.kt`
**Priority:** MEDIUM
**Purpose:** System alerts and activity notifications

#### Design Specification

```
+------------------------------------------+
|  [<-]   Notifications    [Mark All Read] |
|------------------------------------------|
|                                          |
|  New                                     |
|  +--------------------------------------+|
|  | [!] Security Alert           2m ago  ||
|  | Failed verification attempt          ||
|  | detected from unknown device.        ||
|  | (blue left border = unread)          ||
|  +--------------------------------------+|
|  | [check] Enrollment Update    1h ago  ||
|  | Your face enrollment was             ||
|  | successfully updated.                ||
|  +--------------------------------------+|
|                                          |
|  Earlier                                 |
|  +--------------------------------------+|
|  | [info] System Update       Yesterday ||
|  | FIVUCSAS v1.1 is now available.     ||
|  | (gray left border = read)            ||
|  +--------------------------------------+|
|  | [check] Verification        2d ago   ||
|  | Identity verified successfully       ||
|  | at Main Office kiosk.                ||
|  +--------------------------------------+|
|                                          |
|  (Empty state if no notifications)       |
|  "You're all caught up!"                 |
|  [Bell icon with checkmark]              |
|                                          |
+------------------------------------------+
```

**Features:**
- **Unread indicator:** Blue left border + bold title for unread
- **Read indicator:** Gray left border + normal weight
- **Mark All Read:** Button in top bar
- **Swipe actions:** Swipe left to delete, swipe right to mark read/unread
- **Notification types:**
  - Security Alert (warning icon, orange)
  - Verification Result (check/x icon, green/red)
  - Enrollment Update (user icon, blue)
  - System Update (info icon, gray)
  - Account Activity (person icon, purple)
- **Grouped:** "New" (unread) and "Earlier" (read) sections
- **Tap action:** Navigate to relevant screen (e.g., security alert -> activity history)

---

### Screen 8: Settings Screen (Android)

**File:** `androidApp/.../ui/screen/SettingsScreen.kt`
**Priority:** HIGH
**Purpose:** App preferences and account settings

#### Design Specification

```
+------------------------------------------+
|  [<-]   Settings                         |
|------------------------------------------|
|                                          |
|  Account                                 |
|  +--------------------------------------+|
|  | [Person]  Profile Settings      [>]  ||
|  |----------------------------------------|
|  | [Lock]    Change Password       [>]  ||
|  |----------------------------------------|
|  | [Shield]  Two-Factor Auth       [>]  ||
|  |           Enabled (green dot)        ||
|  +--------------------------------------+|
|                                          |
|  Biometric                               |
|  +--------------------------------------+|
|  | [Face]    Face Enrollment       [>]  ||
|  |           Enrolled (green dot)       ||
|  |----------------------------------------|
|  | [Refresh] Re-Enroll Face        [>]  ||
|  +--------------------------------------+|
|                                          |
|  Preferences                             |
|  +--------------------------------------+|
|  | [Palette] Theme                      ||
|  |           [System v] (dropdown)      ||
|  |----------------------------------------|
|  | [Bell]    Notifications         [>]  ||
|  |           All enabled                ||
|  |----------------------------------------|
|  | [Globe]   Language                   ||
|  |           [English v] (dropdown)     ||
|  +--------------------------------------+|
|                                          |
|  About                                   |
|  +--------------------------------------+|
|  | [Info]    About FIVUCSAS        [>]  ||
|  |----------------------------------------|
|  | [Help]    Help & FAQ            [>]  ||
|  |----------------------------------------|
|  | [Doc]     Privacy Policy        [>]  ||
|  |----------------------------------------|
|  | [Doc]     Terms of Service      [>]  ||
|  +--------------------------------------+|
|                                          |
|  [ Log Out ]  (red outlined button)     |
|                                          |
|  App Version 1.0.0                       |
|                                          |
+------------------------------------------+
|  [Home]    [History]    [Profile]        |
+------------------------------------------+
```

**Sections:**
1. **Account:** Profile, password change, 2FA toggle
2. **Biometric:** Enrollment status, re-enroll option
3. **Preferences:** Theme (Light/Dark/System), notification settings, language
4. **About:** App info, help, legal documents
5. **Logout button** at bottom

**Theme switcher:**
- Dropdown: System Default | Light | Dark
- Applies immediately (no save button needed)
- Persisted in SharedPreferences

**Navigation targets:**
- Profile Settings -> EditProfileScreen
- Change Password -> ChangePasswordScreen
- Notifications -> NotificationSettingsScreen (sub-screen)
- About -> AboutScreen
- Help & FAQ -> HelpScreen

---

### Screen 9: Change Password Screen

**File:** `androidApp/.../ui/screen/ChangePasswordScreen.kt`
**Priority:** HIGH
**Purpose:** Secure password change with validation

#### Design Specification

```
+------------------------------------------+
|  [<-]   Change Password                  |
|------------------------------------------|
|                                          |
|           [Lock Icon 48dp]              |
|                                          |
|   +----------------------------------+  |
|   | Current Password         [Eye]   |  |
|   | ••••••••                         |  |
|   +----------------------------------+  |
|                                          |
|   +----------------------------------+  |
|   | New Password             [Eye]   |  |
|   | ••••••••                         |  |
|   +----------------------------------+  |
|                                          |
|   Password Strength: [====----] Good     |
|                                          |
|   +----------------------------------+  |
|   | Confirm New Password     [Eye]   |  |
|   | ••••••••                         |  |
|   +----------------------------------+  |
|                                          |
|   Requirements:                          |
|   [check] At least 8 characters         |
|   [check] Contains uppercase letter     |
|   [x]     Contains number               |
|   [x]     Contains special character    |
|                                          |
|   [ Update Password ]  (full width)     |
|                                          |
+------------------------------------------+
```

**Features:**
- **Password visibility toggle** (eye icon) on all 3 fields
- **Strength indicator:** Bar that fills with color (Red=Weak, Orange=Fair, Green=Good, Blue=Strong)
- **Real-time requirement checklist:**
  - Min 8 characters
  - Contains uppercase
  - Contains number
  - Contains special character
  - Checkmark turns green when met
- **Confirm password:** Shows error if doesn't match
- **Submit:** Disabled until all requirements met and passwords match
- **Success:** Show snackbar "Password updated" and navigate back

---

### Screen 10: Help & FAQ Screen

**File:** `androidApp/.../ui/screen/HelpScreen.kt`
**Priority:** LOW
**Purpose:** User guidance and support

#### Design Specification

```
+------------------------------------------+
|  [<-]   Help & FAQ                       |
|------------------------------------------|
|                                          |
|  +----------------------------------+   |
|  | [Search]  Search help topics...   |   |
|  +----------------------------------+   |
|                                          |
|  Getting Started                         |
|  +--------------------------------------+|
|  | [v] How do I enroll my face?         ||
|  |     To enroll your face:             ||
|  |     1. Go to Dashboard               ||
|  |     2. Tap "Enroll Face"             ||
|  |     3. Position your face in the     ||
|  |        circle guide                   ||
|  |     4. Tap Capture                    ||
|  |     5. Wait for confirmation          ||
|  +--------------------------------------+|
|  | [>] How does verification work?      ||
|  +--------------------------------------+|
|  | [>] What is liveness detection?      ||
|  +--------------------------------------+|
|                                          |
|  Account & Security                      |
|  +--------------------------------------+|
|  | [>] How do I reset my password?      ||
|  +--------------------------------------+|
|  | [>] Is my biometric data secure?     ||
|  +--------------------------------------+|
|  | [>] How do I enable 2FA?             ||
|  +--------------------------------------+|
|                                          |
|  Troubleshooting                         |
|  +--------------------------------------+|
|  | [>] Camera not working               ||
|  +--------------------------------------+|
|  | [>] Verification keeps failing       ||
|  +--------------------------------------+|
|  | [>] App is running slowly            ||
|  +--------------------------------------+|
|                                          |
|  Still need help?                        |
|  +--------------------------------------+|
|  | [Email]  Contact Support             ||
|  |          support@fivucsas.com        ||
|  |----------------------------------------|
|  | [Phone]  Call Us                     ||
|  |          +1 (800) 123-4567           ||
|  +--------------------------------------+|
|                                          |
+------------------------------------------+
```

**Features:**
- **Search bar:** Filter FAQ items by keyword
- **Expandable sections:** Tap to expand/collapse answers
- **Categorized:** Getting Started, Account & Security, Troubleshooting
- **Contact support:** Email and phone links (open email client / dialer)
- **Static content:** FAQ items can be hardcoded or fetched from API

---

### Screen 11: About Screen

**File:** `androidApp/.../ui/screen/AboutScreen.kt`
**Priority:** LOW
**Purpose:** App information, version, legal

#### Design Specification

```
+------------------------------------------+
|  [<-]   About                            |
|------------------------------------------|
|                                          |
|           [FIVUCSAS Logo 80dp]          |
|                                          |
|           FIVUCSAS                       |
|    Secure Identity Verification         |
|                                          |
|    Version 1.0.0 (Build 42)            |
|                                          |
|  +--------------------------------------+|
|  | Application                          ||
|  |  Version: 1.0.0                      ||
|  |  Build: 42                           ||
|  |  Platform: Android                   ||
|  |  Environment: Production             ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Developer                            ||
|  |  Rollingcat Software                 ||
|  |  https://rollingcat.com              ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Legal                                ||
|  |  Privacy Policy               [>]   ||
|  |  Terms of Service             [>]   ||
|  |  Open Source Licenses         [>]   ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | System Info                          ||
|  |  Device: Samsung Galaxy S24          ||
|  |  Android: 14                         ||
|  |  API Level: 34                       ||
|  |  Camera: Available                   ||
|  +--------------------------------------+|
|                                          |
+------------------------------------------+
```

---

## New Desktop Screens

### Desktop Screen 1: Security Tab (Full Implementation)

**File:** `desktopApp/.../tabs/SecurityTab.kt` (rewrite from placeholder)
**Priority:** CRITICAL
**Purpose:** Security monitoring, audit logs, session management

#### Design Specification

```
+------------------------------------------------------------------+
|  Security                                    [Refresh] [Export]   |
|------------------------------------------------------------------|
|                                                                  |
|  +------------+ +------------+ +------------+ +------------+     |
|  | Failed     | | Active     | | Locked     | | Alerts     |     |
|  | Logins     | | Sessions   | | Accounts   | | Today      |     |
|  |    12      | |     34     | |      2     | |     5      |     |
|  | (24 hours) | | (current)  | | (current)  | | (unread)   |     |
|  +------------+ +------------+ +------------+ +------------+     |
|                                                                  |
|  Security Alerts                                   [View All]    |
|  +--------------------------------------------------------------+|
|  | [!] CRITICAL  Failed login from unknown IP    2 min ago      ||
|  |     User: admin@fivucsas.com  IP: 45.67.89.1                ||
|  +--------------------------------------------------------------+|
|  | [!] WARNING   Multiple failed verifications   15 min ago     ||
|  |     User: john@example.com  Attempts: 5                     ||
|  +--------------------------------------------------------------+|
|  | [i] INFO      New device login                1 hour ago     ||
|  |     User: jane@example.com  Device: Windows PC               ||
|  +--------------------------------------------------------------+|
|                                                                  |
|  Audit Log                              [Filter v] [Date Range] |
|  +--------------------------------------------------------------+|
|  | Timestamp        | User        | Action     | Status | IP    ||
|  |------------------|-------------|------------|--------|-------||
|  | 2026-01-31 10:30 | admin@...   | LOGIN      | OK     | 192.. ||
|  | 2026-01-31 10:28 | john@...    | VERIFY     | FAIL   | 192.. ||
|  | 2026-01-31 10:25 | jane@...    | ENROLL     | OK     | 10... ||
|  | 2026-01-31 10:20 | admin@...   | USER_EDIT  | OK     | 192.. ||
|  | 2026-01-31 10:15 | john@...    | VERIFY     | OK     | 192.. ||
|  |             ...more rows...                                  ||
|  +--------------------------------------------------------------+|
|  |  [< Prev]  Page 1 of 12  [Next >]    Rows: [20 v]           ||
|  +--------------------------------------------------------------+|
|                                                                  |
|  Active Sessions                                                 |
|  +--------------------------------------------------------------+|
|  | User          | Device      | IP         | Since    | [Kill] ||
|  |---------------|-------------|------------|----------|--------||
|  | admin@...     | Desktop App | 192.168.1.5| 09:00 AM | [X]   ||
|  | john@...      | Android     | 192.168.1.8| 09:30 AM | [X]   ||
|  | jane@...      | Kiosk       | 192.168.1.2| 08:00 AM | [X]   ||
|  +--------------------------------------------------------------+|
|                                                                  |
+------------------------------------------------------------------+
```

**Sections:**
1. **Summary Cards (4):** Failed logins, active sessions, locked accounts, alerts count
2. **Security Alerts:** Recent alerts with severity badges (Critical=red, Warning=orange, Info=blue)
3. **Audit Log Table:** Paginated, sortable, filterable by action type and date range
4. **Active Sessions:** List with "Kill Session" button per row

---

### Desktop Screen 2: Reports Tab (New)

**File:** `desktopApp/.../tabs/ReportsTab.kt`
**Priority:** MEDIUM
**Purpose:** Generate and export reports for administration

#### Design Specification

```
+------------------------------------------------------------------+
|  Reports                                                         |
|------------------------------------------------------------------|
|                                                                  |
|  Generate Report                                                 |
|  +--------------------------------------------------------------+|
|  | Report Type:  [User Activity Report     v]                   ||
|  | Date Range:   [2026-01-01] to [2026-01-31]                  ||
|  | Format:       (o) PDF  ( ) CSV  ( ) Excel                   ||
|  | Include:      [x] Charts  [x] Raw Data  [ ] User Details    ||
|  |                                                              ||
|  |              [ Generate Report ]                             ||
|  +--------------------------------------------------------------+|
|                                                                  |
|  Available Report Types:                                         |
|  +----------------+ +----------------+ +----------------+        |
|  | User Activity  | | Verification   | | Enrollment     |        |
|  | Report         | | Report         | | Report         |        |
|  | Total users,   | | Success rates, | | New enrollments|        |
|  | login history, | | failed attempts| | quality scores |        |
|  | status changes | | trends, times  | | by period      |        |
|  +----------------+ +----------------+ +----------------+        |
|  +----------------+ +----------------+ +----------------+        |
|  | Security       | | System Health  | | Compliance     |        |
|  | Audit Report   | | Report         | | Report         |        |
|  | Failed logins, | | Uptime, API    | | Data retention |        |
|  | locked accounts| | response times | | audit trail    |        |
|  | IP analysis    | | error rates    | | access logs    |        |
|  +----------------+ +----------------+ +----------------+        |
|                                                                  |
|  Recent Reports                                                  |
|  +--------------------------------------------------------------+|
|  | Name                    | Generated    | Format | [Download] ||
|  |-------------------------|--------------|--------|------------||
|  | User Activity Jan 2026  | Jan 31, 10am | PDF    | [Download] ||
|  | Verification Weekly     | Jan 28, 9am  | CSV    | [Download] ||
|  | Security Audit Q4 2025  | Jan 02, 11am | PDF    | [Download] ||
|  +--------------------------------------------------------------+|
|                                                                  |
+------------------------------------------------------------------+
```

**Report Types:**
1. **User Activity Report:** User counts, login frequency, status distribution
2. **Verification Report:** Success/fail rates, average confidence, trends over time
3. **Enrollment Report:** New enrollments per period, quality scores, rejection rates
4. **Security Audit Report:** Failed logins, locked accounts, suspicious IP analysis
5. **System Health Report:** API uptime, response times, error rates
6. **Compliance Report:** Data retention, audit trail, access logs

---

### Desktop Screen 3: Admin Dashboard Overview Tab (New)

**File:** `desktopApp/.../tabs/OverviewTab.kt`
**Priority:** HIGH
**Purpose:** At-a-glance system status when admin first opens dashboard

#### Design Specification

```
+------------------------------------------------------------------+
|  Overview                                     Last updated: 10:30|
|------------------------------------------------------------------|
|                                                                  |
|  +------------+ +------------+ +------------+ +------------+     |
|  | Total      | | Active     | | Today's    | | Success    |     |
|  | Users      | | Sessions   | | Verif.     | | Rate       |     |
|  |   1,247    | |     34     | |    156     | |   94.2%    |     |
|  | +12 today  | | +5 vs avg  | | +23 vs avg| | +1.3% wow  |     |
|  +------------+ +------------+ +------------+ +------------+     |
|                                                                  |
|  +-----------------------------+ +-----------------------------+ |
|  | Verifications (7 days)      | | Enrollments (7 days)       | |
|  |                             | |                             | |
|  |  [Bar chart with daily      | |  [Line chart with daily    | |
|  |   verification counts       | |   enrollment counts         | |
|  |   stacked success/fail]     | |   with quality avg line]   | |
|  |                             | |                             | |
|  +-----------------------------+ +-----------------------------+ |
|                                                                  |
|  +-----------------------------+ +-----------------------------+ |
|  | Recent Activity             | | System Health               | |
|  |                             | |                             | |
|  | john@ verified OK  2m ago  | |  Identity API:   [GREEN]   | |
|  | jane@ enrolled     5m ago  | |  Biometric API:  [GREEN]   | |
|  | admin@ edited user 8m ago  | |  Database:       [GREEN]   | |
|  | bob@ verify FAIL  12m ago  | |  Camera Service: [YELLOW]  | |
|  | alice@ login       15m ago | |  Storage:  72% [========-] | |
|  |                             | |                             | |
|  | [View All Activity]         | |  Uptime: 99.8% (30 days)  | |
|  +-----------------------------+ +-----------------------------+ |
|                                                                  |
|  +--------------------------------------------------------------+|
|  | Quick Actions                                                ||
|  | [+ Add User] [Generate Report] [Export Users] [Clear Cache]  ||
|  +--------------------------------------------------------------+|
|                                                                  |
+------------------------------------------------------------------+
```

**Sections:**
1. **Summary Cards (4):** Total users (with delta), active sessions, today's verifications, success rate (with week-over-week change)
2. **Charts Row:** Verification bar chart (success/fail stacked) + Enrollment line chart
3. **Two-Column Row:**
   - Recent Activity: Live feed of last 5 events
   - System Health: API status indicators (green/yellow/red), storage usage, uptime
4. **Quick Actions Bar:** Common admin actions as buttons

---

## Updated Navigation Graph

### Android App (16 screens)

```
                    Splash/Onboarding
                          |
                          v
        +---------> Login <--------+
        |             |            |
        |             v            |
   Forgot          Register        |
   Password           |            |
        |             v            |
        +------> Dashboard <-------+
                  (Bottom Nav)
                 /     |     \
                v      v      v
             Home   History  Profile
              |        |        |
              v        v        v
         Enroll   Activity   Edit Profile
         Verify   History    Change Password
              |                Settings
              v                  |
         (Camera                 v
          Flow)            Help / About
                           Notifications
```

### Bottom Navigation (3 tabs)

| Tab | Icon | Screen | Badge |
|-----|------|--------|-------|
| Home | `home` | DashboardScreen | None |
| History | `history` | ActivityHistoryScreen | None |
| Profile | `person` | ProfileScreen | None |

### Full Route Table

| Route | Screen | Parent | Auth Required |
|-------|--------|--------|--------------|
| `splash` | SplashScreen | - | No |
| `onboarding` | OnboardingScreen | - | No |
| `login` | LoginScreen | - | No |
| `register` | RegisterScreen | - | No |
| `forgot-password` | ForgotPasswordScreen | - | No |
| `dashboard` | DashboardScreen | BottomNav | Yes |
| `activity-history` | ActivityHistoryScreen | BottomNav | Yes |
| `profile` | ProfileScreen | BottomNav | Yes |
| `edit-profile` | EditProfileScreen | Profile | Yes |
| `change-password` | ChangePasswordScreen | Settings | Yes |
| `settings` | SettingsScreen | Profile | Yes |
| `notifications` | NotificationsScreen | Dashboard | Yes |
| `help` | HelpScreen | Settings | Yes |
| `about` | AboutScreen | Settings | Yes |
| `biometric/enroll/{userId}` | BiometricEnrollScreen | Dashboard | Yes |
| `biometric/verify/{userId}` | BiometricVerifyScreen | Dashboard | Yes |

### Desktop App (10 screens)

```
Main Window
├── Kiosk Mode
│   ├── Welcome
│   ├── Enroll
│   └── Verify
└── Admin Dashboard
    ├── Overview (new)
    ├── Users
    ├── Analytics
    ├── Security (rewrite)
    ├── Reports (new)
    └── Settings
```

Admin Navigation Rail updated to 6 tabs:
1. Overview (new - dashboard icon)
2. Users (people icon)
3. Analytics (chart icon)
4. Security (shield icon)
5. Reports (document icon - new)
6. Settings (gear icon)

---

## Design System Guidelines

### Color Usage

| Element | Color | Usage |
|---------|-------|-------|
| Primary | `#1976D2` (Blue) | Primary buttons, active nav, links |
| Secondary | `#26A69A` (Teal) | Secondary actions, verify success |
| Success | `#4CAF50` (Green) | Success states, enrolled status, checkmarks |
| Error | `#E53935` (Red) | Errors, failed verification, delete actions |
| Warning | `#FF9800` (Orange) | Warnings, expiring enrollment, low confidence |
| Info | `#2196F3` (Light Blue) | Informational, tips, notifications |
| Surface | `#FFFFFF` / `#1E1E1E` | Cards, dialogs (light/dark) |
| Background | `#F5F5F5` / `#121212` | Screen background (light/dark) |

### Card Design Standard

All cards across the app should follow this pattern:
- Corner radius: 12dp
- Elevation: 2dp (light mode), 4dp (dark mode)
- Padding: 16dp internal
- Margin: 8dp between cards
- Section title above card: 14sp, medium weight, muted color

### Typography Scale

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Display | 32sp | Bold | Splash screen title |
| Headline | 24sp | SemiBold | Screen titles |
| Title | 20sp | SemiBold | Card titles, section headers |
| Subtitle | 16sp | Medium | Card subtitles |
| Body | 14sp | Normal | Main content text |
| Caption | 12sp | Normal | Timestamps, secondary info |
| Label | 11sp | Medium | Chip text, badges |

### Status Badge Design

| Status | Background | Text Color | Border |
|--------|-----------|------------|--------|
| Active / Success | `#E8F5E9` | `#2E7D32` | None |
| Inactive / Failed | `#FFEBEE` | `#C62828` | None |
| Pending / Warning | `#FFF3E0` | `#E65100` | None |
| Locked / Critical | `#FCE4EC` | `#AD1457` | None |
| Info | `#E3F2FD` | `#1565C0` | None |

### Bottom Navigation Bar (Android)

- 3 items maximum (Home, History, Profile)
- Material 3 `NavigationBar` component
- Active: filled icon + label, primary color
- Inactive: outlined icon, muted color
- Height: 80dp
- Always visible on main screens, hidden on sub-screens

### Empty State Standard

```
+--------------------------------------+
|                                      |
|         [Illustration 120dp]        |
|                                      |
|         Title (18sp, bold)          |
|                                      |
|    Description text (14sp, muted)   |
|    explaining what will appear       |
|    here or what action to take       |
|                                      |
|    [ Action Button ] (optional)     |
|                                      |
+--------------------------------------+
```

### Loading State Standard

- **Full screen:** Centered `CircularProgressIndicator` + message text
- **Inline:** Small spinner next to content being loaded
- **Button:** Replace button text with small spinner, disable button
- **Pull-to-refresh:** Material 3 `PullToRefreshBox`
- **Skeleton:** Gray placeholder rectangles matching content shape (for lists)

---

## Implementation Priority

### Priority 1 - Must Have (Critical Path)

| Screen | Platform | Member | Days |
|--------|----------|--------|------|
| Splash/Onboarding | Android | B | 1 |
| Dashboard (enhanced Home) | Android | B | 1.5 |
| Activity History | Android | B | 1 |
| Forgot Password | Android | B | 0.5 |
| Security Tab (full) | Desktop | A | 2 |
| Overview Tab | Desktop | A | 1.5 |
| Bottom Navigation setup | Android | B | 0.5 |

### Priority 2 - Should Have

| Screen | Platform | Member | Days |
|--------|----------|--------|------|
| Profile | Android | B | 1 |
| Settings (Android) | Android | B | 1 |
| Change Password | Android | B | 0.5 |
| Edit Profile | Android | B | 0.5 |
| Reports Tab | Desktop | A | 1.5 |

### Priority 3 - Nice to Have

| Screen | Platform | Member | Days |
|--------|----------|--------|------|
| Notifications | Android | B | 1 |
| Help & FAQ | Android | B | 0.5 |
| About | Android | B | 0.5 |

---

## New Shared Components Needed

These components should be added to the shared UI library to support the new screens:

| Component | Location | Used By |
|-----------|----------|---------|
| `BottomNavBar.kt` | `shared/.../ui/components/organisms/` | Android Dashboard |
| `ActivityItem.kt` | `shared/.../ui/components/molecules/` | Activity History, Dashboard |
| `NotificationItem.kt` | `shared/.../ui/components/molecules/` | Notifications Screen |
| `StatusBadge.kt` | `shared/.../ui/components/atoms/` | Multiple screens |
| `PasswordStrengthIndicator.kt` | `shared/.../ui/components/molecules/` | Change Password |
| `ExpandableCard.kt` | `shared/.../ui/components/molecules/` | FAQ, Settings |
| `SectionHeader.kt` | `shared/.../ui/components/atoms/` | Multiple screens |
| `FilterChipRow.kt` | `shared/.../ui/components/molecules/` | Activity History |
| `OnboardingPage.kt` | `shared/.../ui/components/organisms/` | Onboarding |
| `QuickActionGrid.kt` | `shared/.../ui/components/organisms/` | Dashboard |
| `PaginationControls.kt` | `shared/.../ui/components/molecules/` | Desktop tables |
| `HealthStatusIndicator.kt` | `shared/.../ui/components/atoms/` | Desktop Overview |

---

## New ViewModels Needed

| ViewModel | State Class | Screens Served |
|-----------|-------------|----------------|
| `DashboardViewModel` | `DashboardUiState` | Dashboard, Home |
| `ActivityViewModel` | `ActivityUiState` | Activity History |
| `ProfileViewModel` | `ProfileUiState` | Profile, Edit Profile |
| `NotificationViewModel` | `NotificationUiState` | Notifications |
| `SecurityViewModel` | `SecurityUiState` | Desktop Security Tab |
| `OverviewViewModel` | `OverviewUiState` | Desktop Overview Tab |
| `ReportViewModel` | `ReportUiState` | Desktop Reports Tab |

---

## New API Endpoints Needed

These endpoints would need to exist in the backend to support the new screens:

| Endpoint | Method | Used By |
|----------|--------|---------|
| `GET /users/{id}/activity?page=&size=` | GET | Activity History |
| `GET /notifications?page=&size=&unread=` | GET | Notifications |
| `PUT /notifications/{id}/read` | PUT | Mark notification read |
| `POST /auth/forgot-password` | POST | Forgot Password |
| `POST /auth/change-password` | POST | Change Password |
| `PUT /users/{id}/profile` | PUT | Edit Profile |
| `GET /security/alerts?page=&size=` | GET | Security Tab |
| `GET /security/audit-log?page=&size=&action=` | GET | Security Tab |
| `GET /security/sessions` | GET | Security Tab |
| `DELETE /security/sessions/{id}` | DELETE | Kill Session |
| `GET /dashboard/overview` | GET | Overview Tab |
| `GET /system/health` | GET | Overview Tab |
| `POST /reports/generate` | POST | Reports Tab |
| `GET /reports` | GET | Reports Tab |
| `GET /reports/{id}/download` | GET | Reports Tab |

---

**Document Status:** Ready for review
**Created:** January 31, 2026
**Total New Screens:** 11 Android + 3 Desktop = 14 new screens
**Final Total:** 16 Android + 10 Desktop = 26 screens
