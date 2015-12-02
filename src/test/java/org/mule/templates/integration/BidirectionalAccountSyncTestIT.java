/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.InterceptingChainLifecycleWrapper;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.db.MySQLDbCreator;
import org.mule.transport.NullPayload;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is validating the correct behavior of the flows
 * for this Mule Anypoint Template
 * 
 */
@SuppressWarnings("unchecked")
public class BidirectionalAccountSyncTestIT extends AbstractTemplateTestCase {

	private static final int TIMEOUT_MILLIS = 60;
	private static final String SFDC_INBOUND_FLOW_NAME = "triggerSyncFromSalesforceFlow";
	private static final String DB_INBOUND_FLOW_NAME = "triggerSyncFromDatabaseFlow";
	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	private static final String PATH_TO_SQL_SCRIPT = "src/main/resources/account.sql";
	private static final String DATABASE_NAME = "SFDC2DBAccountBiDir" + System.currentTimeMillis();
	private static final MySQLDbCreator DBCREATOR = new MySQLDbCreator(DATABASE_NAME, PATH_TO_SQL_SCRIPT, PATH_TO_TEST_PROPERTIES);

	private SubflowInterceptingChainLifecycleWrapper insertAccountIntoSalesforceFlow;
	private InterceptingChainLifecycleWrapper queryAccountFromSalesforceFlow;
	private InterceptingChainLifecycleWrapper queryAccountFromDatabaseFlow;
	private SubflowInterceptingChainLifecycleWrapper insertAccountIntoDatabaseFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteAccountFromDatabaseFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforceFlow;
	

	private List<Map<String, Object>> testAccounts = new ArrayList<Map<String, Object>>();
	
	private BatchTestHelper batchTestHelper;

	@BeforeClass
	public static void beforeTestClass() {
		System.setProperty("page.size", "1000");

		// Set polling frequency to 10 seconds
		System.setProperty("poll.frequencyMillis", "10000");
		System.setProperty("poll.startDelayMillis", "1000");

		// Set default water-mark expressions to current time
		final DateTime now = new DateTime(DateTimeZone.UTC);
		final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		System.setProperty("sfdc.watermark.default.expression", now.toString(dateFormat));
		System.setProperty("db.watermark.default.expression", now.toString(dateFormat));
		
		System.setProperty("database.url", DBCREATOR.getDatabaseUrlWithName());
		DBCREATOR.setUpDatabase();
	}

	@Before
	public void setUp() throws MuleException {
		stopAutomaticPollTriggering();
		getAndInitializeFlows();
		batchTestHelper = new BatchTestHelper(muleContext);
	}

	@After
	public void tearDown() throws Exception {
		deleteTestAccountsFromSalesforce(testAccounts);
		deleteTestAccountsFromDatabase(testAccounts);
		testAccounts.clear();
	}
	
	@AfterClass
	public static void tearDownDB() throws Exception {
		DBCREATOR.tearDownDataBase();
	}

	@Test
	public void testDatabase2Salesforce() throws MuleException, Exception {
		final Map<String, Object> testAaccount = createTestAccountObject();
		testAccounts.add(testAaccount);
		
		insertAccountIntoDatabaseFlow.process(getTestEvent(testAaccount, MessageExchangePattern.REQUEST_RESPONSE));
	
		// Execution
		executeWaitAndAssertBatchJob(DB_INBOUND_FLOW_NAME);

		// Assertions
		final Map<String, Object> payload = (Map<String, Object>) queryAccount(testAaccount, queryAccountFromSalesforceFlow);
		Assert.assertNotNull("Synchronized Account should not be null", payload);
		Assert.assertEquals("The Account should have been sync and new Name must match", testAaccount.get("Name"), payload.get("Name"));
		Assert.assertEquals("The Account should have been sync and new AccountNumber must match", testAaccount.get("AccountNumber"), payload.get("AccountNumber"));
		Assert.assertEquals("The Account should have been sync and new NumberOfEmployees must match", String.valueOf(testAaccount.get("NumberOfEmployees")), "" + payload.get("NumberOfEmployees"));
	}
	
	
	@Test
	public void testSalesforce2Database() throws MuleException, Exception {
		final Map<String, Object> testAccount = createTestAccountObject();
		testAccounts.add(testAccount);
		
		insertAccountIntoSalesforceFlow.process(getTestEvent(testAccount, MessageExchangePattern.REQUEST_RESPONSE));
	
		// Execution
		executeWaitAndAssertBatchJob(SFDC_INBOUND_FLOW_NAME);

		// Assertions
		final Map<String, Object> payload = (Map<String, Object>) queryAccount(testAccount, queryAccountFromDatabaseFlow);
		Assert.assertNotNull("Synchronized Account should not be null", payload);
		Assert.assertEquals("The Account should have been sync and new Name must match", testAccount.get("Name"), payload.get("Name"));
		Assert.assertEquals("The Account should have been sync and new AccountNumber must match", testAccount.get("AccountNumber"), payload.get("AccountNumber"));
		Assert.assertEquals("The Account should have been sync and new NumberOfEmployees must match", testAccount.get("NumberOfEmployees"), payload.get("NumberOfEmployees"));
	}
	
	
	private Map<String, Object> createTestAccountObject(){
		final Map<String, Object> testAccount = new HashMap<String, Object>();
		testAccount.put("AccountNumber", "123321");
		testAccount.put("Description", "Description");
		testAccount.put("Industry", "Ecommerce");
		testAccount.put("Name", buildUniqueName(TEMPLATE_NAME, "Test-"));
		testAccount.put("NumberOfEmployees", 289);
		return testAccount;
	}

	private Object queryAccount(Map<String, Object> account, InterceptingChainLifecycleWrapper queryAccountFlow) throws MuleException, Exception {
		return queryAccountFlow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();
	}

	private void executeWaitAndAssertBatchJob(String flowConstructName) throws Exception {
		// Execute synchronization
		runSchedulersOnce(flowConstructName);

		// Wait for the batch job execution to finish
		batchTestHelper.awaitJobTermination(TIMEOUT_MILLIS * 1000, 500);
		batchTestHelper.assertJobWasSuccessful();
	}
	
	private void deleteTestAccountsFromDatabase(List<Map<String, Object>> createdAccountsInSFDC) throws InitialisationException, MuleException, Exception {
		final List<String> nameList = new ArrayList<String>();
		for (Map<String, Object> account : createdAccountsInSFDC) {
			nameList.add(account.get("Name").toString());
		}
		deleteAccountFromDatabaseFlow.process(getTestEvent(nameList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	private void deleteTestAccountsFromSalesforce(List<Map<String, Object>> accountList) throws InitialisationException, MuleException, Exception {
		final List<Map<String, Object>> testAccounts = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> testAccount : accountList) {
			final Map<String, Object> account = invokeRetrieveFlow(queryAccountFromSalesforceFlow, testAccount);
			if (account != null) {
				testAccounts.add(account);
			}
		}

		final List<String> idList = new ArrayList<String>();
		for (Map<String, Object> account : testAccounts) {
			idList.add(account.get("Id").toString());
		}
		deleteAccountFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
}
	
	protected Map<String, Object> invokeRetrieveFlow(InterceptingChainLifecycleWrapper flow, Map<String, Object> payload) throws Exception {
		final MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		final Object resultPayload = event.getMessage().getPayload();
		return resultPayload instanceof NullPayload ? null : (Map<String, Object>) resultPayload;
	}
	
	private void stopAutomaticPollTriggering() throws MuleException {
		stopFlowSchedulers(SFDC_INBOUND_FLOW_NAME);
		stopFlowSchedulers(DB_INBOUND_FLOW_NAME);
	}
	
	private void getAndInitializeFlows() throws InitialisationException {
		insertAccountIntoSalesforceFlow = getSubFlow("insertAccountIntoSalesforceFlow");
		insertAccountIntoSalesforceFlow.initialise();

		insertAccountIntoDatabaseFlow = getSubFlow("insertAccountIntoDatabaseFlow");
		insertAccountIntoDatabaseFlow.initialise();
 
		queryAccountFromSalesforceFlow = getSubFlow("queryAccountFromSalesforceFlow");
		queryAccountFromSalesforceFlow.initialise();

		queryAccountFromDatabaseFlow = getSubFlow("queryAccountFromDatabaseFlow");
		queryAccountFromDatabaseFlow.initialise();
		
		deleteAccountFromDatabaseFlow = getSubFlow("deleteAccountFromDatabaseFlow");
		deleteAccountFromDatabaseFlow.initialise();
		
		deleteAccountFromSalesforceFlow = getSubFlow("deleteAccountFromSalesforceFlow");
		deleteAccountFromSalesforceFlow.initialise();
	}

}
