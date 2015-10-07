package org.hofapps.tinyplanet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private PlanetChangeCallBack mPlanetChangeCallBacks;

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

        SeekBar sizeSeekBar = (SeekBar) view.findViewById(R.id.size_seekBar);
        SeekBar.OnSeekBarChangeListener listener = getSizeSeekBarListener();

        sizeSeekBar.setOnSeekBarChangeListener(listener);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mPlanetChangeCallBacks = (PlanetChangeCallBack) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SeekBarCallBacks.");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mPlanetChangeCallBacks = null;

    }


    private SeekBar.OnSeekBarChangeListener getSizeSeekBarListener() {

        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mPlanetChangeCallBacks.onSizeChange(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                mPlanetChangeCallBacks.onSizeChange();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                mPlanetChangeCallBacks.onSizeChange();

            }
        };

        return l;

    }


    public static interface PlanetChangeCallBack {

        void onSizeChange(int size);

    }




}
