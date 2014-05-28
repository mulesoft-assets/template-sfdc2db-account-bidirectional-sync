package org.mule.templates.util;

import junit.framework.Assert;

import org.junit.Test;
import org.mule.api.transformer.TransformerException;

public class DateUtilsTest {
	@Test(expected = IllegalArgumentException.class)
	public void nullConcactA() {
		String dateA = null;

		String dateB = "2013-12-09T22:15:33.001Z";

		DateUtils.isAfter(dateA, dateB);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullConcactB() {
		String dateA = "2013-12-09T22:15:33.001Z";
		String dateB = null;

		DateUtils.isAfter(dateA, dateB);
	}

	@Test(expected = IllegalArgumentException.class)
	public void malFormeddateA() throws TransformerException {
		String dateA = "";
		String dateB = "2013-12-09T22:15:33.001Z";

		DateUtils.isAfter(dateA, dateB);
	}

	@Test(expected = IllegalArgumentException.class)
	public void malFormeddateB() throws TransformerException {

		String dateA = "2013-12-09T22:15:33.001Z";
		String dateB = "";

		DateUtils.isAfter(dateA, dateB);
	}

	@Test
	public void dateAIsAfterdateB() throws TransformerException {
		String dateA = "2013-12-10T22:15:33.001Z";
		String dateB = "2013-12-09T22:15:33.001Z";

		Assert.assertTrue("The contact A should be after the contact B", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsNotAfterdateB() throws TransformerException {
		String dateA = "2013-12-08T22:15:33.001Z";
		String dateB = "2013-12-09T22:15:33.001Z";

		Assert.assertFalse("The contact A should not be after the contact B", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsTheSameThatdateB() throws TransformerException {
		String dateA = "2013-12-09T22:15:33.001Z";
		String dateB = "2013-12-09T22:15:33.001Z";

		Assert.assertFalse("The contact A should not be after the contact B", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsTheSameThatdateBInDifferentTimezone() throws TransformerException {
		String dateA = "2013-12-09T01:00:33.001-03:00";
		String dateB = "2013-12-09T07:00:33.001+03:00";

		Assert.assertFalse("The date A should not be after the date B as they are the same ", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsAfterThedateBInDifferentTimezone() throws TransformerException {
		String dateA = "2013-12-09T02:00:33.001-03:00";
		String dateB = "2013-12-09T07:00:33.001+03:00";

		Assert.assertTrue("The date A should be after the date B", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsBeforeThedateBInDifferentTimezone() throws TransformerException {
		String dateA = "2013-12-09T01:00:33.001-03:00";
		String dateB = "2013-12-09T08:00:33.001+03:00";

		Assert.assertFalse("The date A should be after the date B", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsTheSameThatdateBInDifferentTimezoneZulu() throws TransformerException {
		String dateA = "2013-12-09T01:00:33.001-03:00";
		String dateB = "2013-12-09T04:00:33.001Z";

		Assert.assertFalse("The date A should not be after the date B as they are the same ", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsAfterThedateBInDifferentTimezoneZulu() throws TransformerException {
		String dateA = "2013-12-09T02:00:33.001-03:00";
		String dateB = "2013-12-09T04:00:33.001Z";

		Assert.assertTrue("The date A should be after the date B", DateUtils.isAfter(dateA, dateB));
	}

	@Test
	public void dateAIsBeforeThedateBInDifferentTimezoneZulu() throws TransformerException {
		String dateA = "2013-12-09T02:00:33.001-03:00";
		String dateB = "2013-12-09T05:00:33.001Z";
		
		Assert.assertFalse("The date A should be after the date B", DateUtils.isAfter(dateA, dateB));
	}

}
