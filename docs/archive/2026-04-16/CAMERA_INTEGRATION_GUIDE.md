# Camera Integration Guide

This guide explains how to use the camera integration in FIVUCSAS mobile and desktop applications.

## Architecture Overview

The camera integration follows **Hexagonal Architecture** (Ports and Adapters):

```
┌─────────────────────────────────────────┐
│         Application Core                │
│    (Domain Layer - Use Cases)           │
└─────────────────┬───────────────────────┘
                  │
                  │ depends on
                  ↓
┌─────────────────────────────────────────┐
│        ICameraService (Port)            │
│    (Interface/Abstraction)              │
└─────────┬──────────────┬────────────────┘
          │              │
          │              │
   ┌──────↓─────┐  ┌────↓──────────────┐
   │  Android   │  │    Desktop        │
   │  Adapter   │  │    Adapter        │
   │ (CameraX)  │  │   (JavaCV)        │
   └────────────┘  └───────────────────┘
```

## Components

### 1. Core Interfaces

#### `ICameraService`
**Location:** `shared/src/commonMain/kotlin/com/fivucsas/shared/platform/ICameraService.kt`

The main camera service interface that all platforms implement.

**Key Methods:**
- `initialize(lensFacing)` - Initialize camera
- `startPreview()` - Start camera preview
- `stopPreview()` - Stop camera preview
- `captureImage()` - Capture high-quality image for biometric processing
- `captureFrame()` - Capture quick frame for previews
- `release()` - Release camera resources

**Properties:**
- `cameraState: StateFlow<CameraState>` - Observable camera state

#### `CameraState`
**Location:** `shared/src/commonMain/kotlin/com/fivucsas/shared/platform/CameraState.kt`

Sealed class representing all possible camera states:
- `Idle` - Camera not initialized
- `Initializing` - Camera is initializing
- `Ready` - Camera ready but not previewing
- `Previewing` - Camera actively showing preview
- `Capturing` - Camera capturing image
- `Error(error)` - Camera encountered error
- `Released` - Camera has been released

### 2. Platform Implementations

#### Android - `AndroidCameraService`
**Location:** `shared/src/androidMain/kotlin/com/fivucsas/shared/platform/AndroidCameraService.kt`

- Uses **CameraX** library for modern Android camera API
- Lifecycle-aware camera management
- Supports front and back cameras
- High-quality image capture (JPEG, 95% quality)

**Features:**
- Auto-focus enabled
- Configurable resolution (default: 640x480)
- Frame analysis for real-time processing
- Proper resource cleanup

#### Desktop - `DesktopCameraServiceImpl`
**Location:** `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/platform/DesktopCameraServiceImpl.kt`

- Uses **JavaCV** (OpenCV wrapper) for webcam access
- Cross-platform webcam support
- JPEG image encoding

**Features:**
- Configurable frame rate (default: 30 FPS)
- Multiple camera support
- BufferedImage conversion for Compose UI

### 3. UI Components

#### `CameraPreviewContainer`
**Location:** `shared/src/commonMain/kotlin/com/fivucsas/shared/ui/components/organisms/CameraPreview.kt`

Common camera UI structure for all platforms.

**Features:**
- State-based UI rendering
- Capture button with loading states
- Close button
- Optional flip camera button
- Overlay support for face detection guides
- Error handling UI

#### `FaceDetectionOverlay`
Provides visual guidance for face positioning.

**Features:**
- Oval face guide
- Customizable guidance text
- Semi-transparent overlay

#### Platform-Specific Previews

**Android:** `AndroidCameraPreview`
- Location: `shared/src/androidMain/kotlin/com/fivucsas/shared/ui/platform/AndroidCameraPreview.kt`
- Embeds CameraX `PreviewView` using `AndroidView`

**Desktop:** `DesktopCameraPreview`
- Location: `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/DesktopCameraPreview.kt`
- Continuously captures and displays frames (~30 FPS)

### 4. Dependency Injection

#### Platform Modules
**Locations:**
- Common: `shared/src/commonMain/kotlin/com/fivucsas/shared/di/PlatformModule.kt`
- Android: `shared/src/androidMain/kotlin/com/fivucsas/shared/di/PlatformModule.android.kt`
- Desktop: `shared/src/desktopMain/kotlin/com/fivucsas/shared/di/PlatformModule.desktop.kt`

Uses **Koin** for dependency injection with platform-specific implementations.

## Usage Examples

### Example 1: Basic Camera Usage

```kotlin
@Composable
fun BiometricCaptureScreen(
    cameraService: ICameraService = get(), // Inject from Koin
    onImageCaptured: (ByteArray) -> Unit
) {
    val cameraState by cameraService.cameraState.collectAsState()
    val scope = rememberCoroutineScope()

    // Initialize camera on screen load
    LaunchedEffect(Unit) {
        cameraService.initialize(LensFacing.FRONT)
        cameraService.startPreview()
    }

    // Cleanup on screen dispose
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                cameraService.release()
            }
        }
    }

    CameraPreviewContainer(
        cameraState = cameraState,
        onCaptureClick = {
            scope.launch {
                cameraService.captureImage()
                    .onSuccess { imageBytes ->
                        onImageCaptured(imageBytes)
                    }
                    .onFailure { error ->
                        // Handle error
                    }
            }
        },
        onCloseClick = { /* Navigate back */ },
        overlayContent = {
            FaceDetectionOverlay(
                showGuide = true,
                guidanceText = "Position your face within the oval"
            )
        }
    ) {
        // Platform-specific preview
        when (cameraService) {
            is AndroidCameraService -> {
                AndroidCameraPreview(
                    cameraService = cameraService,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is DesktopCameraServiceImpl -> {
                DesktopCameraPreview(
                    cameraService = cameraService,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
```

### Example 2: Camera with Permission Handling (Android)

```kotlin
@Composable
fun BiometricCaptureScreenWithPermissions(
    cameraService: AndroidCameraService = get(),
    onImageCaptured: (ByteArray) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    val cameraState by cameraService.cameraState.collectAsState()
    val scope = rememberCoroutineScope()

    // Request camera permission
    RequestCameraPermission(
        onPermissionGranted = {
            permissionGranted = true
            scope.launch {
                cameraService.initialize(LensFacing.FRONT)
                cameraService.startPreview()
            }
        },
        onPermissionDenied = {
            // Show rationale dialog
        },
        onPermissionPermanentlyDenied = {
            // Show settings redirect dialog
        }
    )

    if (permissionGranted) {
        // Show camera UI
        CameraPreviewContainer(
            cameraState = cameraState,
            onCaptureClick = {
                scope.launch {
                    cameraService.captureImage()
                        .onSuccess { imageBytes ->
                            onImageCaptured(imageBytes)
                        }
                }
            },
            onCloseClick = { /* Navigate back */ }
        ) {
            AndroidCameraPreview(
                cameraService = cameraService,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // Show permission required UI
        PermissionRequiredScreen()
    }
}
```

### Example 3: Camera with Flip Support

```kotlin
@Composable
fun BiometricCaptureWithFlip(
    cameraService: ICameraService = get(),
    onImageCaptured: (ByteArray) -> Unit
) {
    var currentLens by remember { mutableStateOf(LensFacing.FRONT) }
    val cameraState by cameraService.cameraState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentLens) {
        cameraService.initialize(currentLens)
        cameraService.startPreview()
    }

    CameraPreviewContainer(
        cameraState = cameraState,
        onCaptureClick = {
            scope.launch {
                cameraService.captureImage()
                    .onSuccess { imageBytes ->
                        onImageCaptured(imageBytes)
                    }
            }
        },
        onCloseClick = { /* Navigate back */ },
        onFlipCamera = {
            scope.launch {
                cameraService.stopPreview()
                currentLens = if (currentLens == LensFacing.FRONT) {
                    LensFacing.BACK
                } else {
                    LensFacing.FRONT
                }
            }
        },
        showFlipButton = cameraService.hasCamera(LensFacing.BACK)
    ) {
        // Platform-specific preview rendering
    }
}
```

## Configuration

### Biometric Configuration
**Location:** `shared/src/commonMain/kotlin/com/fivucsas/shared/config/BiometricConfig.kt`

Adjust camera settings for biometric processing:

```kotlin
object BiometricConfig {
    const val PREFERRED_IMAGE_WIDTH = 640
    const val PREFERRED_IMAGE_HEIGHT = 480
    const val CAMERA_FRAME_RATE_FPS = 30
    const val CAMERA_AUTO_FOCUS_ENABLED = true
    const val MIN_FACE_SIZE_PIXELS = 100
    const val MAX_FACE_SIZE_PIXELS = 500
}
```

## Dependencies

### Android
Already configured in `androidApp/build.gradle.kts`:
- `androidx.camera:camera-core:1.3.0`
- `androidx.camera:camera-camera2:1.3.0`
- `androidx.camera:camera-lifecycle:1.3.0`
- `androidx.camera:camera-view:1.3.0`
- `com.google.accompanist:accompanist-permissions:0.32.0`

### Desktop
Already configured in `desktopApp/build.gradle.kts`:
- `org.bytedeco:javacv-platform:1.5.10`

## Best Practices

### 1. Lifecycle Management
Always release camera resources when done:

```kotlin
DisposableEffect(Unit) {
    onDispose {
        scope.launch {
            cameraService.release()
        }
    }
}
```

### 2. Error Handling
Always handle camera errors gracefully:

```kotlin
cameraService.captureImage()
    .onSuccess { imageBytes ->
        // Process image
    }
    .onFailure { error ->
        // Show user-friendly error message
        when (error) {
            is IllegalStateException -> "Camera not ready"
            is CameraAccessException -> "Camera access denied"
            else -> "Failed to capture image"
        }
    }
```

### 3. State Management
Observe camera state for UI updates:

```kotlin
val cameraState by cameraService.cameraState.collectAsState()

when (cameraState) {
    is CameraState.Idle -> ShowInitializeButton()
    is CameraState.Initializing -> ShowLoadingIndicator()
    is CameraState.Previewing -> EnableCaptureButton()
    is CameraState.Error -> ShowErrorMessage(error)
    else -> ShowPlaceholder()
}
```

### 4. Permissions (Android)
Always check and request permissions before initializing camera:

```kotlin
if (!CameraPermissionChecker.isGranted(context)) {
    // Request permission using RequestCameraPermission composable
}
```

### 5. Testing
Mock `ICameraService` for unit tests:

```kotlin
class MockCameraService : ICameraService {
    override val cameraState = MutableStateFlow<CameraState>(CameraState.Idle)

    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> {
        cameraState.value = CameraState.Ready
        return Result.success(Unit)
    }

    override suspend fun captureImage(): Result<ByteArray> {
        return Result.success(ByteArray(100)) // Mock image data
    }

    // ... implement other methods
}
```

## Troubleshooting

### Android

**Issue:** Camera preview is black
- **Solution:** Ensure PreviewView is properly bound to the camera service
- Check that `cameraService.previewView` is set

**Issue:** Permission denied
- **Solution:** Add camera permission to AndroidManifest.xml:
  ```xml
  <uses-permission android:name="android.permission.CAMERA" />
  ```

**Issue:** Camera not initializing
- **Solution:** Ensure LifecycleOwner is provided in Koin module

### Desktop

**Issue:** Camera not found
- **Solution:** Check JavaCV native libraries are properly loaded
- Ensure webcam is connected and not in use by another application

**Issue:** Low FPS
- **Solution:** Reduce resolution in BiometricConfig
- Increase delay in DesktopCameraPreview LaunchedEffect

## Architecture Benefits

1. **Testability**: Mock ICameraService for unit tests
2. **Platform Independence**: Business logic doesn't depend on platform
3. **Maintainability**: Clear separation of concerns
4. **Flexibility**: Easy to add new camera implementations
5. **Reusability**: Common UI components shared across platforms
6. **Type Safety**: Kotlin type system prevents errors
7. **Reactive**: StateFlow provides reactive state management

## SOLID Principles Applied

1. **Single Responsibility**: Each class has one reason to change
2. **Open/Closed**: Open for extension (new platforms), closed for modification
3. **Liskov Substitution**: All implementations can substitute ICameraService
4. **Interface Segregation**: ICameraService contains only necessary methods
5. **Dependency Inversion**: High-level code depends on abstractions

## Design Patterns Used

1. **Hexagonal Architecture** (Ports and Adapters)
2. **Adapter Pattern**: Platform implementations adapt to ICameraService
3. **State Pattern**: CameraState sealed class
4. **Observer Pattern**: StateFlow for reactive updates
5. **Template Method**: CameraPreviewContainer defines structure
6. **Dependency Injection**: Koin provides implementations
7. **Factory Pattern**: Platform modules create instances
