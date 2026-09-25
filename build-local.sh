#!/usr/bin/env bash
set -euo pipefail
if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle n'est pas installé. Ouvrez le projet dans Android Studio ou installez Gradle." >&2
  exit 1
fi
: "${ANDROID_HOME:?ANDROID_HOME doit pointer vers le SDK Android}"
gradle :app:assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk NoChoiceAlarm-debug.apk
echo "APK: $(pwd)/NoChoiceAlarm-debug.apk"
