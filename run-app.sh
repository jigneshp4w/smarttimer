#!/bin/bash

# SmartTimer - Build and Run Script
# This script builds the app, starts an emulator if needed, and installs/launches the app

ANDROID_SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_SDK/platform-tools/adb"
EMULATOR="$ANDROID_SDK/emulator/emulator"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SmartTimer Build & Run ===${NC}"

# Check if adb exists
if [ ! -f "$ADB" ]; then
    echo -e "${RED}Error: adb not found at $ADB${NC}"
    echo "Please set ANDROID_HOME environment variable or install Android SDK"
    exit 1
fi

# Function to check if any emulator/device is connected
check_device() {
    $ADB devices | grep -v "List" | grep -v "^$" | grep -q "device$"
}

# Function to get first available AVD
get_avd() {
    $EMULATOR -list-avds | head -1
}

# Check if emulator is running
if check_device; then
    echo -e "${GREEN}Device/Emulator already running${NC}"
else
    echo -e "${YELLOW}No device found. Starting emulator...${NC}"

    # Get available AVD
    AVD=$(get_avd)

    if [ -z "$AVD" ]; then
        echo -e "${RED}Error: No AVD found. Please create an emulator in Android Studio.${NC}"
        exit 1
    fi

    echo "Starting AVD: $AVD"

    # Start emulator in background
    $EMULATOR -avd "$AVD" -no-snapshot-load &
    EMULATOR_PID=$!

    # Wait for emulator to boot
    echo -n "Waiting for emulator to boot"
    MAX_WAIT=120
    WAIT_COUNT=0

    while ! check_device && [ $WAIT_COUNT -lt $MAX_WAIT ]; do
        echo -n "."
        sleep 2
        WAIT_COUNT=$((WAIT_COUNT + 2))
    done
    echo ""

    if ! check_device; then
        echo -e "${RED}Error: Emulator failed to start within ${MAX_WAIT}s${NC}"
        exit 1
    fi

    # Wait for boot to complete
    echo -n "Waiting for system boot"
    $ADB wait-for-device

    while [ "$($ADB shell getprop sys.boot_completed 2>/dev/null)" != "1" ]; do
        echo -n "."
        sleep 2
    done
    echo ""
    echo -e "${GREEN}Emulator ready!${NC}"
fi

# Build the app
echo -e "${YELLOW}Building app...${NC}"
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}Build successful!${NC}"

# Install the app
echo -e "${YELLOW}Installing app...${NC}"
$ADB install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -ne 0 ]; then
    echo -e "${RED}Installation failed!${NC}"
    exit 1
fi

echo -e "${GREEN}Installation successful!${NC}"

# Launch the app
echo -e "${YELLOW}Launching app...${NC}"
$ADB shell am start -n com.smarttimer/.MainActivity

echo -e "${GREEN}=== Done! ===${NC}"
