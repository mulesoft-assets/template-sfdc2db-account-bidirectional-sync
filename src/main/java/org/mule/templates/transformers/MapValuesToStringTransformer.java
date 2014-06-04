package org.mule.templates.transformers;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transport.NullPayload;

import com.mulesoft.module.batch.record.Record;


public class MapValuesToStringTransformer extends AbstractMessageTransformer {

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		
		Record batch_record = message.getInvocationProperty("BATCH_RECORD");
		Object accountInTargetInstance = batch_record.getVariable("accountInTargetInstance");
		if (!(accountInTargetInstance instanceof NullPayload)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> accountInTargetInstanceMap = (Map<String, Object>) accountInTargetInstance;
			
			HashMap<String, String> newAccountInTargetInstance = new HashMap<String, String>();
			
			for (String key : accountInTargetInstanceMap.keySet()) {
				if (accountInTargetInstanceMap.get(key) != null) {
					newAccountInTargetInstance.put(key, accountInTargetInstanceMap.get(key).toString());
				}
			}
			message.setInvocationProperty("newAccountInTargetInstance", newAccountInTargetInstance);
		} else {
			message.setInvocationProperty("newAccountInTargetInstance", accountInTargetInstance);
		}
		
		return message;
	}

}
