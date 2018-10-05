#! /bin/bash

KEY_FILE=/Users/giovannivisentini/SDK/Android/BlueMS_Android/myreleasekey.jks
export PATH=~/Library/Android/sdk/build-tools/28.0.3/:$PATH
versionName=$1

cd BlueSTSDK
git tag $versionName
git push --tags origin
cd ..

cd BlueSTSDK_Gui
git tag $versionName
git push --tags origin
cd ..

git tag $versionName
git push --tags origin 

mkdir ../$versionName

zip -r ../$versionName/src_$versionName.zip . -x '*.git*' '*build/*' '*.gradle/*' '*release*'

./gradlew assembleRelease

cp app/build/outputs/apk/release/app-release-unsigned.apk ../$versionName/$versionName-unsigned.apk

zipalign -v -p 4 ../$versionName/$versionName-unsigned.apk ../$versionName/$versionName-unsigned-aligned.apk

apksigner sign --v1-signing-enabled  --v2-signing-enabled   --ks $KEY_FILE --ks-key-alias MyReleaseKey --ks-pass pass:password --out ../$versionName/$versionName-release.apk --in ../$versionName/$versionName-unsigned-aligned.apk
apksigner verify ../$versionName/$versionName-release.apk
