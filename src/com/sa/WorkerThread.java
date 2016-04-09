package com.sa;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.aws.SNSObject;

public class WorkerThread implements Runnable {

    private String command;

    public WorkerThread(String s){
        this.command=s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start. Command = "+command);
        processCommand();
    // Get sentiment and modify object    
        
        PublishRequest publishRequest = new PublishRequest(SNSObject.getTopicArn(), command);
        PublishResult publishResult = SNSObject.getSnsClient().publish(publishRequest);
        //print MessageId of message published to SNS topic
        System.out.println("MessageId - " + publishResult.getMessageId());
        System.out.println(Thread.currentThread().getName()+" End.");
    }

    private void processCommand() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        	System.out.println("Thread Interrupted");
            //e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return this.command;
    }
}
