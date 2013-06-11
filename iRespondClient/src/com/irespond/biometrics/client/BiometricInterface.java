package com.irespond.biometrics.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import android.graphics.Bitmap;

/**
 * The BiometricInterface is used to set up the
 * subsequently called functions in the iRespond
 * library.
 * 
 * @author grahamb5
 * @author angela18
 */
public class BiometricInterface {
	/** All the possible request types. */
	public static enum RequestType {
		IDENTIFY, VERIFY, ENROLL
	}
	
	/** The current request type. */
	protected static RequestType mRequest = null;
	
	/** The collection of biometric IDs to verify against. */
	public static Collection<UUID> mVerifyUuids = null;
	
	/** The collection of WSQ images to send to the server for enrollment. */
	public static Collection<byte[]> mEnrollImages = null;
	
	/* The different messages to send to the IrespondActivity */
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
    
    /** The result of a biometric identification. */
    public static UUID mIdentifyResult;
    
    /**
     * Readies the iRespond Library to perform an identification
     * on the next scan.
     */
    public static void identify() {
    	mRequest = RequestType.IDENTIFY;
    }
    
    /**
     * Readies the iRespond Library to perform a verification against
     * the given biometric ID on the next scan.
     * 
     * @param uuid the biometric ID to verify against.
     */
    public static void verify(UUID uuid) {
    	mRequest = RequestType.VERIFY;
    	mVerifyUuids = new HashSet<UUID>();
    	mVerifyUuids.add(uuid);
    }
    
    /**
     * Readies the iRespond Library to perform a verification against
     * the given biometric IDs on the next scan.
     * 
     * @param uuids The biometric IDs to verify against.
     */
    public static void verify(Collection<UUID> uuids) {
    	mRequest = RequestType.VERIFY;
    	mVerifyUuids = new HashSet<UUID>(uuids);
    }
}
