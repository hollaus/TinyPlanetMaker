package org.hofapps.tinyplanet;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by fabian on 06.10.2015.
 */
public class PlanetMaker {

    private static final double DEG2RAD = 0.017453292519943;

    private Mat mInputImage, mOutputImage, mOriginalImage, mInterimImage;
    private NativeWrapper mNativeWrapper;
    private int mOutputSize;
    private double mSize, mScale, mAngle;
    private RectF mCropRect;
    private int mFullOutputSize;
    private int[] mSizeMinMax;
    private boolean mIsImageLoaded, mIsPlanetInverted, mIsFaded;
    private PlanetTaskCallBack mTaskCallBack;
    private boolean mIsComputing = false;
    private boolean mIsComputingPostponed = false;

    private static String CLASS_NAME = "PlanetMaker";

    public PlanetMaker(NativeWrapper nativeWrapper,int outputSize, int[] sizeMinMax) {

        mNativeWrapper = nativeWrapper;
        mOutputSize = outputSize;
        mSizeMinMax = sizeMinMax;

        setInitValues();

        mIsImageLoaded = false;
        mIsPlanetInverted = false;

    }

    public void setInputImage(Bitmap bitmap, boolean isPano) {

        if (bitmap == null)
            return;

        mIsFaded = !isPano;
        mIsImageLoaded = true;
        mInputImage = new Mat();

//        Check if the bitmap has the correct type for the OpenCV bitmapToMat function:
        if (bitmap.getConfig() != null) { // bitmap.getConfig() just returns a valid value if the format is in one of the public formats.
            if (bitmap.getConfig() != Bitmap.Config.ARGB_8888 && bitmap.getConfig() != Bitmap.Config.RGB_565)
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        else
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);

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


    public Mat getPlanetImage() {

        return mOutputImage;

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


    public void invert(boolean isInverted) {

        mIsPlanetInverted = isInverted;

        if (!mIsImageLoaded)
            return;

        Core.flip(mInterimImage, mInterimImage, -1);
        updatePlanet();

    }

    public void fade(boolean isFaded) {

        mIsFaded = isFaded;

        if (!mIsImageLoaded)
            return;

        if (mIsPlanetInverted)
            Core.flip(mInputImage, mInterimImage, -1);
        else
            mInterimImage = mInputImage.clone();

        if (mCropRect != null) {
            Rect flippedRect = flipCropRect(mInterimImage.width(), mInterimImage.height());
            mInterimImage = mInterimImage.submat(flippedRect).clone();
        }


        if (mIsFaded)
            mInterimImage = getFadeImg(mInterimImage);

        updatePlanet();

    }

    public void setCropRect(RectF cropRect) {

        mCropRect = cropRect;

        if (mIsPlanetInverted)
            Core.flip(mInputImage, mInterimImage, -1);
        else
            mInterimImage = mInputImage.clone();

        if (mCropRect != null) {
//            Take care here, because the images are flipped after they have been loaded:
            Rect flippedRect = flipCropRect(mInterimImage.width(), mInterimImage.height());
            mInterimImage = mInterimImage.submat(flippedRect).clone();
        }

//        // mInterimImage is null if no cropping or invert is done yet:
//        if (mInterimImage == null)
//            mInterimImage = mInputImage.clone();

        if (mIsFaded)
            mInterimImage = getFadeImg(mInterimImage);


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

//        Mat fullResPlanet = new Mat();
//        Rotate the image 90 degrees:
        Core.flip(tmpInputImage.t(), tmpInputImage, 1);

        if (mIsPlanetInverted)
            Core.flip(tmpInputImage, tmpInputImage, -1);

        double fac = tmpInputImage.width() / (double) mInputImage.width();

//        ==============================================================
        if (mCropRect != null) {

//            Take care here, because the images are flipped after they have been loaded:
            Rect flippedRect = flipCropRect(tmpInputImage.width(), tmpInputImage.height());
            Mat image_roi = tmpInputImage.submat(flippedRect);
            tmpInputImage = image_roi.clone();
//            fullResPlanet = new Mat(tmpInputImage.rows(), tmpInputImage.cols(), mInputImage.type());
        }
//        ==============================================================

        if (mIsFaded) {
            tmpInputImage = getFadeImg(tmpInputImage);
        }


        Mat result = new Mat(tmpInputImage.cols(), tmpInputImage.cols(), tmpInputImage.type());
//        TODO: replace with the TaskCallback:
        mNativeWrapper.logPolar(tmpInputImage, result, tmpInputImage.width() * 0.5f, tmpInputImage.width() * 0.5f, mSize * fac, mScale, mAngle * DEG2RAD);
        tmpInputImage.release();

        return result;

    }

    private void setInitValues() {

        mSize = 250;
        mScale = 105;
        mAngle = 180;
        mCropRect = null;
        mIsFaded = false;

        mIsComputingPostponed = false;
        mIsComputing = false;

    }

    public void computePlanet() {
        updatePlanet();
    }

    private void updatePlanet() {

        if (!mIsImageLoaded)
            return;

        if (mIsComputing) {
            mIsComputingPostponed = true;
            return;
        }

        mIsComputingPostponed = false;

        // mInterimImage is used to store the result of the cropping and fading:
        if (mInterimImage == null)
            mInterimImage = mInputImage.clone();

        Log.d(CLASS_NAME, "mInterimImage.cols" + mInterimImage.cols());

        if (!mIsComputing)
            new PlanetTask().execute(mInterimImage);

    }

    private Mat getFadeImg(Mat interimImage) {

        int borderImgHeight = (int) Math.round(interimImage.cols() * .1);
        if ((borderImgHeight % 2) == 1)
            borderImgHeight++;

        Mat result = new Mat();
        mNativeWrapper.blendImgs(interimImage, result, borderImgHeight);

        return result;

    }

    private Rect flipCropRect(int imgW, int imgH) {

        int startX, startY, endX, endY;
        if (!mIsPlanetInverted) {
            startX = Math.round((1 - mCropRect.bottom) * imgH);
            endX = Math.round((1 - mCropRect.top) * imgH);
            startY = Math.round(mCropRect.left * imgW);
            endY = Math.round(mCropRect.right * imgW);
        }
        else {
            startX = Math.round(mCropRect.top * imgH);
            endX = Math.round(mCropRect.bottom * imgH);
            startY = Math.round((1 - mCropRect.right) * imgW);
            endY = Math.round((1 - mCropRect.left) * imgW);
        }

        startX  = (startX < 0) ? 0 : startX;
        startY  = (startY < 0) ? 0 : startY;
        endX    = (endX > imgW) ? imgW : endX;
        endY    = (endY > imgH) ? imgH : endY;

        Rect flippedRect = new Rect(startX, startY, endX - startX, endY - startY);

        return flippedRect;

    }


    private void initImages() {

        Imgproc.resize(mInputImage, mInputImage, new Size(mOutputSize, mOutputSize), 0, 0, Imgproc.INTER_CUBIC);


        Log.d(CLASS_NAME, "mOutputSize: " + mOutputSize);

//        Creates the planet and inverts it:
        Core.flip(mInputImage.t(), mInputImage, 1);

        if (mIsPlanetInverted)
            Core.flip(mInputImage, mInputImage, -1);

        mOutputImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
        mOutputImage = mInputImage.clone();
        mInterimImage = mInputImage.clone();

        if (mIsFaded)
            mInterimImage = getFadeImg(mInterimImage);

        updatePlanet();

    }

    public void setTaskCallBack(PlanetTaskCallBack taskCallBack) {
        mTaskCallBack = taskCallBack;
    }

    public void releaseImgs() {

        if (mOriginalImage != null)
            mOriginalImage.release();
        if (mInputImage != null)
            mInputImage.release();
        if (mInterimImage != null)
            mInterimImage.release();
        if (mOutputImage != null)
            mOutputImage.release();
        Log.d(CLASS_NAME, "releaseImgs: done");
    }

//    mOutputImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
//        mNativeWrapper.logPolar(mInterimImage, mOutputImage, mOutputImage.width() * 0.5f, mOutputImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);


    private class PlanetTask extends AsyncTask<Mat, Void, Mat> {

        @Override
        protected Mat doInBackground(Mat... mats) {

            mIsComputing = true;

            Log.d(CLASS_NAME, "PlanetTask: doInBackGround");
            Mat out = new Mat(mats[0].cols(), mats[0].cols(), mats[0].type());
            mNativeWrapper.logPolar(mats[0], out, out.width() * 0.5f, out.width() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
//            Mat out = mats[0];
            return out;

        }

        protected void onPostExecute(Mat result) {

            mTaskCallBack.onPlanetComputed(result);
            mIsComputing = false;

            if (mIsComputingPostponed)
                updatePlanet();

        }

    }

    public interface PlanetTaskCallBack {

        void onPlanetComputed(Mat mat);

    }

    public interface PlanetChangeCallBack {

        void onSizeChange(int size);
        void onScaleChange(int scale);
        void onAngleChange(int angle);
        void addAngle(float angle);
        void addScaleLog(float scaleLog);
        void onInvertChange(boolean isInverted);
        void onFadeChange(boolean isFaded);
        void onCrop(RectF rect);

    }




}
