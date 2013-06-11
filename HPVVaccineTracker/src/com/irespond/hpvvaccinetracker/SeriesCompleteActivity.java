package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This activity is for patients when their vaccination
 * series is complete.
 * 
 * @author grahamb5
 * @author angela18
 */
public class SeriesCompleteActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_series_complete);
		
		Button doneButton = (Button) findViewById(R.id.completeButton);
		
		// Set the done button to just finish the activity.
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
