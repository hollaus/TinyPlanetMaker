package org.hofapps.tinyplanet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements PlanetMaker.PlanetChangeCallBack,
        MediaScannerConnectionClient, SettingsFragment.FragmentVisibilityCallBack,
        ActivityCompat.OnRequestPermissionsResultCallback, SamplesFragment.SampleSelectedCallBack {
//public class MainActivity extends ActionBarActivity implements PlanetMaker.PlanetChangeCallBack {

    private NativeWrapper nativeWrapper;
    private Mat originalImg, transformedImg;
    private ImageView imageView;
    private PlanetMaker previewPlanetMaker;
    private CoordinatorLayout coordinatorLayout;
    private int[] sizeMinMax;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    private GesturesDialogFragment gestureFragment;
    private SamplesFragment samplesFragment;
    private TabFragment tabFragment;

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE= 2;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE= 1;

    private static final int PICK_IMAGE_REQUEST = 1;

    public static final int MAX_IMG_SIZE = 3000;

    private static final int MENU_ITEM_GALLERY = 0;
    private static final int MENU_ITEM_SHARE = 1;

    private int menuItem = -1;

    private static final String TAG = "Touch";
//    private MainActivityFragment mainActivityFragment;
    private OnPlanetTouchListener onPlanetTouchListener;
    private MediaScannerConnection mediaScannerConnection;
    private LinearLayout settingsTitle;

    private Context context;

    static {

        // It seems like we need this for Android 4:
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        } else {
            System.loadLibrary("wrapper");
            System.loadLibrary("opencv_java");
        }

    }


//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.viewModeRadioButton:
//                if (checked)
//                    onPlanetTouchListener.setGestureMode(false);
//                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
//                    break;
//            case R.id.editModeRadioButton:
//                if (checked) {
//                    onPlanetTouchListener.setGestureMode(true);
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                    break;
//                }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        onPlanetTouchListener = new OnPlanetTouchListener(this);

        imageView.setOnTouchListener(onPlanetTouchListener);


//        Navigation drawer based on this tutorial:
//        https://guides.codepath.com/android/Fragment-Navigation-Drawer

//        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, mPlanetTitles));

//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        mTitle = "title";
        mDrawerTitle = getTitle();

//        TODO: replace R.drawable.ic_action.name with drawer icon
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

//            /** Called when a drawer has settled in a completely closed state. */
//            public void onDrawerClosed(View view) {
//                super.onDrawerClosed(view);
//                getSupportActionBar().setTitle(mTitle);
//            }
//
//            /** Called when a drawer has settled in a completely open state. */
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                getSupportActionBar().setTitle(mDrawerTitle);
//            }


        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavigationView mDrawer = (NavigationView) findViewById(R.id.left_drawer);
        setupDrawerContent(mDrawer);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);



        nativeWrapper = new NativeWrapper();

        sizeMinMax = getResources().getIntArray(R.array.size_seekbar_values);

        previewPlanetMaker = new PlanetMaker(nativeWrapper, 700, sizeMinMax);

        FragmentManager fragmentManager = getFragmentManager();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            tabFragment = (TabFragment) fragmentManager.findFragmentById(R.id.settings_fragment_land);
        else
            tabFragment = (TabFragment) fragmentManager.findFragmentById(R.id.settings_fragment);

        tabFragment.initSeekBarValues((int) previewPlanetMaker.getSize(), (int) previewPlanetMaker.getScale(), (int) previewPlanetMaker.getAngle());

        Intent intent = getIntent();

        // Did the user intended to open an image?
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {

            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                FileLoader fileLoader = new FileLoader(this);
                fileLoader.execute(imageUri);
            }


        } else {
            SharedPreferences pref = getSharedPreferences("org.hofapps.tinyplanet", Context.MODE_PRIVATE);

            if (pref.getBoolean("firstRun", true))
                showFirstTimeDialog();

            pref.edit().putBoolean("firstRun", false).commit();
        }

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;





        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            FileLoader fileLoader = new FileLoader(this);
            fileLoader.execute(uri);

        }
    }

    private void updateImageView() {

        if (!previewPlanetMaker.getIsImageLoaded())
            return;

        Mat previewImg = previewPlanetMaker.getPlanetImage();

        Bitmap bm = Bitmap.createBitmap(previewImg.cols(), previewImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(previewImg, bm);

        imageView.setImageBitmap(bm);

        previewPlanetMaker.releasePlanetImage();

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

//        if (angle > angleMinMax[SettingsFragment.ARRAY_MAX_POS])
//            angle = angleMinMax[SettingsFragment.ARRAY_MIN_POS];
//        else if (angle < angleMinMax[SettingsFragment.ARRAY_MIN_POS])
//            angle = angleMinMax[SettingsFragment.ARRAY_MAX_POS];

        previewPlanetMaker.addAngle(angle);
        updateImageView();

        tabFragment.setRotateBarValue((int) previewPlanetMaker.getAngle());

    }

    @Override
    public void addScaleLog(float scale) {

        previewPlanetMaker.addScale(scale);
        updateImageView();

        tabFragment.setWarpBarValue((int) previewPlanetMaker.getSize());

    }

    @Override
    public void onInvertChange(boolean isInverted) {

        previewPlanetMaker.invert(isInverted);
        updateImageView();

    }

    @Override
    public void onCropLeftChange(int cropLeft) {

        previewPlanetMaker.setCropLeft(cropLeft);
        updateImageView();

    }

    @Override
    public void onCropRightChange(int cropRight) {

        previewPlanetMaker.setCropRight(cropRight);
        updateImageView();

    }

    @Override
    public void onVisibilityChange() {

        float y = settingsTitle.getY();

        LayoutParams params = imageView.getLayoutParams();
        params.height = (int) y;

    }

    @Override
    public void onSampleSelected(int id) {

        int imageId = -1;

        switch (id) {
            case R.id.vienna_imagebutton:
                imageId = R.drawable.vienna_1000;
                break;
            case R.id.rome_imagebutton:
                imageId = R.drawable.rome_1000;
                break;
            case R.id.nancy_imagebutton:
                imageId = R.drawable.nancy_1000;
                break;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId);

        previewPlanetMaker.setInputImage(bitmap);
        updateImageView();

        checkFirstTimeImageOpen();

    }

    public static interface PlanetInitCallBack {

        void onInit(int size, int scale, int angle);

    }

    private void resetPlanetValues() {

        previewPlanetMaker.reset();

        tabFragment.setRotateBarValue((int) previewPlanetMaker.getAngle());
        tabFragment.setWarpBarValue((int) previewPlanetMaker.getSize());
        tabFragment.setZoomBarValue((int) previewPlanetMaker.getScale());
        tabFragment.setInvertPlanetSwitch(previewPlanetMaker.getIsPlanetInverted());

        updateImageView();

    }

    private void openGallery() {

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M)
            requestFileOpen();
        else
            startScan(MENU_ITEM_GALLERY);

    }

    @Override
    public void onMediaScannerConnected() {

        File mediaStorageDir = getMediaStorageDir();

        if (mediaStorageDir == null) {

            showNoFileFoundDialog();
            return;

        }

        String[] files = mediaStorageDir.list();

        if (files == null) {

            showNoFileFoundDialog();
            return;

        }
        else if (files.length == 0) {

            showNoFileFoundDialog();
            return;

        }

        //	    Opens the most recent image:
        Arrays.sort(files);

        String fileName = mediaStorageDir.toString() + "/" + files[files.length - 1];


        mediaScannerConnection.scanFile(fileName, null);
    }


    private void setupDrawerContent(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override

                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        selectDrawerItem(menuItem);

                        return true;

                    }

                });

    }

    private void selectDrawerItem(MenuItem menuItem) {

        switch(menuItem.getItemId()) {

            case R.id.action_share:
                openGallery();
                break;

            case R.id.action_about:
                showAboutDialog();
                break;

            case R.id.action_reset:
                resetPlanetValues();
                break;

            case R.id.action_samples:
                showSamplesFragment();
                break;

            case R.id.action_help:
                showGestureFragment();
                break;


        }

        mDrawerLayout.closeDrawers();

    }

    private void showGestureFragment() {

        if (gestureFragment == null)
            gestureFragment = new GesturesDialogFragment();

        gestureFragment.show(getFragmentManager(), "gesture_fragment");

    }

    private void showSamplesFragment() {

        if (samplesFragment == null)
            samplesFragment = new SamplesFragment();

        samplesFragment.show(getFragmentManager(), "samples_fragment");

    }


    private void showNoFileFoundDialog() {

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.no_file_found_msg).setTitle(R.string.no_file_found_title);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();

    }

    private void checkFirstTimeImageOpen() {

        SharedPreferences pref = getSharedPreferences("org.hofapps.tinyplanet", Context.MODE_PRIVATE);

        if (pref.getBoolean("firstImageOpen", true))
            showGestureFragment();

        pref.edit().putBoolean("firstImageOpen", false).commit();

    }

    private void startScan(int menuItem) {

        this.menuItem = menuItem;

        if(mediaScannerConnection != null)
            mediaScannerConnection.disconnect();


        mediaScannerConnection = new MediaScannerConnection(this, this);
        mediaScannerConnection.connect();

    }

    @Override
    public void onScanCompleted(String path, Uri uri) {

        try {


            if (uri != null) {
                if (menuItem == MENU_ITEM_GALLERY) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    I do not know why setData(uri) is not working with Marshmallows, it just opens one image (not the folder), with setData(Uri.fromFile) it is working:

                    int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentApiVersion >= Build.VERSION_CODES.M)
                        intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
                    else
                        intent.setData(uri);
//

                    startActivity(intent);

                }
                else if (menuItem == MENU_ITEM_SHARE) {
                    //
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("*/*");

                    //        	    TODO: use R.string.text instead of hard-coded string. Do not know why an exception is thrown here:
                    //        	    String shareText = (String) getString(R.string.share_text);
                    //        	    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);


                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(shareIntent);
                }


            }
        }
        finally {
            mediaScannerConnection.disconnect();
            mediaScannerConnection = null;
        }

    }


    // This method is used to enable file saving in marshmallow (Android 6), since in this version file saving is not allowed without user permission:
    private void requestFileSave() {

        // Check Permissions Now
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission:
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
        else
            saveFile();

    }

    // This method is used to enable file saving in marshmallow (Android 6), since in this version external file opening is not allowed without user permission:
    private void requestFileOpen() {

        // Check Permissions Now
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission:
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        else
            startScan(MENU_ITEM_GALLERY);

    }


    private void saveFile() {


        Uri uri = getOutputMediaFile();

        if (uri != null) {

            FileSaver fileSaver = new FileSaver(this);
            fileSaver.execute(uri);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveFile();
            } else {
                showNoSavingPermissionDialog();
            }
        }

        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan(MENU_ITEM_GALLERY);;
            } else {
                showNoReadingPermissionDialog();
            }

        }
    }


    private Uri getOutputMediaFile() {

        // Check if we can access the external storage:
        if (!isExternalStorageWritable()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.external_storage_not_accessible_msg)
                    .setTitle(R.string.external_storage_not_accessible_title);

            AlertDialog dialog = builder.create();

            dialog.show();

            return null;


        }

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        File mediaStorageDir = getMediaStorageDir();
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

        Uri uri = Uri.fromFile(mediaFile);

        return uri;

    }

    private File getMediaStorageDir() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TinyPlanetMaker");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
//	            Log.d("Zelfie", "failed to create directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }

    // cannot save image dialog:

    private void showNoSavingPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_saving_permission_msg).setTitle(R.string.no_permission_title);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showNoReadingPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_reading_permission_msg).setTitle(R.string.no_permission_title);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    // About dialog:

    private void showAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about_msg).setTitle(R.string.action_help_about_title);
//        builder.setIcon(R.drawable.icon_small);


        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
//        dialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, R.drawable.tiny_planet_gray_300px);
        dialog.setIcon(R.drawable.icon_small);
        dialog.show();

    }

    private void showFirstTimeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.firsttime_dialog_msg).setTitle(R.string.firsttime_dialog_title);
//        builder.setIcon(R.drawable.icon_small);

        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                showSamplesFragment();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });


        AlertDialog dialog = builder.create();
//        dialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, R.drawable.tiny_planet_gray_300px);
//        dialog.setIcon(R.drawable.icon_small);
        dialog.show();

    }

    // ==================================== File Handler Classes ====================================

    private abstract class FileHandler extends AsyncTask<Uri, Void, Void> {


        protected Context context;
        protected String spinnerText;
        private ProgressDialog progressDialog;

        public FileHandler() {

        }

        public FileHandler(Context context)
        {
            this.context = context;
        }

//        protected abstract void performTask();

        protected void onPreExecute() {

            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(spinnerText);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected void onPostExecute(Void dummy) {
            // The Void dummy argument is necessary so that onPostExecute gets called.
            progressDialog.dismiss();
        }

    }

    private class FileLoader extends FileHandler {


        public FileLoader(Context context) {

            super(context);
            // TODO: check why this is necessary:
            this.context = context;
            spinnerText = getResources().getString(R.string.file_load_text);

        }

        protected Void doInBackground(Uri... uris) {

            try {

                AssetFileDescriptor fileDescriptor;
                fileDescriptor = getContentResolver().openAssetFileDescriptor(uris[0], "r");

                Bitmap bitmap = ImageReader.decodeSampledBitmapFromResource(getResources(), fileDescriptor, MAX_IMG_SIZE, MAX_IMG_SIZE);
                previewPlanetMaker.setInputImage(bitmap);

                // The image view cannot be touched inside the thread because it has been created on the UI thread.
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // The image view is initialized with a fixed height in order to show the 'gray planet' in a nice manner. Now we need to undo this initialization:
//                        imageView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
                        updateImageView();

                        checkFirstTimeImageOpen();


                    }

                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }

            return null;

        }



    }

    private class FileSaver extends FileHandler {

        public FileSaver(Context context) {

            super(context);
            // TODO: check why this is necessary:
            this.context = context;

            spinnerText = getResources().getString(R.string.file_save_text);

        }

        @Override
        protected Void doInBackground(Uri... uris) {

            if (previewPlanetMaker == null)
                return null;

            Mat planet = previewPlanetMaker.getFullResPlanet();

            if (planet == null) {
                return null;
            }

            Imgproc.cvtColor(planet, planet, Imgproc.COLOR_BGR2RGB);

            final File outFile = new File(uris[0].getPath());

            boolean imgSaved = Highgui.imwrite(outFile.toString(), planet);

            planet.release();

            if (imgSaved) {

                 MediaScannerConnection.scanFile(context,
                        new String[]{outFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {

                            public void onScanCompleted(String path, Uri uri) {


                            }
                });
            }



            return null;


        }


    }


}
