#!/bin/bash
# Install and run SmartTimer app

ADB="/Users/parthpatel/Library/Android/sdk/platform-tools/adb"
APK="app/build/outputs/apk/debug/app-debug.apk"

echo "Waiting for device..."
$ADB wait-for-device

echo "Installing app..."
$ADB install -r "$APK"

echo "Launching app..."
$ADB shell am start -n com.smarttimer/.MainActivity

echo "Done! The app should now be running on your device/emulator."
