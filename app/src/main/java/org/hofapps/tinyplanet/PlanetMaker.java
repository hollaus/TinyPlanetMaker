package org.hofapps.tinyplanet;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by fabian on 06.10.2015.
 */
public class PlanetMaker {

    private static final double DEG2RAD = 0.017453292519943;

    private Mat inputImage, planetImage, originalImage;
    private NativeWrapper nativeWrapper;
    private int outputSize;
    private double size, scale, angle;
    private int fullOutputSize;
    private int[] sizeMinMax;
    private boolean isTaskRunning;

//    private static final int MAX_OUTPUT_SIZE = 3000; // try out different values here:



    public PlanetMaker(NativeWrapper nativeWrapper,int outputSize, int[] sizeMinMax) {

        this.nativeWrapper = nativeWrapper;
        this.outputSize = outputSize;
        this.sizeMinMax = sizeMinMax;

        size = 1000;
        scale = 100;
        angle = 180;

        isTaskRunning = false;


    }

    public void setInputImage(Bitmap bitmap) {

        inputImage = new Mat();

        Utils.bitmapToMat(bitmap, inputImage);

        this.originalImage = inputImage.clone();

        fullOutputSize = Math.max(inputImage.width(), inputImage.height());

        if (fullOutputSize > MainActivity.MAX_IMG_SIZE)
            fullOutputSize = MainActivity.MAX_IMG_SIZE;

        initImages();

    }

    public void setInputImage(Mat inputImage) {

        this.inputImage = inputImage;
        initImages();

    }

    public void releasePlanetImage() {

        planetImage.release();


    }


    public Mat getPlanetImage() {

        return planetImage;

    }

    public double getSize() {

        return size;

    }

    public double getScale() {

        return scale;

    }

    public double getAngle() {

        return angle;

    }

    public void setSize(double size) {

        this.size = size;
        updatePlanet();

    }

    public void setScale(double scale) {

        this.scale = scale;
        updatePlanet();

    }

    public void setAngle(double angle) {

        this.angle = angle;
        updatePlanet();

    }

    public void addAngle(double angleDiff) {



        angle += angleDiff;
        angle = Math.round(angle);
        angle %= 360;

        if (angle < 0)
            angle = 360 + angle;

        updatePlanet();

    }

    public void addScale(double scaleDiff) {

        double newSize = size * scaleDiff;
        newSize = Math.round(newSize);

        if (newSize > sizeMinMax[SettingsFragment.ARRAY_MAX_POS])
            newSize = sizeMinMax[SettingsFragment.ARRAY_MAX_POS];
        else if (newSize < sizeMinMax[SettingsFragment.ARRAY_MIN_POS])
            newSize = sizeMinMax[SettingsFragment.ARRAY_MIN_POS];

        size = newSize;

        updatePlanet();

    }

    public void addScaleLog(double scaleDiff) {

        size *= scaleDiff;
        size = Math.round(size);
        updatePlanet();

    }

    public Mat getFullResPlanet() {

        if ((originalImage == null) || (nativeWrapper == null) || (inputImage == null))
            return null;

        Mat tmpInputImage = originalImage.clone();

        Imgproc.resize(tmpInputImage, tmpInputImage, new Size(fullOutputSize, fullOutputSize), 0, 0, Imgproc.INTER_CUBIC);

        Mat fullResPlanet = new Mat(tmpInputImage.rows(), tmpInputImage.cols(), inputImage.type());

//        Rotate the image 90 degrees:
        Core.flip(tmpInputImage.t(), tmpInputImage, 1);

        nativeWrapper.logPolar(tmpInputImage, fullResPlanet, tmpInputImage.width() * 0.5f, tmpInputImage.height() * 0.5f, size, scale, angle * DEG2RAD);

        tmpInputImage.release();

        return fullResPlanet;

    }

    private void updatePlanet() {

        planetImage = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());

        nativeWrapper.logPolar(inputImage, planetImage, inputImage.width() * 0.5f, inputImage.height() * 0.5f, size, scale, angle * DEG2RAD);

//        if (!isTaskRunning) {
//
//            isTaskRunning = true;
//
//            Double[] values = new Double[3];
//            values[0] = size;
//            values[1] = scale;
//            values[2] = angle * DEG2RAD;
//
//            new PlanetCalcTask().execute(values);
//
//        }

    }

    private void initImages() {

        Imgproc.resize(inputImage, inputImage, new Size(outputSize, outputSize), 0, 0, Imgproc.INTER_CUBIC);

//        Rotate the image 90 degrees:
        Core.flip(inputImage.t(), inputImage, 1);

//        This was necessary when the image was opened by OpenCV and NOT Android BitmapFactory
//            We need COLOR_BGR2RGBA to flip the color channel AND to get a transparent background:
//        Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_BGR2RGBA);
        planetImage = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());

        updatePlanet();

    }



    public static interface PlanetChangeCallBack {

        void onSizeChange(int size);
        void onScaleChange(int scale);
        void onAngleChange(int angle);
        void addAngle(float angle);
        void addScaleLog(float scaleLog);
    }

//    nativeWrapper.logPolar(inputImage, planetImage, inputImage.width() * 0.5f, inputImage.height() * 0.5f, size, scale, angle * DEG2RAD);

    private class PlanetCalcTask extends AsyncTask<Double, Void, Void> {


        protected Void doInBackground(Double... values) {

            nativeWrapper.logPolar(inputImage, planetImage, inputImage.width() * 0.5f, inputImage.height() * 0.5f, values[0], values[1], values[2]);

            return null;

        }

        protected void onPostExecute(Void v) {

            isTaskRunning = false;

        }


    }


}
