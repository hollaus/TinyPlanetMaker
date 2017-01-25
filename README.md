# TinyPlanetMaker
App for tiny planet effect.
The 'Tiny Planet Maker' app is available for free on Google Play.

<a href='https://play.google.com/store/apps/details?id=org.hofapps.tinyplanet&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="250px"/></a>


## Dependencies
- `JDK` v7 or newer
- Install `Android Studio`
- `OpenCV` for Android [1]
- Android NDK (will be installed via Android Studio)

## Configuration
- copy`.\app\src\main\jni\local\AndroidSkel.mk` to `.\app\src\main\jni\local\Android.mk`
- open `.\app\src\main\jni\local\Android.mk` in a text editor
- uncomment and change the line `MY_OPENCVROOT:= C:/someplace/opencv_sdk/OpenCV-android-sdk` such that
it points to your opencv installation (contains the folders: apk, sample, sdk)
- Open Project from Android Studio
- You will see a dialog "Gradle settings for this project are not configured yet. Would you like the project to use the Gradle wrapper? ..." - click Ok
- Import OpenCV module
  - File -> New -> Import module
  - Set the OpenCV SDK path: .\yourlocalpath\OpenCV-android-sdk\sdk\java
  - Finish.
  - Open project view and from there open Project -> openCVLibrary->build.gradle
  - Set compileSdkVersion, targetSdkVersion and buildToolsVersion to the values of your main build.gradle file.
  See step 6 in [2]
  - Set the Android NDK path
- Add Android NDK
  - Open `Tools > Android > SDK Manager`
  - Tab SDK Tools
  - Check NDK (takes a few minutes to download/install) (copy the NDK path, you will need it later)
  - Restart Android Studio
  - right click `app > Open Module Settings > SDK Location`
  - Set the NDK path

## Debugging Hint
 `Genymotion` is a nice tool for debugging the app (on your PC)
 - If you wanna debug on PC with Genymotion you must change the architecture:
 - In `.\app\src\main\jni\Application.mk` set: `APP_ABI := x86`
 - Note: By using the x86 the Genymotion emulation is much faster (compared to the standard ADB emulator which emulates arm processors)

## Author
Fabian Hollaus
