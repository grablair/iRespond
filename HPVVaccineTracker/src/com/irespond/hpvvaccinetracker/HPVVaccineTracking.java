package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class HPVVaccineTracking extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hpvvaccine_tracking);
		
		// 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hpvvaccine_tracking, menu);
		return true;
	}

}
