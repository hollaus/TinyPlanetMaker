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

	Mat image1 = src.clone();

	    dsc::Utils::print("mat-type", dsc::Utils::num2str(image1.type()));

	cvtColor(image1, image1, CV_BGRA2BGR);

	    dsc::Utils::print("mat-type", dsc::Utils::num2str(image1.type()));

	Mat image2;
	cv::flip(image1, image2, 1);
	Mat image1s, image2s;
	image1.convertTo(image1s, CV_16S);
	image2.convertTo(image2s, CV_16S);

	Mat mask1(image1s.size(), CV_8U);
	mask1(Rect(0, 0, (mask1.cols / 2) + 20, mask1.rows)).setTo(255); // Mask defines visible region of 1st  image ,+10 is overlapping region
	mask1(Rect((mask1.cols / 2) + 20, 0, mask1.cols - (mask1.cols / 2) - 20, mask1.rows)).setTo(0); //Hidden portion of 1st  image
	Mat mask2(image2s.size(), CV_8U);
	mask2(Rect(0, 0, mask2.cols / 2, mask2.rows)).setTo(0.4);//Hidden portion of 2nd image
	mask2(Rect(mask2.cols / 2, 0, mask2.cols - mask2.cols / 2, mask2.rows)).setTo(255);//Visible region of 2nd image

	cv::detail::MultiBandBlender blender(0.5f); //sharpness
	blender.prepare(Rect(0, 0, max(image1s.cols, image2s.cols), max(image1s.rows, image2s.rows)));

    blender.feed(image1s, mask1, cv::Point(0, 0));
    blender.feed(image2s, mask2, Point(0, 0));
    cv::Mat result, result_mask;
    blender.blend(result, result_mask);
    result.convertTo(dst, CV_8U);

  //  dst = src.clone();

    cvtColor(dst, dst, CV_BGR2BGRA);

    dsc::Utils::print("dst6-type", dsc::Utils::num2str(result.type()));
    dsc::Utils::print("dst5-type", dsc::Utils::num2str(image1.type()));


/*
    int rectW = 200;
    //Mat image1 = src(cv::Rect(0, 0, src.cols, rectW));
    Mat image1 = src(cv::Rect(100,100,100,100));
    Mat image2 = src(cv::Rect(0, src.rows-rectW, src.cols, rectW));

    Mat image1s, image2s;
    image1s = image1.clone();
    cvtColor(image1s, image1s, CV_BGRA2BGR);
    image1s.convertTo(image1s, CV_16SC3);
    image2s = image2.clone();
    cvtColor(image2s, image2s, CV_BGRA2BGR);
    image2s.convertTo(image2s, CV_16SC3);

    Mat mask1(image1s.size(), CV_8U);
    mask1(Rect(0, 0, (mask1.cols/2)+10, mask1.rows)).setTo(255); // Mask defines visible region of 1st  image ,+10 is overlapping region
    mask1(Rect((mask1.cols/2)+10, 0, mask1.cols - (mask1.cols/2)-10, mask1.rows)).setTo(0); //Hidden portion of 1st  image
    Mat mask2(image2s.size(), CV_8U);
    mask2(Rect(0, 0, mask2.cols/2, mask2.rows)).setTo(0);//Hidden portion of 2nd image
    mask2(Rect(mask2.cols/2, 0, mask2.cols - mask2.cols/2, mask2.rows)).setTo(255);//Visible region of 2nd image

    cv::detail::FeatherBlender blender(0.5f); //sharpness
    blender.prepare(Rect(0, 0, max(image1s.cols, image2s.cols), max(image1s.rows, image2s.rows)));

    dsc::Utils::print("type", dsc::Utils::num2str(image1s.type()));
    dsc::Utils::print("should be", dsc::Utils::num2str(CV_16SC3));

    dsc::Utils::print("size mask", dsc::Utils::num2str(mask1.rows));

    blender.feed(image1s, mask1, cv::Point(0,0));

    blender.feed(image2s, mask2, Point(0,0));

    cv::Mat result, result_mask;

    //

    blender.blend(result, result_mask);
    result.convertTo(dst, CV_8U);

    dst = image1.clone();
    dst.convertTo(dst, CV_8U);

    dsc::Utils::print("rows src", dsc::Utils::num2str(src.rows));
    dsc::Utils::print("cols src", dsc::Utils::num2str(src.cols));
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

JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeImgBlend
  (JNIEnv * jenv, jclass, jlong src, jlong dst) {
    nativeImgBlend(*((Mat*)src), *((Mat*)dst));
  }

JNIEXPORT void JNICALL Java_org_hofapps_tinyplanet_NativeWrapper_nativeLogPolar
(JNIEnv * jenv, jclass, jlong src, jlong dst, jfloat xCenter, jfloat yCenter, jdouble scale, jdouble scaleLog, jdouble angle)
{

    nativeLogPolar(*((Mat*)src), *((Mat*)dst), (float) xCenter, (float) yCenter, (double) scale, (double) scaleLog, (double) angle);
//vector_Rect_to_Mat(RectFaces, *((Mat*)faces));
//overLayFacesMat(*((Mat*)image), *((Mat*)faceMat), (bool) isPictureRequested);

}
