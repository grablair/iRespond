package com.irespond.biometrics.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import android.graphics.Bitmap;

public class BiometricInterface {
	public static enum RequestType {
		IDENTIFY, VERIFY, ENROLL
	}
	
	protected static RequestType mRequest = null;
	public static Collection<UUID> mVerifyUuids = null;
	
	public static Collection<byte[]> mEnrollImages = null;
	
	public static final int MESSAGE_SHOW_MSG = 1;
    public static final int MESSAGE_SHOW_SCANNER_INFO = 2;
    public static final int MESSAGE_SHOW_IMAGE = 3;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_TRACE = 5;
    
    public static boolean mUsbHostMode = true;
    
    public static boolean mStop = true;
    
    public static byte[] mImageFP = null;    
    public static int mImageWidth = 0;
    public static int mImageHeight = 0;
    public static Bitmap mBitmapFP = null;
    
    public static UUID mIdentifyResult;
    
    public static void identify() {
    	mRequest = RequestType.IDENTIFY;
    }
    
    public static void verify(UUID uuid) {
    	mRequest = RequestType.VERIFY;
    	mVerifyUuids = new HashSet<UUID>();
    	mVerifyUuids.add(uuid);
    }
    
    public static void verify(Collection<UUID> uuids) {
    	mRequest = RequestType.VERIFY;
    	mVerifyUuids = new HashSet<UUID>(uuids);
    }
}
