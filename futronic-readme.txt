Introduction:

libftrScanAPI is the API library for application to work with Futronic's FS80/88 USB fingerprint Scanner.

USB host mode is supported in Android 3.1 and higher.

* Folders:

1. libs: the native libraries:
 a. libftrScanAPI.so	- The Android version library
 b. libusb-1.0.so
 c. libftrWSQAndroid.so

2. demo_source: using Android SDK to build the demo program. Please refer to the link for details about the setup of Android SDK: http://developer.android.com/sdk/index.html

 There are three projects:

  a. ftrScanApiAndroidHelperUsbHost: this is a "Android library" project to use the JNI to call the native libraries 
  b. ftrWsqAndroidHelper: this is a "Android library" project to use the JNI to call the native libraries of WSQ
  c. FtrScanDemoUsbHost: Android project to call the library "ftrScanApiAndroidHelperUsbHost".

* System requirement:

1. Has an USB Host port

2. Has external sdcard and mounted as "/mnt/sdcard", need to make directory "Android" under it, look as: "/mnt/sdcard/Android". It is used by the libftrScanAPI.so, the Android application needs to add the permission in file "AndroidManifest.xml" as below:
     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>


* Getting started - for the "armeabi" architecture only, other architectures, you need to rebuild your app based on the demo source.

Make sure that your Android meets the requirements, and install the test application as below steps

1. Copy the "demo_source\FtrScanDemoUsbHost\bin\FtrScanDemoUsbHost.apk" to the sdcard
2. In the Android, "Settings->Application settings", check the "Unknown sources" checkbox.
3. Explore the sdcard and click the "FtrScanDemoUsbHost.apk" to install the app.
4. Plug-in the fingerprint scanner and try to capture image from it.

* Tested Android Device
 
 - Samsung Galaxy Tab 2 7.0
 - Samsung Galaxy Tab 7.7
 - Samsung Galaxy S2
 - Acer Iconia Tab A200

* Notes for libftrWSQAndroid.so

To call the function to convert the RAW image to WSQ format, it is necessary that the Futronic USB scanner is connected.


* Others - for Android v2.x

The libftrScanAPI.so and libusb-1.0.so in this release also support the older version Android v2.x, but it has the additional requirements:

	** The OS kernel must support "usbfs", and mount it from the terminal as below:
		mount -t usbfs -o devmode=666 none /proc/bus/usb

	   It can be added to the file "\init.rc" to mount the usbfs automatically after system startup, add it as the following line:
		mount usbfs none /proc/bus/usb -o devmode=0666

	** The Android UsbManager can not be used, should use the native libs functions to open device


Please contact us for any question by email: inquiry@futronic-tech.com

