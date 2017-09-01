#include <jni.h>
#include "org_hofapps_tinyplanet_NativeWrapper.h"
#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/stitching/detail/blenders.hpp>
#include <stdio.h>
#include "Utils.h"

#include <string.h>

//
//#include <sstream>
//#include <iostream>
//
using namespace cv;
using namespace std;

inline void nativeImgBlend(Mat src, Mat dst) {

    float overlapHalf = .1;
    int patchHeight = dst.rows * (.5 + overlapHalf);
    dsc::Utils::print("patchHeight", dsc::Utils::num2str(patchHeight));

    Mat patchTop (src, Rect(0, 0, src.cols, patchHeight));
    Mat patchBottom (src, Rect(0, src.rows-patchHeight, src.cols, patchHeight));

    Mat maskTop(patchTop.size(), CV_8U);
    //maskTop(Rect(0, 0, maskTop.cols, maskTop.rows/2+50)).setTo(255); // Mask defines visible region of 1st  image ,+10 is overlapping region
    maskTop.setTo(255); // Mask defines visible region of 1st  image ,+10 is overlapping region
    //maskTop(Rect(0, (maskTop.rows/2)+10, maskTop.cols, maskTop.rows - (maskTop.rows/2)-10)).setTo(0); //Hidden portion of 1st  image
    Mat maskBottom(patchBottom.size(), CV_8U);
    dsc::Utils::print("maskBottom", dsc::Utils::num2str(maskBottom.rows));
    dsc::Utils::print("maskBottom", dsc::Utils::num2str(maskBottom.cols));
    maskBottom.setTo(255);
    // Define parts that do not overlap:
 //   maskBottom(Rect(0, maskBottom.rows-5, maskBottom.cols, 5)).setTo(123);//Hidden portion of bottom image
    //maskTop(Rect(0, 0, maskTop.cols, 50)).setTo(0);//Hidden portion of bottom image

    //cv::detail::FeatherBlender  blender(0.01f); //sharpness
    cv::detail::MultiBandBlender blender(false, 4);
    //blender.prepare(Rect(0, 0, max(patchTop.cols, patchBottom.cols), max(patchTop.rows, patchBottom.rows)));
    blender.prepare(Rect(0, 0, dst.cols, dst.rows));

    int patchTopY = dst.rows - patchBottom.rows;
    //int patchTopY = 0;
    blender.feed(patchTop, maskTop, Point(0,patchTopY));
    blender.feed(patchBottom, maskBottom, Point(0,0));

    dsc::Utils::print("patchTopY", dsc::Utils::num2str(patchTopY));
    dsc::Utils::print("patchBottom.rows", dsc::Utils::num2str(patchBottom.rows));
    dsc::Utils::print("patchTop.rows", dsc::Utils::num2str(patchTop.rows));


    Mat result_s, result_mask;
    blender.blend(dst, result_mask);
    dsc::Utils::print("height dest", dsc::Utils::num2str(dst.rows));

//    cv::resize(result_s, dst, dst.size());



// Working example:
/*
    Mat patchTop;
    cv::flip(src.clone(), patchTop, 1);

    Mat patchBottom;
    cv::flip(src.clone(), patchBottom, -1);

    Mat maskTop(patchTop.size(), CV_8U);
    maskTop(Rect(0, 0, (maskTop.cols/2)+10, maskTop.rows)).setTo(255); // Mask defines visible region of 1st  image ,+10 is overlapping region
    maskTop(Rect((maskTop.cols/2)+10, 0, maskTop.cols - (maskTop.cols/2)-10, maskTop.rows)).setTo(0); //Hidden portion of 1st  image
    Mat maskBottom(patchBottom.size(), CV_8U);
    maskBottom(Rect(0, 0, maskBottom.cols/2, maskBottom.rows)).setTo(0);//Hidden portion of 2nd image
    maskBottom(Rect(maskBottom.cols/2, 0, maskBottom.cols - maskBottom.cols/2, maskBottom.rows)).setTo(255);//Visible region of 2nd image

    dsc::Utils::print("mat-type", dsc::Utils::num2str(patchTop.type()));

    //cv::detail::FeatherBlender  blender(0.5f); //sharpness
    cv::detail::MultiBandBlender blender(false, 5);
    blender.prepare(Rect(0, 0, max(patchTop.cols, patchBottom.cols), max(patchTop.rows, patchBottom.rows)));
    blender.feed(patchTop, maskTop, Point(0,0));
    blender.feed(patchBottom, maskBottom, Point(0,0));
    Mat result_s, result_mask;
    blender.blend(dst, result_mask);
    */


}

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

JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeImgBlend
  (JNIEnv * jenv, jclass, jlong src, jlong dst)
{
    nativeImgBlend(*((Mat*)src), *((Mat*)dst));
}
