package org.hofapps.tinyplanet;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SettingsFragment.PlanetChangeCallBack {

    private NativeWrapper nativeWrapper;
    private Mat originalImg, transformedImg;
    private ImageView imageView;
    private PlanetMaker previewPlanetMaker;
    private CoordinatorLayout coordinatorLayout;

    private static final int PICK_IMAGE_REQUEST = 1;


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

//        FragmentManager fragmentManager = getFragmentManager();
//        SettingsFragment settingsFragment = (SettingsFragment) fragmentManager.findFragmentById(R.id.settings_fragment);

        int a = 0;

//        try {


//            originalImg = Utils.loadResource(MainActivity.this, R.drawable.nancy, Highgui.CV_LOAD_IMAGE_ANYCOLOR);
//
//            previewPlanetMaker = new PlanetMaker(originalImg, nativeWrapper, 500);

            previewPlanetMaker = new PlanetMaker(nativeWrapper, 500);

            // TODO: Check why we need here getChildFragmentManager instead of getFragmentManager and set sdkMinVersion to 15 back!


            FragmentManager fragmentManager = getFragmentManager();
            MainActivityFragment mainActivityFragment = (MainActivityFragment) fragmentManager.findFragmentById(R.id.main_fragment);

            mainActivityFragment.initSeekBarValues((int) previewPlanetMaker.getSize(), (int) previewPlanetMaker.getScale(), (int) previewPlanetMaker.getAngle());

//            mainActivityFragment.updatePlanetSize(R.integer.planet_size);
//
//            int size = (int) previewPlanetMaker.getSize();

//            mainActivityFragment.updatePlanetSize(size);

//        } catch (IOException exception) {
//
//        }

//        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
//
//        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                View coordinatorLayoutView = findViewById(R.id.slidingLayout);
//                Snackbar.make(coordinatorLayout, "snackbar test", Snackbar.LENGTH_LONG).show();
////                Snackbar.make(view, "Hello Snackbar", Snackbar.LENGTH_LONG).show();
//            }
//        });


    }


    @Override
    public void onResume() {

        super.onResume();

        FragmentManager fragmentManager = getFragmentManager();
        MainActivityFragment mainActivityFragment = (MainActivityFragment) fragmentManager.findFragmentById(R.id.main_fragment);


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

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_open_file) {

            Intent intent = new Intent();
// Show only images, no videos or anything else
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                previewPlanetMaker.setInputImage(bitmap);
                // Log.d(TAG, String.valueOf(bitmap));

//                ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                imageView.setImageBitmap(bitmap);

                updateImageView();

//                previewPlanetMaker.setInputImage(originalImg);

            } catch (IOException e) {
                e.printStackTrace();
            }

//            String path = uri.getPath();
//            originalImg = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_ANYCOLOR);
//
//            previewPlanetMaker.setInputImage(originalImg);
//            updateImageView();

        }
    }

    private void updateImageView() {

        Mat previewImg = previewPlanetMaker.getPlanetImage();

        File file = getOutputMediaFile();
        boolean imgSaved = Highgui.imwrite(file.toString(), previewImg);

        Bitmap bm = Bitmap.createBitmap(previewImg.cols(), previewImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(previewImg, bm);

        imageView.setImageBitmap(bm);

    }

    @Override
    public void onSizeChange(int size) {

        previewPlanetMaker.setSize((double) size * 10);
        updateImageView();

    }

    @Override
    public void onScaleChange(int scale) {

        previewPlanetMaker.setScale((double) scale * 30);
        updateImageView();

    }

    @Override
    public void onAngleChange(int angle) {

        previewPlanetMaker.setAngle((double) angle);
        updateImageView();

    }


    public static interface PlanetInitCallBack {

        void onInit(int size, int scale, int angle);

    }

//    Debugging methods:

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TinyPlanet");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//	            Log.d("TinyPlanet", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;

    }

}
