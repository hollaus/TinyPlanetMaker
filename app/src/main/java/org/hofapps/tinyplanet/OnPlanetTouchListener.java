package org.hofapps.tinyplanet;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by fabian on 03.11.2015.
 */
public class OnPlanetTouchListener implements View.OnTouchListener {

    private PlanetMaker.PlanetChangeCallBack mPlanetChangeCallBacks;
    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private static final String TAG = "Touch";

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    public OnPlanetTouchListener(Context context) {

        try {
            mPlanetChangeCallBacks = (PlanetMaker.PlanetChangeCallBack) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement PlanetChangeCallBacks.");
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        ImageView imageView = (ImageView) view;

        // Dump touch event to log
        dumpEvent(event);

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                midPoint(mid, event);
                mode = ZOOM;

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {

                    float xDiff = event.getX() - start.x;
                    float yDiff = event.getY() - start.y;

                    float diff;

                    if (Math.abs(xDiff) > Math.abs(yDiff)) {
                        diff = xDiff;
                        if (event.getY() < imageView.getHeight() / 2)
                            diff *= -1;
                    }
                    else {
                        diff = yDiff;
                        if (event.getX() > imageView.getWidth() / 2)
                            diff *= -1;
                    }

                    float fac = .25f;
                    int iDiff = Math.round(diff * fac);

                    Log.d("Rotate", Integer.toString(iDiff));

                    mPlanetChangeCallBacks.addAngle(iDiff);


                } else if (mode == ZOOM && event.getPointerCount() == 2) {
                    float newDist = spacing(event);

                    if (newDist > 10f) {
                        float scale = oldDist / newDist;

                        Log.d("Scale", Float.toString(scale));

                        mPlanetChangeCallBacks.addScale(scale);
                    }
                    if (lastEvent != null) {
                        newRot = rotation(event);
                        float r = newRot - d;
                    }
                }
                break;
        }

        return true;

    }


    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        // ...
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d("test", sb.toString());
    }


    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);

    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {

        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);

    }


    /** Determine the degree between the first two fingers */
    private float rotation(MotionEvent event) {

        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);

        return (float) Math.toDegrees(radians);
    }


}
