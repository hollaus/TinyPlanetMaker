package org.hofapps.tinyplanet;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;


/**
 * Created by fabian on 13.10.2015.
 */
public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {


    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {

        boolean isDependent = dependency instanceof LinearLayout;
        return isDependent;

    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {

        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);

//        child.setY(dependency.getHeight()-dependency.getTranslationY());

        return true;

    }

}
