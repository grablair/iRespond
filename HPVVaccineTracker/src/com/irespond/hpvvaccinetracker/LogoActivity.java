package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class LogoActivity extends Activity {
	private static final int NUM_SECONDS = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logo);
		
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
