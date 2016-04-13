package com.utils;

import org.json.simple.JSONObject;

public class JsonObjectResult {
	@SuppressWarnings("unchecked")
	public static JSONObject getObject(String id, String id_str, String text, String lat, String lng, String sentiment) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("id_str", id_str);
		obj.put("text", text);
		obj.put("lat", lat);
		obj.put("lng", lng);
		obj.put("sentiment", sentiment);
		return obj;
	}

	public static JSONObject convert(JSONObject obj) {
		JSONObject objFields = (JSONObject) obj.get("fields");
		String latlng = (String) objFields.get("latlng");
		return getObject((String) objFields.get("id"), (String) objFields.get("id_str"), (String) objFields.get("text"),
				latlng.split(", ")[0], latlng.split(", ")[1], (String) objFields.get("sentiment"));

	}
}
