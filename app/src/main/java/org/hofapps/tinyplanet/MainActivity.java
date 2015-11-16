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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements PlanetMaker.PlanetChangeCallBack {

    private NativeWrapper nativeWrapper;
    private Mat originalImg, transformedImg;
    private ImageView imageView;
    private PlanetMaker previewPlanetMaker;
    private CoordinatorLayout coordinatorLayout;

    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String TAG = "Touch";
    private MainActivityFragment mainActivityFragment;
    private OnPlanetTouchListener onPlanetTouchListener;
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


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.viewModeRadioButton:
                if (checked)
                    onPlanetTouchListener.setGestureMode(false);
                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
                    break;
            case R.id.editModeRadioButton:
                if (checked) {
                    onPlanetTouchListener.setGestureMode(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    break;
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        onPlanetTouchListener = new OnPlanetTouchListener(this);

        imageView.setOnTouchListener(onPlanetTouchListener);


        // TODO: Put this into MainActivityFragment and connect via callbacks

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.modeRadioGroup);
        radioGroup.check(R.id.editModeRadioButton);

//        /* Attach CheckedChangeListener to radio group */
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                RadioButton rb = (RadioButton) group.findViewById(checkedId);
//                if (null != rb && checkedId > -1) {
//                    Toast.makeText(MainActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

//        Switch gestureSwitch = (Switch) findViewById(R.id.gestureSwitch);
//
//        gestureSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//
//                if (isChecked) {
//                    onPlanetTouchListener.setGestureMode(true);
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                } else {
//                    onPlanetTouchListener.setGestureMode(false);
//                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
//                }
//
//            }
//        });

        nativeWrapper = new NativeWrapper();

        previewPlanetMaker = new PlanetMaker(nativeWrapper, 500);

        // TODO: Check why we need here getChildFragmentManager instead of getFragmentManager and set sdkMinVersion to 15 back!


        FragmentManager fragmentManager = getFragmentManager();
        mainActivityFragment = (MainActivityFragment) fragmentManager.findFragmentById(R.id.main_fragment);

        mainActivityFragment.initSeekBarValues((int) previewPlanetMaker.getSize(), (int) previewPlanetMaker.getScale(), (int) previewPlanetMaker.getAngle());

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

        Bitmap bm = Bitmap.createBitmap(previewImg.cols(), previewImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(previewImg, bm);

        imageView.setImageBitmap(bm);

        imageView.setScaleType(ImageView.ScaleType.MATRIX);

    }

    @Override
    public void onSizeChange(int size) {

        previewPlanetMaker.setSize((double) size);
        updateImageView();

    }

    @Override
    public void onScaleChange(int scale) {

        previewPlanetMaker.setScale((double) scale);
        updateImageView();

    }

    @Override
    public void onAngleChange(int angle) {

        previewPlanetMaker.setAngle((double) angle);
        updateImageView();

    }

    @Override
    public void addAngle(float angle) {

        previewPlanetMaker.addAngle(angle);
        updateImageView();

    }

    @Override
    public void addScale(float scale) {

        previewPlanetMaker.addScale(scale);
        updateImageView();

        mainActivityFragment.setScaleBarValue((int) previewPlanetMaker.getScale());

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
