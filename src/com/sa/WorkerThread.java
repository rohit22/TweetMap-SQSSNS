package com.sa;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.aws.SNSObject;

public class WorkerThread implements Runnable {

	private String command;

	public WorkerThread(String s) {
		this.command = s;
	}

	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName() + " Start. Command = " + command);
		processCommand();
		// Get sentiment and modify object
		JSONParser p = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) p.parse(command);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (obj != null) {
			JSONObject objFields = (JSONObject) obj.get("fields");
			String text = (String) objFields.get("text");
			String sentiment = AlchemyAPI.getSentiment(text);
			objFields.put("sentiment", sentiment);
			obj.put("fields", objFields);
			PublishRequest publishRequest = new PublishRequest(SNSObject.getTopicArn(), obj.toString());
			PublishResult publishResult = SNSObject.getSnsClient().publish(publishRequest);
			// print MessageId of message published to SNS topic
			System.out.println("MessageId - " + publishResult.getMessageId());
			System.out.println(Thread.currentThread().getName() + " End.");
		}
	}

	private void processCommand() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Thread Interrupted");
			// e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return this.command;
	}
}
