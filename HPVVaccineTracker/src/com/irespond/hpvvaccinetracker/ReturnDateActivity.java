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

public class ReturnDateActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_return_date);
		
		Button doneButton = (Button) findViewById(R.id.returnDateDone);
		
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView text = (TextView) findViewById(R.id.returnDateText);
		
		Patient p = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (p.secondDoseDate == null)
			text.setText(MessageFormat.format(getString(R.string.next_dose_desc), p.firstDoseDate.plusMonths(2).toString("dd / MM / yyyy")));
		else if (p.thirdDoseDate == null)
			text.setText(MessageFormat.format(getString(R.string.next_dose_desc), p.firstDoseDate.plusMonths(6).toString("dd / MM / yyyy")));
		else {
			startActivity(new Intent(this, SeriesCompleteActivity.class));
			finish();
		}
	}
}
