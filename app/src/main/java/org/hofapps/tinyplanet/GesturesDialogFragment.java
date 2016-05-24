package org.hofapps.tinyplanet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by fabian on 10.02.2016.
 */
public class GesturesDialogFragment extends DialogFragment {


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        getDialog().setTitle(R.string.gestures_fragment_title);
//        View v = inflater.inflate(R.layout.fragment_gestures, container, false);
//
//
////        int style = DialogFragment.STYLE_NORMAL;
////        int theme = R.style.AppCompatAlertDialogStyle;
////
////        setStyle(theme, style);
//
//        return v;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



//        int title = getArguments().getInt("title");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater =  getActivity().getLayoutInflater();
//        final FrameLayout frameView = new FrameLayout(getActivity());
        View dialogLayout = inflater.inflate(R.layout.fragment_gestures, null);


        dialogBuilder.setView(dialogLayout);

        Dialog dialog = dialogBuilder
                .setTitle(R.string.gestures_fragment_title)
                .setPositiveButton("ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                ((FragmentAlertDialog)getActivity()).doPositiveClick();
                            }
                        }
                )
                .create();


//        alertDialog.show();

                int style = DialogFragment.STYLE_NORMAL;
        int theme = R.style.AppCompatAlertDialogStyle;

        setStyle(theme, style);

        return dialog;

    }





}
