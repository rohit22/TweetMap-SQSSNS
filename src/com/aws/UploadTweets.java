package com.aws;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.Hit;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.utils.JsonObjectResult;

public class UploadTweets {

	private static AmazonCloudSearchDomainClient uDomain;
	private static AmazonCloudSearchDomainClient sDomain;
	private static String END_POINT_DOC;
	private static String END_POINT_SEARCH;

	public UploadTweets() {
		END_POINT_DOC = "http://doc-tweetmap-v2-n5hfrsedqpzu4crrdmpgmuu5ya.us-east-1.cloudsearch.amazonaws.com/";
		END_POINT_SEARCH = "http://search-tweetmap-v2-n5hfrsedqpzu4crrdmpgmuu5ya.us-east-1.cloudsearch.amazonaws.com";
	}

	private AmazonCloudSearchDomainClient getUploadDomain() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (/home/rohitb/.aws/credentials).
		 */
		if (uDomain == null) {
			uDomain = new AmazonCloudSearchDomainClient(new ProfileCredentialsProvider("Arushi"));
			uDomain.setEndpoint(END_POINT_DOC);
		}
		return uDomain;
	}

	private AmazonCloudSearchDomainClient getSearchDomain() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (/home/rohitb/.aws/credentials).
		 */
		if (sDomain == null) {
			//sDomain = new AmazonCloudSearchDomainClient(new DefaultAWSCredentialsProviderChain());
			sDomain = new AmazonCloudSearchDomainClient(new ProfileCredentialsProvider("Arushi"));
			sDomain.setEndpoint(END_POINT_SEARCH);
		}
		return sDomain;
	}

	public boolean addDocumentString(String toWrite) {
		try {
			InputStream stream = new ByteArrayInputStream(toWrite.getBytes("UTF_8"));
			UploadDocumentsRequest req = new UploadDocumentsRequest();
			req.setDocuments(stream);
			req.setContentLength((long) toWrite.getBytes("UTF-8").length);
			UploadDocumentsResult result = getUploadDomain().uploadDocuments(req);
			Logger.getLogger(UploadTweets.class.getName()).log(Level.INFO, "File Uploaded "+result, "upload");
			System.out.println(result.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean addDocumentFile(String fileWrite) {
		try {
			File file = new File(fileWrite);
			InputStream stream = new FileInputStream(file);
			UploadDocumentsRequest req = new UploadDocumentsRequest();
			req.setDocuments(stream);
			req.setContentType("application/json");
			req.setContentLength(file.length());
			UploadDocumentsResult result = getUploadDomain().uploadDocuments(req);
			Logger.getLogger(UploadTweets.class.getName()).log(Level.INFO, "File Uploaded "+result, "upload");
//			System.out.println(result.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public String search(String word) throws Exception {
		SearchRequest q = new SearchRequest();
		q.setQuery(word);
		q.setSize((long) 10000);
		//System.out.println(q.getSize());
		Logger.getLogger(UploadTweets.class.getName()).log(Level.INFO, "Searching with the word "+word, "upload");
		AmazonCloudSearchDomainClient domain = getSearchDomain();
		SearchResult sr = domain.search(q);
		
		Logger.getLogger(UploadTweets.class.getName()).log(Level.INFO, "Results Found "+sr.getHits().getFound(), "upload");
		//System.out.println(sr.getHits().getFound());
		JSONArray array = new JSONArray();
		if (sr.getHits().getFound() > 0) {
			for (Hit h : sr.getHits().getHit()) {
				String latlng = "NA, NA";
				if (h.getFields().containsKey("latlng")){
					latlng = h.getFields().get("latlng").toString();
				}
				if (latlng.contains("[")){
					latlng = latlng.replaceAll("\\[","");
					latlng = latlng.replaceAll("\\]", "");
					latlng = latlng.replaceAll(",", ", ");
				}
				JSONObject obj = JsonObjectResult.getObject(h.getId(), h.getFields().get("id_str").toString(),
						h.getFields().get("text").toString(), latlng.split(", ")[0].trim(), latlng.split(", ")[1].trim(), h.getFields().get("sentiment").toString());
				array.add(obj);
			}
		}
		//System.out.println(array.size());
		return array.toString();
	}

	public static void main(String[] args) throws Exception {
		UploadTweets ut = new UploadTweets();
		// AmazonCloudSearchDomainClient domainClient = ut.getDomain(END_POINT);
		// String fileToWrite = "Upload.json";
		// ut.addDocumentFile(END_POINT,fileToWrite);
		ut.search("twitter");
	}
}
