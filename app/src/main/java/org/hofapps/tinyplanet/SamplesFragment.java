package org.hofapps.tinyplanet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by fabian on 11.02.2016.
 */
public class SamplesFragment extends DialogFragment {

    private SampleSelectedCallBack mSampleSelectedCallBack;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.samples_fragment_title);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.samples_fragment, container, false);

        View.OnClickListener l = getOnClickListener();

        ImageButton button1 = (ImageButton) v.findViewById(R.id.vienna_imagebutton);
        button1.setOnClickListener(l);

        ImageButton button2 = (ImageButton) v.findViewById(R.id.rome_imagebutton);
        button2.setOnClickListener(l);

        ImageButton button3 = (ImageButton) v.findViewById(R.id.nancy_imagebutton);
        button3.setOnClickListener(l);

        Button cancelButton = (Button) v.findViewById(R.id.cancel_samples_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            mSampleSelectedCallBack = (SampleSelectedCallBack) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SampleSelectedCallBack.");
        }

    }

    private View.OnClickListener getOnClickListener() {

        View.OnClickListener l = new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mSampleSelectedCallBack.onSampleSelected(view.getId());
                dismiss();

            }
        };

        return l;

    }

    public static interface SampleSelectedCallBack {

        void onSampleSelected(int id);

    }


}
