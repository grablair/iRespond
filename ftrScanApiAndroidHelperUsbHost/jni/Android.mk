LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := Prebuild_usb1.0
LOCAL_SRC_FILES := libusb-1.0.so
include $(PREBUILT_SHARED_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_MODULE    := Prebuild_ftrScanAPI
LOCAL_SRC_FILES := libftrScanAPI.so
include $(PREBUILT_SHARED_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_MODULE    := ftrScanApiAndroidJni
LOCAL_SRC_FILES := ftrScanApiAndroidJni.cpp
LOCAL_SHARED_LIBRARIES := prebuild_usb1.0 Prebuild_ftrScanAPI
include $(BUILD_SHARED_LIBRARY)
