package org.hofapps.tinyplanet;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by fabian on 03.11.2015.
 */
public class OnPlanetTouchListener implements View.OnTouchListener {

    private static final float MAX_ZOOM = (float) 3;
    private static final float MIN_ZOOM = 1;

    private PlanetMaker.PlanetChangeCallBack mPlanetChangeCallBacks;
    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private static final String TAG = "Touch";

    private static final int GESTURE_EDIT = 0;
    private static final int GESTURE_VIEW = 1;

    private int gestureMode = GESTURE_VIEW;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    int width,height;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();


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

        Rect bounds = imageView.getDrawable().getBounds();

        width = bounds.right - bounds.left;
        height = bounds.bottom - bounds.top;


        // Dump touch event to log
        dumpEvent(event);

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                savedMatrix.set(matrix);

                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                oldDist = spacing(event);

                if (oldDist > 10f) {


                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");

                    lastEvent = new float[4];
                    lastEvent[0] = event.getX(0);
                    lastEvent[1] = event.getX(1);
                    lastEvent[2] = event.getY(0);
                    lastEvent[3] = event.getY(1);
                    d = rotation(event);


                    limitZoom(matrix);
                }

                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_POINTER_UP:

                mode = NONE;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {

                    if (gestureMode == GESTURE_VIEW) {

                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);

                        limitDrag(matrix);

                    }

                    else if (gestureMode == GESTURE_EDIT) {

                        float xDiff = event.getX() - start.x;
                        float yDiff = event.getY() - start.y;

                        float diff;

                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            diff = xDiff;
                            if (event.getY() < imageView.getHeight() / 2)
                                diff *= -1;
                        } else {
                            diff = yDiff;
                            if (event.getX() > imageView.getWidth() / 2)
                                diff *= -1;
                        }

                        float fac = .25f;
                        int iDiff = Math.round(diff * fac);

                        Log.d("Rotate", Integer.toString(iDiff));

                        mPlanetChangeCallBacks.addAngle(iDiff);
                    }


                } else if (mode == ZOOM && event.getPointerCount() == 2) {
                    float newDist = spacing(event);

                    if (newDist > 10f) {


//                        Log.d("Scale", Float.toString(scale));

                        if (gestureMode == GESTURE_EDIT) {
                            float scale = oldDist / newDist;
                            mPlanetChangeCallBacks.addScale(scale);
                        }

                        else {

                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);

                            limitZoom(matrix);

                        }

                    }

                    if (lastEvent != null) {
                        newRot = rotation(event);
                        float r = newRot - d;
                    }
                }



                break;
        }


        ((ImageView) view).setImageMatrix(matrix);

        return true;

    }

    private void limitDrag(Matrix m) {

        float[] values = new float[9];
        m.getValues(values);
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
//--- limit moving to left ---
        float minX = (-width + 0) * (scaleX-1);
        float minY = (-height + 0) * (scaleY-1);
//--- limit moving to right ---
        float maxX=minX+width*(scaleX-1);
        float maxY=minY+height*(scaleY-1);
        if(transX>maxX){transX = maxX;}
        if(transX<minX){transX = minX;}
        if(transY>maxY){transY = maxY;}
        if(transY<minY){transY = minY;}
        values[Matrix.MTRANS_X] = transX;
        values[Matrix.MTRANS_Y] = transY;
        m.setValues(values);
    }

    private void limitZoom(Matrix m) {

        float[] values = new float[9];
        m.getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        if(scaleX > MAX_ZOOM) {
            scaleX = MAX_ZOOM;
        } else if(scaleX < MIN_ZOOM) {
            scaleX = MIN_ZOOM;
        }

        if(scaleY > MAX_ZOOM) {
            scaleY = MAX_ZOOM;
        } else if(scaleY < MIN_ZOOM) {
            scaleY = MIN_ZOOM;
        }

        values[Matrix.MSCALE_X] = scaleX;
        values[Matrix.MSCALE_Y] = scaleY;
        m.setValues(values);
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
