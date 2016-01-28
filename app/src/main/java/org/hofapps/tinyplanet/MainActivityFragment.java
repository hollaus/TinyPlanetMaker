package org.hofapps.tinyplanet;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    //    private PlanetChangeCallBack mPlanetChangeCallBacks;
//    private CoordinatorLayout coordinatorLayout;
    //    private SettingsFragment settingsFragment;
    private TabFragment tabFragment;
    FragmentManager fragmentManager;
    private ImageView imageView;

    private FloatingActionButton fab;
    private Drawable closeIcon;
    private Drawable menuIcon;

//    private static final int ARRAY_MIN_POS = 0;
//    private static final int ARRAY_MAX_POS = 1;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);


//        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);

        // TODO: Check why we need here getChildFragmentManager instead of getFragmentManager and set sdkMinVersion to 15 back!

        imageView = (ImageView) view.findViewById(R.id.imageView);


        fragmentManager = getChildFragmentManager();
//        settingsFragment = (SettingsFragment) fragmentManager.findFragmentById(R.id.settings_fragment);
        tabFragment = (TabFragment) fragmentManager.findFragmentById(R.id.tab_fragment);

        closeIcon = getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp);
        menuIcon = getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp);

    }

//                if (settingsFragment.isHidden()) {
//
//                    fab.setImageDrawable(closeIcon);
//
//                    fragmentManager.beginTransaction().setCustomAnimations(
//                            R.animator.slide_up,
//                            R.animator.slide_down,
//                            R.animator.slide_up,
//                            R.animator.slide_down).show(settingsFragment).commit();
//
//
////                    Animation animation = new ScaleAnimation(1f, .9f, 1f, .9f, imageView.getPivotX(), imageView.getPivotY());
////                    animation.setFillAfter(true);
////                    animation.setDuration(20);
////                    imageView.startAnimation(animation);
//
//
//
//                    float y = imageView.getY();
//
//                } else {
//
//                    fab.setImageDrawable(menuIcon);
//
//                    fragmentManager.beginTransaction().setCustomAnimations(
//                            R.animator.slide_up,
//                            R.animator.slide_down,
//                            R.animator.slide_up,
//                            R.animator.slide_down
//                    ).hide(settingsFragment).commit();


//                    Animation animation = new ScaleAnimation(.9f, 1f, .9f, 1f, imageView.getPivotX(), imageView.getPivotY());
//                    animation.setFillAfter(true);
//
//                    // config_mediumAnimTime
//                    animation.setDuration(20);
//                    imageView.startAnimation(animation);
//
//                }
//
//


    public void initSeekBarValues(int size, int scale, int angle) {

        tabFragment.initSeekBarValues(size, scale, angle);

    }

    public void setSizeBarValue(int position) {

        tabFragment.setWarpBarValue(position);

    }

    public void setAngleBarValue(int position) {

        tabFragment.setRotateBarValue(position);

    }

    public void setZoomBarValue(int position) {

        tabFragment.setZoomBarValue(position);

    }
}




//    public void updatePlanetSize(int size) {
//
//        settingsFragment.updateSizeSeekBar(size);
//
//    }


//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mPlanetChangeCallBacks = (PlanetChangeCallBack) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Activity must implement SeekBarCallBacks.");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//
//        super.onDetach();
//        mPlanetChangeCallBacks = null;
//
//    }


//    private SeekBar.OnSeekBarChangeListener getSeekBarListener() {
//
//        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//                int id = seekBar.getId();
//
//                int value;
//
//                if (id == R.id.size_seekBar) {
//
//                    value = getSeekBarValue(R.array.size_seekbar_values, i);
//                    mPlanetChangeCallBacks.onSizeChange(value);
//                }
//                else if (id == R.id.scale_seekBar) {
//
//                    value = getSeekBarValue(R.array.scale_seekbar_values, i);
//                    mPlanetChangeCallBacks.onScaleChange(value);
//
//                }
//                else if (id == R.id.angle_seekBar) {
//
//                    value = getSeekBarValue(R.array.angle_seekbar_values, i);
//                    mPlanetChangeCallBacks.onAngleChange(value);
//
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        };
//
//        return l;
//
//    }

//    private int getSeekBarValue(int seekBarId, int pos) {
//
//        int value;
//
//        int[] array = getResources().getIntArray(seekBarId);
//
//        value = (int) (pos * (array[ARRAY_MAX_POS] - array[ARRAY_MIN_POS])) / 100;
//
//        return value;
//
//    }

//    private SeekBar.OnSeekBarChangeListener getScaleSeekBarListener() {
//
//        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//                int sId = seekBar.getId();
//                int scaleId = R.id.scale_seekBar;
//
//                mPlanetChangeCallBacks.onScaleChange(i);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
////                mPlanetChangeCallBacks.onSizeChange();
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
////                mPlanetChangeCallBacks.onSizeChange();
//
//            }
//        };
//
//        return l;
//
//    }


//    public static interface PlanetChangeCallBack {
//
//        void onSizeChange(int size);
//        void onScaleChange(int scale);
//        void onAngleChange(int angle);
//    }


//
//    @Override
//    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
//        final int animatorId = (enter) ? R.animator.fade_in : R.animator.fade_out;
//        final Animator anim = AnimatorInflater.loadAnimator(getActivity(), animatorId);
//        anim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//
//            }
//        });
//
//        return anim;
//
//    }
//
//}
