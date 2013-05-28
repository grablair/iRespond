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

public class PatientFoundActivity extends Activity {
	private static Button mConfirmButton;
	private static Button mDenyButton;
	private static ImageView mPatientImage;
	private static ProgressBar mPatientImageLoadingProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_found);
		
		final Patient patient = HPVVaccineTrackerApp.getCurrentPatient();
		
		mConfirmButton = (Button) findViewById(R.id.patientFoundConfirmButton);
		mDenyButton = (Button) findViewById(R.id.patientFoundDenyButton);
		mPatientImage = (ImageView) findViewById(R.id.patientFoundImage);
		mPatientImageLoadingProgress = (ProgressBar) findViewById(R.id.patientImageProgress);
		
		if (patient.photoUrl != null) {
			// show The Image
			new DownloadImageTask(mPatientImage).execute(patient.photoUrl);
			mConfirmButton.setEnabled(false);
		} else {
			mPatientImage.setVisibility(View.VISIBLE);
	        mPatientImageLoadingProgress.setVisibility(View.INVISIBLE);
		}
		
		mConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(PatientFoundActivity.this, GiveDoseActivity.class));
				finish();
			}
		});
		
		mDenyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
			mPatientImage.setVisibility(View.VISIBLE);
			mPatientImageLoadingProgress.setVisibility(View.INVISIBLE);
			mConfirmButton.setEnabled(true);
		}
	}
}

