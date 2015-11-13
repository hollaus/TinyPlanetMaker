package org.hofapps.tinyplanet;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

/**
 * Created by fabian on 14.10.2015.
 */
public class SettingsFragment extends Fragment {

    private PlanetMaker.PlanetChangeCallBack mPlanetChangeCallBacks;
    private static final int ARRAY_MIN_POS = 0;
    private static final int ARRAY_MAX_POS = 1;

    private RangeSeekBar sizeSeekBar, scaleSeekBar, angleSeekBar;

    public SettingsFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SeekBar.OnSeekBarChangeListener listener = getSeekBarListener();

        super.onResume();
        int[] array = getResources().getIntArray(R.array.size_seekbar_values);

        sizeSeekBar = (RangeSeekBar) view.findViewById(R.id.size_seekBar);
        sizeSeekBar.setRange(getResources().getIntArray(R.array.size_seekbar_values));
        sizeSeekBar.setOnSeekBarChangeListener(listener);

        scaleSeekBar = (RangeSeekBar) view.findViewById(R.id.scale_seekBar);
        scaleSeekBar.setRange(getResources().getIntArray(R.array.scale_seekbar_values));
        scaleSeekBar.setOnSeekBarChangeListener(listener);

        angleSeekBar = (RangeSeekBar) view.findViewById(R.id.angle_seekBar);
        angleSeekBar.setRange(getResources().getIntArray(R.array.angle_seekbar_values));
        angleSeekBar.setOnSeekBarChangeListener(listener);

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

    @Override
    public void onDetach() {

        super.onDetach();
        mPlanetChangeCallBacks = null;

    }

    public void initSeekBarValues(int size, int scale, int angle) {

        sizeSeekBar.setValue(size);
        scaleSeekBar.setValue(scale);
        angleSeekBar.setValue(angle);

    }

    public void setScaleBarValue(int position) {

//        TODO: Check if this triggers an event in MainActivity!
        scaleSeekBar.setValue(position);

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

                if (id == R.id.size_seekBar)
                    mPlanetChangeCallBacks.onSizeChange(value);
                else if (id == R.id.scale_seekBar)
                    mPlanetChangeCallBacks.onScaleChange(value);
                else if (id == R.id.angle_seekBar)
                    mPlanetChangeCallBacks.onAngleChange(value);


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

    private int getSeekBarValue(int seekBarId, int pos) {

        int value;

        int[] array = getResources().getIntArray(seekBarId);

        value = (int) (pos * (array[ARRAY_MAX_POS] - array[ARRAY_MIN_POS])) / 100;

        return value;

    }




}
