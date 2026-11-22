#!/bin/bash
set -e
cd "$(dirname "$0")"
SDK="$HOME/android-sdk"
BT="$SDK/build-tools/34.0.0"
JAR="$SDK/platforms/android-34/android.jar"

rm -rf build
mkdir -p build/classes build/dex

echo "== aapt package =="
"$BT/aapt" package -f -M AndroidManifest.xml -I "$JAR" -F build/base.apk

echo "== javac =="
javac -encoding UTF-8 --release 8 -cp "$JAR" -d build/classes MainActivity.java

echo "== d8 =="
"$BT/d8" --min-api 24 --lib "$JAR" --output build/dex build/classes/com/muse/dailyfortune/*.class

echo "== add dex =="
cp build/base.apk build/app.apk
(cd build/dex && "$BT/aapt" add ../app.apk classes.dex)

echo "== zipalign =="
"$BT/zipalign" -f -p 4 build/app.apk build/aligned.apk

echo "== keystore =="
keytool -genkeypair -keystore build/debug.keystore -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=Android Debug,O=Android,C=US"

echo "== sign =="
"$BT/apksigner" sign --ks build/debug.keystore --ks-pass pass:android --key-pass pass:android \
  --out DailyFortune.apk build/aligned.apk

echo "== verify =="
"$BT/apksigner" verify --print-certs DailyFortune.apk | head -3
"$BT/aapt" dump badging DailyFortune.apk | head -4
ls -la DailyFortune.apk
