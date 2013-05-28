package com.irespond.hpvvaccinetracker;

import java.util.UUID;

import com.example.hpvvaccinetracker.R;
import com.irespond.biometrics.client.BiometricInterface;
import com.irespond.biometrics.client.IrespondActivity;
import com.irespond.hpvvaccinetracker.api.ApiCallback;
import com.irespond.hpvvaccinetracker.api.ApiInterface;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ScanPatientActivity extends Activity {

	public static Button mScanButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_patient);

		mScanButton = (Button) findViewById(R.id.scanPatientButton);

		mScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mScanButton.setEnabled(false);
//				BiometricInterface.identify();
//				startActivityForResult(new Intent(ScanPatientActivity.this,
//						IrespondActivity.class), 1);
				BiometricInterface.mIdentifyResult = UUID.fromString("de317bca-74d4-476c-a889-8d0286f79c16");
				onActivityResult(1, RESULT_OK, null);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.scan_patient, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			UUID patientId = BiometricInterface.mIdentifyResult;
			ApiInterface.getInstance().fetchPatient(patientId, new ApiCallback<Patient>() {
				@Override
				public void onSuccess(Patient result) {
					// Successful response.
					if (result != null) {
						// Patient found.
						HPVVaccineTrackerApp.setCurrentPatient(result);
						startActivity(new Intent(ScanPatientActivity.this, PatientFoundActivity.class));
					} else {
						// Patient not found.
						startActivity(new Intent(ScanPatientActivity.this, PatientNotFoundActivity.class));
					}
					mScanButton.setEnabled(true);
				}

				@Override
				public void onFailure(String errorMessage) {
					// Unsuccessful response.
					Toast.makeText(ScanPatientActivity.this, errorMessage, Toast.LENGTH_LONG).show();
					mScanButton.setEnabled(true);
				}
			});
		} else {
			Toast.makeText(this, "Identification unsuccessful.", Toast.LENGTH_LONG).show();
			mScanButton.setEnabled(true);
		}
	}


}
