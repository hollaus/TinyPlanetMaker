package org.hofapps.tinyplanet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fabian on 10.02.2016.
 */
public class GesturesDialogFragment extends DialogFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.gestures_fragment_title);
        View v = inflater.inflate(R.layout.fragment_gestures, container, false);

        

        return v;
    }


}
