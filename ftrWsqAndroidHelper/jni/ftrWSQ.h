#ifndef __ftrWSQ_h__
#define __ftrWSQ_h__
#include "ftrScanAPI.h"
#if defined( FTR_OS_UNIX )
# ifndef FTRWSQAPI
#   define FTRWSQAPI 
# endif
#else
# ifndef FTRWSQAPI
#   define FTRWSQAPI   __declspec(dllimport) WINAPI
# endif
#endif



/*
 *    API functions prototypes
 */
#ifdef __cplusplus
extern "C" { /* assume C declarations for C++ */
#endif

//----------------------------------------------------------------------------

#if defined(__WIN32__)
#pragma pack(push, 1)
#endif

typedef struct FTR_PACKED
{
 FTR_DWORD Width;       // image Width
 FTR_DWORD Height;      // image Height
 FTR_DWORD DPI;         // resolution Dots per inch
 FTR_DWORD RAW_size;    // size of RAW image
 FTR_DWORD BMP_size;    // size of BMP image
 FTR_DWORD WSQ_size;    // size of WSQ image
 float        Bitrate;  // compression
} FTRIMGPARMS, *LPFTRIMGPARMS;

#if defined(__WIN32__)
#pragma pack(pop)
#endif


/*
  ftrWSQ_GetImageParameters    - load WSQ parameters[Width,Height...] from WSQ image
  please call it before ftrWSQ_ToRawImage for allocate right size of memory for output file
  Args  
            ftrWSQbuf       [in]    - pointer to WSQ image
            ImPar.WSQ_size  [in]
            ImPar           [out]   - pointer to FTRIMGPARMS structure
  Return                       - TRUE is OK, FALSE - error
*/
FTR_BOOL FTRWSQAPI ftrWSQ_GetImageParameters(FTR_BYTE *ftrWSQbuf, LPFTRIMGPARMS ftrImPar);
/*
  ftrWSQ_GetDQTTable       - load WSQ parameters[Width,Height,DQTTable,o_quant,...] from WSQ image
  for WSQ compliance only
  Args  
            ftrWSQbuf  [in]    - pointer to WSQ image
            ImPar.WSQ_size [in]
            ImPar      [out]   - pointer to FTRIMGPARMS structure
            DQTTable    [out]
            o_quant     [out]
            o_size      [out]
  Return                       - TRUE is OK, FALSE - error
*/
FTR_BOOL FTRWSQAPI ftrWSQ_GetDQTTable(FTR_BYTE *ftrWSQbuf, LPFTRIMGPARMS ftrImPar, float * DQTTable,FTR_INT16 *o_quant,int*o_size);
//--------------------------------------------------------------------------


/*
  WSQ_ToRawImage                - convert image from WSQ format â RAW format
			 ftrWSQbuf  [in]    -  pointer to WSQ image
             ImPar.WSQ_size [in] - size of WSQ image
             ImPar      [out]   - pointer to FTRIMGPARMS structure
             RAWbuf     [out]   -  pointer to RAW image with size RAW_size after WSQ_GetImageParameters
  Return                        - TRUE is OK, FALSE - error
*/
FTR_BOOL FTRWSQAPI ftrWSQ_ToRawImage(FTR_BYTE *ftrWSQbuf, LPFTRIMGPARMS ftrImPar, FTR_BYTE *RAWbuf);
//--------------------------------------------------------------------------


/*
  WSQ_FromRAWImage                      - Convert image from RAW(8bit grayscale) â WSQ
			 RAWbuf         [in]        - pointer to RAW image
             ImPar.Width    [in]
             ImPar.Height   [in]
             ImPar.DPI      [in]
             ImPar.Bitrate  [in]
             ImPar.WSQ_size [out]       - pointer to FTRIMGPARMS structure
             ftrWSQbuf      [out]       - pointer to WSQ image ,size must be  same as RAWbuf
  Return                                - TRUE is OK, FALSE - error
*/
FTR_BOOL FTRWSQAPI ftrWSQ_FromRAWImage(FTRHANDLE ftrHandle,FTR_BYTE *RAWbuf, LPFTRIMGPARMS ftrImPar, FTR_BYTE *ftrWSQbuf);
//--------------------------------------------------------------------------

#ifdef __cplusplus
};
#endif
//--------------------------------------------------------------------------
#endif //__ftrWSQ_h__
