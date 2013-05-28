package com.irespond.hpvvaccinetracker.api;

public interface ApiCallback<T> {
	/**
	 * Method to be run if the API request completed successfully.
	 *
	 * @param result the data returned from the API request.
	 */
	public void onSuccess(T result);

	/**
	 * Method to be run if the API request completed unsuccessfully.
	 *
	 * @param errorMessage a String explaining why the API request failed.
	 */
	public void onFailure(String errorMessage);
}
