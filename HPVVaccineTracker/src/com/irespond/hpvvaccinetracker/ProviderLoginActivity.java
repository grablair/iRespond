package com.irespond.hpvvaccinetracker;

import java.util.Collection;
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
				mLoginButton.setEnabled(false);
				ApiInterface.getInstance().fetchProviders(new ApiCallback<Collection<UUID>>() {
					@Override
					public void onSuccess(Collection<UUID> result) {
//						BiometricInterface.verify(result);
//						startActivityForResult(new Intent(ProviderLoginActivity.this,
//								IrespondActivity.class), 1);
						onActivityResult(1, RESULT_OK, null);
					}

					@Override
					public void onFailure(String errorMessage) {
						Toast.makeText(ProviderLoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
						mLoginButton.setEnabled(true);
					}
				});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.provider_login, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			//Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, ScanPatientActivity.class));
		}
		
		mLoginButton.setEnabled(true);
	}
}
