package com.sa;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.aws.SQSObject;

public class WorkerPoolCreater implements Runnable {

	private volatile boolean workerStatus = false;
	private ExecutorService executor;

	public void run() {
		// TODO Auto-generated method stub
		executor = Executors.newFixedThreadPool(5);
		AmazonSQS sqs = SQSObject.getSQS();
		while (workerStatus) {
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(SQSObject.getQueueURL());
			List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
			for (Message message : messages) {
				Runnable worker = new WorkerThread("" + message.getBody());
				executor.execute(worker);
				String messageReceiptHandle = message.getReceiptHandle();
				sqs.deleteMessage(new DeleteMessageRequest(SQSObject.getQueueURL(), messageReceiptHandle));
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Interrupted");
			}
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		System.out.println("Finished all threads");

	}

	public void setStart() {
		this.workerStatus = true;
	}

	public void shutDown() {
		this.workerStatus = false;
		if (executor != null) {
			while (!executor.isTerminated()) {
			}
		}

	}

}