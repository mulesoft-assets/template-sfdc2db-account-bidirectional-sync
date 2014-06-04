package org.mule.templates.testutils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.collections.map.CaseInsensitiveMap;

public class MockedPayload {

	public static LinkedList<CaseInsensitiveMap> generate() throws ParseException {
		
		LinkedList<CaseInsensitiveMap> payload = new LinkedList<CaseInsensitiveMap>();
		CaseInsensitiveMap databaseAccount = new CaseInsensitiveMap();
		databaseAccount.put("Phone", "987654");
		databaseAccount.put("Industry", null);
		databaseAccount.put("ParentId", null);
		databaseAccount.put("Name", "MGTest");
		// Timestamp(int year, int month, int date, int hour, int minute, int second, int nano)
		Timestamp timestamp = getOldTimestamp();
		databaseAccount.put("LastModifiedDate", timestamp);
		databaseAccount.put("Type", null);
		databaseAccount.put("LastModifiedById", "mule@localhost");
		databaseAccount.put("Description", "database account description");
		databaseAccount.put("SalesforceId", "");
		databaseAccount.put("ID", 459);
		databaseAccount.put("AccountNumber", null);
		databaseAccount.put("NumberOfEmployees", 99999);
		
		payload.add(databaseAccount);
		return payload;
		
	}

	private static Timestamp getOldTimestamp() throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		Timestamp timestamp = new Timestamp(time);
		return timestamp;
	}
}
