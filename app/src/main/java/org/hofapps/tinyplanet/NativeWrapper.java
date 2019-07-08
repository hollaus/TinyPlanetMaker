package org.hofapps.tinyplanet;

import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Created by fabian on 25.09.2015.
 */
public class NativeWrapper {

//    static {
//        System.loadLibrary("MyLib");
//    }

    public void blendImgs(Mat src, Mat dst, int borderHeight) {
        nativeImgBlend(src.getNativeObjAddr(), dst.getNativeObjAddr(), borderHeight);
    }

    public void logPolar(Mat src, Mat dst, float xCenter, float yCenter, double scaleLog, double scale, double angle) {

        nativeLogPolar(src.getNativeObjAddr(), dst.getNativeObjAddr(), xCenter, yCenter, scaleLog, scale, angle);

    }

    private static native void nativeLogPolar(long src, long dst, float xCenter, float yCenter,  double scaleLog, double scale, double angle);
    private static native void nativeImgBlend(long src, long dst, int borderHeight);

    public native static String getStringFromNative();
}
