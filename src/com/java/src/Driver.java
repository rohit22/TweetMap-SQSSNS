package com.java.src;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.apache.commons.lang3.SystemUtils;

import com.amazonaws.http.IdleConnectionReaper;
import com.aws.SNSObject;
import com.aws.SQSObject;

import com.sa.WorkerPoolCreater;

public class Driver extends Thread {

	static volatile boolean hasStarted = false;
	static TwitterStreamConsumer consumer;
	static WorkerPoolCreater wpc;
	static UploadToWebSockets uws;
	private volatile ExecutorService executor;

	public static boolean hasStarted() {
		return hasStarted;
	}

	/*
	 * public static void initializePipeline() { if (!hasStarted) { hasStarted =
	 * true; consumer = TwitterStreamConsumer.getConsumer(); consumer.start();
	 * wpc = new WorkerPoolCreater(); wpc.setStart(); wpc.start(); uws = new
	 * UploadToWebSockets(); uws.setStart(); uws.start(); } }
	 */

	public void run() {
		if (!hasStarted) {
			hasStarted = true;
			executor = Executors.newFixedThreadPool(3);
			consumer = TwitterStreamConsumer.getConsumer();
			wpc = new WorkerPoolCreater();
			uws = new UploadToWebSockets();
			wpc.setStart();
			uws.setStart();
			executor.execute(consumer);
			executor.execute(wpc);
			executor.execute(uws);
			System.out.println("Started All the threads");
		}
	}

	public void addSession(Session s) {
		startIfNot();
		uws.addSession(s);
	}

	public void addSession(Session s, String queryWords) {
		startIfNot();
		uws.setSessionQueryWords(s, queryWords);
	}

	public void removeSession(Session s) {
		if (uws.getSize() == 1) {
			stopDriver();
			return;
		}
		uws.removeSession(s);
	}

	public void startIfNot() {
		if (!hasStarted) {
			run();
		}
	}

	public static void closeTwitterConsumer() {
		if (hasStarted) {
			hasStarted = false;
			wpc.shutDown();
			consumer.shutDown();
			uws.shutDown();
			IdleConnectionReaper.shutdown();
			// SNSObject.getSnsClient().shutdown();
			// SQSObject.getSQS().shutdown();
		}

	}

	public void stopDriver() {
		hasStarted = false;
		consumer.shutDown();
		wpc.shutDown();
		uws.shutDown();
		executor.shutdown();
		try {
			executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
			executor.shutdownNow();
			IdleConnectionReaper.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.printf("WAS interrupted\n");
		}
		executor = null;
		System.out.println("Stopped everything");
	}

}
