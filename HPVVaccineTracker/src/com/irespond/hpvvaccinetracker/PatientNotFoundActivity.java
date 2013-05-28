package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class PatientNotFoundActivity extends Activity {
	private static Button mEnrollAsNewPatientButton;
	private static Button mRescanButton;
	private static Button mDontEnrollButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_not_found);
		
		mEnrollAsNewPatientButton = (Button) findViewById(R.id.enrollNewPatientButton);
		mRescanButton = (Button) findViewById(R.id.rescanButton);
		mDontEnrollButton = (Button) findViewById(R.id.dontEnrollButton);
		
		mEnrollAsNewPatientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(PatientNotFoundActivity.this, EnrollmentActivity.class));
				finish();
			}
		});
		
		mRescanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mDontEnrollButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(PatientNotFoundActivity.this, "Not Implemented.", Toast.LENGTH_LONG).show();
			}
		});
	}
}
