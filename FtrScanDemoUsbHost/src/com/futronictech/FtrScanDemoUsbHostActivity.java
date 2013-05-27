package com.futronictech;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FtrScanDemoUsbHostActivity extends Activity {
    /** Called when the activity is first created. */
	private static Button mButtonScan;
	private static Button mButtonStop;
	private Button mButtonSave;
	private static TextView mMessage;
	private static TextView mScannerInfo;
	private static TextView mImageRating;
	private static ImageView mFingerImage;
	
    public static boolean mStop = false;
    
    public static final int MESSAGE_SHOW_MSG = 1;
    public static final int MESSAGE_SHOW_SCANNER_INFO = 2;
    public static final int MESSAGE_SHOW_IMAGE = 3;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_TRACE = 5;

    public static byte[] mImageFP = null;    
    public static int mImageWidth = 0;
    public static int mImageHeight = 0;
    private static Bitmap mBitmapFP = null;

    private FPScan mFPScan = null;   
    //
    public static boolean mUsbHostMode = true;

    // Intent request codes
    private static final int REQUEST_FILE_FORMAT = 1;
    private UsbDeviceDataExchangeImpl usb_host_ctx = null;
    
    private boolean verifying = false;
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	
    	mButtonScan = (Button) findViewById(R.id.btnScan);
        mButtonStop = (Button) findViewById(R.id.btnStop);
        mButtonSave = (Button) findViewById(R.id.btnSave);
        mMessage = (TextView) findViewById(R.id.tvMessage);
        mScannerInfo = (TextView) findViewById(R.id.tvScannerInfo);
        mFingerImage = (ImageView) findViewById(R.id.imageFinger);


        usb_host_ctx = new UsbDeviceDataExchangeImpl(this, mHandler);

        mButtonScan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {	        		
	        		if( mFPScan != null )
	        		{
	        			mStop = true;
	        			mFPScan.stop();
	        			
	        		}
	        		mStop = false;
	        		usb_host_ctx.CloseDevice();
	        		if(usb_host_ctx.OpenDevice(0, true))
	                {
	        			if( StartScan() )
		        		{
		        			mButtonScan.setEnabled(false);
		        	        mButtonSave.setEnabled(false);
		        	        mButtonStop.setEnabled(true);
		        		}	
	                }
	            	else
	            	{
	            		if(!usb_host_ctx.IsPendingOpen())
	            		{
	            			mMessage.setText("Can not start scan operation.\nCan't open scanner device");
	            		}
	            	}    
        		}
        });
        
        mButtonStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        		mStop = true;	       
	        		if( mFPScan != null )
	        		{
	        			mFPScan.stop();
	        			mFPScan = null;
       			
	        		}	        		
	        		mButtonScan.setEnabled(true);
	        		mButtonSave.setEnabled(true);
	        		mButtonStop.setEnabled(false);	        		
        		}
        });
        
        mButtonSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {   
            	if( mImageFP != null)
            		SaveImage();
            }
        });
        


    }
    
    
    public native String WSQString();
    public native String bitmapString();
    public native String unimmplementedStringFromJNI();
    
    static {
    	System.loadLibrary("FtrScanDemoUsbHostActivity");
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		mStop = true;	       
		if( mFPScan != null )
		{
			mFPScan.stop();
			mFPScan = null;
		}	        		
		usb_host_ctx.CloseDevice();
		usb_host_ctx.Destroy();
    }

	private boolean StartScan()
    {
		mFPScan = new FPScan(usb_host_ctx, mHandler);
		mFPScan.start();
		
		return true;
    }
    
    private void SaveImage()
    {
//	    File extStorageDirectory = Environment.getExternalStorageDirectory();
//        mDir = new File(extStorageDirectory, "Android//FtrScanDemo"); 
//        mDir.mkdirs();
//        mDir.getAbsolutePath();
        
        // save wsq file
        Scanner devScan = new Scanner();
		boolean bRet;
		if( mUsbHostMode )
			bRet = devScan.OpenDeviceOnUsbHostContext(usb_host_ctx, false);
		else
			bRet = devScan.OpenDevice();
		if( !bRet )
		{
            mMessage.setText(devScan.GetErrorMessage());
            return;    			
		}
		byte[] wsqImg = new byte[mImageWidth*mImageHeight];
		long hDevice = devScan.GetDeviceHandle();
		ftrWsqAndroidHelper wsqHelper = new ftrWsqAndroidHelper();
		
		// byte array created
		if( wsqHelper.ConvertRawToWsq(hDevice, mImageWidth, mImageHeight, 2.25f, mImageFP, wsqImg) )
		{
			try {
			int wsqLength = wsqImg.length;
		
			Socket sendChannel = new Socket("localhost", 12345);
			OutputStream writer = sendChannel.getOutputStream();			
			InputStream reader = sendChannel.getInputStream();
			DataInputStream dis = new DataInputStream(reader);
			
			// call either the verify or identify method
			if (verifying) {
				//verify (wsqImg, wsqLength, writer, dis);
			} else {
				identify (wsqImg, wsqLength, writer, dis);
			}
			} catch (IOException ioe) {
				// TODO
			}
						
		}
		else
			mMessage.setText("Failed to convert the image!");
		if( mUsbHostMode )
			devScan.CloseDeviceUsbHost();
		else
			devScan.CloseDevice();
		return;
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
    	boolean verified;
    	
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
            case MESSAGE_SHOW_MSG:            	
            	String showMsg = (String) msg.obj;
                mMessage.setText(showMsg);
                break;
            case MESSAGE_SHOW_SCANNER_INFO:            	
            	String showInfo = (String) msg.obj;
                mScannerInfo.setText(showInfo);
                break;
            case MESSAGE_SHOW_IMAGE:
            	ShowBitmap();
                break;              
            case MESSAGE_ERROR:
           		//mFPScan = null;
            	mButtonScan.setEnabled(true);
            	mButtonStop.setEnabled(false);
            	break;
            case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE:
            	if(usb_host_ctx.ValidateContext())
            	{
            		if( StartScan() )
	        		{
	        			mButtonScan.setEnabled(false);
	        	        mButtonSave.setEnabled(false);
	        	        mButtonStop.setEnabled(true);
	        		}	
            	}
            	else
            		mMessage.setText("Can't open scanner device");
            	break;           
	        case UsbDeviceDataExchangeImpl.MESSAGE_DENY_DEVICE:
            	mMessage.setText("User deny scanner device");
            	break;
            }
        }
    };
    
    private static void ShowBitmap()
    {
    	int[] pixels = new int[mImageWidth * mImageHeight];
    	for( int i=0; i<mImageWidth * mImageHeight; i++)
    		pixels[i] = mImageFP[i];
    	Bitmap emptyBmp = Bitmap.createBitmap(pixels, mImageWidth, mImageHeight, Config.RGB_565);

        int width, height; 
        height = emptyBmp.getHeight(); 
        width = emptyBmp.getWidth();     
     
        mBitmapFP = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); 
        Canvas c = new Canvas(mBitmapFP); 
        Paint paint = new Paint(); 
        ColorMatrix cm = new ColorMatrix(); 
        cm.setSaturation(0); 
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm); 
        paint.setColorFilter(f); 
        c.drawBitmap(emptyBmp, 0, 0, paint); 
        
        mFingerImage.setImageBitmap(mBitmapFP);
        
        byte[][] image2d = new byte[mImageHeight][mImageWidth];
        for (int i = 0; i < mImageWidth * mImageHeight; i++) {
        	image2d[i / mImageWidth][i % mImageWidth] = mImageFP[i];
        }
    }        

    
    
//    private static void SendWSQ(File f) {
//    	Socket sock = new Socket ("",);
//    	FileInputStream fis = new FileInputStream(f);
//    	OutputStream os = socket.getOutput
//    	
//    }
}