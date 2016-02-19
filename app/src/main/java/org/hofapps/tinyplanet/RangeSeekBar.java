package org.hofapps.tinyplanet;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by fabian on 02.11.2015.
 */
public class RangeSeekBar extends SeekBar {

    private static final int ARRAY_MIN_POS = 0;
    private static final int ARRAY_MAX_POS = 1;

    private int[] range;
    private int id;

    public RangeSeekBar(final Context context) {
        super(context);
    }

    public RangeSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public RangeSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setRange(int[] range) {

        this.range = range;

    }

    public int getSeekBarValue() {

        int progress = getProgress();

        int value = (int) range[ARRAY_MIN_POS] + (getProgress() * (range[ARRAY_MAX_POS] - range[ARRAY_MIN_POS])) / 100;

        return value;

    }

    public void setValue(int value) {


        int pos;
        pos = (int) (value - range[ARRAY_MIN_POS]) * 100 / (range[ARRAY_MAX_POS] - range[ARRAY_MIN_POS]);

//        pos = (int) (value * 100 / (range[ARRAY_MAX_POS] - range[ARRAY_MIN_POS])) - range[ARRAY_MIN_POS];

        setProgress(pos);

    }






}
