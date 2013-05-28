package com.irespond.hpvvaccinetracker;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

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
	
	public Patient(JSONObject obj) throws JSONException {
		update(obj);
	}
	
	public void update(JSONObject obj) throws JSONException {
		id = UUID.fromString(obj.getString("id"));
		localId = obj.getString("local_id");
		address = obj.getString("address");
		area = obj.getString("area");
		familyName = obj.getString("family_name");
		givenName = obj.getString("given_name");
		fathersName = obj.getString("fathers_name");
		mothersName = obj.getString("mothers_name");
		notes = obj.getString("notes");
		phoneNumber = obj.getString("phone_number");
		smsReminders = obj.getBoolean("sms_reminders");
		firstDoseDate = LocalDate.parse(obj.getString("first_dose_date"));
		secondDoseDate = LocalDate.parse(obj.getString("second_dose_date"));
		thirdDoseDate = LocalDate.parse(obj.getString("third_dose_date"));
		birthDay = LocalDate.parse(obj.getString("birth_day"));
		photoUrl = obj.getString("photo_url");
	}
	
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
