package com.irespond.hpvvaccinetracker;

import java.util.UUID;

import com.example.hpvvaccinetracker.R;
import com.irespond.biometrics.client.BiometricInterface;
import com.irespond.biometrics.client.IrespondActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ProviderLoginActivity extends Activity {
	
	public static Button mLoginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_provider_login);
		
		mLoginButton = (Button) findViewById(R.id.providerLoginButton);
		
		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				UUID uuid = UUID.fromString("de317bca-74d4-476c-a889-8d0286f79c16");
				BiometricInterface.verify(uuid);
				startActivityForResult(new Intent(ProviderLoginActivity.this,
						IrespondActivity.class), 1);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.provider_login, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Toast.makeText(this, "Verification successful: " + 
					BiometricInterface.mIdentifyResult, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Verification unsuccessful.", Toast.LENGTH_LONG).show();
		}
	}
}
