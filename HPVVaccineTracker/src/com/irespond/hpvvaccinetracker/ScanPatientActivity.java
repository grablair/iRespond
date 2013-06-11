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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * This activity deals with the patient identification
 * and login process.
 * 
 * @author grahamb5
 * @author angela18
 */
public class ScanPatientActivity extends Activity {

	public static Button mScanButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_patient);

		// Get view.
		mScanButton = (Button) findViewById(R.id.scanPatientButton);

		// Set scan button's on-click listener.
		mScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Set next biometric function to be an identification.
				// Start the IrespondActivity.
				mScanButton.setEnabled(false);
				BiometricInterface.identify();
				startActivityForResult(new Intent(ScanPatientActivity.this,
						IrespondActivity.class), 1);
//				BiometricInterface.mIdentifyResult = UUID.fromString("de317bca-74d4-476c-a889-8d0286f79c16");
//				onActivityResult(1, RESULT_OK, null);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// iRespond Biometric Library returned a value.
			UUID patientId = BiometricInterface.mIdentifyResult;
			
			// Get the patient identified by the biometric id, if it exists.
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
			// iRespond didn't return anything, or returned non RESULT_OK.
			Toast.makeText(this, "Identification unsuccessful.", Toast.LENGTH_LONG).show();
			mScanButton.setEnabled(true);
		}
	}


}
