package com.irespond.hpvvaccinetracker;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

/**
 * Represents a single Patient.
 * 
 * @author grahamb5
 * @author angela18
 */
public class Patient {
	public UUID id;
	public String localId;
	public String address;
	public String area;
	public String familyName;
	public String givenName;
	public String fathersName;
	public String mothersName;
	public String notes;
	public String phoneNumber;
	public String photoUrl;
	public boolean smsReminders;
	public LocalDate firstDoseDate;
	public LocalDate secondDoseDate;
	public LocalDate thirdDoseDate;
	public LocalDate birthDay;
	
	/**
	 * Create a patient with all fields specified.
	 * 
	 * @param id The ID of the patient.
	 * @param localId The local SID of the patient.
	 * @param address The address of the patient.
	 * @param area The area (city, village, etc) of the patient.
	 * @param familyName The family name of the patient.
	 * @param givenName The given name of the patient.
	 * @param fathersName The patient's father's name.
	 * @param mothersName The patient's mother's name.
	 * @param notes Any notes on the patient.
	 * @param phoneNumber The phone number of the patient.
	 * @param smsReminders Whether of not the patient wants SMS reminders.
	 * @param firstDoseDate The first dose date of the patient.
	 * @param secondDoseDate The second dose date of the patient.
	 * @param thirdDoseDate The third dose date of the patient.
	 * @param birthDay The birthday of the patient.
	 * @param photoUrl The URL of the patient's photo.
	 */
	public Patient(UUID id, String localId, String address, String area,
			String familyName, String givenName, String fathersName,
			String mothersName, String notes, String phoneNumber,
			boolean smsReminders, LocalDate firstDoseDate,
			LocalDate secondDoseDate, LocalDate thirdDoseDate,
			LocalDate birthDay, String photoUrl) {
		this.id = id;
		this.localId = localId;
		this.address = address;
		this.area = area;
		this.familyName = familyName;
		this.givenName = givenName;
		this.fathersName = fathersName;
		this.mothersName = mothersName;
		this.notes = notes;
		this.phoneNumber = phoneNumber;
		this.smsReminders = smsReminders;
		this.firstDoseDate = firstDoseDate;
		this.secondDoseDate = secondDoseDate;
		this.thirdDoseDate = thirdDoseDate;
		this.birthDay = birthDay;
		this.photoUrl = photoUrl;
	}
	
	/**
	 * Creates a new patient based off the given JSONObject,
	 * which contains fields in underscore format.
	 * 
	 * @param obj The JSONObject to create this patient from.
	 * @throws JSONException
	 */
	public Patient(JSONObject obj) throws JSONException {
		update(obj);
	}
	
	/**
	 * Updates the patient with the given JSONObject, which
	 * contains fields in underscore format.
	 * 
	 * @param obj The JSONObject to update this patient from.
	 * @throws JSONException
	 */
	public void update(JSONObject obj) throws JSONException {
		id = UUID.fromString(obj.getString("id"));
		localId = obj.isNull("local_id") ? null : obj.getString("local_id");
		address = obj.isNull("address") ? null : obj.getString("address");
		area = obj.isNull("area") ? null : obj.getString("area");
		familyName = obj.isNull("family_name") ? null : obj.getString("family_name");
		givenName = obj.isNull("given_name") ? null : obj.getString("given_name");
		fathersName = obj.isNull("fathers_name") ? null : obj.getString("fathers_name");
		mothersName = obj.isNull("mothers_name") ? null : obj.getString("mothers_name");
		notes = obj.isNull("notes") ? null : obj.getString("notes");
		phoneNumber = obj.isNull("phone_number") ? null : obj.getString("phone_number");
		smsReminders = obj.getBoolean("sms_reminders");
		firstDoseDate = obj.isNull("first_dose_date") ? null : LocalDate.parse(obj.getString("first_dose_date"));
		secondDoseDate = obj.isNull("second_dose_date") ? null : LocalDate.parse(obj.getString("second_dose_date"));
		thirdDoseDate = obj.isNull("third_dose_date") ? null : LocalDate.parse(obj.getString("third_dose_date"));
		birthDay = obj.isNull("birth_day") ? null : LocalDate.parse(obj.getString("birth_day"));
		photoUrl = obj.isNull("photo_url") ? null : obj.getString("photo_url");
	}
	
	/**
	 * Fills the given RequestParams with any field that is non-null
	 * in this patient's information. This is to be used to immediately
	 * send data to the remote server.
	 * 
	 * @param params The RequestParams object to fill with this
	 *               patient's information, if applicable.
	 */
	public void fillParams(RequestParams params) {
		params.put("id", id.toString());
		params.put("sms_reminders", "" + smsReminders);
		if (localId != null)
			params.put("local_id", localId);
		if (address != null)
			params.put("address", address);
		if (area != null)
			params.put("area", area);
		if (familyName != null)
			params.put("family_name", familyName);
		if (givenName != null)
			params.put("given_name", givenName);
		if (fathersName != null)
			params.put("fathers_name", fathersName);
		if (mothersName != null)
			params.put("mothers_name", mothersName);
		if (notes != null)
			params.put("notes", notes);
		if (phoneNumber != null)
			params.put("phone_number", phoneNumber);
		if (firstDoseDate != null)
			params.put("first_dose_date", firstDoseDate.toString());
		if (secondDoseDate != null)
			params.put("second_dose_date", secondDoseDate.toString());
		if (thirdDoseDate != null)
			params.put("third_dose_date", thirdDoseDate.toString());
		if (birthDay != null)
			params.put("birth_day", birthDay.toString());
		if (photoUrl != null)
			params.put("photo_url", photoUrl);
	}
}
