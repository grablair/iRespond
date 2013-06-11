package com.irespond.biometrics.client;

/**
 * The callback for the network-threads connecting to the server.
 * 
 * @author grahamb5
 * @author angela18
 * 
 * @param <T> The type of object to send back.
 */
public interface ServerCallback<T> {
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
