package com.websocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.aws.UploadTweets;
import com.java.src.Driver;
import com.java.src.UploadToWebSockets;


@ServerEndpoint("/getTweets")
public class GetTweets {
	/**
	 * @OnOpen allows us to intercept the creation of a new session. The session
	 *         class allows us to send data to the user. In the method onOpen,
	 *         we'll let the user know that the handshake was successful.
	 */
	private static String queryWord;
	private static Driver d;
	
	
	@OnOpen
	public void onOpen(Session session) {
		Logger.getLogger(GetTweets.class.getName()).log(Level.INFO, "Session Started", "start");
		try {
			d = new Driver();
			d.start();
			Thread.sleep(1000);
			d.addSession(session);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * When a user sends a message to the server, this method will intercept the
	 * message and allow us to react to it. For now the message is read as a
	 * String.
	 */
	@OnMessage
	public synchronized void onMessage(String message, Session session) {
	//	if (streamConsumer == null) {
	//		onOpen(session);
	//	}
		queryWord = message;
		if (queryWord.trim().length() == 0) {
			queryWord = null;
		}
		if (queryWord != null) {
			Logger.getLogger(GetTweets.class.getName()).log(Level.INFO, "Received Query " + message, "start");
			UploadTweets ut = new UploadTweets();
			String results = "No Results In the database";
			try {
				results = ut.search(queryWord).toString();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				session.getBasicRemote().sendText(results);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		d.addSession(session, queryWord);
	}

	/**
	 * The user closes the connection.
	 * 
	 * Note: you can't send messages to the client from this method
	 */
	@OnClose
	public void onClose(Session session) {
		try {
			// streamConsumer.join();
			d.removeSession(session);
			session.close();
			// } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.getLogger(GetTweets.class.getName()).log(Level.INFO, "Session Ended", "start");
	}

	@OnError
	public void onError(Throwable t, Session s) {
		d.removeSession(s);
		d.stopDriver();
		d.interrupt();
	}
}
