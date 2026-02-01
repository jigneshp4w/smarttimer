#!/bin/bash

# Smart Timer - Quick Run Script
# This script starts the emulator, builds, and runs the app

set -e

ANDROID_SDK="$HOME/Library/Android/sdk"
JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
AVD_NAME="Medium_Phone_API_36.1"

echo "🚀 Starting Smart Timer..."

# Check if emulator is already running
if ! $ANDROID_SDK/platform-tools/adb devices | grep -q "emulator"; then
    echo "📱 Starting emulator..."
    $ANDROID_SDK/emulator/emulator -avd $AVD_NAME -no-snapshot-load > /tmp/emulator.log 2>&1 &

    echo "⏳ Waiting for emulator to boot..."
    $ANDROID_SDK/platform-tools/adb wait-for-device
    sleep 5

    # Wait for boot to complete
    while [ "$($ANDROID_SDK/platform-tools/adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    echo "✅ Emulator ready!"
else
    echo "✅ Emulator already running"
fi

# Build and install
echo "🔨 Building and installing app..."
JAVA_HOME=$JAVA_HOME gradle installDebug

# Launch the app
echo "🎯 Launching Smart Timer..."
$ANDROID_SDK/platform-tools/adb shell am start -n com.smarttimer/.MainActivity

echo "✅ Done! Smart Timer is running on the emulator."
