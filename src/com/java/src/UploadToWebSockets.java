package com.java.src;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.aws.SNSObject;
import com.aws.UploadTweets;
import com.utils.JsonObjectResult;

public class UploadToWebSockets implements Runnable {

	private static UploadTweets ut;
	private HashMap<Session, String> sessionQueryWords;
	private static boolean onlyIndex;
	private static JSONArray array;
	private static Integer chunk;
	static BlockingQueue<Map<String, String>> messageQueue = new LinkedBlockingQueue<Map<String, String>>();
	private volatile boolean shouldRun;
	private Server server;
	private SubscribeRequest subscribeReq;

	public void setStart() {
		this.shouldRun = true;
		server = new Server(5000);
		server.setHandler(new AmazonSNSHandler());
		try {
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Subscribe to topic
		subscribeReq = new SubscribeRequest().withTopicArn(SNSObject.getTopicArn()).withProtocol("http")
				.withEndpoint("http://19718a8b.ngrok.io");
		SNSObject.getSnsClient().subscribe(subscribeReq);

	}

	public Integer getSize() {
		if (sessionQueryWords != null) {
			return sessionQueryWords.size();
		}
		return null;
	}

	public void shutDown() {
		try {
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.shouldRun = false;
	}

	public void addSession(Session s) {
		if (sessionQueryWords == null || sessionQueryWords.size() == 0) {
			sessionQueryWords = new HashMap<>();
		}
		onlyIndex = false;
		sessionQueryWords.put(s, null);
		return;
	}

	public void removeSession(Session s) {
		Logger.getLogger(TwitterStreamConsumer.class.getName()).log(Level.INFO, "Session Removed", "");
		sessionQueryWords.remove(s);
	}

	public void setSessionQueryWords(Session s, String q) {
		Logger.getLogger(UploadToWebSockets.class.getName()).log(Level.INFO, "Set Query Words to " + q, "");
		sessionQueryWords.put(s, q);
	}

	public static void writeToFileAndUpload(JSONArray array, int chunk) {
		// System.out.println("Writing the file" + chunk);
		FileWriter fw;
		try {
			fw = new FileWriter(new File(String.valueOf(chunk) + ".json"));
			fw.write(array.toJSONString());
			fw.flush();
			fw.close();
			if (ut == null){
				ut = new UploadTweets();
			}
			ut.addDocumentFile(String.valueOf(chunk) + ".json");
			Logger.getLogger(UploadToWebSockets.class.getName()).log(Level.INFO,
					"Uploaded File " + String.valueOf(chunk) + ".json", "upload");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void pushToSocket(JSONObject obj) throws IOException {

		if (obj != null) {
			JSONObject objT = JsonObjectResult.convert(obj);
			String text = objT.get("text").toString();
			if (sessionQueryWords.isEmpty() && !onlyIndex) {
				// System.out.println(sessionQueryWords.size());
				return;
			} else {
				for (Session s : sessionQueryWords.keySet()) {
					if (s.isOpen()) {
						String queryWord = sessionQueryWords.get(s);
						if (queryWord == null || text.contains(queryWord)) {
							s.getBasicRemote().sendText(objT.toString());
						}
					}
				}
			}
			if (array == null) {
				array = new JSONArray();
				chunk = 0;
			}
			array.add(obj);
			if (array.size() == 100 || !shouldRun) {
				writeToFileAndUpload(array, chunk);
				array = new JSONArray();
			}
		}
		return;
	}

	public void run() {
		// TODO Auto-generated method stub
		try {

			while (shouldRun) {

				// Wait for a message from HTTP server
				Map<String, String> messageMap = messageQueue.take();

				// Look for a subscription confirmation Token
				String token = messageMap.get("Token");
				if (token != null) {

					// Confirm subscription
					ConfirmSubscriptionRequest confirmReq = new ConfirmSubscriptionRequest()
							.withTopicArn(SNSObject.getTopicArn()).withToken(token);
					SNSObject.getSnsClient().confirmSubscription(confirmReq);

					continue;
				}

				// Check for a notification
				String message = messageMap.get("Message");
				if (message != null) {
					// System.out.println("Received message: " + message);
					Logger.getLogger(UploadToWebSockets.class.getName()).log(Level.INFO,
							"Received from SNS -> " + message, "");
					JSONParser parser = new JSONParser();
					pushToSocket((JSONObject) parser.parse(message));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// HTTP handler
	static class AmazonSNSHandler extends AbstractHandler {

		// Handle HTTP request
		public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
				throws IOException {

			// Scan request into a string
			Scanner scanner = new Scanner(request.getInputStream());
			StringBuilder sb = new StringBuilder();
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine());
			}

			// Build a message map from the JSON encoded message
			InputStream bytes = new ByteArrayInputStream(sb.toString().getBytes());
			@SuppressWarnings("unchecked")
			Map<String, String> messageMap = new ObjectMapper().readValue(bytes, Map.class);

			// Enqueue message map for receive loop
			messageQueue.add(messageMap);

			// Set HTTP response
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			((Request) request).setHandled(true);
			scanner.close();
		}
	}

}
