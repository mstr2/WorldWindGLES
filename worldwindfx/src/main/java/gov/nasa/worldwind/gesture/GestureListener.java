/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import gov.nasa.worldwind.input.MotionEvent;

public interface GestureListener {

    void gestureStateChanged(MotionEvent event, GestureRecognizer recognizer);
}
