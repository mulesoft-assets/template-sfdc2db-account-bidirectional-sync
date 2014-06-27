/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
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

	private static final String A_INBOUND_FLOW_NAME = "triggerSyncFromAFlow";
	private static final String B_INBOUND_FLOW_NAME = "triggerSyncFromBFlow";
	private static final int TIMEOUT_MILLIS = 60;

	private SubflowInterceptingChainLifecycleWrapper updateAccountInSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper updateAccountInDatabaseFlow;
	private InterceptingChainLifecycleWrapper queryAccountFromSalesforceFlow;
	private InterceptingChainLifecycleWrapper queryAccountFromDatabaseFlow;
	private BatchTestHelper batchTestHelper;
	
	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	private static final String PATH_TO_SQL_SCRIPT = "src/main/resources/account.sql";
	private static final String DATABASE_NAME = "SFDC2DBAccountBiDir" + new Long(new Date().getTime()).toString();
	private static final MySQLDbCreator DBCREATOR = new MySQLDbCreator(DATABASE_NAME, PATH_TO_SQL_SCRIPT, PATH_TO_TEST_PROPERTIES);

	private List<Map<String, Object>> createdAccountsInDatabase = new ArrayList<Map<String, Object>>();

	@BeforeClass
	public static void beforeTestClass() {
		System.setProperty("page.size", "1000");

		// Set polling frequency to 10 seconds
		System.setProperty("polling.frequency", "10000");

		// Set default water-mark expression to current time
		System.clearProperty("watermark.default.expression");
		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		System.setProperty("watermark.default.expression", now.toString(dateFormat));
		
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
		deleteTestAccountsFromSandBoxA(createdAccountsInDatabase);
		deleteTestAccountsFromSandBoxB(createdAccountsInDatabase);
		DBCREATOR.tearDownDataBase();
	}

	private void stopAutomaticPollTriggering() throws MuleException {
		stopFlowSchedulers(A_INBOUND_FLOW_NAME);
		stopFlowSchedulers(B_INBOUND_FLOW_NAME);
	}

	private void getAndInitializeFlows() throws InitialisationException {
		// Flow for updating a Account in A instance
		updateAccountInSalesforceFlow = getSubFlow("updateAccountInSalesforceFlow");
		updateAccountInSalesforceFlow.initialise();

		// Flow for updating a Account in B instance
		updateAccountInDatabaseFlow = getSubFlow("updateAccountInDatabaseFlow");
		updateAccountInDatabaseFlow.initialise();

		// Flow for querying the Account in A instance
		queryAccountFromSalesforceFlow = getSubFlow("queryAccountFromSalesforceFlow");
		queryAccountFromSalesforceFlow.initialise();

		// Flow for querying the Account in B instance
		queryAccountFromDatabaseFlow = getSubFlow("queryAccountFromDatabaseFlow");
		queryAccountFromDatabaseFlow.initialise();
	}

	@Test
	public void whenUpdatingAnAccountInDatastoreTheBelongingAccountGetsUpdatedInSalesforce() throws MuleException, Exception {
		Map<String, Object> accountDatabase = new HashMap<String, Object>();
		accountDatabase.put("AccountNumber", "123321");
		accountDatabase.put("Description", "Description");
		accountDatabase.put("Industry", "Ecommerce");
		accountDatabase.put("Name", buildUniqueName(TEMPLATE_NAME, "Test-"));
		accountDatabase.put("NumberOfEmployees", 289);

		createdAccountsInDatabase.add(accountDatabase);
		
		SubflowInterceptingChainLifecycleWrapper createAccountInDatabaseFlow = getSubFlow("insertAccountInDatabaseFlow");
		createAccountInDatabaseFlow.initialise();
	
		createAccountInDatabaseFlow.process(getTestEvent(Collections.singletonList(accountDatabase), MessageExchangePattern.REQUEST_RESPONSE));
	
		// Execution
		executeWaitAndAssertBatchJob(B_INBOUND_FLOW_NAME);

		// Assertions
		Map<String, Object> payload = (Map<String, Object>) queryAccount(accountDatabase, queryAccountFromDatabaseFlow);
		Assert.assertNotNull("Synchronized Account should not be null", payload);
		Assert.assertEquals("The Account should have been sync and new Name must match", accountDatabase.get("Name"), payload.get("Name"));
		Assert.assertEquals("The Account should have been sync and new AccountNumber must match", accountDatabase.get("AccountNumber"), payload.get("AccountNumber"));
		Assert.assertEquals("The Account should have been sync and new NumberOfEmployees must match", accountDatabase.get("NumberOfEmployees"), payload.get("NumberOfEmployees"));
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
	
	private void deleteTestAccountsFromSandBoxB(List<Map<String, Object>> createdAccountsInA) throws InitialisationException, MuleException, Exception {
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromDatabaseFlow = getSubFlow("deleteAccountFromDatabaseFlow");
		deleteAccountFromDatabaseFlow.initialise();

		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : createdAccountsInA) {
			idList.add(c.get("Name").toString());
		}
		deleteAccountFromDatabaseFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
}

	private void deleteTestAccountsFromSandBoxA(List<Map<String, Object>> createdAccountsInB) throws InitialisationException, MuleException, Exception {
		List<Map<String, Object>> createdAccountsInA = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> c : createdAccountsInB) {
			Map<String, Object> account = invokeRetrieveFlow(queryAccountFromSalesforceFlow, c);
			if (account != null) {
				createdAccountsInA.add(account);
			}
		}
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforceFlow = getSubFlow("deleteAccountFromSalesforceFlow");
		deleteAccountFromSalesforceFlow.initialise();

		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : createdAccountsInA) {
			idList.add(c.get("Id").toString());
		}
		deleteAccountFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
}
	
	protected Map<String, Object> invokeRetrieveFlow(InterceptingChainLifecycleWrapper flow, Map<String, Object> payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();
		return resultPayload instanceof NullPayload ? null : (Map<String, Object>) resultPayload;
	}

}
