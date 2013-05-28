package com.irespond.hpvvaccinetracker;

import com.example.hpvvaccinetracker.R;
import com.example.hpvvaccinetracker.R.layout;
import com.example.hpvvaccinetracker.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class EnrollmentActivity extends Activity {
	//private 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enrollinfo);
		
//		mRadioGroup1 = (RadioGroup) findViewById(R.id.runandpass);
//        mRadio1 = (RadioButton) findViewById(R.id.radiobuttonrun);
//        mRadio2 = (RadioButton) findViewById(R.id.radiobuttonpass);
//        mcheckBoxcmpltpass= (RadioButton) findViewById(R.id.checkBoxcmpltpass);
//        /*RadioGroup?OnCheckedChangeListener???*/
//        mRadioGroup1.setOnCheckedChangeListener(mChangeRadio);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.enrollment, menu);
		return true;
	}

}
