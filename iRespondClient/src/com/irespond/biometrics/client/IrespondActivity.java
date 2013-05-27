package com.irespond.biometrics.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.futronictech.Scanner;
import com.futronictech.UsbDeviceDataExchangeImpl;
import com.futronictech.ftrWsqAndroidHelper;
import com.irespond.biometrics.client.BiometricInterface.RequestType;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class IrespondActivity extends Activity {
	private static final int ENROLL_IMAGE_COUNT = 3;
	
	/** Called when the activity is first created. */
	private static Button mButtonScan;
	private static ImageView mFingerImage;
	private static ProgressBar mProgressBar;

	private static FPScan mFPScan = null;

	// Intent request codes
	private static UsbDeviceDataExchangeImpl usb_host_ctx = null;

	private static Activity thisActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (BiometricInterface.mRequest == null) {
			Toast.makeText(this, "An invalid function was called.", Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}
		
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
				mFPScan.stop();

				// Convert final image to WSQ.
				Scanner devScan = new Scanner();
				boolean bRet;
				if (BiometricInterface.mUsbHostMode)
					bRet = devScan.OpenDeviceOnUsbHostContext(usb_host_ctx, false);
				else
					bRet = devScan.OpenDevice();
				if (!bRet) {
					Toast.makeText(thisActivity, devScan.GetErrorMessage(), Toast.LENGTH_LONG).show();
					return;    			
				}
				byte[] wsqImg = new byte[BiometricInterface.mImageWidth*BiometricInterface.mImageHeight];
				long hDevice = devScan.GetDeviceHandle();
				ftrWsqAndroidHelper wsqHelper = new ftrWsqAndroidHelper();
				
				// byte array created
				if (wsqHelper.ConvertRawToWsq(hDevice, BiometricInterface.mImageWidth, BiometricInterface.mImageHeight, 2.25f, BiometricInterface.mImageFP, wsqImg)) {

					switch(BiometricInterface.mRequest) {
					case IDENTIFY:
						// Do identify function.
						ServerInterface.identify(wsqImg, new ServerCallback<UUID>() {
							@Override
							public void onSuccess(UUID result) {
								if (result != null) {
									// Success. Set the BiometricInterface identify result field and
									// return OK to the caller.
									BiometricInterface.mIdentifyResult = result;
									setResult(RESULT_OK);
									finish();
								} else {
									// Prompt the user to either try again or enroll as a new user.
									DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
									    @Override
									    public void onClick(DialogInterface dialog, int which) {
									        switch (which){
									        case DialogInterface.BUTTON_POSITIVE:
									        	// Try again.
									        	mButtonScan.setEnabled(true);
												initScanMode();
									            break;
									        case DialogInterface.BUTTON_NEGATIVE:
									        	// Enroll as new user, start that process.
									            BiometricInterface.mRequest = RequestType.ENROLL;
									            BiometricInterface.mEnrollImages = null;
									            mButtonScan.setEnabled(true);
												initScanMode();
												Toast.makeText(thisActivity, "0 / " + ENROLL_IMAGE_COUNT +
														" images taken for enrollment.", Toast.LENGTH_LONG).show();
									            break;
									        }
									    }
									};

									AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
									builder.setMessage("No match was found. Would you like to try again or enroll this person in the system?")
										.setPositiveButton("Try again", dialogClickListener)
									    .setNegativeButton("Enroll", dialogClickListener).show();
									
									
								}
							}

							@Override
							public void onFailure(String errorMessage) {
								// Error.
								mButtonScan.setEnabled(true);
								Toast.makeText(thisActivity, errorMessage, Toast.LENGTH_LONG).show();
								initScanMode();
							}
						});
						break;
						
					case VERIFY:
						// Do verify function.
						ServerInterface.verify(BiometricInterface.mVerifyUuids, wsqImg, new ServerCallback<Boolean>() {
							@Override
							public void onSuccess(Boolean result) {
								if (result) {
									// Verification succeeded. Return OK to caller.
									setResult(RESULT_OK);
									finish();
								} else {
									// Verification failed. Have user try again.
									mButtonScan.setEnabled(true);
									Toast.makeText(thisActivity, "Verification unsuccessful. Pleas try again.", Toast.LENGTH_LONG).show();
									initScanMode();
								}
							}

							@Override
							public void onFailure(String errorMessage) {
								// Error.
								mButtonScan.setEnabled(true);
								Toast.makeText(thisActivity, errorMessage, Toast.LENGTH_LONG).show();
								initScanMode();
							}
						});
						break;
					case ENROLL:
						// Create a new set of images, if necessary.
						if (BiometricInterface.mEnrollImages == null)
							BiometricInterface.mEnrollImages = new HashSet<byte[]>();
							
						// Add the latest WSQ file to the collection.
						BiometricInterface.mEnrollImages.add(wsqImg);
						
						if (BiometricInterface.mEnrollImages.size() == ENROLL_IMAGE_COUNT) {
							// We're up to our enrollment count.
							Collection<byte[]> enrollImages = BiometricInterface.mEnrollImages;
							BiometricInterface.mEnrollImages = null;
							
							ServerInterface.enroll(enrollImages, new ServerCallback<UUID>() {
								@Override
								public void onSuccess(UUID result) {
									// Enrollment succeeded. Set the identify result and respond OK.
									BiometricInterface.mIdentifyResult = result;
									setResult(RESULT_OK);
									finish();
								}

								@Override
								public void onFailure(String errorMessage) {
									// Error.
									mButtonScan.setEnabled(true);
									Toast.makeText(thisActivity, errorMessage, Toast.LENGTH_LONG).show();
									initScanMode();
								}
							});
						} else {
							// We need more enrollment images.
							mButtonScan.setEnabled(true);
							Toast.makeText(thisActivity, "" + BiometricInterface.mEnrollImages.size() + " / " + ENROLL_IMAGE_COUNT +
									" images taken for enrollment.", Toast.LENGTH_LONG).show();
							initScanMode();
						}
						break;
					default:
						// Somehow the RequestType was set to null or some invalid value. Return failure.
						Toast.makeText(thisActivity, "An invalid function was called.", Toast.LENGTH_LONG).show();
						setResult(RESULT_CANCELED);
						finish();
						break;
					}
					
				}
			}
		});
		
		initScanMode();
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

	/**
	 * Starts scanning via the FPScan.
	 * 
	 * @return true iff all succeeds.
	 */
	private static boolean StartScan() {
		mFPScan = new FPScan(usb_host_ctx, mHandler);
		mFPScan.start();

		return true;
	}
	
	/**
	 * Switches to scan mode and waits for the scan button being pressed.
	 */
	private void initScanMode() {
		usb_host_ctx = new UsbDeviceDataExchangeImpl(this, mHandler);

		if(mFPScan != null) {
			BiometricInterface.mStop = true;
			mFPScan.stop();

		}

		BiometricInterface.mStop = false;
		usb_host_ctx.CloseDevice();
		if(usb_host_ctx.OpenDevice(0, true)) {
			if(StartScan()) {
				//Toast.makeText(thisActivity, "Place finger on scanner.", Toast.LENGTH_LONG).show();
			}	
		} else {
			if(!usb_host_ctx.IsPendingOpen()) {
				Toast.makeText(this, "Can not start scan operation. Can't open scanner device", Toast.LENGTH_LONG).show();
			}
		}
		
		showProgress(false);
	}

	// The Handler that gets information back from the FPScan
	private static final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BiometricInterface.MESSAGE_SHOW_MSG:            	
				//String showMsg = (String) msg.obj;
				//Toast.makeText(thisActivity, showMsg, Toast.LENGTH_LONG).show();
				break;
			case BiometricInterface.MESSAGE_SHOW_SCANNER_INFO:            	
				//String showInfo = (String) msg.obj;
				//Toast.makeText(thisActivity, showInfo, Toast.LENGTH_LONG).show();
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

	/**
	 * Displays a frame on the screen.
	 */
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
	 * Shows the progress UI and hides the fingerprint image.
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
