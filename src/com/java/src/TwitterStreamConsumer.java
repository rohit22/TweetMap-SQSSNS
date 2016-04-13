package com.java.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.utils.JsonObjectES;
import com.utils.JsonParseRecursive;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.aws.SQSObject;

public class TwitterStreamConsumer implements Runnable {

	private static final String STREAM_URI = "https://stream.twitter.com/1.1/statuses/filter.json";
	private static GeoApiContext context;
	private volatile static OAuthRequest request;
	private static OAuthService service;
	private static Token accessToken;
	private static TwitterStreamConsumer consumer;
	private volatile static boolean shouldClose;

	public static TwitterStreamConsumer getConsumer() {
		if (consumer == null) {
			consumer = new TwitterStreamConsumer();
			shouldClose = false;
		}
		return consumer;
	}

	private static String queryWordsDefault = "trump, twitter, facebook, zika, america, elections, cloud, bernie"
			+ "budget, linkedin, facebook, yahoo, emoticon, like, mark, share, stock, market, education, obama,"
			+ "bush, clinton, startup, economy, lol, fun, smile, happy, man, women, election, cricket, asia, US,"
			+ "google, goog, china, bjp, budget, irani, smriti, years, arsenal, football, messi, nytimes, ny, winter,"
			+ "snow, temperature, house, animals, zoo, park, donald, obama, health, modi, rahul, donald, brussels";

	private GeoApiContext getContext() {
		if (context == null) {
			context = new GeoApiContext().setApiKey("AIzaSyD55M_a_QoFgzXqsVLmSyC58oZsipgXX1c");
		}
		return context;
	}

	private LatLng getCoordinatesUsingGeo(String location) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoApiContext context = getContext();
		GeocodingResult[] results;
		try {
			results = GeocodingApi.geocode(context, location).await();
			if (results.length > 0) {
				return (results[0].geometry.location);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private void getCoordinates(String location) {
		try {
			Thread.sleep(1000);
			System.out.println(location);
			System.out.println(URLEncoder.encode(location, "utf-8"));
			URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address"
					+ URLEncoder.encode(location, "utf-8") + "&key=");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			System.out.println(sb.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private OAuthRequest getRequest() {
		if (request != null) {
			return request;
		}
		Logger.getLogger(TwitterStreamConsumer.class.getName()).log(Level.INFO,
				"Starting Twitter public stream consumer thread.", "connection");
		// Enter your consumer key and secret below
		service = new ServiceBuilder().provider(TwitterApi.class).apiKey("CJAXt2iFZRB7wAAJZwQX7Flsk")
				.apiSecret("QLiRrx8Knx4AVB9ZrEEafmxplzRdQ97EgysEb9Ka7c1LhySOII").build();

		// Set your access token
		accessToken = new Token("15960177-Rp90ddg8Rc8FBa99PHdXsUeFXeaMRKx8jNyg4QVJR",
				"NkKS7bfa4x0vgC4wU4EHKrnVM8V7DzzWxjQ5wVS8HsvmL");

		// Let's generate the request
		// System.out.println("Connecting to Twitter Public Stream");
		Logger.getLogger(TwitterStreamConsumer.class.getName()).log(Level.INFO, "Connecting to Twitter Public Stream",
				"connection");
		request = new OAuthRequest(Verb.POST, STREAM_URI);
		request.addHeader("version", "HTTP/1.1");
		request.addHeader("host", "stream.twitter.com");
		request.setConnectionKeepAlive(true);
		request.addHeader("user-agent", "Twitter Stream Reader");
		// request.setConnectTimeout(duration, unit);
		shouldClose = false;
		return request;
	}

	public void shutDown() {
		request.setConnectionKeepAlive(false);
		shouldClose = true;
		request = null;
	}

	public void run() {
		try {

			OAuthRequest request = getRequest();

			AmazonSQS sqs = SQSObject.getSQS();

			request.addBodyParameter("track", queryWordsDefault); // Set
			service.signRequest(accessToken, request);
			Response response = request.send();

			// Create a reader to read Twitter's stream
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getStream()));

			String line;
			while (((line = reader.readLine()) != null) && (request != null)) {
				if (shouldClose) {
					response.getStream().close();
					request.setConnectionKeepAlive(false);
					request = null;
					break;
				}
				JSONObject obj = parseTweet(line);
				if (obj != null) {
					Logger.getLogger(TwitterStreamConsumer.class.getName()).log(Level.INFO, "Published to SQS Queue -> " + obj,
							"connection");
					
					// UploadToWebSockets.pushToSocket(obj);
					sqs.sendMessage(new SendMessageRequest(SQSObject.getQueueURL(), obj.toJSONString()));
					
				}

			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private JSONObject parseTweet(String line) {

		JSONObject obj = null;
		Double lat = 3000.0;
		Double lng = 3000.0;
		HashMap<String, Object> map = JsonParseRecursive.getMap(line);
		// System.out.println(map.get("lang"));
		if (map.containsKey("lang") && map.get("lang").toString().equals("en")) {
			Object location = map.get("location");
			if (location == null) {
				location = map.get("place");
			}
			if (map.get("coordinates") != null) {
				// System.out.print("Yes");
				String coordinates = map.get("coordinates").toString();
				// System.out.println(coordinates);
				Logger.getLogger(TwitterStreamConsumer.class.getName()).log(Level.INFO, "Co-ordinates-" + coordinates,
						"upload");
				coordinates = coordinates.replace("[", "");
				coordinates = coordinates.replace("]", "");
				lat = Double.valueOf(coordinates.split(",")[1]);
				lng = Double.valueOf(coordinates.split(",")[0]);

			} else if (false && location != null) {
				if (location.toString().matches("[A-Za-z ]+")) {
					// System.out.println(JsonParseRecursive.getMap(line).get("location"));
					LatLng coordinates = getCoordinatesUsingGeo(
							JsonParseRecursive.getMap(line).get("location").toString());
					if (coordinates != null) {
						lat = coordinates.lat;
						lng = coordinates.lng;
					}
				}
			}
			if (lat != 3000 && lng != 3000) {
				// System.out.println(lat + "--" + lng);
				String text = map.get("text").toString();
				String id = map.get("id").toString();
				String id_str = map.get("id_str").toString();
				obj = JsonObjectES.getObject(id, id_str, text, lat.toString(), lng.toString(), line, "add",null);

				// System.out.println(obj.toJSONString());
			}
		}
		// System.out.println(JsonParseRecursive.getMap(line).get("coordinates"));
		return obj;
	}
}
