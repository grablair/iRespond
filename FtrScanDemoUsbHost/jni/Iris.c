#include <string.h>
#include <jni.h>

jstring Java_com_futronictech_Iris_WSQString( JNIEnv* env, jobject this) {
	return (*env)->NewStringUTF(env, "You picked WSQ!");
}

jstring Java_com_futronictech_Iris_bitmapString( JNIEnv* env, jobject this) {
	return (*env)->NewStringUTF(env, "You picked bitmap!");
}
