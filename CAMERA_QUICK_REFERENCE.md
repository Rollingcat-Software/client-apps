# Camera Integration Quick Reference

## Quick Start

### 1. Inject Camera Service
```kotlin
@Composable
fun MyScreen(cameraService: ICameraService = get())
```

### 2. Initialize & Preview
```kotlin
LaunchedEffect(Unit) {
    cameraService.initialize(LensFacing.FRONT)
    cameraService.startPreview()
}
```

### 3. Capture Image
```kotlin
scope.launch {
    cameraService.captureImage()
        .onSuccess { bytes -> /* Use image */ }
        .onFailure { error -> /* Handle error */ }
}
```

### 4. Cleanup
```kotlin
DisposableEffect(Unit) {
    onDispose {
        scope.launch { cameraService.release() }
    }
}
```

## Camera States

| State | Description | UI Action |
|-------|-------------|-----------|
| `Idle` | Not initialized | Show initialize button |
| `Initializing` | Starting up | Show loading indicator |
| `Ready` | Initialized, not previewing | Ready to start preview |
| `Previewing` | Active preview | Enable capture button |
| `Capturing` | Taking photo | Disable capture, show animation |
| `Error(e)` | Error occurred | Show error message |
| `Released` | Cleaned up | Reset UI |

## Common Patterns

### Full Camera Screen
```kotlin
@Composable
fun CameraScreen(cameraService: ICameraService = get()) {
    val state by cameraService.cameraState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cameraService.initialize(LensFacing.FRONT)
        cameraService.startPreview()
    }

    DisposableEffect(Unit) {
        onDispose { scope.launch { cameraService.release() } }
    }

    CameraPreviewContainer(
        cameraState = state,
        onCaptureClick = {
            scope.launch {
                cameraService.captureImage()
                    .onSuccess { /* Handle */ }
            }
        },
        onCloseClick = { /* Navigate back */ }
    ) {
        when (cameraService) {
            is AndroidCameraService -> AndroidCameraPreview(cameraService)
            is DesktopCameraServiceImpl -> DesktopCameraPreview(cameraService)
        }
    }
}
```

### With Permissions (Android)
```kotlin
@Composable
fun CameraScreenWithPermission(cameraService: ICameraService = get()) {
    var hasPermission by remember { mutableStateOf(false) }

    RequestCameraPermission(
        onPermissionGranted = { hasPermission = true },
        onPermissionDenied = { /* Show rationale */ },
        onPermissionPermanentlyDenied = { /* Show settings */ }
    )

    if (hasPermission) {
        // Show camera UI
    } else {
        // Show permission required UI
    }
}
```

### With Face Guide Overlay
```kotlin
CameraPreviewContainer(
    cameraState = state,
    onCaptureClick = { /* Capture */ },
    onCloseClick = { /* Close */ },
    overlayContent = {
        FaceDetectionOverlay(
            showGuide = true,
            guidanceText = "Center your face in the oval"
        )
    }
) {
    // Camera preview
}
```

### With Camera Flip
```kotlin
var lens by remember { mutableStateOf(LensFacing.FRONT) }

CameraPreviewContainer(
    cameraState = state,
    onCaptureClick = { /* Capture */ },
    onCloseClick = { /* Close */ },
    onFlipCamera = {
        scope.launch {
            cameraService.stopPreview()
            lens = if (lens == LensFacing.FRONT) LensFacing.BACK else LensFacing.FRONT
            cameraService.initialize(lens)
            cameraService.startPreview()
        }
    },
    showFlipButton = cameraService.hasCamera(LensFacing.BACK)
) {
    // Camera preview
}
```

## API Reference

### ICameraService

| Method | Description | Returns |
|--------|-------------|---------|
| `initialize(lensFacing)` | Initialize camera | `Result<Unit>` |
| `startPreview()` | Start preview | `Result<Unit>` |
| `stopPreview()` | Stop preview | `Result<Unit>` |
| `captureImage()` | Capture high-quality JPEG | `Result<ByteArray>` |
| `captureFrame()` | Capture quick frame | `Result<ByteArray>` |
| `isAvailable()` | Check if camera exists | `Boolean` |
| `hasCamera(lensFacing)` | Check specific lens | `Boolean` |
| `release()` | Release resources | `Unit` |
| `getPreviewDimensions()` | Get resolution | `Pair<Int, Int>` |
| `getSupportedResolutions()` | Get all resolutions | `List<Pair<Int, Int>>` |

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `cameraState` | `StateFlow<CameraState>` | Observable camera state |

## Configuration

### Adjust in BiometricConfig.kt
```kotlin
const val PREFERRED_IMAGE_WIDTH = 640      // Image width
const val PREFERRED_IMAGE_HEIGHT = 480     // Image height
const val CAMERA_FRAME_RATE_FPS = 30       // FPS
const val CAMERA_AUTO_FOCUS_ENABLED = true // Auto focus
```

## Error Handling

```kotlin
cameraService.captureImage().fold(
    onSuccess = { bytes ->
        // Success case
    },
    onFailure = { error ->
        val message = when (error) {
            is IllegalStateException -> "Camera not ready"
            is CameraAccessException -> "Camera access denied"
            is TimeoutException -> "Camera timeout"
            else -> "Unknown error: ${error.message}"
        }
        showError(message)
    }
)
```

## Best Practices

### ✅ DO
- Always release camera in DisposableEffect
- Observe cameraState for UI updates
- Handle all error cases
- Request permissions before initialization (Android)
- Use LensFacing.FRONT for biometric capture
- Check camera availability before use

### ❌ DON'T
- Don't forget to release camera resources
- Don't call captureImage when not in Previewing state
- Don't ignore error results
- Don't initialize camera without permission (Android)
- Don't block UI thread with camera operations

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Black preview | Check PreviewView is bound to service |
| Permission denied | Add CAMERA permission to AndroidManifest |
| Camera not found | Check device has camera, not in use |
| Low FPS | Reduce resolution in config |
| Out of memory | Release camera when not in use |

## File Locations

| File | Path |
|------|------|
| Interface | `shared/src/commonMain/.../ICameraService.kt` |
| Android Impl | `shared/src/androidMain/.../AndroidCameraService.kt` |
| Desktop Impl | `desktopApp/src/desktopMain/.../DesktopCameraServiceImpl.kt` |
| UI Component | `shared/src/commonMain/.../CameraPreview.kt` |
| Android UI | `shared/src/androidMain/.../AndroidCameraPreview.kt` |
| Desktop UI | `desktopApp/src/desktopMain/.../DesktopCameraPreview.kt` |
| Permissions | `shared/src/androidMain/.../CameraPermissionHelper.kt` |
| DI Module | `shared/src/.../PlatformModule.kt` |
| Config | `shared/src/commonMain/.../BiometricConfig.kt` |

## Dependencies

### Android
```gradle
androidx.camera:camera-core:1.3.0
androidx.camera:camera-camera2:1.3.0
androidx.camera:camera-lifecycle:1.3.0
androidx.camera:camera-view:1.3.0
com.google.accompanist:accompanist-permissions:0.32.0
```

### Desktop
```gradle
org.bytedeco:javacv-platform:1.5.10
```

## Testing

### Mock Camera Service
```kotlin
class MockCameraService : ICameraService {
    override val cameraState = MutableStateFlow<CameraState>(CameraState.Idle)

    override suspend fun initialize(lensFacing: LensFacing) = Result.success(Unit)
    override suspend fun startPreview() = Result.success(Unit)
    override suspend fun captureImage() = Result.success(ByteArray(100))
    // ... implement other methods
}
```

### Test Setup
```kotlin
@Before
fun setup() {
    startKoin {
        modules(module {
            single<ICameraService> { MockCameraService() }
        })
    }
}
```

## Platform Differences

| Feature | Android | Desktop |
|---------|---------|---------|
| Library | CameraX | JavaCV |
| Preview | PreviewView | BufferedImage |
| Quality | High (95% JPEG) | Good (PNG/JPEG) |
| FPS | 30+ | 30 |
| Permissions | Required | Not required |
| Lifecycle | Managed | Manual |

## Support

For detailed information, see:
- **Full Guide**: `CAMERA_INTEGRATION_GUIDE.md`
- **Implementation**: `CAMERA_IMPLEMENTATION_SUMMARY.md`
- **Code**: Browse implementation files
