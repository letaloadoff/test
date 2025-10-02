# DungeonCrawlerCompose

A lightweight, original roguelike inspired by classic dungeon crawlers, built with Android Jetpack Compose. This is an original alternative to shattered Pixel Dungeon (no reused assets or code).

## Requirements
- Android Studio Ladybug+
- Android SDK 35, Build Tools 35
- JDK 17

## Build & Run
1. Open the project in Android Studio
2. Let Gradle sync complete
3. Select `app` run configuration
4. Run on a device or emulator running Android 14/15 (API 34/35)

### Samsung Galaxy S25
- Target SDK/Compile SDK: 35
- Works on 1080p+ displays; UI scales using Compose units
- If gestures conflict with edges, enable gesture hints or 3-button nav in system settings during gameplay

## Controls
- On-screen D-pad
- "." waits a turn
- "New Game" regenerates a new dungeon

## Architecture
- Compose UI with Material3
- `GameViewModel` holds `StateFlow<GameState>`
- `GameEngine` handles map generation, movement, and simple enemy chase AI

## Notes
- No assets required; grid is rendered with colored boxes
- Extendable: add FOV, items, combat, fog of war, and save system