package com.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonParseRecursive {
	
	public static HashMap<String, Object> setKeys(String str,
			ArrayList<String> keys) {
		HashMap<String, Object> toReturn = new HashMap<String, Object>();
		for (String key : keys) {
			toReturn.put(key, null);
		}
		JSONObject obj = (JSONObject) JSONValue.parse(str);
		setKeysR(obj, toReturn);
		return toReturn;
	}

	private static void setKeysR(JSONObject obj, HashMap<String, Object> map) {
		if (obj.keySet().size() > 0) {
			for (String key : map.keySet()) {
				if (obj.containsKey(key)) {
					map.put(key, obj.get(key));
					System.out.println("Found! -->" + obj.get(key).toString());
				}
			}
			for (Object keyT : obj.keySet()) {
				if (obj.get(keyT) instanceof JSONArray) {
					for (Object objT : (JSONArray) obj.get(keyT)) {
						if (objT instanceof JSONObject) {
							setKeysR((JSONObject) objT, map);
						}
					}
				}
				if (obj.get(keyT) instanceof JSONObject) {
					setKeysR((JSONObject) obj.get(keyT), map);
				}
			}
		}
		return;
	}
	
	
	public static HashMap<String, Object> getMap(String str) {
		HashMap<String, Object> toReturn = new HashMap<String, Object>();
		JSONObject obj = (JSONObject) JSONValue.parse(str);
		getKeysR(obj, toReturn);
		return toReturn;
	}

	private static void getKeysR(JSONObject obj, HashMap<String, Object> map) {
		if (obj==null){
			return;
		}
		if (obj.keySet().size() > 0) {
			for (Object keyT : obj.keySet()) {
				map.put(keyT.toString(), obj.get(keyT));
				if (obj.get(keyT) instanceof JSONArray) {
					for (Object objT : (JSONArray) obj.get(keyT)) {
						if (objT instanceof JSONObject) {
							getKeysR((JSONObject) objT, map);
						}
					}
				} else if (obj.get(keyT) instanceof JSONObject) {
					getKeysR((JSONObject) obj.get(keyT), map);
				}
			}

		}
		return;
	}
}
