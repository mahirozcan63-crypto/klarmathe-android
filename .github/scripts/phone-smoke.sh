#!/usr/bin/env bash
set -euo pipefail
SDK_BIN="$ANDROID_HOME/cmdline-tools/latest/bin"
ADB="$ANDROID_HOME/platform-tools/adb"
IMAGE='system-images;android-36;google_apis;x86_64'
mkdir -p build/phone-checks
test -c /dev/kvm
sudo setfacl -m "u:$(id -un):rw" /dev/kvm
"$SDK_BIN/sdkmanager" "$IMAGE"
printf 'no\n' | "$SDK_BIN/avdmanager" create avd --force --name klarlernen-phone --package "$IMAGE" --device pixel_5
"$ANDROID_HOME/emulator/emulator" -avd klarlernen-phone -no-window -no-audio -no-boot-anim -no-snapshot -gpu software -memory 2048 -cores 2 -camera-back none -camera-front none > build/phone-checks/emulator.log 2>&1 &
EMULATOR_PID=$!
trap '"$ADB" emu kill >/dev/null 2>&1 || kill "$EMULATOR_PID" 2>/dev/null || true' EXIT
timeout 240 "$ADB" wait-for-device
BOOTED=false
for n in $(seq 1 90); do
  if [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = '1' ]; then
    BOOTED=true
    break
  fi
  sleep 2
done
if [ "$BOOTED" != true ]; then
  tail -80 build/phone-checks/emulator.log
  exit 1
fi
"$ADB" shell input keyevent 82
"$ADB" shell settings put global window_animation_scale 0
"$ADB" shell settings put global transition_animation_scale 0
"$ADB" shell settings put global animator_duration_scale 0
"$ADB" shell wm size 1080x2400
"$ADB" shell wm density 480
"$ADB" install -r app/build/outputs/apk/debug/app-debug.apk
"$ADB" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
"$ADB" logcat -c
timeout 240 "$ADB" shell am instrument -w -r de.klarlernen.app.test/de.klarlernen.app.FlowInstrumentation | tee build/phone-checks/result.txt
"$ADB" logcat -d -s PhoneFlow:E AndroidRuntime:E > build/phone-checks/runtime.log
cat build/phone-checks/runtime.log
grep -q 'FLOW_RESULT=PASS' build/phone-checks/result.txt
