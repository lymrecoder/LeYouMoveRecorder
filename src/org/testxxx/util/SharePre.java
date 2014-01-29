package org.testxxx.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePre extends Activity {
	private SharedPreferences sp; 
	//private static String sharefile;
	
	public SharePre(SharedPreferences shared){
		sp = shared;
	}
	
	//设置int变量
	public void setInt(String key, int value){
		Editor editor = sp.edit(); 
		editor.putInt(key, value);
		editor.commit(); 
	}
	
	//设置string变量
	public void setString(String key, String value){
		Editor editor = sp.edit(); 
		editor.putString(key, value);
		editor.commit(); 
	}
	
	//设置boolean变量
	public void setBoolean(String key, Boolean value){
		Editor editor = sp.edit(); 
		editor.putBoolean(key, value);
		editor.commit(); 
	}
	
	//设置数组
	public void setStringSet(){
		
	}
	
	//获取int值
	public int getInt(String key, int defValue){
		int val = sp.getInt(key, defValue);
		return val;
	}
	
	//获取string值
	public String getString(String key, String defValue){
		String val = sp.getString(key, defValue);
		return val;
	}
	
	//获取Boolean值
	public Boolean getBoolean(String key, Boolean defValue){
		Boolean val = sp.getBoolean(key, defValue);
		return val;
	}
	
	
	
	
}
