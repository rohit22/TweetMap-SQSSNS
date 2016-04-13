package com.sa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AlchemyAPI {

	private static String _apiKey = "0da824ce491b87189b5ed8bc230e0c18d0a295b4";
	private static String _requestUri = "https://gateway-a.watsonplatform.net/calls/";

	public static String getSentiment(String text) {
		try {
			Document doc = AlchemyAPI.TextGetTextSentiment(text);
			if (doc != null) {
				return getSentimentFromDocument(doc);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "nuetral";
	}

	public static Document TextGetTextSentiment(String text)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		return TextGetTextSentiment(text, new AlchemyAPI_Params());
	}

	public static Document TextGetTextSentiment(String text, AlchemyAPI_Params params)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		CheckText(text);

		params.setText(text);

		return POST("TextGetTextSentiment", "text", params);
	}

	private static void CheckText(String text) {
		if (null == text || text.length() == 0)
			throw new IllegalArgumentException("Enter some text to analyze.");
	}

	private static Document POST(String callName, String callPrefix, AlchemyAPI_Params params)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		URL url = new URL(_requestUri + callPrefix + "/" + callName);

		HttpURLConnection handle = (HttpURLConnection) url.openConnection();
		handle.setDoOutput(true);
		StringBuilder data = new StringBuilder();
		data.append("apikey=").append(_apiKey);
		data.append(params.getParameterString());
		handle.addRequestProperty("Content-Length", Integer.toString(data.length()));
		DataOutputStream ostream = new DataOutputStream(handle.getOutputStream());
		ostream.write(data.toString().getBytes());
		ostream.close();
		return doRequest(handle, params.getOutputMode());
	}

	private static Document doRequest(HttpURLConnection handle, String outputMode)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		DataInputStream istream = new DataInputStream(handle.getInputStream());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(istream);

		istream.close();
		handle.disconnect();
		XPathFactory factory = XPathFactory.newInstance();

		if (AlchemyAPI_Params.OUTPUT_XML.equals(outputMode)) {
			String statusStr = getNodeValue(factory, doc, "/results/status/text()");
			if (null == statusStr || !statusStr.equals("OK")) {
				String statusInfoStr = getNodeValue(factory, doc, "/results/statusInfo/text()");
				if (null != statusInfoStr && statusInfoStr.length() > 0) {
					System.out.println("Error making API call: " + statusInfoStr + '.');
					// throw new IOException();
				}
				System.out.println("Error making API call: " + statusStr + '.');
				// throw new IOException("Error making API call: " + statusStr +
				// '.');
			}
		} else if (AlchemyAPI_Params.OUTPUT_RDF.equals(outputMode)) {
			String statusStr = getNodeValue(factory, doc, "//RDF/Description/ResultStatus/text()");
			if (null == statusStr || !statusStr.equals("OK")) {
				String statusInfoStr = getNodeValue(factory, doc, "//RDF/Description/ResultStatus/text()");
				if (null != statusInfoStr && statusInfoStr.length() > 0) {
					System.out.println("Error making API call: " + statusInfoStr + '.');
					// throw new IOException();
				}
				System.out.println("Error making API call: " + statusStr + '.');
				// throw new IOException("Error making API call: " + statusStr +
				// '.');
			} else {
				System.out.println("Here!");
			}
		}

		return doc;
	}

	private static String getNodeValue(XPathFactory factory, Document doc, String xpathStr)
			throws XPathExpressionException {
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(xpathStr);
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList results = (NodeList) result;

		if (results.getLength() > 0 && null != results.item(0))
			return results.item(0).getNodeValue();

		return null;
	}

	public static void main(String[] args)
			throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
		// TODO Auto-generated method stub

		/*
		 * AlchemyLanguage service = new AlchemyLanguage();
		 * service.setApiKey("0da824ce491b87189b5ed8bc230e0c18d0a295b4");
		 * 
		 * Map<String, Object> params = new HashMap<String, Object>();
		 * params.put(AlchemyLanguage.TEXT,
		 * "IBM Watson won the Jeopardy television show hosted by Alex Trebek");
		 * DocumentSentiment sentiment = service.getSentiment(params);
		 * 
		 * System.out.println(sentiment);
		 */
		// AlchemyAPI e = new AlchemyAPI();

		System.out.println(getSentimentFromDocument(
				AlchemyAPI.TextGetTextSentiment("IBM Watson won the Jeopardy television show hosted by Alex Trebek")));
	}

	private static String getSentimentFromDocument(Document doc) {
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//docSentiment/type");
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			String s = nl.item(0).getTextContent();
			return s;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "neutral";
	}

}
