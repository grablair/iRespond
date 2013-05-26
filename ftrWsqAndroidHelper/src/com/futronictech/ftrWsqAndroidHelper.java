package com.futronictech;

public class ftrWsqAndroidHelper {
	public int mWidth;       // image Width
    public int mHeight;      // image Height
    public int mDPI;         // resolution Dots per inch
    public int mRAW_size;    // size of RAW image
    public int mBMP_size;    // size of BMP image
    public int mWSQ_size;    // size of WSQ image
    public float mBitrate;   // compression
	
    static {
    	System.loadLibrary("usb-1.0");
    }
    static {
    	System.loadLibrary("ftrScanAPI");
    }
	static {    
		System.loadLibrary("ftrWSQAndroid");  
		}   
	static {    
		System.loadLibrary("ftrWSQAndroidJni");  
		}   
	
	private native boolean JNIRawToWsqImage(long hDevice, int nWidth, int nHeight, float fBitrate, byte[] rawImg, byte[] wsqImg);
	private native boolean JNIGetImageParameters(byte[] wsqImg);
	private native boolean JNIWsqToRawImage(byte[] wsqImg, byte[] rawImg);
	
	public ftrWsqAndroidHelper()
	{
		mWidth = mHeight = 0;
		mDPI = mRAW_size = mBMP_size = mWSQ_size = 0;
		mBitrate = 0.75f;	
	}
	
	//wsqImg - assign nWidth*nHeight bytes array
	//the real size of WSQ image will be hold in mWSQ_size after returned from JNIRawToWsqImage.
	public boolean ConvertRawToWsq(long hDevice, int nWidth, int nHeight, float fBitrate, byte[] rawImg, byte[] wsqImg)
	{
		if(rawImg.length != (nWidth*nHeight))
			return false;
		if(wsqImg.length != (nWidth*nHeight))
			return false;
		if( fBitrate > 2.25 || fBitrate < 0.75)
			return false;
		
		return JNIRawToWsqImage(hDevice, nWidth, nHeight, fBitrate, rawImg, wsqImg);			
	}

	public int GetWsqImageRawSize(byte[] wsqImg)
	{
		if( !JNIGetImageParameters(wsqImg) )
			return 0;
		return (mWidth * mHeight);
	}
	
	public boolean ConvertWsqToRaw(byte[] wsqImg, byte[] rawImg)
	{
		if(rawImg.length < (mWidth*mHeight))
			return false;
		return JNIWsqToRawImage(wsqImg, rawImg);			
	}
	

}
