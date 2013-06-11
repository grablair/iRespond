package com.irespond.hpvvaccinetracker;

import org.joda.time.LocalDate;

import com.example.hpvvaccinetracker.R;
import com.irespond.hpvvaccinetracker.api.ApiCallback;
import com.irespond.hpvvaccinetracker.api.ApiInterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity controls instruction to dispense
 * doses.
 * 
 * @author grahamb5
 * @author angela18
 */
public class GiveDoseActivity extends Activity {
	private static TextView mTitle;
	private static Button mDone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_give_dose);
		
		// Get the current patient.
		Patient p = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (p == null) {
			// There was no patient.
			Toast.makeText(this, "No active patient.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		// Get views.
		mTitle = (TextView) findViewById(R.id.doseTitle);
		mDone = (Button) findViewById(R.id.doseDone);
		
		// Check to see what dose is necessary.
		// Also checks to see if the patient returned
		// on the wrong date (was too early).
		boolean tooEarly;
		if (p.firstDoseDate == null) {
			mTitle.setText(getString(R.string.first_dose_text));
			tooEarly = false;
		} else if (p.secondDoseDate == null) {
			mTitle.setText(getString(R.string.second_dose_text));
			tooEarly = LocalDate.now().isBefore(p.firstDoseDate.plusMonths(2));
		} else if (p.thirdDoseDate == null) {
			mTitle.setText(getString(R.string.third_dose_text));
			tooEarly = LocalDate.now().isBefore(p.firstDoseDate.plusMonths(6));
		} else {
			startActivity(new Intent(this, SeriesCompleteActivity.class));
			finish();
			return;
		}
		
		if (tooEarly) {
			// Patient was too early. Start the TooEarlyActivity.
			startActivity(new Intent(this, TooEarlyActivity.class));
			finish();
		} else {
			mDone.setOnClickListener(doneListener);
		}
	}
	
	// Set the submission button listener.
	private OnClickListener doneListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mDone.setEnabled(false);
			final Patient p = HPVVaccineTrackerApp.getCurrentPatient();
			
			// Set the next dose date to now.
			if (p.firstDoseDate == null) {
				p.firstDoseDate = LocalDate.now();
			} else if (p.secondDoseDate == null) {
				p.secondDoseDate = LocalDate.now();
			} else {
				p.thirdDoseDate = LocalDate.now();
			}
			
			// Update the patient in the remote server.
			ApiInterface.getInstance().updatePatient(p, new ApiCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					startActivity(new Intent(GiveDoseActivity.this, ReturnDateActivity.class));
					finish();
				}

				@Override
				public void onFailure(String errorMessage) {
					Toast.makeText(GiveDoseActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
					mDone.setEnabled(true);
					
					// Reset the updated dose date.
					if (p.secondDoseDate == null) {
						p.firstDoseDate = null;
					} else if (p.thirdDoseDate == null) {
						p.secondDoseDate = null;
					} else {
						p.thirdDoseDate = null;
					}
				}
			});
		}
	};
}
