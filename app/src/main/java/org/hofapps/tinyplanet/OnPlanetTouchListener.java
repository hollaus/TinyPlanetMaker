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

    private PlanetMaker.PlanetChangeCallBack mPlanetChangeCallBacks;

    // These matrices will be used to move and zoom image
    public static Matrix matrix = new Matrix();
    public static Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private static final float MAX_ZOOM = (float) 3;
    private static final float MIN_ZOOM = 1;

    int mode = NONE;

    private boolean gesturesModifyPlanet;
    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    int width,height;


    public OnPlanetTouchListener(Context context) {

        try {
            mPlanetChangeCallBacks = (PlanetMaker.PlanetChangeCallBack) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement PlanetChangeCallBacks.");
        }

        gesturesModifyPlanet = true;

    }

    public void setGestureMode(boolean gesturesModifyPlanet) {

        this.gesturesModifyPlanet = gesturesModifyPlanet;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        ImageView view = (ImageView) v;
        Rect bounds = view.getDrawable().getBounds();

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
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) {

                    if (gesturesModifyPlanet) {

                        float xDiff = event.getX() - start.x;
                        float yDiff = event.getY() - start.y;

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

                        Log.d("Rotate", Integer.toString(iDiff));

                        mPlanetChangeCallBacks.addAngle(iDiff);

                    }
                    else {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    }

                } else if (mode == ZOOM) {

                    if (gesturesModifyPlanet) {

                        float newDist = spacing(event);

                        if (newDist > 10f) {
                            float scale = oldDist / newDist;

                            Log.d("Scale", Float.toString(scale));

                            mPlanetChangeCallBacks.addScale(scale);
                        }

                    }
                    else  {

                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }

                    }
                }
                break;
        }

        if (!gesturesModifyPlanet) {

            limitZoom(matrix);
            limitDrag(matrix);

            view.setImageMatrix(matrix);

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
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
//        return FloatMath.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
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

}
