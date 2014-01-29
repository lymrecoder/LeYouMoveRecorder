package org.testxxx.util;

import java.util.HashMap;

public class UserSession {

	static private HashMap<String, String> userLoginInfo = new HashMap<String, String>();
	
	static public String get(String key){
		return userLoginInfo.get(key);
	}
	
	static public void set(String key, String val){
		userLoginInfo.put(key, val);
	}
}
