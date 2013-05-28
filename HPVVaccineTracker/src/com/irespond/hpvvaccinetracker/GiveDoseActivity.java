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

public class GiveDoseActivity extends Activity {
	private static TextView mTitle;
	private static Button mDone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_give_dose);
		
		Patient p = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (p == null) {
			Toast.makeText(this, "No active patient.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		mTitle = (TextView) findViewById(R.id.doseTitle);
		mDone = (Button) findViewById(R.id.doseDone);
		
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
			startActivity(new Intent(this, TooEarlyActivity.class));
			finish();
		} else {
			mDone.setOnClickListener(doneListener);
		}
	}
	
	private OnClickListener doneListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mDone.setEnabled(false);
			Patient p = HPVVaccineTrackerApp.getCurrentPatient();
			
			if (p.firstDoseDate == null) {
				p.firstDoseDate = LocalDate.now();
			} else if (p.secondDoseDate == null) {
				p.secondDoseDate = LocalDate.now();
			} else {
				p.thirdDoseDate = LocalDate.now();
			}
			
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
				}
			});
		}
	};
}
