package com.irespond.hpvvaccinetracker;

import java.text.MessageFormat;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity facilitates the return date of a patient
 * for their next dose.
 * 
 * @author grahamb5
 * @author angela18
 */
public class ReturnDateActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_return_date);
		
		// Get view.
		Button doneButton = (Button) findViewById(R.id.returnDateDone);
		
		// Set done button's on-click.
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView text = (TextView) findViewById(R.id.returnDateText);
		
		// Get current patient.
		Patient p = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (p == null) {
			// There was no patient.
			Toast.makeText(this, "No active patient.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		// Set date to proper date after the first dose.
		if (p.secondDoseDate == null)
			text.setText(MessageFormat.format(getString(R.string.next_dose_desc), p.firstDoseDate.plusMonths(2).toString("dd / MM / yyyy")));
		else if (p.thirdDoseDate == null)
			text.setText(MessageFormat.format(getString(R.string.next_dose_desc), p.firstDoseDate.plusMonths(6).toString("dd / MM / yyyy")));
		else {
			// All doses have been given, show the SeriesCompleteActivity.
			startActivity(new Intent(this, SeriesCompleteActivity.class));
			finish();
		}
	}
}
