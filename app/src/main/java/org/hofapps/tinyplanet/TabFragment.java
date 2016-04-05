package org.hofapps.tinyplanet;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

public class TabFragment extends Fragment {

    private TabHost mTabHost;
    private PlanetMaker.PlanetChangeCallBack mPlanetChangeCallBacks;
    private RangeSeekBar rotateSeekBar, warpSeekBar, zoomSeekBar;
    private android.support.v7.widget.SwitchCompat invertSwitch;


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView;

        final SeekBar.OnSeekBarChangeListener listener = getSeekBarListener();

        // Get the orientation of the layout:

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            rootView = inflater.inflate(R.layout.fragment_settings_land, container, false);

            warpSeekBar = (RangeSeekBar) rootView.findViewById(R.id.warp_seekBar);
            warpSeekBar.setRange(getResources().getIntArray(R.array.size_seekbar_values));
            warpSeekBar.setOnSeekBarChangeListener(listener);

            rotateSeekBar = (RangeSeekBar) rootView.findViewById(R.id.rotate_seekBar);
            rotateSeekBar.setRange(getResources().getIntArray(R.array.angle_seekbar_values));
            rotateSeekBar.setOnSeekBarChangeListener(listener);

            zoomSeekBar = (RangeSeekBar) rootView.findViewById(R.id.zoom_seekBar);
            zoomSeekBar.setRange(getResources().getIntArray(R.array.zoom_seekbar_values));
            zoomSeekBar.setOnSeekBarChangeListener(listener);

            invertSwitch = (android.support.v7.widget.SwitchCompat) rootView.findViewById(R.id.invert_switch);
            enableInvertSwitchListener();

        }
        else {


            rootView = inflater.inflate(R.layout.tablayout_settings, container, false);




            mTabHost = (TabHost) rootView.findViewById(R.id.tab_host);
            mTabHost.setup();


//        TabHost.TabSpec spec = mTabHost.newTabSpec("tag");
//        spec.setIndicator("Warp");

            TabHost.TabSpec spec = mTabHost.newTabSpec("tag0");
            spec.setIndicator(createTabView(inflater, container, "Warp"));

            spec.setContent(new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {

                    View view = inflater.inflate(R.layout.fragment_warp, container, false);

                    warpSeekBar = (RangeSeekBar) view.findViewById(R.id.warp_seekBar);
                    warpSeekBar.setRange(getResources().getIntArray(R.array.size_seekbar_values));
                    warpSeekBar.setOnSeekBarChangeListener(listener);

                    return (view);
                }
            });
            mTabHost.addTab(spec);


//        spec = mTabHost.newTabSpec("tag1");
//        spec.setIndicator("Rotate");

            spec = mTabHost.newTabSpec("tag1");
            spec.setIndicator(createTabView(inflater, container, "Rotate"));

            spec.setContent(new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {

                    View view = inflater.inflate(R.layout.fragment_rotate, container, false);

                    rotateSeekBar = (RangeSeekBar) view.findViewById(R.id.rotate_seekBar);
                    rotateSeekBar.setRange(getResources().getIntArray(R.array.angle_seekbar_values));
                    rotateSeekBar.setOnSeekBarChangeListener(listener);

                    return (view);

                }
            });
            mTabHost.addTab(spec);

//        spec = mTabHost.newTabSpec("tag2");
//        spec.setIndicator("Zoom");

            spec = mTabHost.newTabSpec("tag2");
            spec.setIndicator(createTabView(inflater, container, "Zoom"));

            spec.setContent(new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {

                    View view = inflater.inflate(R.layout.fragment_zoom, container, false);

                    zoomSeekBar = (RangeSeekBar) view.findViewById(R.id.zoom_seekBar);
                    zoomSeekBar.setRange(getResources().getIntArray(R.array.zoom_seekbar_values));
                    zoomSeekBar.setOnSeekBarChangeListener(listener);

                    return (view);

                }
            });

            mTabHost.addTab(spec);

            spec = mTabHost.newTabSpec("tag3");
            spec.setIndicator(createTabView(inflater, container, "Invert"));

            spec.setContent(new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {

                    View view = inflater.inflate(R.layout.fragment_invert, container, false);

                    invertSwitch = (android.support.v7.widget.SwitchCompat) view.findViewById(R.id.invert_switch);
                    enableInvertSwitchListener();

                    return (view);

                }
            });

            mTabHost.addTab(spec);

            mTabHost.setOnTabChangedListener(new AnimatedTabHostListener(mTabHost));


            // Initialize the tabs. Otherwise they will be initialized after the user clicks on them, but we need to connect the sliders to the callback:
            for (int i = mTabHost.getTabWidget().getTabCount() - 1; i >= 0; i--)
                mTabHost.setCurrentTab(i);

        }

        return rootView;

    }

    private void enableInvertSwitchListener() {
        invertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPlanetChangeCallBacks.onInvertChange(isChecked);
            }
        });
    }


    public View createTabView(final LayoutInflater inflater, final ViewGroup container, final String text) {
//        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout,
//                null);
        View view = inflater.inflate(R.layout.tab_layout, container, false);

        TextView tv = (TextView) view.findViewById(R.id.tab_text_view);
        tv.setText(text);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            mPlanetChangeCallBacks = (PlanetMaker.PlanetChangeCallBack) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement PlanetChangeCallBacks.");
        }

    }



    private SeekBar.OnSeekBarChangeListener getSeekBarListener() {

        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {

                if (!fromUser)
                    return;

                int id = seekBar.getId();

                int value;

                value = ((RangeSeekBar) seekBar).getSeekBarValue();

                if (id == R.id.warp_seekBar) {

                    if (value == 0)
                        value = 1;

                    mPlanetChangeCallBacks.onSizeChange(value);

                }
//                else if (id == R.id.scale_seekBar)
//                    mPlanetChangeCallBacks.onScaleChange(value);
                else if (id == R.id.rotate_seekBar)
                    mPlanetChangeCallBacks.onAngleChange(value);

                else if (id == R.id.zoom_seekBar) {

                    if (value == 0)
                        value = 1;

                    mPlanetChangeCallBacks.onScaleChange(value);

                }



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        return l;

    }

    public void setRotateBarValue(int position) {

        rotateSeekBar.setValue(position);

    }

    public void setZoomBarValue(int position) {

        zoomSeekBar.setValue(position);

    }


    public void setWarpBarValue(int position) {

        warpSeekBar.setValue(position);

    }

    public void setInvertPlanetSwitch(boolean isInverted) {

        // TODO: Check why we have to disable the listener. If it is enabled the preview image is null.
        invertSwitch.setOnCheckedChangeListener(null);
        invertSwitch.setChecked(isInverted);
        enableInvertSwitchListener();


    }

    public void initSeekBarValues(int size, int scale, int angle) {

        warpSeekBar.setValue(size);
        rotateSeekBar.setValue(angle);
        zoomSeekBar.setValue(scale);

    }



}