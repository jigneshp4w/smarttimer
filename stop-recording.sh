#!/bin/bash

# Stop recording and pull the demo video

ANDROID_SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_SDK/platform-tools/adb"

echo "Stopping screen recording..."
$ADB shell pkill -SIGINT screenrecord

sleep 2

echo "Pulling video from emulator..."
$ADB pull /sdcard/smarttimer_demo.mp4 demo/smarttimer_demo.mp4

echo "Cleaning up..."
$ADB shell rm /sdcard/smarttimer_demo.mp4

echo ""
echo "Demo video saved to: demo/smarttimer_demo.mp4"
echo ""

# Get video info
if command -v ffprobe &> /dev/null; then
    echo "Video details:"
    ffprobe -v quiet -show_format -show_streams demo/smarttimer_demo.mp4 2>/dev/null | grep -E "(duration|width|height|codec_name)" | head -6
fi

echo ""
echo "To view: open demo/smarttimer_demo.mp4"
