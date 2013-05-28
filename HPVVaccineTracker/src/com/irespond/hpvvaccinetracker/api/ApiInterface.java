package com.irespond.hpvvaccinetracker.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.net.SocketTimeoutException;


import org.apache.http.client.HttpResponseException;
import org.apache.http.client.params.ClientPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.hpvvaccinetracker.R;
import com.irespond.hpvvaccinetracker.HPVVaccineTrackerApp;
import com.irespond.hpvvaccinetracker.Patient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

/**
 * Singleton class that facilitates connections to the HTTP API.
 *
 * @author Chris brucec5
 * @author Graham grahamb5
 *
 */
public class ApiInterface {

	// The singleton instance of ApiInterface.
	private static ApiInterface instance;
	private static final String TAG = "ApiInterface";

	private final String baseUrl;
	private final String providersUrl;
	private final String patientsUrl;

	private final AsyncHttpClient client;

	/**
	 * Singleton factory method to get the singleton instance.
	 *
	 * @return singleton ApiInterface instance
	 */
	public static ApiInterface getInstance() {
		Log.d(TAG, "Getting instance of api interface");
		if (instance == null) {
			Log.d(TAG, "Creating new instance of api interface");
			instance = new ApiInterface(HPVVaccineTrackerApp.getAppContext());
		}
		return instance;
	}

	private ApiInterface(Context context) {
		Resources r = context.getResources();
		baseUrl = r.getString(R.string.base_url);
		providersUrl = baseUrl + r.getString(R.string.providers);
		patientsUrl = baseUrl + r.getString(R.string.patients);

		PersistentCookieStore cookieStore = new PersistentCookieStore(context);
		client = new AsyncHttpClient();
		client.setTimeout(10000);
		client.setCookieStore(cookieStore);
		client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true); 

		// Need to specify that we want JSON back from the server.
		client.addHeader("Accept", "application/json");
	}

	/**
	 * Creates a provider on the API server. Asynchronous.
	 *
	 * @param uuid the iRespond UUID of the new provider.
	 * @param callback Callbacks to run on success or failure, or
	 * <code>null</code> for no callbacks.
	 */
	public void createProvider(final UUID uuid, final ApiCallback<Void> callback) {
		if (failOnNoInternet(callback))
			return;

		RequestParams params = new RequestParams();
		
		params.put("id", uuid.toString());

		client.get(providersUrl + "/create", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject obj) {
				callback.onSuccess(null);
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}
	
	public void fetchProviders(final ApiCallback<Collection<UUID>> callback) {
		if (failOnNoInternet(callback))
			return;
		
		client.get(providersUrl + "/index", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray providersJson) {
				Set<UUID> result = new HashSet<UUID>();
				
				int providersLen = providersJson.length();
				
				for (int i = 0; i < providersLen; i++) {
					try {
						JSONObject providerObject = providersJson.getJSONObject(i);
						
						UUID uuid = UUID.fromString(providerObject.getString("id"));
						result.add(uuid);
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
						callback.onFailure(e.getMessage());
						return;
					}
				}
				
				callback.onSuccess(result);
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}
	
	public void deleteProvider(final UUID uuid, final ApiCallback<Void> callback) {
		if (failOnNoInternet(callback))
			return;

		RequestParams params = new RequestParams();
		
		params.put("id", uuid.toString());

		client.get(providersUrl + "/destroy", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject obj) {
				try {
					if (obj.getBoolean("destroyed"))
						callback.onSuccess(null);
					else
						callback.onFailure("Not deleted.");
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					callback.onFailure(e.getMessage());
					return;
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}
	
	public void createPatient(final Patient p, final ApiCallback<Void> callback) {
		if (failOnNoInternet(callback))
			return;

		RequestParams params = new RequestParams();
		
		p.fillParams(params);
		
		Log.d(TAG, "Calling " + patientsUrl + "/create with the following args: " + params);

		client.get(patientsUrl + "/create", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject obj) {
				try {
					p.update(obj);
					callback.onSuccess(null);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					callback.onFailure(e.getMessage());
					return;
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}
	
	public void updatePatient(final Patient p, final ApiCallback<Void> callback) {
		if (failOnNoInternet(callback))
			return;

		RequestParams params = new RequestParams();
		
		p.fillParams(params);

		client.get(patientsUrl + "/update", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject obj) {
				try {
					p.update(obj);
					callback.onSuccess(null);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					callback.onFailure(e.getMessage());
					return;
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}
	
	public void deletePatient(final Patient p, final ApiCallback<Void> callback) {
		if (failOnNoInternet(callback))
			return;

		RequestParams params = new RequestParams();
		
		params.put("id", p.id.toString());

		client.get(patientsUrl + "/destroy", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject obj) {
				try {
					if (obj.getBoolean("destroyed"))
						callback.onSuccess(null);
					else
						callback.onFailure("Not deleted.");
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					callback.onFailure(e.getMessage());
					return;
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}
	
	public void fetchPatient(final UUID id, final ApiCallback<Patient> callback) {
		if (failOnNoInternet(callback))
			return;

		RequestParams params = new RequestParams();
		
		params.put("id", id.toString());
		
		Log.d(TAG, "In fetchPatient for " + id);

		client.get(patientsUrl + "/show", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject obj) {
				Log.d(TAG, "In onSuccess of fetchPatient");
				try {
					callback.onSuccess(new Patient(obj));
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					callback.onFailure(e.getMessage());
					return;
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t.getMessage());
			}

			@Override
			public void onFailure(Throwable e, String message) {
				if (e instanceof SocketTimeoutException) {
					callback.onFailure("Network Timeout");
				} else if (e instanceof HttpResponseException) {
					HttpResponseException he = (HttpResponseException) e;
					if (he.getStatusCode() == 404)
						callback.onSuccess(null);
					else
						callback.onFailure(he.getMessage());
				} else {
					callback.onFailure("FAILURE");
				}
			}
		});
	}

	/**
	 * Checks if there is an active connection to the Internet. If there is no connection,
	 * the callback specified is alerted via onFailure with an error specifying so.
	 * 
	 * @param callback The callback to call onFailure on if there is no Internet.
	 * @return <code>true</code> if failure occurs (no internet), <code>false</code> otherwise.
	 */
	private boolean failOnNoInternet(ApiCallback<?> callback) {
		ConnectivityManager conMgr = (ConnectivityManager)HPVVaccineTrackerApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

		if (activeNetwork != null && activeNetwork.isConnected()) {
			Log.d(TAG, "Internet connection found.");
			return false;
		}

		Log.d(TAG, "No internet connection found.");
		callback.onFailure("Active internet connection required.");
		return true;
	}
}
