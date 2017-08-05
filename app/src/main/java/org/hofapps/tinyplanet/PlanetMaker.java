package org.hofapps.tinyplanet;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
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
    private double mSize, mScale, mAngle;
    private RectF mCropRect;

    private int mFullOutputSize;
    private int[] mSizeMinMax;
    private boolean mIsImageLoaded, mIsPlanetInverted, mIsFaded;


    public PlanetMaker(NativeWrapper nativeWrapper,int outputSize, int[] sizeMinMax) {

        mNativeWrapper = nativeWrapper;
        mOutputSize = outputSize;
        mSizeMinMax = sizeMinMax;

        setInitValues();

        mIsImageLoaded = false;
        mIsPlanetInverted = false;

    }

    public void setInputImage(Bitmap bitmap) {

        if (bitmap == null)
            return;

        mIsImageLoaded = true;
        mInputImage = new Mat();

//        Check if the bitmap has the correct type for the OpenCV bitmapToMat function:
        if (bitmap.getConfig() != null) { // bitmap.getConfig() just returns a valid value if the format is in one of the public formats.
            if (bitmap.getConfig() != Bitmap.Config.ARGB_8888 && bitmap.getConfig() != Bitmap.Config.RGB_565)
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }

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

    public void fade(boolean isFaded) {

        mIsFaded = isFaded;

        if (!mIsImageLoaded)
            return;

        updatePlanet();

    }

    public void setCropRect(RectF cropRect) {

        mCropRect = cropRect;
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

//        ==============================================================
        if (mCropRect != null) {


//            Take care here, because the images are flipped after they have been loaded:
            Rect flippedRect = flipCropRect(tmpInputImage.width(), tmpInputImage.height());
            Mat image_roi = tmpInputImage.submat(flippedRect);

//            Imgproc.resize(image_roi, image_roi, new Size(mOutputSize, mOutputSize), 0, 0, Imgproc.INTER_CUBIC);

            tmpInputImage = image_roi.clone();
            fullResPlanet = new Mat(tmpInputImage.rows(), tmpInputImage.cols(), mInputImage.type());
//            mNativeWrapper.logPolar(image_roi, mPlanetImage, image_roi.width() * 0.5f, image_roi.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
//            mPlanetImage = mInputImage.clone();
        }
//        ==============================================================

        mNativeWrapper.logPolar(tmpInputImage, fullResPlanet, tmpInputImage.width() * 0.5f, tmpInputImage.height() * 0.5f, mSize * fac, mScale, mAngle * DEG2RAD);
        tmpInputImage.release();

        return fullResPlanet;

    }

    private void setInitValues() {

        mSize = 250;
        mScale = 105;
        mAngle = 180;
        mCropRect = null;

    }

    private void updatePlanet() {

        if (!mIsImageLoaded)
            return;
//
////        mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
////        mPlanetImage = mInputImage.clone();
////        mNativeWrapper.logPolar(mInputImage, mPlanetImage, mInputImage.width() * 0.5f, mInputImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
//        mNativeWrapper.blendImgs(mInputImage, mPlanetImage);


        if (mCropRect != null) {


//            Take care here, because the images are flipped after they have been loaded:
            Rect flippedRect = flipCropRect(mInputImage.width(), mInputImage.height());
            Mat image_roi = mInputImage.submat(flippedRect);

            mPlanetImage = new Mat(image_roi.rows(), image_roi.cols(), image_roi.type());
            mNativeWrapper.logPolar(image_roi, mPlanetImage, image_roi.width() * 0.5f, image_roi.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);

        }

        else {

            Mat tmp = new Mat();
            Imgproc.cvtColor(mInputImage, tmp, Imgproc.COLOR_RGBA2RGB);
            tmp.convertTo(tmp, CvType.CV_16SC3);
            Mat blendImg = new Mat(200, mInputImage.cols(), tmp.type());

            mNativeWrapper.blendImgs(tmp, blendImg);
            blendImg.convertTo(blendImg, mInputImage.type());
            Imgproc.cvtColor(blendImg, blendImg, Imgproc.COLOR_RGB2RGBA);

            mPlanetImage = mInputImage.clone();
//            mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());

            // Shift the input image to the bottom:
            Mat shiftedImg = new Mat();
            mPlanetImage.rowRange(100, mPlanetImage.rows()-100).
                    copyTo(shiftedImg);
            Mat subMat = mPlanetImage.submat(200, mPlanetImage.rows(), 0, mPlanetImage.cols());
            shiftedImg.copyTo(subMat);

            // Copy the blended part:
            Mat subMatBlended = mPlanetImage.submat(0,blendImg.rows(), 0, mPlanetImage.cols());
            blendImg.copyTo(subMatBlended);

            mNativeWrapper.logPolar(mPlanetImage, mPlanetImage, mPlanetImage.width() * 0.5f, mPlanetImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);
        }

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

        // TODO: uncomment:
//        mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
//        mNativeWrapper.logPolar(mInputImage, mPlanetImage, mInputImage.width() * 0.5f, mInputImage.height() * 0.5f, mSize, mScale, mAngle * DEG2RAD);

        mPlanetImage = new Mat(mInputImage.rows(), mInputImage.cols(), mInputImage.type());
        mPlanetImage = mInputImage.clone();
//        mNativeWrapper.blendImgs(mInputImage, mPlanetImage);

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
        void onFadeChange(boolean isFaded);
        void onCrop(RectF rect);

    }




}
