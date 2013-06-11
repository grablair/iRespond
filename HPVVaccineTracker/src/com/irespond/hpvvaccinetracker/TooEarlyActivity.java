package com.irespond.hpvvaccinetracker;

import java.text.MessageFormat;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity deals with the case when patients return
 * before their specified return date.
 * 
 * @author grahamb5
 * @author angela18
 */
public class TooEarlyActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_too_early);
		
		Button doneButton = (Button) findViewById(R.id.tooEarlyButton);
		
		// Set the done button to just finish the activity.
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// Get return date text field.
		TextView text = (TextView) findViewById(R.id.tooEarlyText);
		
		// Get patient
		Patient p = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (p == null) {
			// There was no patient.
			Toast.makeText(this, "No active patient.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		// Set the return date text.
		if (p.secondDoseDate == null)
			text.setText(MessageFormat.format(getString(R.string.wrong_date_desc), p.firstDoseDate.plusMonths(2).toString("dd / MM / yyyy")));
		else
			text.setText(MessageFormat.format(getString(R.string.wrong_date_desc), p.firstDoseDate.plusMonths(6).toString("dd / MM / yyyy")));
	}
}
