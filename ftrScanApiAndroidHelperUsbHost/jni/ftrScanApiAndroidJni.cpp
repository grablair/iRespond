#include "ftrScanApiAndroidJni.h"
#include "ftrScanAPI.h"
#include <string.h>
#include <stdio.h>

struct ANDROID_IO_CTX
{
	jobject io_ctx_;
	JNIEnv *android_env_;
};

FTRHANDLE GetDeviceHandle(JNIEnv *env, jobject obj)
{
	/* Get a reference to obj¡¦s class */
	jclass cls = env->GetObjectClass(obj);
	jfieldID handle = NULL; 
    handle = env->GetFieldID( cls, "m_hDevice", "J" );
    if( handle == NULL )
        return NULL;
	FTRHANDLE hDevice = reinterpret_cast<FTRHANDLE>(env->GetLongField( obj, handle ));
	return hDevice;
}

FTR_BOOL SetDeviceHandle(JNIEnv *env, jobject obj, FTRHANDLE hDevice)
{
	/* Get a reference to obj¡¦s class */
	jclass cls = env->GetObjectClass(obj);
	jfieldID handle = NULL; 
    handle = env->GetFieldID( cls, "m_hDevice", "J" );
	env->SetLongField( obj, handle, reinterpret_cast<jlong>(hDevice) );
	return TRUE;
}

void SetErrorCode(JNIEnv *env, jobject obj, jint errCode)
{
	jclass cls = env->GetObjectClass(obj);
	jfieldID nErrCode = NULL;
	nErrCode = env->GetFieldID( cls, "m_ErrorCode", "I" );
    if( nErrCode != NULL )
    {
    	env->SetIntField( obj, nErrCode, errCode );
    }
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_OpenDevice(JNIEnv *env, jobject obj)
{
    FTRHANDLE hDevice;
	hDevice = ftrScanOpenDevice();
	if( hDevice == NULL )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
	if( SetDeviceHandle( env, obj, hDevice ) )
		return JNI_TRUE;
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_OpenDeviceCtx(JNIEnv *env, jobject obj, jobject io_ctx)
{
    FTRHANDLE hDevice;

    ANDROID_IO_CTX ctx;
    ctx.io_ctx_ = io_ctx;
    ctx.android_env_ = env;

	hDevice = ftrScanOpenDeviceWithIoContext( &ctx );
	if( hDevice == NULL )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
	if( SetDeviceHandle( env, obj, hDevice ) )
		return JNI_TRUE;
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_CloseDevice(JNIEnv *env, jobject obj)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice != NULL )
	{
		ftrScanCloseDevice(hDevice);
		SetDeviceHandle( env, obj, NULL );
	}
	return JNI_TRUE;
}

JNIEXPORT jstring JNICALL Java_com_futronictech_Scanner_GetVersionInfo(JNIEnv *env, jobject obj)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return NULL;	    	

	char szInfo[128];
	memset(szInfo, 0, 128);
	FTRSCAN_DEVICE_INFO infoDevice;
	// Initialize the FTRSCAN_DEVICE_INFO structure.
	infoDevice.dwStructSize = sizeof(infoDevice);
	FTR_BOOL bRet = ftrScanGetDeviceInfo( hDevice, &infoDevice );
	if( bRet )
	{
		switch( infoDevice.byDeviceCompatibility )
		{
		case FTR_DEVICE_USB_1_1:
		case FTR_DEVICE_USB_2_0_TYPE_1:
		case FTR_DEVICE_USB_2_0_TYPE_2:
		case FTR_DEVICE_USB_2_0_TYPE_80W:
			sprintf(szInfo, "%d:FS80", infoDevice.byDeviceCompatibility );
			break;
		case FTR_DEVICE_USB_2_0_TYPE_3:
			sprintf(szInfo, "%d:FS88", infoDevice.byDeviceCompatibility );
			break;
		case FTR_DEVICE_USB_2_0_TYPE_4:
		case FTR_DEVICE_USB_2_0_TYPE_90B:
			sprintf(szInfo, "%d:FS90", infoDevice.byDeviceCompatibility );
			break;
		case FTR_DEVICE_USB_2_0_TYPE_50:
			sprintf(szInfo, "%d:FS50", infoDevice.byDeviceCompatibility );
			break;
		case FTR_DEVICE_USB_2_0_TYPE_60:
			sprintf(szInfo, "%d:FS60", infoDevice.byDeviceCompatibility );
			break;
		case FTR_DEVICE_USB_2_0_TYPE_25:
			sprintf(szInfo, "%d:FS25", infoDevice.byDeviceCompatibility );
			break;
		default:
			sprintf(szInfo, "%d: -UNKNOWN Device-", infoDevice.byDeviceCompatibility );
			break;
		}
		//
		FTRSCAN_VERSION_INFO infoVersion;
		infoVersion.dwVersionInfoSize = sizeof( FTRSCAN_VERSION_INFO );
		bRet = ftrScanGetVersion( hDevice, &infoVersion );
		if( bRet )
		{
			char szVer[64];
			char szTmp[64];
			memset(szVer, 0, 64);
			memset(szTmp, 0, 64);
			sprintf(szVer, ",ScanAPI: %d.%d.%d.%d\r\n", 
				infoVersion.APIVersion.wMajorVersionHi, 
				infoVersion.APIVersion.wMajorVersionLo,
				infoVersion.APIVersion.wMinorVersionHi,
				infoVersion.APIVersion.wMinorVersionLo );
			strcat(szInfo, szVer);
			memset(szVer, 0, 64);
			sprintf(szVer, "Firmware: %d.%d", 
				infoVersion.FirmwareVersion.wMajorVersionHi,
				infoVersion.FirmwareVersion.wMajorVersionLo );
			if( infoVersion.FirmwareVersion.wMinorVersionHi != 0xffff )
			{
				sprintf(szTmp, ".%d", infoVersion.FirmwareVersion.wMinorVersionHi);
				strcat(szVer, szTmp);
				memset(szTmp, 0, 64);
			}
			if( infoVersion.FirmwareVersion.wMinorVersionLo != 0xffff )
			{
				sprintf( szTmp, ".%d", infoVersion.FirmwareVersion.wMinorVersionLo);
				strcat(szVer, szTmp);
				memset(szTmp, 0, 64);
			}
			sprintf(szTmp, ", Hardware: %d.%d", 
				infoVersion.HardwareVersion.wMajorVersionHi,
				infoVersion.HardwareVersion.wMajorVersionLo );
			strcat(szVer, szTmp);
			if( infoVersion.HardwareVersion.wMinorVersionHi != 0xffff )
			{
				sprintf(szTmp, ".%d", infoVersion.HardwareVersion.wMinorVersionHi);
				strcat(szVer, szTmp);
				memset(szTmp, 0, 64);
			}
			if( infoVersion.HardwareVersion.wMinorVersionLo != 0xffff )
			{
				sprintf( szTmp, ".%d", infoVersion.HardwareVersion.wMinorVersionLo);
				strcat(szVer, szTmp);
			}
			strcat(szInfo, szVer);
		}
		else
		{
			SetErrorCode(env, obj, (jint)ftrScanGetLastError());
			return NULL;
		}
	}
	else
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return NULL;
	}
	
	return env->NewStringUTF((char *)szInfo);
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetImageSize(JNIEnv *env, jobject obj)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;	    	

	FTRSCAN_IMAGE_SIZE ImageSize;
    if( !ftrScanGetImageSize( hDevice, &ImageSize ) )
    {
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
    }

	/* Get a reference to obj¡¦s class */
	jclass cls = env->GetObjectClass(obj);
	jfieldID fidw = NULL; 
	jfieldID fidh = NULL;
    fidw = env->GetFieldID( cls, "m_ImageWidth", "I" );
    if( fidw == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidw, ImageSize.nWidth );
	//
    fidh = env->GetFieldID( cls, "m_ImageHeight", "I" );
    if( fidh == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidh, ImageSize.nHeight );
	
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_IsFingerPresent(JNIEnv *env, jobject obj)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( ftrScanIsFingerPresent(hDevice, NULL) )
	{
		return JNI_TRUE;
	}
	SetErrorCode(env, obj, (jint)ftrScanGetLastError());
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetFrame(JNIEnv *env, jobject obj, jbyteArray pImage)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( pImage, &isCopy );
	if( !ftrScanGetFrame( hDevice, pJData, NULL ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		env->ReleaseByteArrayElements( pImage, pJData, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( pImage, pJData, 0 );
	
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetImage2(JNIEnv *env, jobject obj, jint dose, jbyteArray pImage)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	int nDose = dose;
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( pImage, &isCopy );

	if( !ftrScanGetImage2( hDevice, nDose, pJData) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		env->ReleaseByteArrayElements( pImage, pJData, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( pImage, pJData, 0 );

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_SetOptions(JNIEnv *env, jobject obj, jint mask, jint flag)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	unsigned long ulMask = (unsigned long)mask;
	unsigned long ulFlags = (unsigned long)flag;

	if( !ftrScanSetOptions(hDevice, ulMask, ulFlags ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_Save7Bytes(JNIEnv *env, jobject obj, jbyteArray buffer)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( env->GetArrayLength(buffer) != 7 )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_INVALID_PARAMETER);
		return JNI_FALSE;
	}

    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( buffer, &isCopy );

	if( !ftrScanSave7Bytes( hDevice, pJData ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
	    env->ReleaseByteArrayElements( buffer, pJData, 0 );
		return JNI_FALSE;
	}
    env->ReleaseByteArrayElements( buffer, pJData, 0 );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_Restore7Bytes(JNIEnv *env, jobject obj, jbyteArray buffer)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( env->GetArrayLength(buffer) != 7 )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_INVALID_PARAMETER);
		return JNI_FALSE;
	}

	char buf7Bytes[7];
	memset( buf7Bytes, 0, 7 );	
	if( !ftrScanRestore7Bytes( hDevice, buf7Bytes ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( buffer, &isCopy );
    memcpy( pJData, buf7Bytes, 7 );
    env->ReleaseByteArrayElements( buffer, pJData, 0 );

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_SetNewAuthorizationCode(JNIEnv *env, jobject obj, jbyteArray SevenBytesAuthorizationCode)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( env->GetArrayLength(SevenBytesAuthorizationCode) != 7 )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_INVALID_PARAMETER);
		return JNI_FALSE;
	}
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( SevenBytesAuthorizationCode, &isCopy );

	if( !ftrScanSetNewAuthorizationCode( hDevice, pJData ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
	    env->ReleaseByteArrayElements( SevenBytesAuthorizationCode, pJData, 0 );
		return JNI_FALSE;
	}
    env->ReleaseByteArrayElements( SevenBytesAuthorizationCode, pJData, 0 );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_SaveSecret7Bytes(JNIEnv *env, jobject obj, jbyteArray SevenBytesAuthorizationCode, jbyteArray buffer)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( env->GetArrayLength(SevenBytesAuthorizationCode) != 7 || 
		env->GetArrayLength(buffer) != 7 )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_INVALID_PARAMETER);
		return JNI_FALSE;
	}

    jboolean isCopy;
    jbyte *pJData1 = env->GetByteArrayElements( SevenBytesAuthorizationCode, &isCopy );
    jbyte *pJData2 = env->GetByteArrayElements( buffer, &isCopy );

	if( !ftrScanSaveSecret7Bytes( hDevice, pJData1, pJData2 ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
	    env->ReleaseByteArrayElements( SevenBytesAuthorizationCode, pJData1, 0 );
	    env->ReleaseByteArrayElements( buffer, pJData2, 0 );
		return JNI_FALSE;
	}
    env->ReleaseByteArrayElements( SevenBytesAuthorizationCode, pJData1, 0 );
    env->ReleaseByteArrayElements( buffer, pJData2, 0 );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_RestoreSecret7Bytes(JNIEnv *env, jobject obj, jbyteArray SevenBytesAuthorizationCode, jbyteArray buffer)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( env->GetArrayLength(SevenBytesAuthorizationCode) != 7 || 
		env->GetArrayLength(buffer) != 7 )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_INVALID_PARAMETER);
		return JNI_FALSE;
	}

    jboolean isCopy;
    jbyte *pJData1 = env->GetByteArrayElements( SevenBytesAuthorizationCode, &isCopy );
	char bufRead[7];
	memset( bufRead, 0, 7 );
	if( !ftrScanRestoreSecret7Bytes( hDevice, pJData1, bufRead ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
	    env->ReleaseByteArrayElements( SevenBytesAuthorizationCode, pJData1, 0 );
		return JNI_FALSE;
	}
    env->ReleaseByteArrayElements( SevenBytesAuthorizationCode, pJData1, 0 );
    jbyte *pJData2 = env->GetByteArrayElements( buffer, &isCopy );
    memcpy( pJData2, bufRead, 7 );
    env->ReleaseByteArrayElements( buffer, pJData2, 0 );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_SetDiodesStatus(JNIEnv *env, jobject obj, jint nGreenDiodeStatus, jint nRedDiodeStatus)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	FTR_BYTE byGreen = (FTR_BYTE)nGreenDiodeStatus;
	FTR_BYTE byRed = (FTR_BYTE)nRedDiodeStatus;
	if( !ftrScanSetDiodesStatus( hDevice, byGreen, byRed ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetDiodesStatus(JNIEnv *env, jobject obj, jbyteArray Status)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	if( env->GetArrayLength(Status) != 2 )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_INVALID_PARAMETER);
		return JNI_FALSE;
	}

	FTR_BOOL bGreen = 0;
	FTR_BOOL bRed = 0;
	if( !ftrScanGetDiodesStatus( hDevice, &bGreen, &bRed ) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
	char buf[2];
	memset( buf, 0, 2 );
	if( bGreen )
		buf[0] = 1;
	if( bRed )
		buf[1] = 1;
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( Status, &isCopy );
    memcpy( pJData, buf, 2 );
    env->ReleaseByteArrayElements( Status, pJData, 0 );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetInterfaces(JNIEnv *env, jobject obj, jbyteArray pInterfaceList)
{
	if( env->GetArrayLength(pInterfaceList) < FTR_MAX_INTERFACE_NUMBER )
	{
		SetErrorCode(env, obj, (jint)FTR_ERROR_NOT_ENOUGH_MEMORY);
		return JNI_FALSE;
	}

	FTRSCAN_INTERFACES_LIST InterfaceList;
	if( ftrScanGetInterfaces(&InterfaceList) )
	{
		char pInterfaces[FTR_MAX_INTERFACE_NUMBER];
		memset( pInterfaces, FTRSCAN_INTERFACE_STATUS_DISCONNECTED, FTR_MAX_INTERFACE_NUMBER );
		for (int i=0; i<FTR_MAX_INTERFACE_NUMBER; i++)
		{
			if( InterfaceList.InterfaceStatus[i] == FTRSCAN_INTERFACE_STATUS_CONNECTED )
				pInterfaces[i] = FTRSCAN_INTERFACE_STATUS_CONNECTED;
		}
		jboolean isCopy;
		jbyte *pJData = env->GetByteArrayElements( pInterfaceList, &isCopy );
		memcpy( pJData, pInterfaces, FTR_MAX_INTERFACE_NUMBER );
		env->ReleaseByteArrayElements( pInterfaceList, pJData, 0 );
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_OpenDeviceOnInterface(JNIEnv *env, jobject obj, jint nInterface)
{
    FTRHANDLE hDevice;
	hDevice = ftrScanOpenDeviceOnInterface(nInterface);
	if( hDevice == NULL )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		return JNI_FALSE;
	}
	if( SetDeviceHandle( env, obj, hDevice ) )
		return JNI_TRUE;
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_SetGlobalSyncDir (JNIEnv *env, jobject obj, jstring szDir)
{
	const char *str_new_sync_dir;
	str_new_sync_dir = env->GetStringUTFChars(szDir, NULL);
	if (str_new_sync_dir == NULL) {
		env->ReleaseStringUTFChars(szDir, str_new_sync_dir);
		return JNI_FALSE;
	}
	if( !ftrScanGlobalSetOptions( FTR_GLOBAL_SYNC_DIR, (FTR_PVOID)str_new_sync_dir) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		env->ReleaseStringUTFChars(szDir, str_new_sync_dir);
		return JNI_FALSE;
	}
	env->ReleaseStringUTFChars(szDir, str_new_sync_dir);
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetImage(JNIEnv *env, jobject obj, jint dose, jbyteArray pImage)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	int nDose = dose;
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( pImage, &isCopy );

	if( !ftrScanGetImage( hDevice, nDose, pJData) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		env->ReleaseByteArrayElements( pImage, pJData, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( pImage, pJData, 0 );

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_futronictech_Scanner_GetImageByVariableDose(JNIEnv *env, jobject obj, jint dose, jbyteArray pImage)
{
	FTRHANDLE hDevice = GetDeviceHandle(env, obj);
	if( hDevice == NULL )
		return JNI_FALSE;

	int nDose = dose;
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( pImage, &isCopy );

	if( !ftrScanGetImageByVariableDose( hDevice, nDose, pJData) )
	{
		SetErrorCode(env, obj, (jint)ftrScanGetLastError());
		env->ReleaseByteArrayElements( pImage, pJData, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( pImage, pJData, 0 );

	return JNI_TRUE;
}

