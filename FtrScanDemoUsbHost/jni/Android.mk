APP_PLATFORM := android-8

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := FtrScanDemoUsbHostActivity

LOCAL_SRC_FILES := Iris.c


include $(BUILD_SHARED_LIBRARY)

