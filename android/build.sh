#!/bin/bash
set -e
cd "$(dirname "$0")"
SDK="$HOME/android-sdk"
BT="$SDK/build-tools/34.0.0"
JAR="$SDK/platforms/android-34/android.jar"

rm -rf build
mkdir -p build/classes build/dex

"$BT/aapt" package -f -M AndroidManifest.xml -I "$JAR" -F build/base.apk
javac -encoding UTF-8 --release 8 -cp "$JAR" -d build/classes src/com/muse/snake/MainActivity.java
"$BT/d8" --min-api 24 --lib "$JAR" --output build/dex build/classes/com/muse/snake/*.class

cp build/base.apk build/app.apk
(cd build/dex && "$BT/aapt" add ../app.apk classes.dex)
"$BT/zipalign" -f -p 4 build/app.apk build/aligned.apk

keytool -genkeypair -keystore build/debug.keystore -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=Android Debug,O=Android,C=US"

"$BT/apksigner" sign --ks build/debug.keystore --ks-pass pass:android --key-pass pass:android \
  --out Snake.apk build/aligned.apk

"$BT/apksigner" verify Snake.apk && echo SIGN_OK
"$BT/aapt" dump badging Snake.apk | head -3
ls -la Snake.apk
