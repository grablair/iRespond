package com.irespond.hpvvaccinetracker;

import java.text.MessageFormat;

import com.example.hpvvaccinetracker.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TooEarlyActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_too_early);
		
		Button doneButton = (Button) findViewById(R.id.tooEarlyButton);
		
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView text = (TextView) findViewById(R.id.tooEarlyText);
		
		Patient p = HPVVaccineTrackerApp.getCurrentPatient();
		
		if (p.secondDoseDate == null)
			text.setText(MessageFormat.format(getString(R.string.wrong_date_desc), p.firstDoseDate.plusMonths(2).toString("dd / MM / yyyy")));
		else
			text.setText(MessageFormat.format(getString(R.string.wrong_date_desc), p.firstDoseDate.plusMonths(6).toString("dd / MM / yyyy")));
	}
}
