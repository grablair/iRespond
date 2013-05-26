package com.irespond.biometrics.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.futronictech.UsbDeviceDataExchangeImpl;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class IdentifyActivity extends Activity {
    /** Called when the activity is first created. */
	private static Button mButtonScan;
	private static ImageView mFingerImage;
	private static ProgressBar mProgressBar;

    private FPScan mFPScan = null;
    
    // Intent request codes
    private UsbDeviceDataExchangeImpl usb_host_ctx = null;
        
    private Activity thisActivity;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = this;
        setContentView(R.layout.activity_main);
        
        mButtonScan = (Button) findViewById(R.id.scanButton);
        mFingerImage = (ImageView) findViewById(R.id.fingerImage);
        mProgressBar = (ProgressBar) findViewById(R.id.loadingBar);
        
        mButtonScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mButtonScan.setEnabled(false);
				showProgress(true);
			}
        });
        
        usb_host_ctx = new UsbDeviceDataExchangeImpl(this, mHandler);

        if(mFPScan != null) {
			BiometricInterface.mStop = true;
			mFPScan.stop();
			
		}
        
		BiometricInterface.mStop = false;
		usb_host_ctx.CloseDevice();
		if(usb_host_ctx.OpenDevice(0, true)) {
			if(StartScan()) {
				Toast.makeText(thisActivity, "Place finger on scanner.", Toast.LENGTH_LONG).show();
    		}	
        } else {
    		if(!usb_host_ctx.IsPendingOpen()) {
    			Toast.makeText(this, "Can not start scan operation. Can't open scanner device", Toast.LENGTH_LONG).show();
    		}
    	}    
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		BiometricInterface.mStop = true;	       
		if( mFPScan != null )
		{
			mFPScan.stop();
			mFPScan = null;
		}	        		
		usb_host_ctx.CloseDevice();
		usb_host_ctx.Destroy();
    }

	private boolean StartScan() {
		mFPScan = new FPScan(usb_host_ctx, mHandler);
		mFPScan.start();
		
		return true;
    }
    
    private UUID identify(byte[] wsqImg, int wsqLength, OutputStream o, DataInputStream dis) throws IOException {
		// create byte[] to send
    	byte[] wsqAndLength = new byte[wsqLength + 4 + 1];		
		// convert length of byte array to byte array
		ByteBuffer b = ByteBuffer.allocate(4);
		//b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		b.putInt(0xAABBCCDD);
		byte[] length = b.array();
		
		// put the length and wsq into the array
		for (int n = 0; n < 4; n++) {
			wsqAndLength[n] = length[n];
		}		
		for (int i1 = 0; i1 < wsqLength; i1++) {
			wsqAndLength[i1 + 4] = wsqImg[i1];
		}
		// send the packet to the server
		o.write(wsqAndLength);
        o.flush();
        
        // receives the ensuing packet from the server
        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        
        // failed to get UUID, do something 
        if (data.length == 1) {
        	
        } else {
        	// read UUID from byte[]
        	byte[] UUIDarray = new byte[data.length - 1];
        	for (int n = 0; n < UUIDarray.length; n++) {
        		UUIDarray[n] = data[n + 1];
        	}
        	UUID id = UUID.nameUUIDFromBytes(UUIDarray);
        	return id;
        }
        return null;
    	
    }
    
    private boolean verify(byte[] wsqImg, int wsqLength, OutputStream o, DataInputStream dis, byte[] UUIDs) throws IOException {    	
		byte[] wsqAndLength = new byte[wsqLength + 4];

		// convert length of byte array to byte arra
		ByteBuffer b = ByteBuffer.allocate(4);
		//b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		b.putInt(0xAABBCCDD);
		byte[] length = b.array();
		
		wsqAndLength[0] = new Byte("0x02");
		
		// put the length and wsq into the array
		for (int n = 0; n < 4; n++) {
			wsqAndLength[n + 1] = length[n];
		}
		
		for (int i1 = 0; i1 < wsqLength; i1++) {
			wsqAndLength[i1 + 5] = wsqImg[i1];
		}
    	
		// put number of UUIDs and UUIDs into the array
		
		// send the packet to the server
		o.write(wsqAndLength);
        o.flush();
        
        // receives the ensuing packet from the server
        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
		
    	return true;
    	
    }
    
    // The Handler that gets information back from the FPScan
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BiometricInterface.MESSAGE_SHOW_MSG:            	
            	String showMsg = (String) msg.obj;
            	Toast.makeText(thisActivity, showMsg, Toast.LENGTH_LONG).show();
                break;
            case BiometricInterface.MESSAGE_SHOW_SCANNER_INFO:            	
            	String showInfo = (String) msg.obj;
            	Toast.makeText(thisActivity, showInfo, Toast.LENGTH_LONG).show();
                break;
            case BiometricInterface.MESSAGE_SHOW_IMAGE:
            	ShowBitmap();
                break;              
            case BiometricInterface.MESSAGE_ERROR:
           		//mFPScan = null;
            	mButtonScan.setEnabled(true);
            	break;
            case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE:
            	if(usb_host_ctx.ValidateContext()) {
            		if(StartScan()) {
	        			Toast.makeText(thisActivity, "Place finger on scanner.", Toast.LENGTH_LONG).show();
	        		}	
            	} else
            		Toast.makeText(thisActivity, "Can't open scanner device", Toast.LENGTH_LONG).show();
            	break;           
	        case UsbDeviceDataExchangeImpl.MESSAGE_DENY_DEVICE:
	        	Toast.makeText(thisActivity, "User deny scanner device", Toast.LENGTH_LONG).show();
            	break;
            }
        }
    };
    
    private static void ShowBitmap() {
    	int[] pixels = new int[BiometricInterface.mImageWidth * BiometricInterface.mImageHeight];
    	for( int i=0; i<BiometricInterface.mImageWidth * BiometricInterface.mImageHeight; i++)
    		pixels[i] = BiometricInterface.mImageFP[i];
    	Bitmap emptyBmp = Bitmap.createBitmap(pixels, BiometricInterface.mImageWidth, BiometricInterface.mImageHeight, Config.RGB_565);

        int width, height; 
        height = emptyBmp.getHeight(); 
        width = emptyBmp.getWidth();     
     
        BiometricInterface.mBitmapFP = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); 
        Canvas c = new Canvas(BiometricInterface.mBitmapFP); 
        Paint paint = new Paint(); 
        ColorMatrix cm = new ColorMatrix(); 
        cm.setSaturation(0); 
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm); 
        paint.setColorFilter(f); 
        c.drawBitmap(emptyBmp, 0, 0, paint); 
        
        mFingerImage.setImageBitmap(BiometricInterface.mBitmapFP);
        
        byte[][] image2d = new byte[BiometricInterface.mImageHeight][BiometricInterface.mImageWidth];
        for (int i = 0; i < BiometricInterface.mImageWidth * BiometricInterface.mImageHeight; i++) {
        	image2d[i / BiometricInterface.mImageWidth][i % BiometricInterface.mImageWidth] = BiometricInterface.mImageFP[i];
        }
    }     

    
    
    /**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mProgressBar.setVisibility(View.VISIBLE);
			mProgressBar.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressBar.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mFingerImage.setVisibility(View.VISIBLE);
			mFingerImage.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mFingerImage.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mFingerImage.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
