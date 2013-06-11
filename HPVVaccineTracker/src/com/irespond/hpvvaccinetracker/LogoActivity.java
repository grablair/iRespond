package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * This activity merely shows the iRespond logo
 * for a certain amount of time.
 * 
 * @author grahamb5
 * @author angela18
 */
public class LogoActivity extends Activity {
	/** The length to show the logo. */
	private static final int NUM_SECONDS = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logo);
		
		// Wait NUM_SECONDS seconds and change activities.
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					Thread.sleep(NUM_SECONDS * 1000);
				} catch (InterruptedException e) { }
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				startActivity(new Intent(LogoActivity.this, ProviderLoginActivity.class));
				finish();
			}
		}.execute();
	}
}
