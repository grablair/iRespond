package com.irespond.hpvvaccinetracker;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.example.hpvvaccinetracker.R;
import com.irespond.biometrics.client.BiometricInterface;
import com.irespond.hpvvaccinetracker.api.ApiCallback;
import com.irespond.hpvvaccinetracker.api.ApiInterface;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class EnrollmentActivity extends Activity {
	//private 
	private static EditText mFamilyName;
	private static EditText mGivenName;
	private static EditText mMothersName;
	private static EditText mFathersName;
	private static EditText mPhoneNumber;
	private static EditText mAddress;
	private static EditText mArea;
	private static EditText mAge;
	private static EditText mDobDay;
	private static EditText mDobMonth;
	private static EditText mDobYear;
	private static EditText mNotes;
	private static EditText mSid1;
	private static EditText mSid2;
	private static EditText mSid3;
	
	private static CheckBox mSmsReminders;
	
	private static RadioButton mAgeEstimated;
	private static RadioButton mAgeReported;
	private static RadioButton mAgeChoice;
//	private static RadioButton mDobChoice;
	
	private static RadioGroup mAgeOrDob;
//	private static RadioGroup mAgeEstimatedOrReported;
	
	private static Button mSubmitButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enrollinfo);
		
		mFamilyName = (EditText) findViewById(R.id.familyname);
		mGivenName = (EditText) findViewById(R.id.givenname);
		mMothersName = (EditText) findViewById(R.id.mothername);
		mFathersName = (EditText) findViewById(R.id.fathername);
		mPhoneNumber = (EditText) findViewById(R.id.phone);
		mAddress = (EditText) findViewById(R.id.address);
		mArea = (EditText) findViewById(R.id.location);
		mAge = (EditText) findViewById(R.id.age);
		mDobDay = (EditText) findViewById(R.id.dob_day);
		mDobMonth = (EditText) findViewById(R.id.dob_month);
		mDobYear = (EditText) findViewById(R.id.dob_year);
		mNotes = (EditText) findViewById(R.id.enrollnotes);
		mSid1 = (EditText) findViewById(R.id.localId1);
		mSid2 = (EditText) findViewById(R.id.localId2);
		mSid3 = (EditText) findViewById(R.id.localId3);
		
		mSmsReminders = (CheckBox) findViewById(R.id.sms);
		
		mAgeEstimated = (RadioButton) findViewById(R.id.ageestimated);
		mAgeReported = (RadioButton) findViewById(R.id.agereported);
		mAgeChoice = (RadioButton) findViewById(R.id.radioAge);
//		mDobChoice = (RadioButton) findViewById(R.id.radioDOB);
		
		mAgeOrDob = (RadioGroup) findViewById(R.id.radioSex);
//		mAgeEstimatedOrReported = (RadioGroup) findViewById(R.id.ageEstimatedOrReported);
		
		mSubmitButton = (Button) findViewById(R.id.enrollInfoDone);
		
		mAgeChoice.setChecked(true);
		mAge.setEnabled(true);
		mAgeEstimated.setEnabled(true);
		mAgeReported.setEnabled(true);
		mDobDay.setEnabled(false);
		mDobMonth.setEnabled(false);
		mDobYear.setEnabled(false);
		
		mAgeEstimated.setChecked(true);
		
		mAgeOrDob.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				boolean ageChecked = (checkedId == R.id.radioAge);
				
				mAge.setEnabled(ageChecked);
				mAgeEstimated.setEnabled(ageChecked);
				mAgeReported.setEnabled(ageChecked);
				mDobDay.setEnabled(!ageChecked);
				mDobMonth.setEnabled(!ageChecked);
				mDobYear.setEnabled(!ageChecked);				
			}
		});
		
		mSubmitButton.setOnClickListener(onSubmitListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.enrollment, menu);
		return true;
	}

	private OnClickListener onSubmitListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			boolean problems = false;
			
			int day = 0;
			int month = 0;
			int year = 0;
			
			if (!(mSid1.length() == 4 && mSid2.length() == 3 && mSid3.length() == 4) &&
					!(mSid1.length() == 0 && mSid2.length() == 0 && mSid3.length() == 0)) {
				mSid1.setError("SID must be either empty or full.");
				mSid2.setError("SID must be either empty or full.");
				mSid3.setError("SID must be either empty or full.");
				problems = true;
			} else if (mSid1.length() > 0 && mSid1.getText().charAt(0) == '0') {
				mSid1.setError("SID cannot start with a zero.");
				problems = true;
			}
			
			if (mPhoneNumber.length() == 0 && mSmsReminders.isChecked()) {
				mSmsReminders.setError("No phone number is available.");
				problems = true;
			}
			
			if (mAgeOrDob.getCheckedRadioButtonId() == R.id.radioAge && mAge.length() == 0) {
				mAge.setError("You must enter an age.");
				problems = true;
			} else if (mAgeOrDob.getCheckedRadioButtonId() == R.id.radioDOB) {
				if (mDobMonth.length() == 0) {
					mDobMonth.setError("Must enter a month.");
					problems = true;
				} else {
					month = Integer.parseInt(mDobMonth.getText().toString());
					if (month < 1 || month > 12) {
						mDobMonth.setError("Must enter a valid month.");
						month = 0;
						problems = true;
					}
				}
				
				if (mDobYear.length() == 0) {
					mDobYear.setError("Must enter a year.");
					problems = true;
				} else {
					year = Integer.parseInt(mDobYear.getText().toString());
				}
				
				if (mDobDay.length() == 0) {
					mDobDay.setError("Must enter a day.");
					problems = true;
				} else {
					day = Integer.parseInt(mDobDay.getText().toString());
					if (month != 0 && (day < 1 || day > daysOfMonth(year, month))) {
						mDobDay.setError("Must be a valid day of the specified month.");
						problems = true;
					}
				}
			}
			
			if (mFamilyName.length() == 0) {
				mFamilyName.setError("You must enter an family name.");
			}
			if (mGivenName.length() == 0) {
				mGivenName.setError("You must enter a given name.");
			}
			
			if (!problems) {
				mSubmitButton.setEnabled(false);
				
				String localId = null;
				String address = null;
				String area = null;
				String familyName = mFamilyName.getText().toString();
				String givenName = mGivenName.getText().toString();
				String fathersName = null;
				String mothersName = null;
				String notes = null;
				String phoneNumber = null;
				boolean smsReminders = mSmsReminders.isChecked();
				LocalDate birthDay = null;
				
				if (mSid1.length() > 0)
					localId = mSid1.getText().toString() + mSid2.getText().toString() + mSid3.getText().toString();
				if (mAddress.length() > 0)
					address = mAddress.getText().toString();
				if (mArea.length() > 0)
					area = mArea.getText().toString();
				if (mFathersName.length() > 0)
					fathersName = mFathersName.getText().toString();
				if (mMothersName.length() > 0)
					mothersName = mMothersName.getText().toString();
				if (mPhoneNumber.length() > 0)
					phoneNumber = mPhoneNumber.getText().toString();
				if (mNotes.length() > 0)
					notes = mNotes.getText().toString();
				
				if (mAgeOrDob.getCheckedRadioButtonId() == R.id.radioAge) {
					birthDay = LocalDate.now().minusYears(Integer.parseInt(mAge.getText().toString()));
				} else {
					birthDay = new LocalDate(year, month, day);
				}
				
				final Patient p = new Patient(BiometricInterface.mIdentifyResult, localId, address, area, 
						familyName, givenName, fathersName, mothersName, notes, phoneNumber,
						smsReminders, null, null, null, birthDay, null);
				
				ApiInterface.getInstance().createPatient(p, new ApiCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						HPVVaccineTrackerApp.setCurrentPatient(p);
						startActivity(new Intent(EnrollmentActivity.this, GiveDoseActivity.class));
						finish();
					}

					@Override
					public void onFailure(String errorMessage) {
						Toast.makeText(EnrollmentActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
						mSubmitButton.setEnabled(true);
					}
				});
			}
		}
	};
	
	public static int daysOfMonth(int year, int month) {
		DateTime dateTime = new DateTime(year, month, 14, 12, 0, 0, 000);
		return dateTime.dayOfMonth().getMaximumValue();
	}
}
