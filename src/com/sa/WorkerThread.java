package com.sa;

import java.util.logging.Level;
import java.util.logging.Logger;

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

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Logger.getLogger(WorkerThread.class.getName()).log(Level.INFO,
				Thread.currentThread().getName() + " Started. Fetching sentiment...", "");
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
			Logger.getLogger(WorkerThread.class.getName()).log(Level.INFO, "Publishing to SNS -> " + obj.toString(),
					"");
			PublishRequest publishRequest = new PublishRequest(SNSObject.getTopicArn(), obj.toString());
			PublishResult publishResult = SNSObject.getSnsClient().publish(publishRequest);
			Logger.getLogger(WorkerThread.class.getName()).log(Level.INFO,
					"SNS Message Id -> " + publishResult.getMessageId(), "");
			Logger.getLogger(WorkerThread.class.getName()).log(Level.INFO, Thread.currentThread().getName() + " Ended.",
					"");
		}
	}

	private void processCommand() {
		try {
			Thread.sleep(1000);
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
