# TinyPlanetMaker
App for tiny planet effect.
The 'TinyPlanetMaker' app is available for free on Google Play.

## Build Instructions
TinyPlanetMaker makes use of native (C++) OpenCV and OpenCV Java API. The Java part is automatically downloaded after syncing.
### Download native library
For the native part you have to clone this repo:

```shell
git clone https://github.com/hollaus/opencv_libs.git opencv_native
```
### Add native library to the project
Next you have to copy two folders from the native library to the project.
- copy `opencv_native/sdk/native/libs` to `app/src/main/jniLibs`
- copy `opencv_native/sdk/native/jni/include` to `app/src/main/include`

Alternatively, you can create symlinks - as written below. It is assumed that your project root folder (DocScan) and the opencv_native folder are on the same hierarchy level.

Windows:

```shell
cd app/src/main
mklink /d jniLibs ..\..\..\..\opencv_native\sdk\native\libs
mklink /d include ..\..\..\..\opencv_native\sdk\native\jni\include
```

Linux:
```shell
cd app/src/main
ln -s ../../../opencv_native/sdk/native/libs jniLibs
ln -s ../../../opencv_native/sdk/native/jni/include include
```


## Author
[Fabian Hollaus](https://github.com/hollaus/)

[Markus Diem](https://github.com/diemmarkus/) implemented the log-polar function that generates the tiny planets and designed the neat app icon.
