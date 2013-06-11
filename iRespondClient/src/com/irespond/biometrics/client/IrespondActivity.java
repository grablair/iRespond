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
import android.annotation.SuppressLint;
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

/**
 * The <code>IrespondActivity</code> is the only activity
 * in the library, and deals with all the UI involved in
 * the identify, verify and enrollment functions.
 * 
 * @author grahamb5
 * @author angela18
 */
public class IrespondActivity extends Activity {
	/** The number of images to send in enrollment. */
	private static final int ENROLL_IMAGE_COUNT = 3;
	
	/* The views needed by the activity. */
	private static Button mButtonScan;
	private static ImageView mFingerImage;
	private static ProgressBar mProgressBar;

	/** The scanner running object. */
	private static FPScan mFPScan = null;

	/** The USB device context. */
	private static UsbDeviceDataExchangeImpl usb_host_ctx = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Checks to see if a request has actually
		// been requested.
		if (BiometricInterface.mRequest == null) {
			Toast.makeText(this, "An invalid function was called.", Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		// Sets the layout.
		setContentView(R.layout.activity_irespond);

		// Gets all of the necessary views.
		mButtonScan = (Button) findViewById(R.id.scanbtn);
		mFingerImage = (ImageView) findViewById(R.id.fingerImage);
		mProgressBar = (ProgressBar) findViewById(R.id.loadingBar);

		// The on-click listener for the main button.
		mButtonScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Change UI state.
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
					Toast.makeText(IrespondActivity.this, devScan.GetErrorMessage(), Toast.LENGTH_LONG).show();
					return;    			
				}
				byte[] wsqImg = new byte[BiometricInterface.mImageWidth*BiometricInterface.mImageHeight];
				long hDevice = devScan.GetDeviceHandle();
				ftrWsqAndroidHelper wsqHelper = new ftrWsqAndroidHelper();
				
				// byte array created
				if (wsqHelper.ConvertRawToWsq(hDevice, BiometricInterface.mImageWidth, BiometricInterface.mImageHeight, 2.25f, BiometricInterface.mImageFP, wsqImg)) {
					// The WSQ conversion succeeded. Perform network request.
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
												Toast.makeText(IrespondActivity.this, "0 / " + ENROLL_IMAGE_COUNT +
														" images taken for enrollment.", Toast.LENGTH_LONG).show();
									            break;
									        }
									    }
									};

									AlertDialog.Builder builder = new AlertDialog.Builder(IrespondActivity.this);
									builder.setMessage("No match was found. Would you like to try again or enroll this person in the system?")
										.setPositiveButton("Try again", dialogClickListener)
									    .setNegativeButton("Enroll", dialogClickListener).show();
								}
							}

							@Override
							public void onFailure(String errorMessage) {
								// Error. Show the error message.
								mButtonScan.setEnabled(true);
								Toast.makeText(IrespondActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
									Toast.makeText(IrespondActivity.this, "Verification unsuccessful. Pleas try again.", Toast.LENGTH_LONG).show();
									initScanMode();
								}
							}

							@Override
							public void onFailure(String errorMessage) {
								// Error. Show the error message.
								mButtonScan.setEnabled(true);
								Toast.makeText(IrespondActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
									Toast.makeText(IrespondActivity.this, errorMessage, Toast.LENGTH_LONG).show();
									initScanMode();
								}
							});
						} else {
							// We need more enrollment images.
							mButtonScan.setEnabled(true);
							Toast.makeText(IrespondActivity.this, "" + BiometricInterface.mEnrollImages.size() + " / " + ENROLL_IMAGE_COUNT +
									" images taken for enrollment.", Toast.LENGTH_LONG).show();
							initScanMode();
						}
						break;
					default:
						// Somehow the RequestType was set to null or some invalid value. Return failure.
						Toast.makeText(IrespondActivity.this, "An invalid function was called.", Toast.LENGTH_LONG).show();
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
		// Stop the scanner and close devices.
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
	private boolean StartScan() {
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
	// Certain Toasts are commented out as they are for debugging
	// purposes.
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
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
						Toast.makeText(IrespondActivity.this, "Place finger on scanner.", Toast.LENGTH_LONG).show();
					}	
				} else
					Toast.makeText(IrespondActivity.this, "Can't open scanner device", Toast.LENGTH_LONG).show();
				break;           
			case UsbDeviceDataExchangeImpl.MESSAGE_DENY_DEVICE:
				Toast.makeText(IrespondActivity.this, "User deny scanner device", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	/**
	 * Displays a frame on the screen.
	 * 
	 * Taken from the Futronic API.
	 */
	private static void ShowBitmap() {
		int[] pixels = new int[BiometricInterface.mImageWidth * BiometricInterface.mImageHeight];
		for( int i=0; i<BiometricInterface.mImageWidth * BiometricInterface.mImageHeight; i++)
			pixels[i] = BiometricInterface.mImageFP[i];
		
		// Create the empty bitmap.
		Bitmap emptyBmp = Bitmap.createBitmap(pixels, BiometricInterface.mImageWidth, BiometricInterface.mImageHeight, Config.RGB_565);

		int width, height; 
		height = emptyBmp.getHeight(); 
		width = emptyBmp.getWidth();     

		// Draw the bitmap from the image.
		BiometricInterface.mBitmapFP = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); 
		Canvas c = new Canvas(BiometricInterface.mBitmapFP); 
		Paint paint = new Paint(); 
		ColorMatrix cm = new ColorMatrix(); 
		cm.setSaturation(0); 
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm); 
		paint.setColorFilter(f); 
		c.drawBitmap(emptyBmp, 0, 0, paint); 

		// Set the image to the newly created bitmap.
		mFingerImage.setImageBitmap(BiometricInterface.mBitmapFP);
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

			 // Show progress bar.
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

			 // Hide fingerprint image.
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
