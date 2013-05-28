package com.irespond.hpvvaccinetracker;

import android.app.Application;
import android.content.Context;

public class HPVVaccineTrackerApp extends Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		HPVVaccineTrackerApp.context = getApplicationContext();
	}

	/**
	 * Provides a static way for classes to access the main application
	 * Context.
	 *
	 * @return the main application Context.
	 */
	public static Context getAppContext() {
		return HPVVaccineTrackerApp.context;
	}
}
