#include <jni.h>
#include "org_hofapps_tinyplanet_NativeWrapper.h"
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <stdio.h>

#include <string.h>

//
//#include <sstream>
//#include <iostream>
//
//using namespace std;


inline void nativeLogPolar(Mat src, Mat dst, float xCenter, float yCenter, double scaleLog, double scale, double angle) {

//    cv::Mat tmp;

}

JNIEXPORT jstring JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_getStringFromNative
        (JNIEnv * env, jobject obj){
    return (*env)->NewStringUTF(env, "Hello from JNI");
}

//JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeLogPolar
//(JNIEnv *, jclass, jlong, jlong, jfloat, jfloat, jdouble, jdouble, jdouble);

JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeLogPolar
(JNIEnv * jenv, jclass class, jlong src, jlong dst, jfloat xCenter, jfloat yCenter, jdouble scale, jdouble scaleLog, jdouble angle)
{
//vector_Rect_to_Mat(RectFaces, *((Mat*)faces));
//overLayFacesMat(*((Mat*)image), *((Mat*)faceMat), (bool) isPictureRequested);

}
