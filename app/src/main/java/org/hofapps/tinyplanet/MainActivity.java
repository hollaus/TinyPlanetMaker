package org.hofapps.tinyplanet;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.PlanetChangeCallBack {

    private NativeWrapper nativeWrapper;
    private Mat originalImg, transformedImg;
    private ImageView imageView;
    private PlanetMaker previewPlanetMaker;

    static {
        System.loadLibrary("MyLib");
        System.loadLibrary("opencv_java");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        nativeWrapper = new NativeWrapper();

        try {

            originalImg = Utils.loadResource(MainActivity.this, R.drawable.nancy, Highgui.CV_LOAD_IMAGE_ANYCOLOR);

            previewPlanetMaker = new PlanetMaker(originalImg, nativeWrapper, 500);
//            int maxLength = 1000;
//            double scaleFac;
//            if (originalImg.width() > originalImg.height())
//                scaleFac = (double) maxLength / originalImg.width();
//
//            else
//                scaleFac = (double) maxLength / originalImg.height();
//
////            Imgproc.resize(originalImg, originalImg, new Size(), scaleFac, scaleFac, Imgproc.INTER_CUBIC);
//
//            Imgproc.resize(originalImg, originalImg, new Size(1000, 1000),0, 0, Imgproc.INTER_CUBIC);
//
//            Core.flip(originalImg.t(), originalImg, 1);
//
////            We need COLOR_BGR2RGBA to flip the color channel AND to get a transparent background:
//            Imgproc.cvtColor(originalImg, originalImg, Imgproc.COLOR_BGR2RGBA);
//            transformedImg = new Mat(originalImg.rows(), originalImg.cols(), originalImg.type());
//
////            NativeWrapper wrapper = new NativeWrapper();
//            nativeWrapper.logPolar(originalImg, transformedImg, 200, 200, 30, 300, 0);
//
//
////            Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
////            Utils.matToBitmap(m, bm);
//
//            Bitmap bm = Bitmap.createBitmap(transformedImg.cols(), transformedImg.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(transformedImg, bm);
//
//
//            // find the imageview and draw it!
//            imageView = (ImageView) findViewById(R.id.imageView);
//            imageView.setImageBitmap(bm);

        }
        catch (IOException exception) {

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateImageView() {

        Mat previewImg = previewPlanetMaker.getPlanetImage();

        Bitmap bm = Bitmap.createBitmap(previewImg.cols(), previewImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(previewImg, bm);

        imageView.setImageBitmap(bm);

    }

    @Override
    public void onSizeChange(int size) {

        previewPlanetMaker.setSize((double) size);
//        nativeWrapper.logPolar(originalImg, transformedImg, originalImg.width() * 0.5f, originalImg.height() * 0.5f, (double) size, 300, 0);
        updateImageView();

    }
}
