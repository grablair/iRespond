package com.irespond.hpvvaccinetracker;

import java.io.InputStream;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * This activity deals with a patient being found, showing
 * an image of the patient, if possible.
 * 
 * @author grahamb5
 * @author angela18
 */
public class PatientFoundActivity extends Activity {
	private static Button mConfirmButton;
	private static Button mDenyButton;
	private static ImageView mPatientImage;
	private static ProgressBar mPatientImageLoadingProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_found);
		
		// Get the current patient.
		final Patient patient = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (patient == null) {
			// There was no patient.
			Toast.makeText(this, "No active patient.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		// Get views.
		mConfirmButton = (Button) findViewById(R.id.patientFoundConfirmButton);
		mDenyButton = (Button) findViewById(R.id.patientFoundDenyButton);
		mPatientImage = (ImageView) findViewById(R.id.patientFoundImage);
		mPatientImageLoadingProgress = (ProgressBar) findViewById(R.id.patientImageProgress);
		
		if (patient.photoUrl != null) {
			// Show the image
			new DownloadImageTask(mPatientImage).execute(patient.photoUrl);
			mConfirmButton.setEnabled(false);
		} else {
			// Show the "no image found" image. 
			mPatientImage.setVisibility(View.VISIBLE);
	        mPatientImageLoadingProgress.setVisibility(View.INVISIBLE);
		}
		
		mConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Show the GiveDoseActivity for this patient.
				startActivity(new Intent(PatientFoundActivity.this, GiveDoseActivity.class));
				finish();
			}
		});
		
		mDenyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Go back to patient login.
				finish();
			}
		});
	}

	/**
	 * Downloads an image from the web and inserts it into the given
	 * ImageView on completion.
	 * 
	 * @author grahamb5
	 * @author angela18
	 */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		/**
		 * Creates the Task.
		 * 
		 * @param bmImage The ImageView to insert the downlaoded image in.
		 */
		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				// Read image.
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			// Set ImageView.
			bmImage.setImageBitmap(result);
			mPatientImage.setVisibility(View.VISIBLE);
			mPatientImageLoadingProgress.setVisibility(View.INVISIBLE);
			mConfirmButton.setEnabled(true);
		}
	}
}

