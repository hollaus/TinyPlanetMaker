package org.hofapps.tinyplanet;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SettingsFragment.PlanetChangeCallBack, View.OnTouchListener {

    private NativeWrapper nativeWrapper;
    private Mat originalImg, transformedImg;
    private ImageView imageView;
    private PlanetMaker previewPlanetMaker;
    private CoordinatorLayout coordinatorLayout;

    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String TAG = "Touch";
    // These matrices will be used to move and zoom image

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    float oldscale =0;
    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    static {
        System.loadLibrary("MyLib");
        System.loadLibrary("opencv_java");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        imageView.setOnTouchListener(this);

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




//            imageView.setOnTouchListener(gestureDetector);


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

                AssetFileDescriptor fileDescriptor;
                fileDescriptor = getContentResolver().openAssetFileDescriptor(uri, "r");


//                TODO: Find a better way to deal with large images!
                Bitmap bitmap = ImageReader.decodeSampledBitmapFromResource(getResources(), fileDescriptor, 1000, 1000);

                previewPlanetMaker.setInputImage(bitmap);

                updateImageView();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void updateImageView() {

        Mat previewImg = previewPlanetMaker.getPlanetImage();

//        File file = getOutputMediaFile();
//        boolean imgSaved = Highgui.imwrite(file.toString(), previewImg);

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


    // ============================================================================================

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        // ...
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d("test", sb.toString());
    }

    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);

    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;

        // Dump touch event to log
        dumpEvent(event);

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
//                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
//                if (Constant.TRACE) Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
//                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
//                if (Constant.TRACE) Log.d(TAG, "mode=ZOOM");

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
//                if (Constant.TRACE) Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {

                    float xDiff = event.getX() - start.x;
                    float yDiff = event.getY() - start.y;

                    float diff;

                    if (Math.abs(xDiff) > Math.abs(yDiff)) {
                        diff = xDiff;
                        if (event.getY() < imageView.getHeight() / 2)
                            diff *= -1;
                    }
                    else {
                        diff = yDiff;
                        if (event.getX() > imageView.getWidth() / 2)
                            diff *= -1;
                    }

                    float fac = .25f;
                    int iDiff = Math.round(diff * fac);

                    Log.d("Rotate", Integer.toString(iDiff));

                    int angle = (int) Math.round(previewPlanetMaker.getAngle()) + iDiff;

                    angle = angle % 360;


                    previewPlanetMaker.setAngle(angle);
                    updateImageView();


                } else if (mode == ZOOM && event.getPointerCount() == 2) {
                    float newDist = spacing(event);
//                    if (Constant.TRACE) Log.d(TAG, "Count=" + event.getPointerCount());
//                    if (Constant.TRACE) Log.d(TAG, "newDist=" + newDist);
//                    matrix.set(savedMatrix);
                    if (newDist > 10f) {
                        float scale = oldDist / newDist;

                        Log.d("Scale", Float.toString(scale));

                        previewPlanetMaker.setScale(previewPlanetMaker.getScale() * scale);
                        updateImageView();


//                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null) {
                        newRot = rotation(event);
//                        if (Constant.TRACE) Log.d("Degreeeeeeeeeee", "newRot=" + (newRot));
                        float r = newRot - d;
//                        matrix.postRotate(r, imgView.getMeasuredWidth() / 2, imgView.getMeasuredHeight() / 2);
                    }
                }
                break;
        }

//        view.setImageMatrix(matrix);
        return true; // indicate event was handled

    }

    /** Determine the degree between the first two fingers */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
//        if (Constant.TRACE) Log.d("Rotation ~~~~~~~~~~~~~~~~~", delta_x+" ## "+delta_y+" ## "+radians+" ## "
//                +Math.toDegrees(radians));
        return (float) Math.toDegrees(radians);
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
