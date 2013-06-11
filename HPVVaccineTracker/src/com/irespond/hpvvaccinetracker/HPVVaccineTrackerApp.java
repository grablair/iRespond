package com.irespond.hpvvaccinetracker;

import android.app.Application;
import android.content.Context;

/**
 * Controls all the central HPV Vaccine tracking app's
 * persistent variables.
 * 
 * @author grahamb5
 * @author angela18
 */
public class HPVVaccineTrackerApp extends Application {
	private static Context context;
	private static Patient currentPatient;

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
	
	/**
	 * Returns the current patient logged in.
	 * 
	 * @return The current patient.
	 */
	public static Patient getCurrentPatient() {
		return currentPatient;
	}
	
	/**
	 * Sets the current patient logged in.
	 * 
	 * @param currentPatient The patient logged in.
	 */
	public static void setCurrentPatient(Patient currentPatient) {
		HPVVaccineTrackerApp.currentPatient = currentPatient;
	}
}
