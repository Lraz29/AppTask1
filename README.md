# AppTask1 – Android Game Project

## Overview
This project is an Android game developed as part of an academic assignment.
The game is a lane-based runner where the player avoids obstacles, collects verification items,
and progresses based on distance.

## Main Features
- Lane-based movement 
- Three game modes:
    - Buttons – Slow (Beginner)
    - Buttons – Fast (Hard)
    - Tilt-based control
- Lives system (game ends after 3 crashes)
- Distance-based scoring
- Sound effects and background music
- Persistent Top-10 scores
- GPS-based score location
- Scores screen with list and map view

## Game Flow
1. Splash screen with app logo
2. Menu screen:
    - Select game mode
    - View scores
3. Gameplay:
    - Avoid obstacles
    - Collect verification items
    - Score increases with distance
4. Game Over:
    - Score is saved with timestamp and location
5. Scores Screen:
    - Displays top 10 scores
    - Clicking a score updates the map location

## Technical Details
- Language: Kotlin
- Architecture: Activity + Fragments
- UI: ConstraintLayout, LinearLayout 
- Sensors: Accelerometer (Tilt mode)
- Location: Fused Location Provider (Google Play Services)
- Data Storage: SharedPreferences (JSON)
- Audio:
    - MediaPlayer (background music)
    - SoundPool (short sound effects)

## Permissions
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION

## Libraries & SDKs
- Google Maps SDK
- Google Play Services Location
- AndroidX

## How to Run
1. Open project in Android Studio
2. Sync Gradle
3. Run on emulator or real device
4. Grant location permission when requested

## Notes
- On emulator, a mock location must be set to enable GPS-based scoring.
- Screen recording for demo was done using Android Studio Emulator.

## Author
Liel Razayev
