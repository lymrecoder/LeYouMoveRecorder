package org.testxxx.service;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.testxxx.helloworld.GroupMessageActivity;
import org.testxxx.helloworld.R;
import org.testxxx.helloworld.TaskDetail;
import org.testxxx.util.ConnectionLog;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttPersistence;
import com.ibm.mqtt.MqttPersistenceException;
import com.ibm.mqtt.MqttSimpleCallback;

/* 
 * PushService that does all of the work.
 * Most of the logic is borrowed from KeepAliveService.
 * http://code.google.com/p/android-random/source/browse/trunk/TestKeepAlive/src/org/devtcg/demo/keepalive/KeepAliveService.java?r=219
 */
public class PushService extends Service
{
	// this is the log tag
	public static final String		TAG = "MsgPushService";
	public static final String		USERINFO = "userInfo";

	// the IP address, where your MQTT broker is running.
//	private static final String		MQTT_HOST = "115.28.24.92";
	private static final String		MQTT_HOST = "119.161.160.70";
	// the port at which the broker is running. 
//	private static int				MQTT_BROKER_PORT_NUM      = 1883;
	private static int				MQTT_BROKER_PORT_NUM      = 35440;
	// Let's not use the MQTT persistence.
	private static MqttPersistence	MQTT_PERSISTENCE          = null;
	// We don't need to remember any state between the connections, so we use a clean start. 
	private static boolean			MQTT_CLEAN_START          = true;
	// Let's set the internal keep alive for MQTT to 15 mins. I haven't tested this value much. It could probably be increased.
	private static short			MQTT_KEEP_ALIVE           = 30;
	// Set quality of services to 0 (at most once delivery), since we don't want push notifications 
	// arrive more than once. However, this means that some messages might get lost (delivery is not guaranteed)
	private static int[]			MQTT_QUALITIES_OF_SERVICE = { 2 } ;
	private static int				MQTT_QUALITY_OF_SERVICE   = 2;
	// The broker should not retain any messages.
	private static boolean			MQTT_RETAINED_PUBLISH     = false;
		
	// MQTT client ID, which is given the broker. In this example, I also use this for the topic header. 
	// You can use this to run push notifications for multiple apps with one MQTT broker. 
	public static String			MQTT_CLIENT_ID = "push";
	
	//默认的系统服务接收AI
	public static String 			MQTT_CLIENT_AI_ID = "LYCLIENTAI";
	public static String 			MQTT_SERVER_AI_ID = "LYSERVERAI";
	public static String			NET_MASTER_GROUP = "net_master_group";
	public static String 			NET_LEADER_GROUP = "net_leader_group";
	public static String 			USER_SUB_ARR = "USER_SUB_ARR";

	// These are the actions for the service (name are descriptive enough)
	private static final String		ACTION_START = MQTT_CLIENT_ID + ".START";
	private static final String		ACTION_STOP = MQTT_CLIENT_ID + ".STOP";
	private static final String		ACTION_KEEPALIVE = MQTT_CLIENT_ID + ".KEEP_ALIVE";
	private static final String		ACTION_RECONNECT = MQTT_CLIENT_ID + ".RECONNECT";
	
	// Connection log for the push service. Good for debugging.
	private ConnectionLog 			mLog;
	
	// Connectivity manager to determining, when the phone loses connection
	private ConnectivityManager		mConnMan;
	// Notification manager to displaying arrived push notifications 
	private NotificationManager		mNotifMan;

	// Whether or not the service has been started.	
	private boolean 				mStarted;

	// This the application level keep-alive interval, that is used by the AlarmManager
	// to keep the connection active, even when the device goes to sleep.
	private static final long		KEEP_ALIVE_INTERVAL = 1000 * 60 * 28;

	// Retry intervals, when the connection is lost.
	private static final long		INITIAL_RETRY_INTERVAL = 1000 * 10;
	private static final long		MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 30;

	// Preferences instance 
	private static SharedPreferences 		mPrefs;
	private static SharedPreferences 		userInfo;
	// We store in the preferences, whether or not the service has been started
	public static final String		PREF_STARTED = "isStarted";
	// We also store the deviceID (target)
	public static final String		PREF_DEVICE_ID = "deviceID";
	// We store the last retry interval
	public static final String		PREF_RETRY = "retryInterval";

	// Notification title
	public static String			NOTIF_TITLE = "乐友移动网管"; 
	// Notification ticket			
	public static String 			NOTIF_TICKET = "亲，您有新消息啦！";
	// Notification id
	private static int		NOTIF_CONNECTED = 0;	
		
	// This is the instance of an MQTT connection.
	private static MQTTConnection			mConnection;
	private long					mStartTime;
	

	// Static method to start the service
	public static void actionStart(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_START); 
		ctx.startService(i);
	}

	// Static method to stop the service
	public static void actionStop(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_STOP);
		ctx.startService(i);
	}
	
	// Static method to send a keep alive message
	public static void actionPing(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_KEEPALIVE);
		ctx.startService(i);
	}
	
	//发布消息
	public static void publishMsg(String msgTo,String msgFrom,String msg,String mantisId,int EventType){
		if(msgTo.isEmpty()){
			//接收人为空的时候，先check分发组是否有人在线，有则发送到组，否则默认为系统AI
			//TODO:检查分发组是否有人在线
			msgTo = MQTT_SERVER_AI_ID;
		}
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("msgFrom",msgFrom);
			jsonObj.put("msgTo",msgTo);
			jsonObj.put("msg",msg);
			jsonObj.put("mantis_num",mantisId);
			String jsonMsg = jsonObj.toString();
			try {
				System.out.println("pub:----msgto-----"+msgTo+"-----msg---"+msg);
				mConnection.publishToTopic(msgTo,jsonMsg);
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				System.out.println("-------------------push---------");
				e.printStackTrace();
			}
			System.out.println("login-----------msgto-------"+msgTo+"-----------msg-----"+msg+"--------"+jsonMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//订阅主题消息
	public static void subscribeMsg(String msgFrom){
		//对于普通网管只需要订阅网管组，默认AI，和自己ID的主题消息
		//对于管理员来说 需要订阅管理员组，默认AI，和自己ID的主题消息
		if(msgFrom.isEmpty()){
			msgFrom = MQTT_CLIENT_AI_ID;
		}
		try {
			mConnection.subscribeToTopic(msgFrom);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			System.out.println("---------subscribe error---------"+msgFrom);
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		log("Creating service");
		mStartTime = System.currentTimeMillis();

		try {
			mLog = new ConnectionLog();
			Log.i(TAG, "Opened log at " + mLog.getPath());
		} catch (IOException e) {
			Log.e(TAG, "Failed to open log", e);
		}

		// Get instances of preferences, connectivity manager and notification manager
		mPrefs = getSharedPreferences(TAG, MODE_PRIVATE);
		userInfo = getSharedPreferences(USERINFO, MODE_PRIVATE);
		mConnMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		mNotifMan = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	
		/* If our process was reaped by the system for any reason we need
		 * to restore our state with merely a call to onCreate.  We record
		 * the last "started" value and restore it here if necessary. */
		handleCrashedService();
	}
	
	// This method does any necessary clean-up need in case the server has been destroyed by the system
	// and then restarted
	private void handleCrashedService() {
		if (wasStarted() == true) {
			log("Handling crashed service...");
			 // stop the keep alives
			stopKeepAlives(); 
				
			// Do a clean start
			System.out.println("already---------------to-------start()-------");
			start();
		}
	}
	
	//@Override
	/*public void onDestroy() {
		log("Service destroyed (started=" + mStarted + ")");

		// Stop the services, if it has been started
		if (mStarted == true) {
			stop();
		}
		
		try {
			if (mLog != null)
				mLog.close();
		} catch (IOException e) {}		
	}*/
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		log("Service started with intent=" + intent);
		
		// Do an appropriate action based on the intent.
		if (intent.getAction().equals(ACTION_STOP) == true) {
			stop();
			stopSelf();
		} else if (intent.getAction().equals(ACTION_START) == true) {
			System.out.println("--------------start---pushservice---------");
			start();
		} else if (intent.getAction().equals(ACTION_KEEPALIVE) == true) {
			keepAlive();
		} else if (intent.getAction().equals(ACTION_RECONNECT) == true) {
			if (isNetworkAvailable()) {
				reconnectIfNecessary();
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// log helper function
	private void log(String message) {
		log(message, null);
	}
	private void log(String message, Throwable e) {
		if (e != null) {
			Log.e(TAG, message, e);
			
		} else {
			Log.i(TAG, message);			
		}
		
		if (mLog != null)
		{
			try {
				mLog.println(message);
			} catch (IOException ex) {}
		}		
	}
	
	// Reads whether or not the service has been started from the preferences
	private boolean wasStarted() {
		return mPrefs.getBoolean(PREF_STARTED, false);
	}

	// Sets whether or not the services has been started in the preferences.
	private void setStarted(boolean started) {
		mPrefs.edit().putBoolean(PREF_STARTED, started).commit();		
		mStarted = started;
	}

	private synchronized void start() {
		log("Starting service...");
		
		// Do nothing, if the service is already running.
		if (mStarted == true) {
			Log.w(TAG, "Attempt to start connection that is already active");
			return;
		}
		
		// Establish an MQTT connection
		connect();
		System.out.println("-----connected----------goto----registerReceiver");
		// Register a connectivity listener
		registerReceiver(mConnectivityChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		System.out.println("------registerReceiver----------end");
	}

	private synchronized void stop() {
		// Do nothing, if the service is not running.
		if (mStarted == false) {
			Log.w(TAG, "Attempt to stop connection not active.");
			return;
		}

		// Save stopped state in the preferences
		setStarted(false);

		// Remove the connectivity receiver
		unregisterReceiver(mConnectivityChanged);
		// Any existing reconnect timers should be removed, since we explicitly stopping the service.
		cancelReconnect();

		// Destroy the MQTT connection if there is one
		if (mConnection != null) {
			mConnection.disconnect();
			mConnection = null;
		}
	}
	
	// 
	private synchronized void connect() {		
		log("Connecting...");
		// fetch the device ID from the preferences.
		String deviceID = mPrefs.getString(PREF_DEVICE_ID, null);
		System.out.println("action start--------1--"+deviceID); 
		// Create a new connection only if the device id is not NULL
		if (deviceID == null) {
			log("Device ID not found.");
		} else {
			try {
				System.out.println("action start--------2--"+MQTT_HOST); 
				mConnection = new MQTTConnection(MQTT_HOST, deviceID);
				System.out.println("action start--------3--"+MQTT_HOST);
			} catch (MqttException e) {
				// Schedule a reconnect, if we failed to connect
				log("MqttException: " + (e.getMessage() != null ? e.getMessage() : "NULL"));
	        	if (isNetworkAvailable()) {
	        		scheduleReconnect(mStartTime);
	        	}
			}
			setStarted(true);
		}
	}

	private synchronized void keepAlive() {
		try {
			// Send a keep alive, if there is a connection.
			if (mStarted == true && mConnection != null) {
				mConnection.sendKeepAlive();
			}
		} catch (MqttException e) {
			log("MqttException: " + (e.getMessage() != null? e.getMessage(): "NULL"), e);
			
			mConnection.disconnect();
			mConnection = null;
			cancelReconnect();
		}
	}

	// Schedule application level keep-alives using the AlarmManager
	private void startKeepAlives() {
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
		  System.currentTimeMillis() + KEEP_ALIVE_INTERVAL,
		  KEEP_ALIVE_INTERVAL, pi);
	}

	// Remove all scheduled keep alives
	private void stopKeepAlives() {
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}

	// We schedule a reconnect based on the starttime of the service
	public void scheduleReconnect(long startTime) {
		// the last keep-alive interval
		long interval = mPrefs.getLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);

		// Calculate the elapsed time since the start
		long now = System.currentTimeMillis();
		long elapsed = now - startTime;


		// Set an appropriate interval based on the elapsed time since start 
		if (elapsed < interval) {
			interval = Math.min(interval * 4, MAXIMUM_RETRY_INTERVAL);
		} else {
			interval = INITIAL_RETRY_INTERVAL;
		}
		
		log("Rescheduling connection in " + interval + "ms.");

		// Save the new internval
		mPrefs.edit().putLong(PREF_RETRY, interval).commit();

		// Schedule a reconnect using the alarm manager.
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
	}
	
	// Remove the scheduled reconnect
	public void cancelReconnect() {
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}
	
	private synchronized void reconnectIfNecessary() {		
		if (mStarted == true && mConnection == null) {
			log("Reconnecting...");
			connect();
		}
	}

	// This receiver listeners for network changes and updates the MQTT connection
	// accordingly
	private BroadcastReceiver mConnectivityChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get network info
			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra (ConnectivityManager.EXTRA_NETWORK_INFO);
			
			// Is there connectivity?
			boolean hasConnectivity = (info != null && info.isConnected()) ? true : false;

			log("Connectivity changed: connected=" + hasConnectivity);
			System.out.println("-------onReceive------");
			if (hasConnectivity) {
				reconnectIfNecessary();
			} else if (mConnection != null) {
				// if there no connectivity, make sure MQTT connection is destroyed
				mConnection.disconnect();
				cancelReconnect();
				mConnection = null;
			}
		}
	};
	
	// Display the topbar notification
	private void showNotification(String json) {
		Notification n = new Notification();
				
		n.flags |= Notification.FLAG_SHOW_LIGHTS;
      	n.flags |= Notification.FLAG_AUTO_CANCEL;

        n.defaults = Notification.DEFAULT_ALL;
        n.ledARGB = Color.MAGENTA;   
        n.ledOnMS =2000;
      	
		n.icon = R.drawable.ic_launcher;
		n.when = System.currentTimeMillis();
		n.tickerText = NOTIF_TICKET;
		//n.number++;
		// Simply open the parent activity
		System.out.println("receive--------"+json);
		Intent intent = new Intent(this, TaskDetail.class);
		Intent intentmsg = new Intent(this, GroupMessageActivity.class);
		String msg = "";
		Boolean isTask = true;
		try {
			JSONObject msgJson = new JSONObject(json);
			System.out.println("receive2--------"+msgJson);
			if(msgJson.getString("mantis_num").equals("groupmsg")){
				System.out.println("-----------groupmsg--------"+msgJson.getString("msg"));
				intentmsg.putExtra("msgFrom", msgJson.getString("msgFrom"));
				intentmsg.putExtra("msgTo", msgJson.getString("msgTo"));
				intentmsg.putExtra("mantis_num", msgJson.getString("mantis_num"));
				intentmsg.putExtra("msg", msgJson.getString("msg"));
				isTask = false;
			}else{
				System.out.println("-----------mantis msg--------");
				intent.putExtra("msgFrom", msgJson.getString("msgFrom"));
				intent.putExtra("msgTo", msgJson.getString("msgTo"));
				intent.putExtra("mantis_num", msgJson.getString("mantis_num"));
			}
			msg = msgJson.getString("msg");
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!isTask){
			PendingIntent pi = PendingIntent.getActivity(this, 0, intentmsg, Intent.FLAG_ACTIVITY_NEW_TASK);
			n.setLatestEventInfo(this, NOTIF_TITLE, msg, pi);
		}else{
			PendingIntent pi = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			n.setLatestEventInfo(this, NOTIF_TITLE, msg, pi);
		}

		// Change the name of the notification here
		//n.setLatestEventInfo(this, NOTIF_TITLE, msg, pi);
		//mNotifMan.notify(NOTIF_CONNECTED, n);
		//NOTIF_CONNECTED++;
		mNotifMan.notify(NOTIF_CONNECTED++, n);
	}
	
	// Check if we are online
	private boolean isNetworkAvailable() {
		NetworkInfo info = mConnMan.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		return info.isConnected();
	}
	
	// This inner class is a wrapper on top of MQTT client.
	private class MQTTConnection implements MqttSimpleCallback {
		IMqttClient mqttClient = null;
		
		// Creates a new connection given the broker address and initial topic
		public MQTTConnection(String brokerHostName, String initTopic) throws MqttException {
			// Create connection spec
	    	String mqttConnSpec = "tcp://" + brokerHostName + "@" + MQTT_BROKER_PORT_NUM;
	        	// Create the client and connect
	    		System.out.println("action start---------4--"+mqttConnSpec);
	        	mqttClient = MqttClient.createMqttClient(mqttConnSpec, MQTT_PERSISTENCE);
	        	System.out.println("action start---------5--"+MQTT_PERSISTENCE);
	        	String clientID = MQTT_CLIENT_ID + "/" + mPrefs.getString(PREF_DEVICE_ID, "");
	        	System.out.println("action start---------6--"+clientID);
	        	mqttClient.connect(clientID, MQTT_CLEAN_START, MQTT_KEEP_ALIVE);
	        	System.out.println("action start---------7--"+clientID);
		        // register this client app has being able to receive messages
				mqttClient.registerSimpleHandler(this);
				
				// Subscribe to an initial topic, which is combination of client ID and device ID.
				//initTopic = MQTT_CLIENT_ID + "/" + initTopic;
				//subscribeToTopic(initTopic);
		
				log("Connection established to " + brokerHostName + " on topic " + initTopic);
		
				// Save start time
				mStartTime = System.currentTimeMillis();
				// Star the keep-alives
				startKeepAlives();				        
		}
		
		// Disconnect
		public void disconnect() {
			try {			
				stopKeepAlives();
				mqttClient.disconnect();
			} catch (MqttPersistenceException e) {
				log("MqttException" + (e.getMessage() != null? e.getMessage():" NULL"), e);
			}
		}
		/*
		 * Send a request to the message broker to be sent messages published with 
		 *  the specified topic name. Wildcards are allowed.	
		 */
		private void subscribeToTopic(String topicName) throws MqttException {
			
			if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
				// quick sanity check - don't try and subscribe if we don't have
				//  a connection
				log("Connection error" + "No connection");	
			} else {									
				String[] topics = { topicName };
				mqttClient.subscribe(topics, MQTT_QUALITIES_OF_SERVICE);
			}
		}	
		/*
		 * Sends a message to the message broker, requesting that it be published
		 *  to the specified topic.
		 */
		public void publishToTopic(String topicName, String message) throws MqttException {		
			if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
				// quick sanity check - don't try and publish if we don't have
				//  a connection				
				log("No connection to public to");		
			} else {
				mqttClient.publish(topicName, 
								   message.getBytes(),
								   MQTT_QUALITY_OF_SERVICE, 
								   MQTT_RETAINED_PUBLISH);
			}
		}		
		
		/*
		 * Called if the application loses it's connection to the message broker.
		 */
		public void connectionLost() throws Exception {
			log("Loss of connection" + "connection downed");
			stopKeepAlives();
			// null itself
			mConnection = null;
			if (isNetworkAvailable() == true) {
				reconnectIfNecessary();	
			}
		}		
		
		/*
		 * Called when we receive a message from the message broker. 
		 */
		public void publishArrived(String topicName, byte[] payload, int qos, boolean retained) {
			// Show a notification
			String s = new String(payload);
			showNotification(s);	
			log("Got message: " + s);
		}   
		
		public void sendKeepAlive() throws MqttException {
			log("Sending keep alive");
			// publish to a keep-alive topic
			publishToTopic(MQTT_CLIENT_ID + "/keepalive", mPrefs.getString(PREF_DEVICE_ID, ""));
		}		
	}
}