package org.ubstorm.service.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonUtils {
	public HashMap<String, Object> jsonToMap(String json) throws ParseException
	{
		HashMap<String, Object> retMap = new HashMap<String, Object>();

		JSONObject _jsonObj = (JSONObject) new JSONParser().parse(json);
		
	    if(json != null) {
	        retMap = toMap(_jsonObj);
	    }
	    return retMap;
	}

	private HashMap<String, Object> toMap(JSONObject _jsonObj)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();

		HashMap<String, Object> _jsonObj2 = (HashMap<String, Object>) _jsonObj;
		map = _jsonObj2;
		
//	    Iterator<String> keysItr = _jsonObj
//	    while(keysItr.hasNext()) {
//	        String key = keysItr.next();
//	        Object value = object.get(key);
//
//	        if(value instanceof JSONArray) {
//	            value = toList((JSONArray) value);
//	        }
//
//	        else if(value instanceof JSONObject) {
//	            value = toMap((JSONObject) value);
//	        }
//	        map.put(key, value);
//	    }
	    return map;
	}
//
//	public List<Object> toList(JSONArray array) throws JSONException {
//	    List<Object> list = new ArrayList<Object>();
//	    for(int i = 0; i < array.length(); i++) {
//	        Object value = array.get(i);
//	        if(value instanceof JSONArray) {
//	            value = toList((JSONArray) value);
//	        }
//
//	        else if(value instanceof JSONObject) {
//	            value = toMap((JSONObject) value);
//	        }
//	        list.add(value);
//	    }
//	    return list;
//	}
}