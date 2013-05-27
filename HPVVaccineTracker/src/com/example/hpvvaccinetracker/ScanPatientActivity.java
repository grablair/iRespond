package com.example.hpvvaccinetracker;

import com.irespond.biometrics.client.BiometricInterface;
import com.irespond.biometrics.client.IrespondActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class ScanPatientActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_patient);
		
		BiometricInterface.identify();
		startActivityForResult(new Intent(ScanPatientActivity.this, IrespondActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_patient, menu);
		return true;
	}

	
}
