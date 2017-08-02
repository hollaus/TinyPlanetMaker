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
- Open the project
- Setup OpenCV
  - Follow the steps listed here: https://www.learn2crack.com/2016/03/setup-opencv-sdk-android-studio.html in the section 'Download OpenCV Android SDK' and 'Setup OpenCV Android SDK'
  (The steps in the other sections are not needed for this project.)
- Tell the project the location of your OpenCV installation:
  - copy`.\app\src\main\jni\local\AndroidSkel.mk` to `.\app\src\main\jni\local\Android.mk`
  - open `.\app\src\main\jni\local\Android.mk` in a text editor
  - uncomment and change the line `MY_OPENCVROOT:= somepath` such that
  it points to your opencv installation (contains the folders: apk, sample, sdk)

## Debugging Hint
 `Genymotion` is a nice tool for debugging the app (on your PC)
 - If you wanna debug on PC with Genymotion you must change the architecture:
 - In `.\app\src\main\jni\Application.mk` set: `APP_ABI := x86`
 - Note: By using the x86 the Genymotion emulation is much faster (compared to the standard ADB emulator which emulates arm processors)

## Author
Fabian Hollaus
