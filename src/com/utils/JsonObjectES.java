package com.utils;

import org.json.simple.JSONObject;

public class JsonObjectES {

	
	@SuppressWarnings("unchecked")
	public static JSONObject getObject(String id, String id_str, String text, String lat, String lng, String original, String operation){
		JSONObject obj = new JSONObject();
		obj.put("type", operation);
		obj.put("id", id);
		JSONObject objFields = new JSONObject();
		objFields.put("id", id);	
		objFields.put("id_str", id_str);
		objFields.put("text", text);
		objFields.put("latlng", lat+", "+lng);
		objFields.put("sentiment", null);
		//objFields.put("lng", lng);
		objFields.put("original", original);
		obj.put("fields", objFields);
		return obj;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
