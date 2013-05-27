package com.example.hpvvaccinetracker;

import com.irespond.biometrics.client.BiometricInterface;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ScanPatient extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_patient);
		
		BiometricInterface.identify();
		startActivityForResult(new Intent(ThisActivity.this, IrespondActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_patient, menu);
		return true;
	}

	
}
