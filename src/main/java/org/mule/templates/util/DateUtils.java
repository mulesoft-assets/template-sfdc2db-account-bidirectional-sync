package org.mule.templates.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
	
	
	/**
	 * The method will try to format a date string to match the ISO 8601.
	 * 
	 * Valid examples of that are:
	 * 
	 * - 2014-05-25T16:45:49+00:00
	 * 
	 * - 2014-05-25T16:45:49Z
	 * 
	 * If provided the method will use format to parse the provided date. If not
	 * it will try with its default which matches the SQL format:
	 * "yyyy-MM-dd HH:mm:ss.SSS".
	 * 
	 * If provided the method will set the time zone. If not it will infer that
	 * the date has been provided in UTC.
	 * 
	 * @param date
	 *            a string representing a date
	 * @param format
	 *            pattern to parse the provided date
	 * @param timeZoneOffset
	 *            time zone in the format (+|-)HH:mm
	 * @return a date representing a string wiht the ISO 8601 format
	 *         yyyy-MM-dd'T'HH:mm:ss.SSSZ
	 */
	public static String dateStringToISODateString(String date, String format, String timeZoneOffset) {
		Validate.notEmpty(date, "The date should not be null or empty");

		if (StringUtils.isEmpty(format)) {
			// Defaulting to MySQL format
			format = "yyyy-MM-dd HH:mm:ss.SSS";
		}

		if (StringUtils.isEmpty(timeZoneOffset)) {
			// Defaulting same date as if it were UTC
			timeZoneOffset = "+00:00";
		}

		DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
		DateTime dateTime = formatter.withZone(DateTimeZone.forID(timeZoneOffset)).parseDateTime(date);

		return dateTime.toString();
	}
}
