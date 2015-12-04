#include <jni.h>
#include "org_hofapps_tinyplanet_NativeWrapper.h"
#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <stdio.h>

#include <string.h>

//
//#include <sstream>
//#include <iostream>
//
using namespace std;
using namespace cv;


inline void nativeLogPolar(Mat src, Mat dst, float xCenter, float yCenter, double scaleLog, double scale, double angle) {

    cv::Mat mapx, mapy;


    cv::Size ssize, dsize;
    ssize = src.size();
    dsize = dst.size();

    mapx = cv::Mat( dsize.height, dsize.width, CV_32F );
    mapy = cv::Mat( dsize.height, dsize.width, CV_32F );

    float xDist = dst.cols - xCenter;
    float yDist = dst.rows - yCenter;

    float radius = std::sqrt(xDist*xDist + yDist*yDist);

    float fixedScale = src.cols / std::log(radius/scaleLog + 1);
    scale = fixedScale / (scale/100);

    int x, y;
    cv::Mat bufx, bufy, bufp, bufa;
    double ascale = ssize.height/(2*CV_PI);
    cv::AutoBuffer<float> _buf(4*dsize.width);
    float* buf = _buf;

    bufx = cv::Mat( 1, dsize.width, CV_32F, buf );
    bufy = cv::Mat( 1, dsize.width, CV_32F, buf + dsize.width );
    bufp = cv::Mat( 1, dsize.width, CV_32F, buf + dsize.width*2 );
    bufa = cv::Mat( 1, dsize.width, CV_32F, buf + dsize.width*3 );

    for( x = 0; x < dsize.width; x++ )
        bufx.ptr<float>()[x] = (float)x - xCenter;

    for( y = 0; y < dsize.height; y++ ) {
        float* mx = mapx.ptr<float>(y);
        float* my = mapy.ptr<float>(y);

        for( x = 0; x < dsize.width; x++ )
            bufy.ptr<float>()[x] = (float)y - yCenter;

        cv::cartToPolar(bufx, bufy, bufp, bufa);

        for( x = 0; x < dsize.width; x++ ) {
            bufp.ptr<float>()[x] /= (float)scaleLog;
            bufp.ptr<float>()[x] += 1.0f;
        }

        cv::log(bufp, bufp);

        for( x = 0; x < dsize.width; x++ ) {
            double rho = bufp.ptr<float>()[x]*scale;
            double phi = bufa.ptr<float>()[x] + angle;

            if (phi < 0)
                phi += 2*CV_PI;
            else if (phi > 2*CV_PI)
                phi -= 2*CV_PI;

            phi *= ascale;

            //qDebug() << "phi: " << bufa.data.fl[x];

            mx[x] = (float)rho;
            my[x] = (float)phi;
        }
    }

    cv::remap(src, dst, mapx, mapy, CV_INTER_AREA, BORDER_REPLICATE);

}

JNIEXPORT jstring JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_getStringFromNative
        (JNIEnv * env, jobject obj){


    return NULL;
}

//JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeLogPolar
//(JNIEnv *, jclass, jlong, jlong, jfloat, jfloat, jdouble, jdouble, jdouble);

JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeLogPolar
(JNIEnv * jenv, jclass, jlong src, jlong dst, jfloat xCenter, jfloat yCenter, jdouble scale, jdouble scaleLog, jdouble angle)
{

    nativeLogPolar(*((Mat*)src), *((Mat*)dst), (float) xCenter, (float) yCenter, (double) scale, (double) scaleLog, (double) angle);
//vector_Rect_to_Mat(RectFaces, *((Mat*)faces));
//overLayFacesMat(*((Mat*)image), *((Mat*)faceMat), (bool) isPictureRequested);

}
