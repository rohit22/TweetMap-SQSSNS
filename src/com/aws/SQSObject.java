package com.aws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class SQSObject {

	private static AmazonSQS sqs;
	private static String queueURL;
	
	public static AmazonSQS getSQS(){
		if (sqs == null){
			sqs = new AmazonSQSClient(new DefaultAWSCredentialsProviderChain());
	        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
	        sqs.setRegion(usEast1);
		}
		setQueueURL();
		return sqs;
	}

	public static String getQueueURL() {
		if (queueURL == null){
			setQueueURL();
		}
		return queueURL;
	}

	public static void setQueueURL() {
		if (sqs == null){
			getSQS();
		}
		for (String url : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + url);
            if (url.contains("TweetMap")){
            	queueURL = url;
            }
        }	
	}
	
	
}
