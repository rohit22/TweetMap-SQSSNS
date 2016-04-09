package com.aws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;

public class SNSObject {

	private static String topicArn = "arn:aws:sns:us-east-1:571059497776:TweetMap";

	private static AmazonSNSClient snsClient;
	
	
	
	public static String getTopicArn() {
		return topicArn;
	}

	public static void setTopicArn(String topicArn) {
		SNSObject.topicArn = topicArn;
	}

	public static AmazonSNSClient getSnsClient() {
		if (snsClient == null){
			snsClient = new AmazonSNSClient(new DefaultAWSCredentialsProviderChain());		                           
			snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		}
		return snsClient;
	}
	
	
	
}
