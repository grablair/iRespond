package com.example.hpvvaccinetracker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ProviderLogin extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_provider_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.provider_login, menu);
		return true;
	}

}
