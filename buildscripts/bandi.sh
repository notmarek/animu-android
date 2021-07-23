./buildall.sh --no-deps
cd ../app/build/outputs/apk/release 
./sign.sh
adb install test-debugSigned.apk