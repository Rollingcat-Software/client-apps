# Professional Implementation Plan - Mobile App Refactoring

**Project**: FIVUCSAS Mobile App
**Phase**: 0 - Architectural Refactoring
**Duration**: 14 working days (2.8 weeks)
**Start Date**: TBD
**Owner**: Development Team
**Status**: READY TO IMPLEMENT

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Phase 0.1: Package Consolidation](#phase-01-package-consolidation-1-day)
4. [Phase 0.2: Extract Configuration](#phase-02-extract-configuration-1-day)
5. [Phase 0.3: Shared UI Components](#phase-03-shared-ui-components-2-days)
6. [Phase 0.4: Refactor AdminDashboard](#phase-04-refactor-admindashboard-3-days)
7. [Phase 0.5: Refactor KioskMode](#phase-05-refactor-kioskmode-2-days)
8. [Phase 0.6: Platform Abstractions](#phase-06-platform-abstractions-2-days)
9. [Phase 0.7: ViewModel Tests](#phase-07-viewmodel-tests-2-days)
10. [Phase 0.8: Documentation](#phase-08-documentation-1-day)
11. [Testing Checklist](#testing-checklist)
12. [Rollback Procedures](#rollback-procedures)

---

## Overview

### Goal
Transform the mobile-app codebase from a **B+ (Good foundation, poor organization)** to **A+ (Professional in all aspects)** through systematic refactoring.

### Key Improvements
- 🔴 **Reduce largest file** from 2,335 lines → 150 lines
- 🟢 **Increase test coverage** from 10% → 70%
- 🔵 **Extract components** from 0 → 20+ reusable components
- 🟣 **Consolidate packages** from 2 duplicates → 1 clean structure
- 🟠 **Add abstractions** for platform-specific code

### Success Criteria
- ✅ All functionality preserved
- ✅ All tests passing
- ✅ Code coverage ≥ 70%
- ✅ Largest file < 500 lines
- ✅ Build time unchanged
- ✅ No regression bugs

---

## Prerequisites

### Before Starting
- [ ] Read `ARCHITECTURE_REVIEW.md` completely
- [ ] Understand current architecture
- [ ] Set up development environment
- [ ] Ensure IntelliJ IDEA with Kotlin plugin installed
- [ ] Gradle 8+ configured
- [ ] Git configured with proper credentials

### Branch Strategy
```bash
# Create feature branch
git checkout -b refactor/professional-architecture

# Backup current state
git tag backup-before-refactor

# Create sub-branches for each phase
git checkout -b refactor/phase-0.1-packages
```

### Development Environment
```bash
# Verify setup
./gradlew clean build

# Run desktop app
./gradlew desktopApp:run

# Run tests
./gradlew test

# Expected output: All builds successful, app runs, ~10% test coverage
```

---

## Phase 0.1: Package Consolidation (1 Day)

### Goal
Eliminate duplicate package structure: merge `com.fivucsas.mobile` → `com.fivucsas.shared`

### Current Structure
```
shared/src/commonMain/kotlin/
├── com.fivucsas.mobile/    # 🔴 TO BE DELETED
│   ├── data/
│   ├── domain/
│   └── presentation/
└── com.fivucsas.shared/    # ✅ KEEP
    ├── data/
    ├── domain/
    ├── di/
    └── presentation/
```

### Steps

#### Step 1: Analyze Differences (30 minutes)
```bash
# List files in both packages
find shared/src/commonMain/kotlin/com/fivucsas/mobile -name "*.kt"
find shared/src/commonMain/kotlin/com/fivucsas/shared -name "*.kt"

# Check for duplicates
diff -r shared/src/commonMain/kotlin/com/fivucsas/mobile \
        shared/src/commonMain/kotlin/com/fivucsas/shared
```

**Expected Findings**:
- Some models duplicated (User, AuthToken)
- Legacy ViewModels in mobile package
- Older repository implementations

**Decision**:
- Keep `shared` package versions (newer)
- Migrate any unique code from `mobile` to `shared`
- Delete `mobile` package entirely

#### Step 2: Identify Dependencies (30 minutes)
```bash
# Find all imports from mobile package
grep -r "import com.fivucsas.mobile" shared/
grep -r "import com.fivucsas.mobile" desktopApp/
grep -r "import com.fivucsas.mobile" androidApp/
```

**Document**:
- Which files import from mobile package?
- Are they using unique code or duplicates?
- Create migration list

#### Step 3: Migrate Unique Code (2 hours)
For each unique file in `com.fivucsas.mobile`:

**Example: Migrate LoginViewModel**
```bash
# 1. Check if equivalent exists in shared
ls shared/src/commonMain/kotlin/com/fivucsas/shared/presentation/viewmodel/

# 2. If unique, move to shared
mkdir -p shared/src/commonMain/kotlin/com/fivucsas/shared/presentation/viewmodel/auth/
mv shared/src/commonMain/kotlin/com/fivucsas/mobile/presentation/login/LoginViewModel.kt \
   shared/src/commonMain/kotlin/com/fivucsas/shared/presentation/viewmodel/auth/LoginViewModel.kt

# 3. Update package declaration in file
# Change: package com.fivucsas.mobile.presentation.login
# To:     package com.fivucsas.shared.presentation.viewmodel.auth
```

**IntelliJ Method** (Recommended):
1. Right-click file → Refactor → Move
2. Select new package: `com.fivucsas.shared.presentation.viewmodel.auth`
3. IDE will update imports automatically
4. Review changes before committing

#### Step 4: Update All Imports (2 hours)
```bash
# Use IntelliJ's "Find and Replace in Path"
# Find:    import com.fivucsas.mobile
# Replace: import com.fivucsas.shared

# Or use sed (Linux/Mac)
find . -name "*.kt" -exec sed -i 's/com\.fivucsas\.mobile/com.fivucsas.shared/g' {} +
```

**Verify Each Change**:
- [ ] File compiles
- [ ] Imports resolved
- [ ] No duplicate imports
- [ ] Tests still pass

#### Step 5: Delete Mobile Package (30 minutes)
```bash
# After ALL imports updated and verified
rm -rf shared/src/commonMain/kotlin/com/fivucsas/mobile/

# Rebuild and test
./gradlew clean build
./gradlew test
```

#### Step 6: Update Build Files (30 minutes)
Check if `build.gradle.kts` files reference mobile package:
```kotlin
// Search for any hardcoded package references
grep -r "com.fivucsas.mobile" *.gradle.kts
```

### Verification Checklist
- [ ] `com.fivucsas.mobile` package deleted
- [ ] All imports use `com.fivucsas.shared`
- [ ] `./gradlew clean build` succeeds
- [ ] `./gradlew test` passes all tests
- [ ] Desktop app runs: `./gradlew desktopApp:run`
- [ ] No compilation errors
- [ ] No unused imports

### Commit
```bash
git add .
git commit -m "refactor: Consolidate packages - merge mobile into shared

- Migrated unique code from com.fivucsas.mobile to com.fivucsas.shared
- Updated all imports to use shared package
- Deleted duplicate mobile package
- All tests passing
- No functionality changes"

git push origin refactor/phase-0.1-packages
```

---

## Phase 0.2: Extract Configuration (1 Day)

### Goal
Centralize all magic numbers and configuration into config objects

### Target Files to Create
```
shared/src/commonMain/kotlin/com/fivucsas/shared/config/
├── AppConfig.kt          # App-wide constants
├── UIDimens.kt           # UI dimensions
├── AnimationConfig.kt    # Animation timings
└── BiometricConfig.kt    # Biometric thresholds
```

### Steps

#### Step 1: Create Configuration Files (1 hour)

**File: `AppConfig.kt`**
```kotlin
package com.fivucsas.shared.config

object AppConfig {
    const val APP_NAME = "FIVUCSAS"
    const val APP_VERSION = "1.0.0"
    const val APP_ID = "com.fivucsas.mobile"

    object Api {
        const val BASE_URL = "https://api.fivucsas.com"
        const val API_VERSION = "v1"
        const val TIMEOUT_SECONDS = 30L
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 1000L
    }

    object Cache {
        const val MAX_AGE_MINUTES = 15
        const val MAX_SIZE_MB = 50
        const val ENABLE_CACHE = true
    }

    object Logging {
        const val ENABLE_DEBUG_LOGS = true
        const val ENABLE_NETWORK_LOGS = true
        const val ENABLE_ANALYTICS = false
    }

    object Session {
        const val TIMEOUT_MINUTES = 30
        const val AUTO_LOGOUT_ENABLED = true
        const val REMEMBER_ME_DAYS = 30
    }
}
```

**File: `UIDimens.kt`**
```kotlin
package com.fivucsas.shared.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object UIDimens {
    // Spacing
    val SpacingXSmall: Dp = 4.dp
    val SpacingSmall: Dp = 8.dp
    val SpacingMedium: Dp = 16.dp
    val SpacingLarge: Dp = 24.dp
    val SpacingXLarge: Dp = 32.dp
    val SpacingXXLarge: Dp = 64.dp

    // Icon Sizes
    val IconXSmall: Dp = 16.dp
    val IconSmall: Dp = 24.dp
    val IconMedium: Dp = 32.dp
    val IconLarge: Dp = 48.dp
    val IconXLarge: Dp = 64.dp
    val IconXXLarge: Dp = 120.dp

    // Button Sizes
    val ButtonHeight: Dp = 48.dp
    val ButtonHeightSmall: Dp = 36.dp
    val ButtonHeightLarge: Dp = 56.dp
    val ButtonHeightKiosk: Dp = 80.dp
    val ButtonWidthKiosk: Dp = 250.dp

    // Component Sizes
    val CardRadius: Dp = 12.dp
    val CardElevation: Dp = 4.dp
    val InputFieldHeight: Dp = 56.dp
    val DialogWidth: Dp = 400.dp
    val DialogMaxWidth: Dp = 600.dp

    // Kiosk Specific
    val KioskIconSize: Dp = 120.dp
    val CameraPreviewHeight: Dp = 400.dp
    val CameraPreviewWidth: Dp = 600.dp

    // Table
    val TableRowHeight: Dp = 56.dp
    val TableHeaderHeight: Dp = 64.dp
    val TableCellPadding: Dp = 16.dp
}
```

**File: `AnimationConfig.kt`**
```kotlin
package com.fivucsas.shared.config

object AnimationConfig {
    // Duration (milliseconds)
    const val DURATION_INSTANT = 0
    const val DURATION_FAST = 150
    const val DURATION_NORMAL = 300
    const val DURATION_SLOW = 500
    const val DURATION_VERY_SLOW = 1000

    // Delays (milliseconds)
    const val DELAY_SHORT = 100L
    const val DELAY_MEDIUM = 500L
    const val DELAY_LONG = 1000L
    const val DELAY_VERIFICATION = 2000L

    // Fade
    const val FADE_IN_ALPHA_START = 0f
    const val FADE_IN_ALPHA_END = 1f
    const val FADE_OUT_ALPHA_START = 1f
    const val FADE_OUT_ALPHA_END = 0f
}
```

**File: `BiometricConfig.kt`**
```kotlin
package com.fivucsas.shared.config

object BiometricConfig {
    // Verification Thresholds
    const val CONFIDENCE_THRESHOLD = 0.85
    const val LIVENESS_THRESHOLD = 0.80
    const val QUALITY_THRESHOLD = 0.75

    // Retry Limits
    const val MAX_ENROLLMENT_RETRIES = 3
    const val MAX_VERIFICATION_RETRIES = 3
    const val MAX_CAMERA_INIT_RETRIES = 3

    // Timeouts (seconds)
    const val CAMERA_INIT_TIMEOUT = 10L
    const val CAPTURE_TIMEOUT = 5L
    const val LIVENESS_CHECK_TIMEOUT = 10L
    const val PROCESSING_TIMEOUT = 30L

    // Image Requirements
    const val MIN_FACE_SIZE_PIXELS = 100
    const val MAX_FACE_SIZE_PIXELS = 500
    const val PREFERRED_IMAGE_WIDTH = 640
    const val PREFERRED_IMAGE_HEIGHT = 480

    // Quality Checks
    const val MIN_BRIGHTNESS = 0.3
    const val MAX_BRIGHTNESS = 0.9
    const val MIN_SHARPNESS = 0.5
    const val MAX_BLUR_SCORE = 0.3

    // Enrollment
    const val ENROLLMENT_SAMPLES_REQUIRED = 1
    const val ENROLLMENT_DIVERSITY_THRESHOLD = 0.2
}
```

#### Step 2: Find and Replace Magic Numbers (3-4 hours)

**Search for Magic Numbers**:
```bash
# In AdminDashboard.kt
grep -n "\.dp" desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/admin/AdminDashboard.kt | head -20

# In KioskMode.kt
grep -n "\.dp" desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/kiosk/KioskMode.kt | head -20

# Find delay() calls
grep -rn "delay(" shared/
```

**Example Refactoring**:

**Before**:
```kotlin
@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier.padding(64.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Icon(
            Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .width(250.dp)
                .height(80.dp)
        ) {
            Text("Enroll")
        }
    }
}
```

**After**:
```kotlin
import com.fivucsas.shared.config.UIDimens

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier.padding(UIDimens.SpacingXXLarge),
        verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingXLarge)
    ) {
        Icon(
            Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(UIDimens.KioskIconSize)
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        Button(
            modifier = Modifier
                .width(UIDimens.ButtonWidthKiosk)
                .height(UIDimens.ButtonHeightKiosk)
        ) {
            Text("Enroll")
        }
    }
}
```

**Files to Update**:
1. AdminDashboard.kt
2. KioskMode.kt
3. All ViewModels (for delays, thresholds)
4. Repository implementations (for timeouts, retries)

**Use Find/Replace**:
```
Find:    Modifier.padding(16.dp)
Replace: Modifier.padding(UIDimens.SpacingMedium)

Find:    delay(2000)
Replace: delay(AnimationConfig.DELAY_VERIFICATION)

Find:    if (confidence > 0.85)
Replace: if (confidence > BiometricConfig.CONFIDENCE_THRESHOLD)
```

#### Step 3: Update ViewModel Configurations (1 hour)

**Example: AdminViewModel.kt**
```kotlin
// Before
private fun loadUsers() {
    viewModelScope.launch {
        delay(500) // Loading delay
        // ...
    }
}

// After
import com.fivucsas.shared.config.AnimationConfig

private fun loadUsers() {
    viewModelScope.launch {
        delay(AnimationConfig.DELAY_MEDIUM)
        // ...
    }
}
```

### Verification Checklist
- [ ] All 4 config files created
- [ ] All magic numbers replaced in UI code
- [ ] All magic numbers replaced in ViewModels
- [ ] All magic numbers replaced in repositories
- [ ] `./gradlew clean build` succeeds
- [ ] `./gradlew test` passes
- [ ] Desktop app runs with same behavior
- [ ] No hardcoded values remain (verify with grep)

### Commit
```bash
git add .
git commit -m "refactor: Extract configuration constants

- Created AppConfig, UIDimens, AnimationConfig, BiometricConfig
- Replaced all magic numbers with config references
- Improved maintainability and consistency
- All tests passing"

git push origin refactor/phase-0.2-configuration
```

---

## Phase 0.3: Shared UI Components (2 Days)

### Goal
Create reusable UI component library for cross-platform use

### Target Structure
```
shared/src/commonMain/kotlin/com/fivucsas/shared/ui/
├── components/
│   ├── atoms/
│   │   ├── buttons/
│   │   │   ├── PrimaryButton.kt
│   │   │   ├── SecondaryButton.kt
│   │   │   └── GradientButton.kt
│   │   └── inputs/
│   │       ├── ValidatedTextField.kt
│   │       └── SearchField.kt
│   ├── molecules/
│   │   ├── cards/
│   │   │   ├── StatisticCard.kt
│   │   │   └── InfoCard.kt
│   │   └── feedback/
│   │       ├── LoadingIndicator.kt
│   │       ├── SuccessMessage.kt
│   │       └── ErrorMessage.kt
│   └── organisms/
│       └── DataTable.kt (future)
├── theme/
│   ├── AppTheme.kt
│   ├── Colors.kt
│   └── Typography.kt
└── modifiers/
    └── GradientModifiers.kt
```

### Day 1: Extract Atoms & Molecules

#### Step 1: Identify Reusable Components (1 hour)
Scan AdminDashboard.kt and KioskMode.kt for repeated patterns:

**Buttons**:
```bash
grep -A 10 "@Composable.*Button" desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/admin/AdminDashboard.kt
```

**Cards**:
```bash
grep -A 10 "Card\(" desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/admin/AdminDashboard.kt
```

**List of Components to Extract**:
- [ ] StatisticCard (used 4+ times in AdminDashboard)
- [ ] GradientButton (used in KioskMode)
- [ ] ValidatedTextField (used 3+ times)
- [ ] LoadingIndicator
- [ ] SuccessMessage / ErrorMessage
- [ ] SearchBar

#### Step 2: Create Component Files (3 hours)

**Example: StatisticCard.kt**
```kotlin
package com.fivucsas.shared.ui.components.molecules.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens

/**
 * Reusable statistic card component
 *
 * @param title Card title (e.g., "Total Users")
 * @param value Main value to display (e.g., "150")
 * @param icon Optional icon
 * @param trend Optional trend indicator (UP, DOWN, NEUTRAL)
 * @param modifier Optional modifier
 */
@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    trend: Trend? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(UIDimens.CardRadius),
        elevation = CardDefaults.cardElevation(UIDimens.CardElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(UIDimens.SpacingMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(UIDimens.IconMedium),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (trend != null) {
                        TrendIndicator(trend)
                    }
                }
            }
        }
    }
}

enum class Trend {
    UP, DOWN, NEUTRAL
}

@Composable
private fun TrendIndicator(trend: Trend) {
    val color = when (trend) {
        Trend.UP -> Color(0xFF4CAF50)
        Trend.DOWN -> Color(0xFFF44336)
        Trend.NEUTRAL -> Color(0xFFFF9800)
    }

    val icon = when (trend) {
        Trend.UP -> Icons.Default.TrendingUp
        Trend.DOWN -> Icons.Default.TrendingDown
        Trend.NEUTRAL -> Icons.Default.TrendingFlat
    }

    Icon(
        imageVector = icon,
        contentDescription = trend.name,
        modifier = Modifier.size(UIDimens.IconSmall),
        tint = color
    )
}
```

**Create Similar Files**:
1. `PrimaryButton.kt`
2. `GradientButton.kt`
3. `ValidatedTextField.kt`
4. `LoadingIndicator.kt`
5. `SuccessMessage.kt`
6. `ErrorMessage.kt`

#### Step 3: Replace Private Functions with Components (2 hours)

**In AdminDashboard.kt**:

**Before**:
```kotlin
@Composable
private fun StatisticCard(title: String, value: String, icon: ImageVector) {
    Card(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        // ... 40 lines of code
    }
}

@Composable
fun UsersTab() {
    StatisticCard("Total Users", "150", Icons.Default.People)
    StatisticCard("Active Users", "120", Icons.Default.CheckCircle)
    // ...
}
```

**After**:
```kotlin
import com.fivucsas.shared.ui.components.molecules.cards.StatisticCard
import com.fivucsas.shared.ui.components.molecules.cards.Trend

@Composable
fun UsersTab() {
    StatisticCard(
        title = "Total Users",
        value = "150",
        icon = Icons.Default.People,
        trend = Trend.UP
    )
    StatisticCard(
        title = "Active Users",
        value = "120",
        icon = Icons.Default.CheckCircle,
        trend = Trend.UP
    )
    // ...
}

// Remove private StatisticCard function - now uses shared component
```

### Day 2: Create Theme System

#### Step 4: Extract Theme (2 hours)

**File: `Colors.kt`**
```kotlin
package com.fivucsas.shared.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val PrimaryLight = Color(0xFFBB86FC)

    // Secondary
    val Secondary = Color(0xFF03DAC6)
    val SecondaryVariant = Color(0xFF018786)

    // Background
    val Background = Color(0xFFFAFAFA)
    val BackgroundDark = Color(0xFF121212)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF1E1E1E)

    // Status
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF2196F3)

    // Text
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF757575)
    val TextDisabled = Color(0xFFBDBDBD)
}
```

**File: `AppTheme.kt`**
```kotlin
package com.fivucsas.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    secondary = AppColors.Secondary,
    background = AppColors.Background,
    surface = AppColors.Surface,
    error = AppColors.Error
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = Color.Black,
    secondary = AppColors.Secondary,
    background = AppColors.BackgroundDark,
    surface = AppColors.SurfaceDark,
    error = AppColors.Error
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

#### Step 5: Update Desktop App to Use Theme (1 hour)

**In Main.kt**:
```kotlin
import com.fivucsas.shared.ui.theme.AppTheme

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        AppTheme {
            // App content
        }
    }
}
```

### Verification Checklist
- [ ] 15+ component files created
- [ ] All components documented
- [ ] Components use config constants (UIDimens, etc.)
- [ ] Desktop UI updated to use new components
- [ ] Private component functions removed/replaced
- [ ] Theme system in place
- [ ] `./gradlew clean build` succeeds
- [ ] Desktop app runs with same appearance
- [ ] No visual regressions

### Commit
```bash
git add .
git commit -m "feat: Create shared UI component library

- Extracted 15+ reusable components (atoms, molecules)
- Created theme system (Colors, Typography, AppTheme)
- Replaced private functions with shared components
- Improved consistency and reusability
- All functionality preserved"

git push origin refactor/phase-0.3-components
```

---

## Phase 0.4: Refactor AdminDashboard (3 Days)

This is the most critical phase - breaking down a 2,335-line file.

### Goal
Transform AdminDashboard.kt from monolithic 2,335 lines → organized 20+ files averaging 150 lines

### Strategy: Incremental Extraction
**Don't try to refactor everything at once!**
- Extract one tab per session
- Test after each extraction
- Keep original file working until all extractions complete

### Day 1: Extract Users Tab

#### Morning Session: Create Structure (2 hours)
```bash
# Create directory structure
mkdir -p desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/admin/tabs/users/components
mkdir -p desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/admin/tabs/users/dialogs
```

#### Step 1: Extract User Statistics (30 minutes)

**Create: `tabs/users/components/UserStatisticsCards.kt`**
```kotlin
package com.fivucsas.desktop.ui.admin.tabs.users.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.ui.components.molecules.cards.StatisticCard
import com.fivucsas.shared.ui.components.molecules.cards.Trend

/**
 * User statistics cards - displays Total, Active, Inactive, Pending counts
 */
@Composable
fun UserStatisticsCards(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
    ) {
        StatisticCard(
            title = "Total Users",
            value = statistics.totalUsers.toString(),
            icon = Icons.Default.People,
            trend = Trend.UP,
            modifier = Modifier.weight(1f)
        )

        StatisticCard(
            title = "Active Users",
            value = statistics.activeUsers.toString(),
            icon = Icons.Default.CheckCircle,
            trend = Trend.UP,
            modifier = Modifier.weight(1f)
        )

        StatisticCard(
            title = "Inactive Users",
            value = statistics.inactiveUsers.toString(),
            icon = Icons.Default.Block,
            trend = Trend.NEUTRAL,
            modifier = Modifier.weight(1f)
        )

        StatisticCard(
            title = "Pending Users",
            value = statistics.pendingUsers.toString(),
            icon = Icons.Default.Schedule,
            trend = Trend.DOWN,
            modifier = Modifier.weight(1f)
        )
    }
}
```

#### Step 2: Extract User Table (1 hour)

**Create: `tabs/users/components/UserTable.kt`**
```kotlin
package com.fivucsas.desktop.ui.admin.tabs.users.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.User

/**
 * User table component - displays list of users with actions
 */
@Composable
fun UserTable(
    users: List<User>,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        LazyColumn {
            // Header
            item {
                UserTableHeader()
            }

            // Rows
            items(users) { user ->
                UserTableRow(
                    user = user,
                    onEditClick = { onEditClick(user) },
                    onDeleteClick = { onDeleteClick(user) }
                )
                Divider()
            }
        }
    }
}

@Composable
private fun UserTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(UIDimens.TableHeaderHeight)
            .padding(horizontal = UIDimens.TableCellPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Name", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
        Text("Email", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
        Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        Text("Actions", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun UserTableRow(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(UIDimens.TableRowHeight)
            .padding(horizontal = UIDimens.TableCellPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.name, modifier = Modifier.weight(2f))
        Text(user.email, modifier = Modifier.weight(2f))

        UserStatusBadge(
            status = user.status,
            modifier = Modifier.weight(1f)
        )

        Row(modifier = Modifier.weight(1f)) {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}

@Composable
private fun UserStatusBadge(status: UserStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        UserStatus.ACTIVE -> Color(0xFF4CAF50)
        UserStatus.INACTIVE -> Color(0xFF9E9E9E)
        UserStatus.PENDING -> Color(0xFFFF9800)
    }

    AssistChip(
        onClick = {},
        label = { Text(status.name) },
        colors = AssistChipDefaults.assistChipColors(containerColor = color),
        modifier = modifier
    )
}
```

#### Step 3: Extract Dialogs (1 hour)

**Create 3 dialog files**:
1. `tabs/users/dialogs/AddUserDialog.kt`
2. `tabs/users/dialogs/EditUserDialog.kt`
3. `tabs/users/dialogs/DeleteUserDialog.kt`

**(Example file content omitted for brevity - similar pattern)**

#### Step 4: Compose UsersTab (30 minutes)

**Create: `tabs/users/UsersTab.kt`**
```kotlin
package com.fivucsas.desktop.ui.admin.tabs.users

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.fivucsas.desktop.ui.admin.tabs.users.components.*
import com.fivucsas.desktop.ui.admin.tabs.users.dialogs.*
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel

/**
 * Users Tab - User management interface
 *
 * Features:
 * - View all users in table
 * - Search and filter users
 * - Add new users
 * - Edit existing users
 * - Delete users
 * - View statistics
 */
@Composable
fun UsersTab(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(UIDimens.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingLarge)
    ) {
        // Statistics Cards
        UserStatisticsCards(statistics = uiState.statistics)

        // Search Bar and Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SearchField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(UIDimens.SpacingMedium))

            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add User")
                Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
                Text("Add User")
            }
        }

        // User Table
        UserTable(
            users = uiState.filteredUsers,
            onEditClick = { user ->
                selectedUser = user
                showEditDialog = true
            },
            onDeleteClick = { user ->
                selectedUser = user
                showDeleteDialog = true
            }
        )
    }

    // Dialogs
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { user ->
                viewModel.createUser(user)
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { user ->
                viewModel.updateUser(user)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && selectedUser != null) {
        DeleteUserDialog(
            user = selectedUser!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteUser(selectedUser!!.id)
                showDeleteDialog = false
            }
        )
    }
}
```

#### Afternoon Session: Test Users Tab (2 hours)

1. Update AdminDashboard.kt to use new UsersTab
2. Test all functionality
3. Fix any bugs
4. Commit

**In AdminDashboard.kt**:
```kotlin
import com.fivucsas.desktop.ui.admin.tabs.users.UsersTab

@Composable
fun AdminDashboard(viewModel: AdminViewModel = koinInject()) {
    // ... scaffold code

    when (uiState.selectedTab) {
        AdminTab.USERS -> UsersTab(viewModel)
        AdminTab.ANALYTICS -> AnalyticsTabOld() // Still using old code
        AdminTab.SECURITY -> SecurityTabOld()
        AdminTab.SETTINGS -> SettingsTabOld()
    }
}
```

**Test**:
- [ ] Users tab displays correctly
- [ ] Statistics cards show data
- [ ] Search works
- [ ] Add user dialog opens
- [ ] Edit user dialog opens
- [ ] Delete user dialog opens
- [ ] All actions work

### Day 2: Extract Analytics, Security, Settings Navigation

**Repeat similar process** for:
1. AnalyticsTab (2 hours)
2. SecurityTab (2 hours)
3. SettingsTab container + navigation (2 hours)

### Day 3: Extract Settings Sections & Final Integration

**Morning**: Extract 6 settings sections (3 hours)
**Afternoon**: Refactor AdminDashboard.kt to use all new tabs, remove old code, test (3 hours)

### Final AdminDashboard.kt (Target: ~150 lines)
```kotlin
package com.fivucsas.desktop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.fivucsas.desktop.ui.admin.navigation.AdminNavigationRail
import com.fivucsas.desktop.ui.admin.tabs.users.UsersTab
import com.fivucsas.desktop.ui.admin.tabs.analytics.AnalyticsTab
import com.fivucsas.desktop.ui.admin.tabs.security.SecurityTab
import com.fivucsas.desktop.ui.admin.tabs.settings.SettingsTab
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.state.AdminTab
import org.koin.compose.koinInject

/**
 * Admin Dashboard - Main container
 *
 * Provides navigation between tabs:
 * - Users: User management
 * - Analytics: Statistics and charts
 * - Security: Audit logs and security
 * - Settings: Configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onBack: () -> Unit,
    viewModel: AdminViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Navigation Rail
            AdminNavigationRail(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab
            )

            // Content Area
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (uiState.selectedTab) {
                    AdminTab.USERS -> UsersTab(viewModel)
                    AdminTab.ANALYTICS -> AnalyticsTab(viewModel)
                    AdminTab.SECURITY -> SecurityTab(viewModel)
                    AdminTab.SETTINGS -> SettingsTab(viewModel)
                }
            }
        }
    }
}
```

### Verification Checklist
- [ ] AdminDashboard.kt reduced to ~150 lines
- [ ] 20+ new files created
- [ ] All tabs working
- [ ] All dialogs working
- [ ] All CRUD operations working
- [ ] Search working
- [ ] Settings persist
- [ ] No regressions
- [ ] `./gradlew clean build` succeeds
- [ ] Desktop app fully functional

### Commit
```bash
git add .
git commit -m "refactor: Break down AdminDashboard into modular components

- Extracted Users, Analytics, Security, Settings tabs
- Created 20+ organized files (avg 150 lines each)
- Reduced AdminDashboard from 2,335 → 150 lines
- All functionality preserved and tested
- Improved maintainability and testability"

git push origin refactor/phase-0.4-admin-dashboard
```

---

## Phase 0.5: Refactor KioskMode (2 Days)

Similar approach to AdminDashboard but simpler (3 screens vs 4 complex tabs)

### Target Structure
```
desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/kiosk/
├── KioskMode.kt (100 lines - container)
├── screens/
│   ├── WelcomeScreen.kt
│   ├── enrollment/
│   │   ├── EnrollmentScreen.kt
│   │   └── components/
│   │       ├── EnrollmentForm.kt
│   │       └── CameraPreviewCard.kt
│   └── verification/
│       ├── VerificationScreen.kt
│       └── components/
│           ├── VerificationCamera.kt
│           ├── VerificationSuccess.kt
│           └── VerificationFailure.kt
└── components/
    └── KioskCard.kt
```

### Day 1: Extract Screens (4 hours)
1. Create WelcomeScreen.kt
2. Create EnrollmentScreen.kt
3. Create VerificationScreen.kt
4. Test navigation

### Day 2: Extract Components & Integration (4 hours)
1. Extract enrollment components
2. Extract verification components
3. Refactor KioskMode.kt to container
4. Test end-to-end flows
5. Commit

**(Detailed steps similar to AdminDashboard - omitted for brevity)**

### Verification Checklist
- [ ] KioskMode.kt reduced to ~100 lines
- [ ] 12+ new files created
- [ ] Welcome screen working
- [ ] Enrollment flow working
- [ ] Verification flow working
- [ ] Camera integration working
- [ ] Success/failure states working
- [ ] No regressions

### Commit
```bash
git add .
git commit -m "refactor: Break down KioskMode into modular screens

- Extracted Welcome, Enrollment, Verification screens
- Created 12+ organized files
- Reduced KioskMode from 1,756 → 100 lines
- All functionality preserved
- Improved navigation and maintainability"

git push origin refactor/phase-0.5-kiosk-mode
```

---

## Phase 0.6: Platform Abstractions (2 Days)

### Goal
Create interfaces for platform-specific code (camera, logger, storage)

### Day 1: Define Interfaces

**Create: `shared/src/commonMain/kotlin/com/fivucsas/shared/platform/camera/ICameraService.kt`**
```kotlin
package com.fivucsas.shared.platform.camera

/**
 * Platform-agnostic camera service interface
 *
 * Implementations:
 * - Desktop: JavaCV or WebCam Capture
 * - Android: CameraX
 * - iOS: AVFoundation
 */
interface ICameraService {
    /**
     * Initialize camera hardware
     */
    suspend fun initialize(config: CameraConfig): Result<Unit>

    /**
     * Capture a single photo
     * @return ByteArray of image data (JPEG format)
     */
    suspend fun capturePhoto(): Result<ByteArray>

    /**
     * Start camera preview
     * @param onFrame Callback for each frame
     */
    suspend fun startPreview(onFrame: (ByteArray) -> Unit): Result<Unit>

    /**
     * Stop camera preview
     */
    suspend fun stopPreview(): Result<Unit>

    /**
     * Release camera resources
     */
    suspend fun release(): Result<Unit>

    /**
     * Check if camera is available
     */
    fun isAvailable(): Boolean
}

data class CameraConfig(
    val resolution: Resolution = Resolution.HD,
    val facing: CameraFacing = CameraFacing.FRONT,
    val enableFlash: Boolean = false,
    val fps: Int = 30
)

enum class Resolution(val width: Int, val height: Int) {
    VGA(640, 480),
    HD(1280, 720),
    FULL_HD(1920, 1080),
    FOUR_K(3840, 2160)
}

enum class CameraFacing {
    FRONT, BACK
}
```

**Similarly create**:
- `ILogger.kt`
- `ISecureStorage.kt`
- `IFileStorage.kt`

### Day 2: Implement Desktop Versions

**Create: `shared/src/desktopMain/kotlin/com/fivucsas/shared/platform/camera/DesktopCameraService.kt`**
```kotlin
package com.fivucsas.shared.platform.camera

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.OpenCVFrameGrabber

/**
 * Desktop camera implementation using JavaCV
 */
class DesktopCameraService(
    private val config: CameraConfig
) : ICameraService {

    private var grabber: FrameGrabber? = null
    private var isInitialized = false

    override suspend fun initialize(config: CameraConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            grabber = OpenCVFrameGrabber(0).apply {
                imageWidth = config.resolution.width
                imageHeight = config.resolution.height
                start()
            }
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun capturePhoto(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized) {
                return@withContext Result.failure(IllegalStateException("Camera not initialized"))
            }

            val frame = grabber?.grab() ?: return@withContext Result.failure(
                IllegalStateException("Failed to grab frame")
            )

            // Convert frame to JPEG ByteArray
            val bytes = frameToJpeg(frame)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startPreview(onFrame: (ByteArray) -> Unit): Result<Unit> {
        // Implementation
        return Result.success(Unit)
    }

    override suspend fun stopPreview(): Result<Unit> {
        // Implementation
        return Result.success(Unit)
    }

    override suspend fun release(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            grabber?.stop()
            grabber?.release()
            grabber = null
            isInitialized = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            OpenCVFrameGrabber.list.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun frameToJpeg(frame: Frame): ByteArray {
        // Convert JavaCV Frame to JPEG bytes
        // ... implementation
        return byteArrayOf()
    }
}
```

**Update DI Module**:
```kotlin
// shared/src/commonMain/kotlin/.../di/PlatformModule.kt
expect fun platformModule(): Module

// shared/src/desktopMain/kotlin/.../di/PlatformModule.kt
actual fun platformModule() = module {
    single<ICameraService> { DesktopCameraService(get()) }
    single<ILogger> { DesktopLogger() }
    single<ISecureStorage> { DesktopSecureStorage() }
    single<IFileStorage> { DesktopFileStorage() }
}
```

### Verification Checklist
- [ ] All platform interfaces defined
- [ ] Desktop implementations complete
- [ ] DI configured
- [ ] Tests written for interfaces
- [ ] Desktop app uses new abstractions
- [ ] Ready for Android/iOS implementations

### Commit
```bash
git add .
git commit -m "feat: Add platform abstractions for multiplatform support

- Created ICameraService, ILogger, ISecureStorage, IFileStorage interfaces
- Implemented desktop versions using JavaCV
- Updated DI with platform module
- Ready for Android/iOS implementations"

git push origin refactor/phase-0.6-platform-abstractions
```

---

## Phase 0.7: ViewModel Tests (2 Days)

### Goal
Achieve 70%+ test coverage on ViewModels

### Day 1: AdminViewModel Tests (4 hours)

**Create: `shared/src/commonTest/kotlin/com/fivucsas/shared/presentation/viewmodel/AdminViewModelTest.kt`**
```kotlin
package com.fivucsas.shared.presentation.viewmodel

import app.cash.turbine.test
import com.fivucsas.shared.domain.model.*
import com.fivucsas.shared.domain.usecase.admin.*
import com.fivucsas.shared.fixtures.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AdminViewModelTest {

    private lateinit var viewModel: AdminViewModel
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var getUsersUseCase: GetUsersUseCase
    private lateinit var deleteUserUseCase: DeleteUserUseCase
    private lateinit var updateUserUseCase: UpdateUserUseCase
    private lateinit var getStatisticsUseCase: GetStatisticsUseCase

    @BeforeTest
    fun setup() {
        fakeUserRepository = FakeUserRepository()
        getUsersUseCase = GetUsersUseCase(fakeUserRepository)
        deleteUserUseCase = DeleteUserUseCase(fakeUserRepository)
        updateUserUseCase = UpdateUserUseCase(fakeUserRepository)
        getStatisticsUseCase = GetStatisticsUseCase(fakeUserRepository)

        viewModel = AdminViewModel(
            getUsersUseCase = getUsersUseCase,
            deleteUserUseCase = deleteUserUseCase,
            updateUserUseCase = updateUserUseCase,
            getStatisticsUseCase = getStatisticsUseCase
        )
    }

    @Test
    fun `init should load users and statistics`() = runTest {
        // Given
        val testUsers = listOf(
            User(id = "1", name = "John Doe", email = "john@test.com", status = UserStatus.ACTIVE),
            User(id = "2", name = "Jane Smith", email = "jane@test.com", status = UserStatus.ACTIVE)
        )
        fakeUserRepository.setUsers(testUsers)

        // When - ViewModel initialization
        viewModel.uiState.test {
            // Then
            val state = awaitItem()
            assertEquals(testUsers.size, state.users.size)
            assertEquals(false, state.loading)
            assertNull(state.error)
        }
    }

    @Test
    fun `updateSearchQuery should filter users`() = runTest {
        // Given
        val testUsers = listOf(
            User(id = "1", name = "John Doe", email = "john@test.com", status = UserStatus.ACTIVE),
            User(id = "2", name = "Jane Smith", email = "jane@test.com", status = UserStatus.ACTIVE)
        )
        fakeUserRepository.setUsers(testUsers)

        // When
        viewModel.updateSearchQuery("jane")

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.filteredUsers.size)
            assertEquals("Jane Smith", state.filteredUsers.first().name)
        }
    }

    @Test
    fun `deleteUser should remove user from list`() = runTest {
        // Given
        val testUsers = listOf(
            User(id = "1", name = "John Doe", email = "john@test.com", status = UserStatus.ACTIVE),
            User(id = "2", name = "Jane Smith", email = "jane@test.com", status = UserStatus.ACTIVE)
        )
        fakeUserRepository.setUsers(testUsers.toMutableList())
        viewModel.loadUsers()

        // When
        viewModel.deleteUser("1")

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.users.size)
            assertEquals("Jane Smith", state.users.first().name)
        }
    }

    @Test
    fun `loadUsers should set error state on failure`() = runTest {
        // Given
        fakeUserRepository.setShouldFail(true)

        // When
        viewModel.loadUsers()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.users.isEmpty())
            assertFalse(state.loading)
            assertNotNull(state.error)
        }
    }

    @Test
    fun `selectTab should update selected tab`() = runTest {
        // When
        viewModel.selectTab(AdminTab.ANALYTICS)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AdminTab.ANALYTICS, state.selectedTab)
        }
    }

    // Add 10+ more tests for other operations
}
```

**Write Tests For**:
1. Loading users (success/failure)
2. Creating users
3. Updating users
4. Deleting users
5. Searching/filtering
6. Tab navigation
7. Statistics loading
8. Error handling
9. State transitions
10. Edge cases

### Day 2: KioskViewModel Tests (4 hours)

**Create: `shared/src/commonTest/kotlin/com/fivucsas/shared/presentation/viewmodel/KioskViewModelTest.kt`**

Write similar comprehensive tests for KioskViewModel:
1. Enrollment flow
2. Verification flow
3. Camera state management
4. Error handling
5. Success/failure states
6. Navigation
7. Form validation
8. Edge cases

### Run Coverage Report
```bash
./gradlew koverReport

# Open report
open shared/build/reports/kover/html/index.html
```

**Target**: 70%+ coverage on ViewModels

### Verification Checklist
- [ ] AdminViewModel: 15+ tests written
- [ ] KioskViewModel: 15+ tests written
- [ ] All tests passing
- [ ] Coverage ≥ 70% on ViewModels
- [ ] Coverage report generated
- [ ] CI/CD updated with coverage check

### Commit
```bash
git add .
git commit -m "test: Add comprehensive ViewModel tests

- Added 30+ ViewModel tests
- Achieved 70%+ test coverage
- Tested success/failure scenarios
- Tested state transitions
- Added coverage reporting"

git push origin refactor/phase-0.7-tests
```

---

## Phase 0.8: Documentation (1 Day)

### Goal
Document all changes and create migration guides

### Tasks

#### 1. Update README (1 hour)
- Document new structure
- Update getting started guide
- Add component usage examples

#### 2. Create Component Catalog (2 hours)
```markdown
# Component Catalog

## Atoms

### PrimaryButton
...

### StatisticCard
...
```

#### 3. Create Migration Guide (2 hours)
```markdown
# Migration Guide

## For Contributors

### Old Structure
...

### New Structure
...

### How to Add New Features
...
```

#### 4. Update Architecture Docs (1 hour)
- Update architecture diagrams
- Document package structure
- Document testing strategy

#### 5. Create Demo/Examples (2 hours)
- Create component showcase app
- Record demo video
- Update screenshots

### Verification Checklist
- [ ] README updated
- [ ] Component catalog created
- [ ] Migration guide written
- [ ] Architecture docs updated
- [ ] Examples created
- [ ] Demo video recorded

### Commit
```bash
git add .
git commit -m "docs: Complete refactoring documentation

- Updated README with new structure
- Created component catalog
- Wrote migration guide for contributors
- Updated architecture documentation
- Added usage examples"

git push origin refactor/phase-0.8-documentation
```

---

## Testing Checklist

### After Each Phase
- [ ] Code compiles: `./gradlew clean build`
- [ ] Tests pass: `./gradlew test`
- [ ] Desktop app runs: `./gradlew desktopApp:run`
- [ ] No console errors
- [ ] All features work as before

### Final Integration Testing
- [ ] **Users Tab**
  - [ ] View users list
  - [ ] Search users
  - [ ] Add new user
  - [ ] Edit user
  - [ ] Delete user
  - [ ] Statistics update correctly

- [ ] **Analytics Tab**
  - [ ] View statistics
  - [ ] Charts render
  - [ ] Recent verifications list
  - [ ] Data refreshes

- [ ] **Security Tab**
  - [ ] Security alerts display
  - [ ] Audit logs table
  - [ ] Expandable details
  - [ ] Filtering works

- [ ] **Settings Tab**
  - [ ] All 6 sections accessible
  - [ ] Profile settings update
  - [ ] Security settings update
  - [ ] Biometric settings update
  - [ ] System settings update
  - [ ] Notification settings update
  - [ ] Appearance settings update
  - [ ] Settings persist

- [ ] **Kiosk Mode**
  - [ ] Welcome screen displays
  - [ ] Navigate to enrollment
  - [ ] Enrollment form works
  - [ ] Camera preview (if available)
  - [ ] Submit enrollment
  - [ ] Navigate to verification
  - [ ] Verification flow works
  - [ ] Success state displays
  - [ ] Failure state displays
  - [ ] Back navigation works

- [ ] **Performance**
  - [ ] App startup time < 2 seconds
  - [ ] Tab switching instant
  - [ ] Search responsive
  - [ ] No UI lag
  - [ ] Memory usage normal

---

## Rollback Procedures

### If Issues Arise During Any Phase

#### Option 1: Rollback Phase
```bash
# Rollback to previous phase
git reset --hard HEAD~1

# Or rollback to specific tag
git reset --hard backup-before-refactor
```

#### Option 2: Fix Forward
1. Identify issue
2. Create fix branch
3. Apply fix
4. Test
5. Merge

#### Option 3: Partial Rollback
```bash
# Rollback specific files
git checkout HEAD~1 -- path/to/file.kt
```

### Emergency Rollback (Critical Production Issue)
```bash
# Full rollback to backup
git reset --hard backup-before-refactor
git push --force origin main
```

---

## Success Metrics

### Code Quality
- ✅ Largest file: 2,335 lines → < 500 lines
- ✅ Test coverage: 10% → 70%+
- ✅ Package structure: 2 duplicates → 1 clean
- ✅ Reusable components: 0 → 20+
- ✅ Platform abstractions: 0 → 4+

### Developer Experience
- ✅ Time to find code: ~5 min → < 30 sec
- ✅ Time to add feature: High → Low
- ✅ Merge conflicts: Frequent → Rare
- ✅ Onboarding time: 3 days → 1 day

### SOLID Compliance
- ✅ SRP: 60% → 95%
- ✅ OCP: 90% → 95%
- ✅ LSP: 95% → 95%
- ✅ ISP: 85% → 90%
- ✅ DIP: 95% → 95%

---

## Next Steps After Phase 0

Once refactoring is complete:

1. **Merge to Main**
```bash
git checkout main
git merge refactor/professional-architecture
git tag v1.0-refactored
git push origin main --tags
```

2. **Start Phase 1: Backend Integration**
- See MODULE_PLAN.md Phase 1
- Easier to implement with clean codebase

3. **Start Phase 2: Camera Integration**
- Use new ICameraService abstraction
- Implement for each platform

4. **Start Phase 3: Mobile Development**
- Reuse shared components
- Parallel development possible

---

**Document Version**: 1.0
**Created**: 2025-11-17
**Status**: READY TO IMPLEMENT
**Estimated Duration**: 14 working days
**Expected Outcome**: Professional A+ codebase
