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

    // We can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private static final int LEFT_MARGIN = 50;

    private int mode = NONE;
    private float oldDist = 1f;

    private PointF lastPoint = new PointF();

    public OnPlanetTouchListener(Context context) {

        try {
            mPlanetChangeCallBacks = (PlanetMaker.PlanetChangeCallBack) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement PlanetChangeCallBacks.");
        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ImageView view = (ImageView) v;

        // Dump touch event to log
        dumpEvent(event);

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastPoint.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                mode = ZOOM;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) {

                    // Do nothing at the outer left side. Probably the user opened the navigation drawer:
                    if (event.getX() < LEFT_MARGIN)
                        return false;

                    float xDiff = event.getX() - lastPoint.x;
                    float yDiff = event.getY() - lastPoint.y;

                    float diff;

                    if (Math.abs(xDiff) > Math.abs(yDiff)) {
                        diff = xDiff;
                        if (event.getY() < view.getHeight() / 2)
                            diff *= -1;
                    }
                    else {
                        diff = yDiff;
                        if (event.getX() > view.getWidth() / 2)
                            diff *= -1;
                    }

                    float fac = .25f;
                    int iDiff = Math.round(diff * fac);

                    mPlanetChangeCallBacks.addAngle(iDiff);

                    lastPoint.set(event.getX(), event.getY());


                } else if (mode == ZOOM) {

                    float newDist = spacing(event);

                    float scale = newDist / oldDist;
                    mPlanetChangeCallBacks.addScaleLog(scale * scale);

                    oldDist = newDist;

                }
                break;
        }


        return true; // indicate event was handled
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
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
        Log.d("Touch", sb.toString());
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
//        return FloatMath.sqrt(x * x + y * y);
    }



}
