package org.hofapps.tinyplanet;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by fabian on 06.10.2015.
 */
public class PlanetMaker {

    private static final double DEG2RAD = 0.017453292519943;

    private Mat mInputImage, mPlanetImage, mOriginalImage;
    private NativeWrapper mNativeWrapper;
    private int mOutputSize;
    private double mSize, mScale, mAngle, cropLeft, cropRight;
    private RectF mCropRect;

    private int mFullOutputSize;
    private int[] mSizeMinMax;
    private boolean mIsImageLoaded, mIsPlanetInverted;
//    private ComputePlanetTask task;

//    private boolean isComputingPlanet = false;
//    private long lastRecognitionTime = -1;
//    private static final int MAX_OUTPUT_SIZE = 3000; // try out different values here:



    public PlanetMaker(NativeWrapper nativeWrapper,int outputSize, int[] sizeMinMax) {

        mNativeWrapper = nativeWrapper;
        mOutputSize = outputSize;
        mSizeMinMax = sizeMinMax;

        setInitValues();

        mIsImageLoaded = false;
        mIsPlanetInverted = false;

    }

    public void setInputImage(Bitmap bitmap) {

        mIsImageLoaded = true;
        mInputImage = new Mat();
        Utils.bitmapToMat(bitmap, mInputImage);
        mOriginalImage = mInputImage.clone();

        mFullOutputSize = Math.max(mInputImage.width(), mInputImage.height());

        if (mFullOutputSize > MainActivity.MAX_IMG_SIZE)
            mFullOutputSize = MainActivity.MAX_IMG_SIZE;

        initImages();

    }

    public void reset() {

        setInitValues();

        if (mIsPlanetInverted && mInputImage != null)
            Core.flip(mInputImage, mInputImage, -1);

        mIsPlanetInverted = false;
        updatePlanet();

    }

    public boolean getIsImageLoaded() {

        return mIsImageLoaded;

    }


    public void releasePlanetImage() {

//        planetImage.release();


    }


    public Mat getPlanetImage() {

        return mPlanetImage;

    }

    public double getSize() {

        return mSize;

    }

    public double getScale() {

        return mScale;

    }

    public double getAngle() {

        return mAngle;

    }

    public void setSize(double size) {

        mSize = size;
        updatePlanet();

    }

    public void setScale(double scale) {

        mScale = scale;
        updatePlanet();

    }

    public void setAngle(double angle) {

        mAngle = angle;
        updatePlanet();

    }

    public void addAngle(double angleDiff) {



        mAngle += angleDiff;
        mAngle = Math.round(mAngle);
        mAngle %= 360;

        if (mAngle < 0)
            mAngle = 360 + mAngle;

        updatePlanet();

    }

    public void addScale(double scaleDiff) {

        double newSize = mSize * scaleDiff;
        newSize = Math.round(newSize);

        if (newSize > mSizeMinMax[SettingsFragment.ARRAY_MAX_POS])
            newSize = mSizeMinMax[SettingsFragment.ARRAY_MAX_POS];
        else if (newSize < mSizeMinMax[SettingsFragment.ARRAY_MIN_POS])
            newSize = mSizeMinMax[SettingsFragment.ARRAY_MIN_POS];

        mSize = newSize;

        updatePlanet();

    }

    public void addScaleLog(double scaleDiff) {

        mSize *= scaleDiff;
        mSize = Math.round(mSize);
        updatePlanet();

    }

    public void invert(boolean isInverted) {

        mIsPlanetInverted = isInverted;

        if (!mIsImageLoaded)
            return;

        Core.flip(mInputImage, mInputImage, -1);
        updatePlanet();

    }

    public void setCropRect(RectF cropRect) {

        mCropRect = cropRect;
        updatePlanet();
    }


    public void setCropLeft(int cropLeft) {

        this.cropLeft = (double) cropLeft / (double) 100;
        updatePlanet();

    }


    public void setCropRight(int cropRight) {

        this.cropRight = (double) cropRight / (double) 100;
        updatePlanet();

    }


    public boolean getIsPlanetInverted() {

        return mIsPlanetInverted;

    }

    public Mat getFullResPlanet() {

        if ((mOriginalImage == null) || (mNativeWrapper == null) || (mInputImage == null))
            return null;

        Mat tmpInputImage = mOriginalImage.clone();
        Imgproc.resize(tmpInputImage, tmpInputImage, new Size(mFullOutputSize, mFullOutputSize), 0, 0, Imgproc.INTER_CUBIC);
        Mat fullResPlanet = new Mat(tmpInputImage.rows(), tmpInputImage.cols(), mInputImage.type());

//        Rotate the image 90 degrees:
        Core.flip(tmpInputImage.t(), tmpInputImage, 1);

        if (mIsPlanetInverted)
            Core.flip(tmpInputImage, tmpInputImage, -1);

        double fac = tmpInputImage.width() / mInputImage.width();
        mNativeWrapper.logPolar(tmpInputImage, fullResPlanet, tmpInputImage.width() * 0.5f, tmpInputImage.height() * 0.5f, mSize * fac, mScale, mAngle * DEG2RAD);
        tmpInputImage.release();

        return fullResPlanet;

    }

    private void setInitValues() {

        mSize = 250;
        mScale = 105;
        mAngle = 180;
        cropLeft = 0;
        cropRight = 0;

    }

    private void updatePlanet() {

        if (!mIsImageLoaded)
            return;


//        new Thread(){
//            @Override
//            public void run() {
//                Message msg = new Message();
//                if(!isComputingPlanet) {
//                    planetImage = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());
//                    nativeWrapper.logPolar(inputImage, planetImage, inputImage.width() * 0.5f, inputImage.height() * 0.5f, size, scale, angle * DEG2RAD);
//                    isComputingPlanet = false;
//                }
//            }
//        }).start();

//        long now = Calendar.getInstance().getTimeInMillis();

//        if ((lastRecognitionTime == -1) || (now - lastRecognitionTime > 500)) {
//            lastRecognitionTime = now;



        if (mCropRect != null) {


//            Take care here, because the images are flipped after they have been loaded:
            Rect flippedRect = flipCropRect();
            Mat image_roi = mInputImage.submat(flippedRect);

//            Imgproc.resize(image_roi, image_roi, new Size(mOutputSize, mOutputSize), 0, 0, Imgproc.INTER_CUBIC);

            mPlanetImage = new Mat(image_roi.rows(), image_roi.cols(), image_roi.type());
            mNativeWrapper.logPolar(image_roi, mPlanetImage, image_roi.width() * 0.5f, image_roi.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
//            mPlanetImage = mInputImage.clone();
        }

        else {
            mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
            mNativeWrapper.logPolar(mInputImage, mPlanetImage, mInputImage.width() * 0.5f, mInputImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
        }

    }

    private Rect flipCropRect() {

        int startX, startY, endX, endY;
        if (!mIsPlanetInverted) {
            startX = Math.round((1 - mCropRect.bottom) * mInputImage.height());
            endX = Math.round((1 - mCropRect.top) * mInputImage.height());
            startY = Math.round(mCropRect.left * mInputImage.width());
            endY = Math.round(mCropRect.right * mInputImage.width());
        }
        else {
            startX = Math.round(mCropRect.top * mInputImage.height());
            endX = Math.round(mCropRect.bottom * mInputImage.height());
            startY = Math.round((1 - mCropRect.right) * mInputImage.width());
            endY = Math.round((1 - mCropRect.left) * mInputImage.width());
        }

        startX  = (startX < 0) ? 0 : startX;
        startY  = (startY < 0) ? 0 : startY;
        endX    = (endX > mInputImage.width()) ? mInputImage.width() : endX;
        endY    = (endY > mInputImage.height()) ? mInputImage.height() : endY;

        Rect flippedRect = new Rect(startX, startY, endX - startX, endY - startY);

        return flippedRect;

    }

    private void blurImage() {

        int rectW = 255;

//        Rect rectLeft = new Rect(0, 0, rectW, mInputImage.height());
        Rect rectLeft = new Rect(0, 0, mInputImage.width(), rectW);
        Mat subMatLeft = mInputImage.submat(rectLeft);
//        image_roi = image_roi.clone();

        Rect rectRight = new Rect(0, mInputImage.height()-rectW, mInputImage.width(), rectW);
        Mat subMatRight = mInputImage.submat(rectRight);

//        Mat tmp = new Mat(subMatLeft.rows(), subMatLeft.cols(), subMatLeft.type());
//        Mat tmp = new Mat();
//        Core.addWeighted(subMatLeft, .5, subMatRight, .5, 0, tmp);

        Mat tmp = new Mat();
        Mat mask = new Mat(subMatLeft.rows(), subMatLeft.cols(), subMatLeft.type());

        for (int i = 0; i < subMatLeft.rows(); i++)
            mask.row(i).setTo(new Scalar(i, i, i, 255));

//        Imgproc.cvtColor(subMatLeft, subMatLeft, Imgproc.COLOR_RGBA2GRAY, 1); //make it gray
//        Imgproc.cvtColor(subMatLeft, subMatLeft, Imgproc.COLOR_GRAY2RGBA, 4); //change to rgb

        mask.copyTo(mInputImage.submat(rectLeft));


    }

    private void initImages() {

        Imgproc.resize(mInputImage, mInputImage, new Size(mOutputSize, mOutputSize), 0, 0, Imgproc.INTER_CUBIC);

//        Rotate the image 90 degrees:

//        Creates the planet and inverts it:
        Core.flip(mInputImage.t(), mInputImage, 1);

        if (mIsPlanetInverted)
            Core.flip(mInputImage, mInputImage, -1);


//        Inverts the planet and undos it:
//        Core.flip(inputImage.t(), inputImage, 0);
//        Core.flip(inputImage, inputImage, -1);

        // rotates -90 and undos
//        Core.flip(inputImage.t(), inputImage, 0);
//        Core.flip(inputImage.t(), inputImage, 1);

//        This was necessary when the image was opened by OpenCV and NOT Android BitmapFactory
//            We need COLOR_BGR2RGBA to flip the color channel AND to get a transparent background:
//        Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_BGR2RGBA);
        mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
        mNativeWrapper.logPolar(mInputImage, mPlanetImage, mInputImage.width() * 0.5f, mInputImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);

        updatePlanet();

    }



    //    private class ComputePlanetTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            isComputingPlanet = true;
//            mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
//            mNativeWrapper.logPolar(mInputImage, mPlanetImage, mInputImage.width() * 0.5f, mInputImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            isComputingPlanet = false;
//        }
//    }
//
//
//
    public interface PlanetChangeCallBack {

        void onSizeChange(int size);
        void onScaleChange(int scale);
        void onAngleChange(int angle);
        void addAngle(float angle);
        void addScaleLog(float scaleLog);
        void onInvertChange(boolean isInverted);
        void onCrop(RectF rect);
        void onCropLeftChange(int cropLeft);
        void onCropRightChange(int cropRight);

    }




}
