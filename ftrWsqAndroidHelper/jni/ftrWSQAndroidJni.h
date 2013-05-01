#include <string.h>
#include <jni.h>
#ifndef _Included_com_futronictech_ftrWsqAndroidHelper
#define _Included_com_futronictech_ftrWsqAndroidHelper

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_futronictech_ftrWsqAndroidHelper
 * Method:    JNIRawToWsqImage
 * Signature: (JIIF[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_futronictech_ftrWsqAndroidHelper_JNIRawToWsqImage
  (JNIEnv *, jobject, jlong, jint, jint, jfloat, jbyteArray, jbyteArray);

/*
 * Class:     com_futronictech_ftrWsqAndroidHelper
 * Method:    JNIGetImageParameters
 * Signature: ([B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_futronictech_ftrWsqAndroidHelper_JNIGetImageParameters
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_futronictech_ftrWsqAndroidHelper
 * Method:    JNIWsqToRawImage
 * Signature: ([B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_futronictech_ftrWsqAndroidHelper_JNIWsqToRawImage
  (JNIEnv *, jobject, jbyteArray, jbyteArray);


#ifdef __cplusplus
}
#endif

#endif
