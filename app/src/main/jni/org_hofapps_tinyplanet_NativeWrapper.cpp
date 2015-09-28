//
// Created by fabian on 25.09.2015.
//

#include <org_hofapps_tinyplanet_NativeWrapper.h>

//#include <com_example_ndk_opencv_androidstudio_NativeClass.h>
#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace std;
using namespace cv;

JNIEXPORT jstring JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_getStringFromNative
        (JNIEnv * env, jobject obj){
    return env->NewStringUTF("Hello from JNIX");
}
