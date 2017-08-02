LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
# include file with user defined OpenCV SDK path (MY_OPENCVROOT):
include $(LOCAL_PATH)/local/Android.mk
OPENCVROOT:= $(MY_OPENCVROOT)
LOCAL_LDLIBS := -LC:\Users\fabian\files\TinyPlanetMaker\app\src\main\jniLibs

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := Utils.cpp NativeWrapper.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := wrapper

include $(BUILD_SHARED_LIBRARY)
