# Smart Timer - Android Application

A native Android timer application that allows users to create and execute sequential workflow timers with customizable alert durations.

## Features

- **Workflow Management**: Create multiple timer workflows, each containing sequential timers
- **Custom Timers**: Add timers with custom labels and durations (minutes and seconds)
- **Configurable Wait Period**: Set wait period between tasks (up to 5 minutes)
- **Voice Announcements (TTS)**: Speaks workflow and task names at start/completion (toggleable per workflow)
- **Vibration Alerts**: Device vibrates during wait periods between tasks (toggleable per workflow)
- **Sequential Execution**: Timers execute automatically one after another
- **Background Service**: Reliable foreground service ensures timers run even when app is minimized
- **Navigation Lock**: Prevents switching tabs while a workflow is running
- **Auto-Resume**: Automatically returns to running timer when app is reopened
- **Material Design 3**: Beautiful, modern UI with dynamic color support (Android 12+)
- **Fully Offline**: Works completely without internet connection

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room (SQLite)
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Async Operations**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose

## Project Structure

```
com.smarttimer/
├── data/                           # Data layer
│   ├── local/
│   │   ├── database/              # Room database setup
│   │   ├── dao/                   # Data Access Objects
│   │   └── entity/                # Database entities
│   └── repository/                # Repository implementations
├── di/                            # Hilt dependency injection modules
├── presentation/                  # UI layer
│   ├── theme/                     # Material Design 3 theme
│   ├── components/                # Reusable UI components
│   ├── configuration/             # Workflow configuration screens
│   ├── execution/                 # Timer execution screens
│   └── navigation/                # Navigation setup
├── service/                       # Background timer service
│   ├── TimerService.kt           # Foreground service
│   ├── TimerExecutor.kt          # Timer countdown logic
│   ├── TimerState.kt             # Timer state management
│   └── NotificationHelper.kt     # Notification management
└── util/                          # Utility classes
    ├── SoundPlayer.kt            # Alert sound playback
    ├── TtsManager.kt             # Text-to-speech announcements
    ├── VibrationManager.kt       # Vibration alerts
    └── TimeFormatter.kt          # Time formatting utilities
```

## Building the Project

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK API 34
- Minimum SDK: API 26 (Android 8.0)

### Build Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Run on an emulator or physical device

### Build from Command Line

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Usage

### Creating a Workflow

1. Navigate to the **Configuration** tab
2. Tap the **+ New Workflow** button
3. Enter workflow name, description (optional), and alert duration
4. Tap **Create**

### Adding Timers to Workflow

1. In the Configuration tab, tap on a workflow card
2. Tap the **+ Add Timer** button
3. Enter timer label, minutes, and seconds
4. Tap **Add**
5. Repeat to add more timers

### Executing a Workflow

1. Navigate to the **Execution** tab
2. Select a workflow from the list
3. Tap **Start Workflow**
4. Use **Pause**, **Resume**, or **Stop** buttons to control execution

### Timer Behavior

- Voice announces workflow name when starting (if TTS enabled)
- Timers execute sequentially in the order they were added
- Voice announces each task name when starting (if TTS enabled)
- When a timer completes:
  - Voice announces task completion (if TTS enabled)
  - Alert sound plays during wait period
  - Device vibrates during wait period (if vibration enabled)
  - Wait period countdown is displayed on screen
  - Next timer starts automatically after wait period
- Voice announces workflow completion (if TTS enabled)
- Workflow completes when all timers finish

## Database Schema

### WorkflowEntity
- `id`: Primary key (auto-generated)
- `name`: Workflow name
- `description`: Optional description
- `alertDurationSeconds`: Wait period between tasks (1-300 seconds, default: 3)
- `ttsEnabled`: Voice announcements enabled (default: true)
- `vibrationEnabled`: Vibration alerts enabled (default: true)
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

### TimerEntity
- `id`: Primary key (auto-generated)
- `workflowId`: Foreign key to WorkflowEntity (CASCADE DELETE)
- `label`: Timer label
- `durationSeconds`: Timer duration in seconds
- `position`: Order position in workflow
- `createdAt`: Creation timestamp

## Permissions

The app requires the following permissions:

- `FOREGROUND_SERVICE`: For reliable background timer execution
- `POST_NOTIFICATIONS`: For timer notifications (Android 13+)
- `WAKE_LOCK`: To keep device awake during timer execution
- `VIBRATE`: For vibration alerts during wait periods

## Testing

### Manual Testing Checklist

- [x] Create workflow with multiple timers
- [x] Edit workflow alert duration
- [x] Add/delete timers
- [x] Start workflow execution
- [x] Verify countdown accuracy
- [x] Test pause/resume functionality
- [x] Verify alert sound plays for configured duration
- [x] Confirm auto-transition between timers
- [x] Test workflow completion
- [x] Verify app works offline
- [x] Test foreground service in background
- [x] Verify light/dark theme support
- [x] Test dynamic colors (Android 12+)

### Running Unit Tests

```bash
./gradlew test
```

### Running Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Architecture Highlights

### MVVM Pattern
- **Model**: Room entities and repositories
- **View**: Jetpack Compose UI components
- **ViewModel**: State management and business logic

### Reactive Data Flow
- Room queries return `Flow` for reactive updates
- ViewModels expose `StateFlow` for UI state
- UI automatically updates when data changes

### Foreground Service
- Ensures reliable timer execution
- Displays persistent notification
- Survives app being sent to background

## Future Enhancements

- [ ] Timer reordering (drag and drop)
- [ ] Workflow categories/tags
- [ ] Custom alert sounds
- [ ] Statistics and history
- [ ] Export/import workflows
- [ ] Widget support
- [ ] Repeat workflow option
- [ ] Timer templates

## License

This project was created as a demonstration of modern Android development practices using Kotlin, Jetpack Compose, and Material Design 3.

## Support

For issues or questions, please refer to the project documentation or create an issue in the repository.
