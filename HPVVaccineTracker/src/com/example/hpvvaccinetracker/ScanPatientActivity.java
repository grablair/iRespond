package com.example.hpvvaccinetracker;

import com.irespond.biometrics.client.BiometricInterface;
import com.irespond.biometrics.client.IrespondActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ScanPatientActivity extends Activity {
	
	public static Button mScanButton;
	
	private static RelativeLayout scanLayout;
	private static RelativeLayout recordFoundLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_patient);
		
		mScanButton = (Button) findViewById(R.id.patientScanButton);
		
		mScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				BiometricInterface.identify();
				startActivityForResult(new Intent(ScanPatientActivity.this,
						IrespondActivity.class), 1);
			}
		});
		
		scanLayout = (RelativeLayout) findViewById(R.id.scanPatientUI);
		recordFoundLayout = (RelativeLayout) findViewById(R.id.recordFoundUI);
		recordFoundLayout.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_patient, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// check if the returned UUID is in the database
			
			// if the UUID is found, bring up record found UI
			scanLayout.setVisibility(View.INVISIBLE);
			recordFoundLayout.setVisibility(View.VISIBLE);
			
			// otherwise, ask if the 
            
			Toast.makeText(this, "Identification successful: " + 
					BiometricInterface.mIdentifyResult, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Identification unsuccessful.", Toast.LENGTH_LONG).show();
		}
	}

	
}
