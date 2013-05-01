#include "ftrWSQAndroidJni.h"
#include "ftrWSQ.h"


JNIEXPORT jboolean JNICALL Java_com_futronictech_ftrWsqAndroidHelper_JNIRawToWsqImage(JNIEnv *env, jobject obj, jlong hDevice, jint nWidth, jint nHeight, jfloat fBitrate, jbyteArray rawImg, jbyteArray wsqImg)
{
	int imgLength = env->GetArrayLength(wsqImg);
	if( imgLength <= 0 )
		return JNI_FALSE;
	imgLength = env->GetArrayLength(rawImg);
	if( imgLength <= 0 )
		return JNI_FALSE;
	if( nWidth <= 0 || nHeight <= 0 )
		return JNI_FALSE;
	if( fBitrate < 0.75 || fBitrate > 2.25 )
		return JNI_FALSE;

    jboolean isCopy;
    jbyte *pJRAW = env->GetByteArrayElements( rawImg, &isCopy );
    jbyte *pJWSQ = env->GetByteArrayElements( wsqImg, &isCopy );
    FTRIMGPARMS ImgParm;
    ImgParm.Bitrate = fBitrate;	
	ImgParm.DPI = -1;
	ImgParm.Width = nWidth;
	ImgParm.Height = nHeight;
	ImgParm.RAW_size =  nWidth * nHeight;

	if( !ftrWSQ_FromRAWImage( (void *)hDevice, (unsigned char *)pJRAW, &ImgParm, (unsigned char *)pJWSQ ) )
	{
		env->ReleaseByteArrayElements( wsqImg, pJWSQ, 0 );
		env->ReleaseByteArrayElements( rawImg, pJRAW, 0 );
		return JNI_FALSE;
	}
	jclass cls = env->GetObjectClass(obj);
	jfieldID fidWsqImageSize = NULL;
    fidWsqImageSize = env->GetFieldID( cls, "mWSQ_size", "I" );
    if( fidWsqImageSize == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidWsqImageSize, ImgParm.WSQ_size );
	env->ReleaseByteArrayElements( wsqImg, pJWSQ, 0 );
	env->ReleaseByteArrayElements( rawImg, pJRAW, 0 );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_futronictech_com_ftrWsqAndroidHelper_JNIGetImageParameters(JNIEnv *env, jobject obj, jbyteArray wsqImg)
{
	int wsqLength = env->GetArrayLength(wsqImg);
	if( wsqLength <= 0 )
		return JNI_FALSE;
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( wsqImg, &isCopy );
    FTRIMGPARMS params;
    params.WSQ_size = wsqLength;
	if( !ftrWSQ_GetImageParameters((unsigned char *)pJData, &params) )
	{
		env->ReleaseByteArrayElements( wsqImg, pJData, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( wsqImg, pJData, 0 );

	jclass cls = env->GetObjectClass(obj);
	jfieldID fidw = NULL;
	jfieldID fidh = NULL;
    fidw = env->GetFieldID( cls, "mWidth", "I" );
    if( fidw == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidw, params.Width );
	//
    fidh = env->GetFieldID( cls, "mHeight", "I" );
    if( fidh == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidh, params.Height );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_futronictech_com_ftrWsqAndroidHelper_JNIWsqToRawImage(JNIEnv *env, jobject obj, jbyteArray wsqImg, jbyteArray rawImg)
{
	int wsqLength = env->GetArrayLength(wsqImg);
	if( wsqLength <= 0 )
		return JNI_FALSE;
    jboolean isCopy;
    jbyte *pJWSQ = env->GetByteArrayElements( wsqImg, &isCopy );
    jbyte *pJRAW = env->GetByteArrayElements( rawImg, &isCopy );
    FTRIMGPARMS params;
    params.WSQ_size = wsqLength;
	if( !ftrWSQ_ToRawImage((unsigned char *)pJWSQ, &params, (unsigned char *)pJRAW) )
	{
		env->ReleaseByteArrayElements( wsqImg, pJWSQ, 0 );
		env->ReleaseByteArrayElements( rawImg, pJRAW, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( wsqImg, pJWSQ, 0 );
	env->ReleaseByteArrayElements( rawImg, pJRAW, 0 );
	return JNI_TRUE;
}

