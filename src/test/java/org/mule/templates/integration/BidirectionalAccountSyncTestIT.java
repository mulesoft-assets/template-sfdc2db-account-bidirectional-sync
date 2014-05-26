package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.Collections;
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
import org.mule.transport.NullPayload;
import org.mule.util.UUID;

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

	private SubflowInterceptingChainLifecycleWrapper updateAccountInAFlow;
	private SubflowInterceptingChainLifecycleWrapper updateAccountInBFlow;
	private InterceptingChainLifecycleWrapper queryAccountFromAFlow;
	private InterceptingChainLifecycleWrapper queryAccountFromBFlow;
	private BatchTestHelper batchTestHelper;

	private List<Map<String, Object>> createdAccountsInB = new ArrayList<Map<String, Object>>();

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
	}

	@Before
	public void setUp() throws MuleException {
		stopAutomaticPollTriggering();
		getAndInitializeFlows();

		batchTestHelper = new BatchTestHelper(muleContext);
	}

	@After
	public void tearDown() throws Exception {
		deleteTestAccountsFromSandBoxA(createdAccountsInB);
		deleteTestAccountsFromSandBoxB(createdAccountsInB);
	}

	private void stopAutomaticPollTriggering() throws MuleException {
		stopFlowSchedulers(A_INBOUND_FLOW_NAME);
		stopFlowSchedulers(B_INBOUND_FLOW_NAME);
	}

	private void getAndInitializeFlows() throws InitialisationException {
		// Flow for updating a Account in A instance
		updateAccountInAFlow = getSubFlow("updateAccountInAFlow");
		updateAccountInAFlow.initialise();

		// Flow for updating a Account in B instance
		updateAccountInBFlow = getSubFlow("updateAccountInBFlow");
		updateAccountInBFlow.initialise();

		// Flow for querying the Account in A instance
		queryAccountFromAFlow = getSubFlow("queryAccountFromAFlow");
		queryAccountFromAFlow.initialise();

		// Flow for querying the Account in B instance
		queryAccountFromBFlow = getSubFlow("queryAccountFromBFlow");
		queryAccountFromBFlow.initialise();
	}

	@Test
	public void whenUpdatingAnAccountInInstanceBTheBelongingAccountGetsUpdatedInInstanceA() throws MuleException, Exception {
		Map<String, Object> user_0_B = new HashMap<String, Object>();
		user_0_B.put("AccountNumber", "123321");
		user_0_B.put("AccountSource", "AccountSource");
		user_0_B.put("AnnualRevenue", "11000");
		user_0_B.put("BillingCity", "San Francisco");
		user_0_B.put("BillingCountry", "USA");
		user_0_B.put("BillingPostalCode", "94108");
		user_0_B.put("BillingState", "California");
		user_0_B.put("BillingStreet", "77 Geary Street");
		user_0_B.put("Description", "Description");
		user_0_B.put("Fax", "(415) 888-112233");
		user_0_B.put("Industry", "Ecommerce");
		user_0_B.put("Name", buildUniqueName(TEMPLATE_NAME, "Test-"));
		user_0_B.put("NumberOfEmployees", 289);
		user_0_B.put("Phone", "(415) 888-1122");
		user_0_B.put("Rating", "");
		user_0_B.put("ShippingCity", "San Francisco");
		user_0_B.put("ShippingCountry", "USA");
		user_0_B.put("ShippingPostalCode", "94108");
		user_0_B.put("ShippingState", "California");
		user_0_B.put("ShippingStreet", "77 Geary Street");
		user_0_B.put("Sic", "Sic");
		user_0_B.put("SicDesc", "SicDesc");
		user_0_B.put("Site", "www.mulsesoft.com");
		user_0_B.put("TickerSymbol", "");
		user_0_B.put("Type", "OEM");
		user_0_B.put("Website", "www.mulsesoft.com");

		createdAccountsInB.add(user_0_B);
		
		SubflowInterceptingChainLifecycleWrapper createAccountInAFlow = getSubFlow("insertAccountInBFlow");
		createAccountInAFlow.initialise();
	
		createAccountInAFlow.process(getTestEvent(Collections.singletonList(user_0_B), MessageExchangePattern.REQUEST_RESPONSE));
	
		// Execution
		executeWaitAndAssertBatchJob(B_INBOUND_FLOW_NAME);

		// Assertions
		Map<String, Object> payload = (Map<String, Object>) queryAccount(user_0_B, queryAccountFromBFlow);
		Assert.assertNotNull("Synchronized Account should not be null", payload);
		Assert.assertEquals("The Account should have been sync and new Name must match", user_0_B.get("Name"), payload.get("Name"));
		Assert.assertEquals("The Account should have been sync and new AccountNumber must match", user_0_B.get("AccountNumber"), payload.get("AccountNumber"));
		Assert.assertEquals("The Account should have been sync and new BillingCity must match", user_0_B.get("BillingCity"), payload.get("BillingCity"));
		Assert.assertEquals("The Account should have been sync and new Phone must match", user_0_B.get("Phone"), payload.get("Phone"));
		Assert.assertEquals("The Account should have been sync and new NumberOfEmployees must match", user_0_B.get("NumberOfEmployees"), payload.get("NumberOfEmployees"));
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
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromBFlow = getSubFlow("deleteAccountFromBFlow");
		deleteAccountFromBFlow.initialise();

		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : createdAccountsInA) {
			idList.add(c.get("Name").toString());
		}
		deleteAccountFromBFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
}

	private void deleteTestAccountsFromSandBoxA(List<Map<String, Object>> createdAccountsInB) throws InitialisationException, MuleException, Exception {
		List<Map<String, Object>> createdAccountsInA = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> c : createdAccountsInB) {
			Map<String, Object> account = invokeRetrieveFlow(queryAccountFromAFlow, c);
			if (account != null) {
				createdAccountsInA.add(account);
			}
		}
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromAFlow = getSubFlow("deleteAccountFromAFlow");
		deleteAccountFromAFlow.initialise();

		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : createdAccountsInA) {
			idList.add(c.get("Id").toString());
		}
		deleteAccountFromAFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
}
	
	protected Map<String, Object> invokeRetrieveFlow(InterceptingChainLifecycleWrapper flow, Map<String, Object> payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();
		return resultPayload instanceof NullPayload ? null : (Map<String, Object>) resultPayload;
	}

}
