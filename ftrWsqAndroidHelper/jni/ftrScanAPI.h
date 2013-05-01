/*

Copyright (c) 2003-2012 Futronic Technology Company Ltd. All rights reserved.

Abstract:

Definitions and prototypes for the Futronic Scanner API.

*/

#ifndef __FUTRONIC_SCAN_API_H__
#define __FUTRONIC_SCAN_API_H__

#if ( !defined( __WIN32__ ) && ( defined( WIN32 ) || defined( _WIN32 ) || defined( _WIN32_WCE ) ) )
#  define __WIN32__
#  include <windows.h>
#endif

#if defined( MSDOS ) || defined( __MSDOS__ ) || defined( __OS2__ ) || defined( _WINDOWS ) || defined ( _Windows ) || defined( __WIN32__ )
#  define FTR_OS_DOS_WIN_OS2
#elif defined( unix ) || defined( __unix__ ) || defined( __sun ) || defined( __APPLE__ )
#  define FTR_OS_UNIX
#elif defined( VAX )
#  define FTR_OS_VAX 
#else
#  error Cannot determine target OS - contact your vendor for support
#endif

#if defined( __sun )
#  include <sys/isa_defs.h>
#endif

#if defined( __WIN32__ )
#  define FTR_PACKED
#elif defined( FTR_OS_UNIX )
#  define FTR_PACKED __attribute__ ((aligned(1),packed))
#else
#  error Cannot determine alligment - contact your vendor for support
#endif

#ifndef TRUE
#  define TRUE 1
#endif

#ifndef FALSE
#  define FALSE 0
#endif

#ifndef NULL
#  define NULL 0
#endif

#if defined( __UNIX__ ) || defined( unix ) || defined( __APPLE__ )
#include <sys/time.h>
#include <sys/param.h>
#include <stdlib.h>
#ifndef min
#define min(a, b)  (((a) < (b)) ? (a) : (b))
#endif

#ifndef max
#define max(a, b)  (((a) > (b)) ? (a) : (b))
#endif
#endif

#ifndef ERROR_NOT_ENOUGH_MEMORY
#define ERROR_NOT_ENOUGH_MEMORY 8
#endif

#ifndef ERROR_WRITE_PROTECT
#define ERROR_WRITE_PROTECT 19
#endif

#ifndef ERROR_NOT_READY
#define ERROR_NOT_READY 21
#endif

#ifndef ERROR_NOT_SUPPORTED
#define ERROR_NOT_SUPPORTED 50
#endif

#ifndef ERROR_INVALID_PARAMETER
#define ERROR_INVALID_PARAMETER 87
#endif

#ifndef ERROR_CALL_NOT_IMPLEMENTED
#define ERROR_CALL_NOT_IMPLEMENTED 120
#endif

#ifndef ERROR_NO_MORE_ITEMS
#define ERROR_NO_MORE_ITEMS 259
#endif

#ifndef ERROR_NO_SYSTEM_RESOURCES
#define ERROR_NO_SYSTEM_RESOURCES 1450
#endif

#ifndef ERROR_TIMEOUT
#define ERROR_TIMEOUT 1460
#endif

#ifndef ERROR_BAD_CONFIGURATION
#define ERROR_BAD_CONFIGURATION 1610
#endif

#ifndef ERROR_MESSAGE_EXCEEDS_MAX_SIZE
#define ERROR_MESSAGE_EXCEEDS_MAX_SIZE 4336
#endif

#if defined( __WIN32__ )
#  define FTR_API_PREFIX
#  define FTR_API       __stdcall
#elif defined( FTR_OS_UNIX ) || defined( FTR_OS_VAX )
#  define FTR_API_PREFIX
#	define FTR_API
#elif defined( __OS2__ )
#  define FTR_API_PREFIX
#  define FTR_API       _pascal _export
#else
#  define FTR_API_PREFIX
#  define FTR_API       _far _pascal
#endif

typedef void *          FTRHANDLE;
typedef	unsigned char   FTR_BYTE;
typedef int             FTR_BOOL;
typedef	unsigned long   FTR_DWORD;
typedef	unsigned short  FTR_WORD;

typedef	char            FTR_INT8;
typedef	short           FTR_INT16;
typedef	long            FTR_INT32;

typedef	void *          FTR_PVOID;
typedef	FTR_BOOL *      FTR_PBOOL;
typedef	FTR_BYTE *      FTR_PBYTE;
typedef	FTR_WORD *      FTR_PWORD;
typedef	FTR_DWORD *     FTR_PDWORD;

#if defined( __cplusplus )
extern "C" { /* assume C declarations for C++ */
#endif


#if defined(__WIN32__)
#pragma pack(push, 1)
#endif

#define FTR_MAX_INTERFACE_NUMBER                        128

#define FTR_OPTIONS_CHECK_FAKE_REPLICA                  0x00000001
#define FTR_OPTIONS_DETECT_FAKE_FINGER                  FTR_OPTIONS_CHECK_FAKE_REPLICA
#define FTR_OPTIONS_FAST_FINGER_DETECT_METHOD           0x00000002
#define FTR_OPTIONS_RECEIVE_LONG_IMAGE                  0x00000004
#define FTR_OPTIONS_RECEIVE_FAKE_IMAGE                  0x00000008
#define FTR_OPTIONS_SCALE_IMAGE                         0x00000010
#define FTR_OPTIONS_IMPROVE_IMAGE                       0x00000020 /* for PIV compatible devices */
#define FTR_OPTIONS_INVERT_IMAGE                        0x00000040
#define FTR_OPTIONS_PREVIEW_MODE                        0x00000080
#define FTR_OPTIONS_IMAGE_FORMAT_MASK                   0x00000700
#define FTR_OPTIONS_IMAGE_FORMAT_1                      0x00000100
#define FTR_OPTIONS_ELIMINATE_BACKGROUND                0x00000800
#define FTR_OPTIONS_IMPROVE_BACKGROUND                  0x00001000

#define FTR_GLOBAL_ENABLE_REMOTE_SESSION_DETECTION      0x00000001
#define FTR_GLOBAL_SYNC_DIR                             0x00000002 /* for Android only */

#define FTR_GLOBAL_VALUE_REMOTE_SESSION_ENABLE          0x00000001
#define FTR_GLOBAL_VALUE_REMOTE_SESSION_DISABLE         0x00000000

#define FTR_LIGHT_NO_LIGHT                              0x00 /* Can not be combined with other flags */
#define FTR_LIGHT_TURN_ON( x )                          (0x80 | (x))
#define FTR_LIGHT_TURN_OFF( x )                         (0x7F & (x))
#define FTR_LIGHT_MAIN_IFRED                            0x01
#define FTR_LIGHT_ADDITIONAL_IFRED                      0x02
#define FTR_LIGHT_BACK_IFRED                            0x04
#define FTR_LIGHT_GREEN                                 0x08
#define FTR_LIGHT_OFF_MAIN_IFRED                        FTR_LIGHT_TURN_OFF( FTR_LIGHT_MAIN_IFRED )
#define FTR_LIGHT_OFF_ADDITIONAL_IFRED                  FTR_LIGHT_TURN_OFF( FTR_LIGHT_ADDITIONAL_IFRED )
#define FTR_LIGHT_OFF_BACK_IFRED                        FTR_LIGHT_TURN_OFF( FTR_LIGHT_BACK_IFRED )
#define FTR_LIGHT_OFF_GREEN                             FTR_LIGHT_TURN_OFF( FTR_LIGHT_GREEN )
#define FTR_LIGHT_ON_MAIN_IFRED                         FTR_LIGHT_TURN_ON( FTR_LIGHT_MAIN_IFRED )
#define FTR_LIGHT_ON_ADDITIONAL_IFRED                   FTR_LIGHT_TURN_ON( FTR_LIGHT_ADDITIONAL_IFRED )
#define FTR_LIGHT_ON_BACK_IFRED                         FTR_LIGHT_TURN_ON( FTR_LIGHT_BACK_IFRED )
#define FTR_LIGHT_ON_GREEN                              FTR_LIGHT_TURN_ON( FTR_LIGHT_GREEN )

#define FTR_ERROR_BASE                                  0x20000000
#define FTR_ERROR_CODE( x )                             (FTR_ERROR_BASE | (x))

#define FTR_ERROR_NO_ERROR                              0
#define FTR_ERROR_EMPTY_FRAME                           4306 /* ERROR_EMPTY */
#define FTR_ERROR_MOVABLE_FINGER                        FTR_ERROR_CODE( 0x0001 )
#define FTR_ERROR_NO_FRAME                              FTR_ERROR_CODE( 0x0002 )
#define FTR_ERROR_USER_CANCELED                         FTR_ERROR_CODE( 0x0003 )
#define FTR_ERROR_HARDWARE_INCOMPATIBLE                 FTR_ERROR_CODE( 0x0004 )
#define FTR_ERROR_FIRMWARE_INCOMPATIBLE                 FTR_ERROR_CODE( 0x0005 )
#define FTR_ERROR_INVALID_AUTHORIZATION_CODE            FTR_ERROR_CODE( 0x0006 )
#define FTR_ERROR_ROLL_NOT_STARTED                      FTR_ERROR_CODE( 0x0007 )
#define FTR_ERROR_ROLL_PROGRESS_DATA                    FTR_ERROR_CODE( 0x0008 )
#define FTR_ERROR_ROLL_TIMEOUT                          FTR_ERROR_CODE( 0x0009 )
#define FTR_ERROR_ROLL_ABORTED                          FTR_ERROR_CODE( 0x000A )
#define FTR_ERROR_ROLL_ALREADY_STARTED                  FTR_ERROR_CODE( 0x000B )
#define FTR_ERROR_ROLL_PROGRESS_REMOVE_FINGER           FTR_ERROR_CODE( 0x000C )
#define FTR_ERROR_ROLL_PROGRESS_PUT_FINGER              FTR_ERROR_CODE( 0x000D )
#define FTR_ERROR_ROLL_PROGRESS_POST_PROCESSING         FTR_ERROR_CODE( 0x000E )
#define FTR_ERROR_FINGER_IS_PRESENT                     FTR_ERROR_CODE( 0x000F )
#define FTR_ERROR_NULL_PARAMETER                        FTR_ERROR_CODE( 0x0010 )
#define FTR_ERROR_LIBUSB_ERROR                          FTR_ERROR_CODE( 0x0011 )

/* Other return codes are Windows-compatible */
#define FTR_ERROR_NO_MORE_ITEMS                         ERROR_NO_MORE_ITEMS
#define FTR_ERROR_NOT_ENOUGH_MEMORY                     ERROR_NOT_ENOUGH_MEMORY
#define FTR_ERROR_NO_SYSTEM_RESOURCES                   ERROR_NO_SYSTEM_RESOURCES
#define FTR_ERROR_TIMEOUT                               ERROR_TIMEOUT
#define FTR_ERROR_NOT_READY                             ERROR_NOT_READY
#define FTR_ERROR_BAD_CONFIGURATION                     ERROR_BAD_CONFIGURATION
#define FTR_ERROR_INVALID_PARAMETER                     ERROR_INVALID_PARAMETER
#define FTR_ERROR_CALL_NOT_IMPLEMENTED                  ERROR_CALL_NOT_IMPLEMENTED
#define FTR_ERROR_NOT_SUPPORTED                         ERROR_NOT_SUPPORTED
#define FTR_ERROR_WRITE_PROTECT                         ERROR_WRITE_PROTECT
#define FTR_ERROR_MESSAGE_EXCEEDS_MAX_SIZE              ERROR_MESSAGE_EXCEEDS_MAX_SIZE

#define FTR_CONST_DIODE_OFF                             ((FTR_BYTE)0)
#define FTR_CONST_DIODE_ON                              ((FTR_BYTE)255)

#define FTR_LOG_MASK_OFF                                0
#define FTR_LOG_MASK_TO_FILE                            0x00000001
#define FTR_LOG_MASK_TO_AUX                             0x00000002
#define FTR_LOG_MASK_TIMESTAMP                          0x00000004
#define FTR_LOG_MASK_THREAD_ID                          0x00000008
#define FTR_LOG_MASK_PROCESS_ID                         0x00000010

#define FTR_LOG_LEVEL_MIN                               0
#define FTR_LOG_LEVEL_OPTIMAL                           1
#define FTR_LOG_LEVEL_FULL                              2

#define FTR_DEVICE_USB_1_1                              0
#define FTR_DEVICE_USB_2_0_TYPE_1                       1
#define FTR_DEVICE_SWEEP                                2
#define FTR_DEVICE_BLACKFIN                             3
#define FTR_DEVICE_USB_2_0_TYPE_2                       4
#define FTR_DEVICE_USB_2_0_TYPE_3                       5
#define FTR_DEVICE_USB_2_0_TYPE_4                       6
#define FTR_DEVICE_USB_2_0_TYPE_50                      7
#define FTR_DEVICE_USB_2_0_TYPE_60                      8
#define FTR_DEVICE_USB_2_0_TYPE_25                      9
#define FTR_DEVICE_USB_2_0_TYPE_80W                     11
#define FTR_DEVICE_USB_2_0_TYPE_90B                     12
#define FTR_DEVICE_USB_2_0_TYPE_98                      210

/*
byDeviceCompatibility:
        0 - USB 1.1 device,
        1 - USB 2.0 device (SOI966)
        2 - "Sweep" scanner
        3 - "BlackFin" scanner
        4 - USB 2.0 device (SOI968)
        5 - USB 2.0 device (FS88 compatible - SOI968)
        6 - USB 2.0 device (FS90 compatible - PAS202)
        7 - USB 2.0 device (FS50 compatible)
        8 - USB 2.0 device (FS60 compatible)
        9 - USB 2.0 device (FS25 compatible)
       11 - USB 2.0 device (FS80 compatible - SOI968 + Prism w/o Lens)
       12 - USB 2.0 device (FS90 compatible - GC0303 w/o PIV)
      210 - USB 2.0 device (FS98 compatible)
*/

typedef struct FTR_PACKED __FTRSCAN_DEVICE_INFO 
{
    FTR_DWORD                           dwStructSize; /* [in, out] */
    FTR_BYTE                            byDeviceCompatibility;
    FTR_WORD                            wPixelSizeX;
    FTR_WORD                            wPixelSizeY;
} FTRSCAN_DEVICE_INFO, *PFTRSCAN_DEVICE_INFO;

typedef struct FTR_PACKED __FTRSCAN_IMAGE_SIZE
{
    int                                 nWidth;
    int                                 nHeight;
    int                                 nImageSize;
} FTRSCAN_IMAGE_SIZE, *PFTRSCAN_IMAGE_SIZE;

typedef struct FTR_PACKED __FTRSCAN_FAKE_REPLICA_PARAMETERS
{
    FTR_BOOL                            bCalculated;
    int                                 nCalculatedSum1;
    int                                 nCalculatedSumFuzzy;
    int                                 nCalculatedSumEmpty;
    int                                 nCalculatedSum2;
    double                              dblCalculatedTremor;
    double                              dblCalculatedValue;
} FTRSCAN_FAKE_REPLICA_PARAMETERS, *PFTRSCAN_FAKE_REPLICA_PARAMETERS;

typedef struct FTR_PACKED __FTRSCAN_FAKE_REPLICA_BUFFER
{
    FTR_BOOL                            bCalculated;
    int                                 nBuffers;
    int                                 nWidth;
    int                                 nHeight;
    int                                 nSize;
    FTR_PVOID                           pBuffers;
} FTRSCAN_FAKE_REPLICA_BUFFER, *PFTRSCAN_FAKE_REPLICA_BUFFER;

typedef struct FTR_PACKED __FTRSCAN_LFD_CONSTANTS
{
    int                                 nLMin;
    int                                 nLMax;
    int                                 nCMin;
    int                                 nCMax;
    int                                 nEEMin;
    int                                 nEEMax;
} FTRSCAN_LFD_CONSTANTS, *PFTRSCAN_LFD_CONSTANTS;

typedef struct FTR_PACKED __FTRSCAN_FRAME_PARAMETERS
{
    int                                 nContrastOnDose2;
    int                                 nContrastOnDose4;
    int                                 nDose;
    int                                 nBrightnessOnDose1;
    int                                 nBrightnessOnDose2;
    int                                 nBrightnessOnDose3;
    int                                 nBrightnessOnDose4;
    FTRSCAN_FAKE_REPLICA_PARAMETERS     FakeReplicaParams;
    FTR_BYTE                            Reserved[64-sizeof(FTRSCAN_FAKE_REPLICA_PARAMETERS)];
} FTRSCAN_FRAME_PARAMETERS, *PFTRSCAN_FRAME_PARAMETERS;

typedef enum __FTRSCAN_INTERFACE_STATUS
{
    FTRSCAN_INTERFACE_STATUS_CONNECTED,
    FTRSCAN_INTERFACE_STATUS_DISCONNECTED
} FTRSCAN_INTERFACE_STATUS, *PFTRSCAN_INTERFACE_STATUS;

typedef struct FTR_PACKED __FTRSCAN_INTERFACES_LIST
{
    FTRSCAN_INTERFACE_STATUS            InterfaceStatus[FTR_MAX_INTERFACE_NUMBER];
} FTRSCAN_INTERFACES_LIST, *PFTRSCAN_INTERFACES_LIST;

#define FTR_VERSION_UNKNOWN_VERSION         0xFFFF

typedef struct FTR_PACKED __FTRSCAN_VERSION
{
    FTR_WORD                            wMajorVersionHi; 
    FTR_WORD                            wMajorVersionLo; 
    FTR_WORD                            wMinorVersionHi; 
    FTR_WORD                            wMinorVersionLo; 
} FTRSCAN_VERSION, *PFTRSCAN_VERSION;

typedef struct FTR_PACKED __FTRSCAN_VERSION_INFO
{
    FTR_DWORD                           dwVersionInfoSize; /* [in, out] */
    FTRSCAN_VERSION                     APIVersion;
    FTRSCAN_VERSION                     HardwareVersion;
    FTRSCAN_VERSION                     FirmwareVersion;
} FTRSCAN_VERSION_INFO, *PFTRSCAN_VERSION_INFO;

typedef struct FTR_PACKED __FTRSCAN_VAR_DOSE_EXTRA_PARAMS
{
    FTR_BYTE                            byNumberOfFramesMinusOne;
    FTR_BYTE                            byReserved1;
    FTR_BYTE                            byReserved2;
    FTR_BYTE                            byReserved3;
    FTR_BYTE                            byReserved4;
} FTRSCAN_VAR_DOSE_EXTRA_PARAMS, *PFTRSCAN_VAR_DOSE_EXTRA_PARAMS;

typedef struct FTR_PACKED __FTRPIPE_COMMAND_PACKET
{
    FTR_DWORD                           dwLabel;
    FTR_DWORD                           dwInDataSize;
    FTR_DWORD                           dwWaitDataSize;
    FTR_DWORD                           dwOperationStatus;
} FTRPIPE_COMMAND_PACKET, *PFTRPIPE_COMMAND_PACKET;

#define FTR_SCANNER_FEATURE_LFD                         1
#define FTR_SCANNER_FEATURE_DIODES                      2
#define FTR_SCANNER_FEATURE_GET_IMAGE2                  3
#define FTR_SCANNER_FEATURE_SERIAL_NUMBER               4
#define FTR_SCANNER_FEATURE_LONG_IMAGE                  5
#define FTR_SCANNER_FEATURE_IS_CALIBRATED               6
#define FTR_SCANNER_FEATURE_IS_LFD_CALIBRATED           7
#define FTR_SCANNER_FEATURE_ROLL                        8

#define FTR_POWER_EVENT_SLEEP                           0x00000001
#define FTR_POWER_EVENT_SESSION_DISCONNECT              0x00000002
#define FTR_POWER_EVENT_SESSION_LOGOFF                  0x00000004
#define FTR_POWER_EVENT_LOGGING_OFF                     0x00000008
#define FTR_POWER_EVENT_SHUTDOWN                        0x00000010
#define FTR_POWER_EVENT_LOCK                            0x00000020

#define FTR_POWER_EVENT_ALL                             0xFFFFFFFF
#define FTR_POWER_EVENT_DISABLE_ALL                     FTR_POWER_EVENT_ALL, 0
#define FTR_POWER_EVENT_ENABLE_ALL                      FTR_POWER_EVENT_ALL, FTR_POWER_EVENT_ALL

#define FTR_TIMEOUT_INFINITE                            0xFFFFFFFF 

#define FTR_REGISTRY_INDEX_LUM_AVERAGE                  0

#define FTR_PROPERTY_NUMBER_OF_IMAGE_SIZES              1
#define FTR_PROPERTY_LFD_LEVEL                          2
#define FTR_PROPERTY_LFD_SW_1_CALCULATED_DATA           3
#define FTR_PROPERTY_LFD_SW_1_PARAM                     4
#define FTR_PROPERTY_LFD_SW_1_RESERVED                  5

typedef struct FTR_PACKED __FTRSCAN_PROPERTY_NUMBER_OF_IMAGE_SIZES
{
    int                                 nNumberOfImagesSizes;
} FTRSCAN_PROPERTY_NUMBER_OF_IMAGE_SIZES, *PFTRSCAN_PROPERTY_NUMBER_OF_IMAGE_SIZES;

typedef struct FTR_PACKED __FTRSCAN_PROPERTY_LFD_LEVEL
{
    FTR_DWORD                           dwLfdLevel;
} FTRSCAN_PROPERTY_LFD_LEVEL, *PFTRSCAN_PROPERTY_LFD_LEVEL;

typedef struct FTR_PACKED _FTRSCAN_PROPERTY_LFD_SW_1_DATA
{
    FTR_DWORD                           dwStrength;
} FTRSCAN_PROPERTY_LFD_SW_1_DATA, *PFTRSCAN_PROPERTY_LFD_SW_1_DATA;

typedef struct FTR_PACKED _FTRSCAN_PROPERTY_LFD_SW_1_PARAM
{
    FTR_DWORD                           dwStrength;
} FTRSCAN_PROPERTY_LFD_SW_1_PARAM, *PFTRSCAN_PROPERTY_LFD_SW_1_PARAM;

#define FTR_ROLL_DIRECTION_NOT_DEFINED                  0
#define FTR_ROLL_DIRECTION_FROM_LEFT_TO_RIGHT           1
#define FTR_ROLL_DIRECTION_FROM_RIGHT_TO_LEFT           2

#define FTR_ROLL_FRAME_PARAM_FLAG_NOT_CALIBRATED        0x00000001
#define FTR_ROLL_FRAME_PARAM_FLAG_INDEX                 0x00000002
#define FTR_ROLL_FRAME_PARAM_FLAG_DOSE                  0x00000004
#define FTR_ROLL_FRAME_PARAM_FLAG_CONRAST               0x00000008

#define FTR_ROLL_RESULT_OK                              0
#define FTR_ROLL_RESULT_REVERSE_ROLLING                 1
#define FTR_ROLL_RESULT_TOO_FAST_ROLLING                2

typedef struct FTR_PACKED __FTRSCAN_ROLL_FRAME_PARAMETERS
{
    FTR_DWORD                           dwSize;
    FTR_DWORD                           dwFlags;
    FTR_DWORD                           dwStatus;
    FTR_DWORD                           dwRollingResult;
    FTR_DWORD                           dwDirection;
    FTR_DWORD                           dwFrameIndex;
    FTR_DWORD                           dwFrameDose;
    FTR_DWORD                           dwFrameContrast;
    FTR_DWORD                           dwFrameTimeMs;
} FTRSCAN_ROLL_FRAME_PARAMETERS, *PFTRSCAN_ROLL_FRAME_PARAMETERS;

#define FTR_LFD_MODE_HW                                 0x00000001
#define FTR_LFD_MODE_SW_1                               0x00000002

#define FTR_LFD_MODE_SW_1_MAX_STRENGTH                  9

#define FTR_LFD_LEVEL_1                                 FTR_LFD_MODE_HW  /* default */
#define FTR_LFD_LEVEL_2                                 (FTR_LFD_MODE_HW | FTR_LFD_MODE_SW_1)
#define FTR_LFD_LEVEL_MAX                               (FTR_LFD_MODE_HW | FTR_LFD_MODE_SW_1)

#if defined(__WIN32__)
#pragma pack(pop)
#endif

FTR_API_PREFIX FTRHANDLE FTR_API ftrScanOpenDevice();
FTRHANDLE FTR_API ftrScanOpenDeviceOnInterface( int nInterface );
FTR_API_PREFIX FTRHANDLE FTR_API ftrScanOpenDeviceWithIoContext( FTR_PVOID pContext );

FTR_API_PREFIX void FTR_API ftrScanCloseDevice( FTRHANDLE ftrHandle );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSetOptions( FTRHANDLE ftrHandle, FTR_DWORD dwMask, FTR_DWORD dwFlags );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetOptions( FTRHANDLE ftrHandle, FTR_PDWORD lpdwFlags );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetDeviceInfo( FTRHANDLE ftrHandle, PFTRSCAN_DEVICE_INFO pDeviceInfo );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetInterfaces( PFTRSCAN_INTERFACES_LIST pInterfaceList );
FTR_API_PREFIX FTR_BOOL FTR_API ftrSetBaseInterface( int nBaseInterface );
FTR_API_PREFIX int FTR_API ftrGetBaseInterfaceNumber();
FTR_API_PREFIX FTR_BOOL FTR_API ftrSetLoggingFacilityLevel( FTR_DWORD dwLogMask, FTR_DWORD dwLogLevel, char *lpFileName );

FTR_API_PREFIX FTR_DWORD FTR_API ftrScanGetLastError();
FTR_API_PREFIX void FTR_API ftrScanSetLastError( FTR_DWORD dwErrCode );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetVersion( FTRHANDLE ftrHandle, PFTRSCAN_VERSION_INFO pVersionInfo );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanIsScannerFeaturePresent( FTRHANDLE ftrHandle, int nScannerFeature, FTR_PBOOL pIsPresent );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetFakeReplicaInterval( double *pdblMinFakeReplicaValue, double *pdblMaxFakeReplicaValue );
FTR_API_PREFIX void FTR_API ftrScanSetFakeReplicaInterval( double dblMinFakeReplicaValue, double dblMaxFakeReplicaValue );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetLFDParameters( PFTRSCAN_LFD_CONSTANTS pLFDParameters );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSetLFDParameters( PFTRSCAN_LFD_CONSTANTS pLFDParameters );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetScanParameters( FTRHANDLE ftrHandle, FTR_DWORD dwParamCode, FTR_PVOID pOutBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImageSize( FTRHANDLE ftrHandle, PFTRSCAN_IMAGE_SIZE pImageSize );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImage( FTRHANDLE ftrHandle, int nDose, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImage2( FTRHANDLE ftrHandle, int nDose, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetFuzzyImage( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetBacklightImage( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetDarkImage( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImageByVariableDose( FTRHANDLE ftrHandle, int nVariableDose, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGet4in1Image( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImageByVariableDoseEx( FTRHANDLE ftrHandle, int nVariableDose, FTR_BYTE byLights, PFTRSCAN_VAR_DOSE_EXTRA_PARAMS pExtraParams, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetRawImageSize( FTRHANDLE ftrHandle, PFTRSCAN_IMAGE_SIZE pImageSize );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetRawImageByVariableDose( FTRHANDLE ftrHandle, int nVariableDose, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanIsFingerPresent( FTRHANDLE ftrHandle, PFTRSCAN_FRAME_PARAMETERS pFrameParameters );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetFrame( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, PFTRSCAN_FRAME_PARAMETERS pFrameParameters );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSave7Bytes( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRestore7Bytes( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetExtMemorySize( FTRHANDLE ftrHandle, int *pnSize );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSaveExtMemory( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, int nOffset, int nCount );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRestoreExtMemory( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, int nOffset, int nCount );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetSerialNumber( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSaveSerialNumber( FTRHANDLE ftrHandle, FTR_PVOID pReserved );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetFirmwareMemorySize( FTRHANDLE ftrHandle, int *pnSize );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSaveFirmwareMemory( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, int nOffset, int nCount );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRestoreFirmwareMemory( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, int nOffset, int nCount );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetCalibrationConstants( FTRHANDLE ftrHandle, FTR_PBYTE pbyIRConst, FTR_PBYTE pbyFuzzyConst );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanStoreCalibrationConstants( FTRHANDLE ftrHandle, FTR_BYTE byIRConst, FTR_BYTE byFuzzyConst, FTR_BOOL bBurnToFlash );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetFakeReplicaParameters( FTRHANDLE ftrHandle, PFTRSCAN_FAKE_REPLICA_PARAMETERS pFakeReplicaParams );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetFakeReplicaBuffer( FTRHANDLE ftrHandle, PFTRSCAN_FAKE_REPLICA_BUFFER pFakeReplicaBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSetNewAuthorizationCode( FTRHANDLE ftrHandle, FTR_PVOID pSevenBytesAuthorizationCode );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSaveSecret7Bytes( FTRHANDLE ftrHandle, FTR_PVOID pSevenBytesAuthorizationCode, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRestoreSecret7Bytes( FTRHANDLE ftrHandle, FTR_PVOID pSevenBytesAuthorizationCode, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSetDiodesStatus( FTRHANDLE ftrHandle, FTR_BYTE byGreenDiodeStatus, FTR_BYTE byRedDiodeStatus );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetDiodesStatus( FTRHANDLE ftrHandle, FTR_PBOOL pbIsGreenDiodeOn, FTR_PBOOL pbIsRedDiodeOn );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSave7ControlBytes( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, FTR_BOOL bBurnToFlash );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRestore7ControlBytes( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollStart( FTRHANDLE ftrHandle );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollStarWithVariableDose( FTRHANDLE ftrHandle, int nVariableDose );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollRawStart( FTRHANDLE ftrHandle );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollRawStarWithVariableDose( FTRHANDLE ftrHandle, int nVariableDose );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollAbort( FTRHANDLE ftrHandle, FTR_BOOL bSynchronous );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollGetImage( FTRHANDLE ftrHandle, FTR_PVOID pBuffer, FTR_DWORD dwMilliseconds );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanRollGetFrameParameters( FTRHANDLE ftrHandle, PFTRSCAN_ROLL_FRAME_PARAMETERS pFrameParameters, FTR_PVOID pBuffer, FTR_DWORD dwMilliseconds );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanChangeSystemNotification( FTR_DWORD dwMask, FTR_DWORD dwFlags );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGlobalSetOptions( FTR_DWORD dwOption, FTR_PVOID pOptionData );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGlobalGetOptions( FTR_DWORD dwOption, FTR_PVOID pOptionData );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetStripImageSize( FTRHANDLE ftrHandle, PFTRSCAN_IMAGE_SIZE pImageSize );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetStripImageByVariableDose( FTRHANDLE ftrHandle, int nVariableDose, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetRegistryValues( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanConvertRawToFinalImage( FTRHANDLE ftrHandle, FTR_PVOID pRawImageBuffer, FTR_PVOID pFinalImageBuffer, int nDose );

FTR_API_PREFIX FTR_BOOL FTR_API ftrSweepGetSlice( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrSweepGetMultipleSlices( FTRHANDLE ftrHandle, int nSlices, FTR_PVOID pBuffer );

#define FTR_BLACKFIN_MAX_WRITE_DATA_LEN        4096
FTR_API_PREFIX FTR_BOOL FTR_API ftrBlackfinDataExchange( FTRHANDLE ftrHandle, FTR_PVOID pWriteBuffer, int nWriteBufferLength, FTR_PVOID pReadBuffer, int nReadBufferLength );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanControlPin3( FTRHANDLE ftrHandle, FTR_DWORD *pdwParam1, FTR_DWORD dwParam2, FTR_DWORD dwPeriod );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetButtonState( FTRHANDLE ftrHandle, FTR_DWORD *pdwParam1 );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanMainLEDsTimeout( FTRHANDLE ftrHandle, FTR_DWORD *pwdParam1 ,FTR_BYTE byFlag );

FTR_API_PREFIX void FTR_API ftrSetGlobalDeviceSync( FTR_BOOL fSet );

FTR_API_PREFIX FTR_BOOL FTR_API ftrSweepGetSlice( FTRHANDLE ftrHandle, FTR_PVOID pBuffer );

FTR_API_PREFIX FTR_BOOL FTR_API ftrInternalDeviceIoExchange( FTRHANDLE ftrHandle, FTR_PVOID pData );

FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetProperty( FTRHANDLE ftrHandle, int nProperty, FTR_PVOID pPropertyData );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImageSizes( FTRHANDLE ftrHandle, PFTRSCAN_IMAGE_SIZE pArrayOfImageSizes );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanGetImageOfSpecificSize( FTRHANDLE ftrHandle, int nVariableDose, FTR_BYTE byLights, int nWidth, int nHeight, FTR_PVOID pBuffer );
FTR_API_PREFIX FTR_BOOL FTR_API ftrScanSetProperty( FTRHANDLE ftrHandle, int nProperty, FTR_PVOID pPropertyData );

#ifdef __cplusplus
}
#endif

#endif // __FUTRONIC_SCAN_API_H__
