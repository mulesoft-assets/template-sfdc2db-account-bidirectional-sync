package org.mule.templates.util;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The function of this class is provide date comparation an transformation
 * functionality.
 * 
 * @author damiansima
 */
public class DateUtils {

	/**
	 * The method will take any date and validate if it finish with "Z"
	 * indicating GMT 0 time zone. If so it will transform it to +00:00 offset.
	 * 
	 * If no it will return the same string.
	 * 
	 * @param date
	 *            string representing a date
	 * @return a string representing a date with the time zone with format
	 *         +HH:mm
	 */
	private static String reformatZuluTimeZoneToOffsetIfNecesary(String date) {
		String reformatedDate = "";

		if (date.charAt(date.length() - 1) == 'Z') {
			reformatedDate = date.substring(0, date.length() - 1);
			reformatedDate += "+00:00";
		} else {
			reformatedDate = date;
		}

		return reformatedDate;
	}

	/**
	 * Validate which date is older.
	 * 
	 * @param dateA
	 *            a string representing a date
	 * @param dateB
	 *            a string representing a date
	 * @return true if the date A is after the date B
	 */
	public static boolean isAfter(String dateA, String dateB) {
		Validate.notEmpty(dateA, "The date A should not be null or empty");
		Validate.notEmpty(dateB, "The date A should not be null or empty");

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ'");

		DateTime lastModifiedDateOfA = formatter.withOffsetParsed().parseDateTime(reformatZuluTimeZoneToOffsetIfNecesary(dateA));

		DateTime lastModifiedDateOfB = formatter.parseDateTime(reformatZuluTimeZoneToOffsetIfNecesary(dateB));

		return lastModifiedDateOfA.isAfter(lastModifiedDateOfB);
	}
}
