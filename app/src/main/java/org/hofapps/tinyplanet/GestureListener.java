package org.hofapps.tinyplanet;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by fabian on 02.11.2015.
 */
class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 70;
    private static final int SWIPE_VELOCITY_THRESHOLD = 70;

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        super.onSingleTapConfirmed(event);

        return super.onSingleTapConfirmed(event);
    }



    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
                result = true;
            }
//                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffY > 0) {
//                            onSwipeBottom();
//                        } else {
//                            onSwipeTop();
//                        }
//                    }
//                    result = true;

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }


    private void onSwipeRight() {

//        decZombieIdx();

    }

    private void onSwipeLeft() {

//        incZombieIdx();

    }

    private void onSwipeTop() {
    }

    private void onSwipeBottom() {
    }

}


