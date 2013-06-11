package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * This activity deals with the case where the patient
 * is not found in the database.
 * 
 * @author grahamb5
 * @author angela18
 */
public class PatientNotFoundActivity extends Activity {
	private static Button mEnrollAsNewPatientButton;
	private static Button mRescanButton;
	private static Button mDontEnrollButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_not_found);
		
		// Get views.
		mEnrollAsNewPatientButton = (Button) findViewById(R.id.enrollNewPatientButton);
		mRescanButton = (Button) findViewById(R.id.rescanButton);
		mDontEnrollButton = (Button) findViewById(R.id.dontEnrollButton);
		
		// Set enrollment button listener.
		mEnrollAsNewPatientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(PatientNotFoundActivity.this, EnrollmentActivity.class));
				finish();
			}
		});
		
		// Set rescan listener.
		mRescanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// Set the don't enroll button listener.
		// TODO: Needs implementation.
		mDontEnrollButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(PatientNotFoundActivity.this, "Not Implemented.", Toast.LENGTH_LONG).show();
			}
		});
	}
}
