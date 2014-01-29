package org.testxxx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class JsonDataParser extends AbstractDataParser {

	public Hashtable<?, ?> JSonObjectToHashtable(String data) {
        Hashtable<String, Object> htJS = new Hashtable<String, Object>();
        JSONObject objJS;
        try {
            objJS = new JSONObject(data);
            Iterator<String> it = objJS.keys();
            String key = null;
            Object value = null;
            while (it.hasNext()) {
                key = it.next();
                value = objJS.get(key);
                if (value instanceof JSONObject) {
                    value = JSonObjectToHashtable(value.toString());
                }
                htJS.put((String) key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return htJS;
    }

	public List<Hashtable<String, Object>> JSonArrayToHashtable(String data) {
        List<Hashtable<String, Object>> listMap = new ArrayList<Hashtable<String,Object>>();
        Hashtable<String,Object> entry = new Hashtable<String,Object>();
        JSONArray objJSA;

        try {
            objJSA = new JSONArray(data);
            for (int i = 0; i < objJSA.length(); i++) {
                JSONObject objJS = objJSA.getJSONObject(i);
                Iterator<String> it = objJS.keys();
                String key = null;
                Object value = null;
                while (it.hasNext()) {
                    key = it.next();
                    value = objJS.get(key);
                    if (value instanceof JSONObject) {
                        value = JSonObjectToHashtable(value.toString());
                    }
                    entry.put((String) key, value);
                }
                listMap.add(entry);
                entry = new Hashtable<String,Object>();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listMap;
    }
	
	@Override
	public Hashtable<String, Object> decode(String recvdata) {
		JSONObject objJS = null;
		Hashtable<String, Object> retval = new Hashtable<String, Object>();
		try {
			objJS = new JSONObject(recvdata);
			Iterator<String> it = objJS.keys();
            String key = null;
            Object value = null;
            while (it.hasNext()) {
                key = it.next();
                value = objJS.get(key);
                // 暂时不处理非string值，即暂时只允许简单结构
                if (value instanceof JSONObject) {
           //         continue;
                	value = JSonObjectToHashtable(value.toString());
                }else if (value instanceof JSONArray) {
           //     	continue;
                    value = JSonArrayToHashtable(value.toString());
                }
                retval.put(key, value);
            }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return retval;
	}

	@Override
	public String encode(Hashtable<String, Object> senddata) {
		JSONObject objJS = new JSONObject(senddata);
		Log.v("json encode", objJS.toString());
		
		// TODO Auto-generated method stub
		return null;
	}

}
