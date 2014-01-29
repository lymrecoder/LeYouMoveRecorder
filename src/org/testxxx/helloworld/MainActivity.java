package org.testxxx.helloworld;

import java.util.Hashtable;

import org.testxxx.itfc.CommAsyncTask;
import org.testxxx.itfc.CommIBinder;
import org.testxxx.itfc.SrvrCommDelegate;
import org.testxxx.service.PushService;
import org.testxxx.util.SharePre;
import org.testxxx.util.UserSession;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private EditText username, password;  
    private CheckBox saveInfo, autoLogin;  
    private String usernameValue,passwordValue;
    //private Button loginBtn;
    private SharedPreferences shared; 
    private SharePre sp;
    private SharePre pushSP;
    private CommAsyncTask commasynctask;
	private SrvrCommDelegate scd;
	//设备id
	private String mDeviceID;
   
	/**
	 * 开启所有service
	 */
	private void startGpsService(){
		Intent intent = new Intent();
    	intent.setAction("org.testxxx.service.GPS_DETECT");
    	startService(intent);
	}
	
    private void startCommService(){
		Intent intent_srvr = new Intent();
		intent_srvr.setAction("org.testxxx.service.SRVR_COMM");
    	startService(intent_srvr);
	}
    
    /*private void stopCommService(){
    	Intent intent_srvr = new Intent();
		intent_srvr.setAction("org.testxxx.service.SRVR_COMM");
    	stopService(intent_srvr);
    }*/
	
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        startCommService();
        //获取的实例对象
        shared = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences pushServ = this.getSharedPreferences(PushService.TAG, MODE_PRIVATE);
        sp = new SharePre(shared);  
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        autoLogin = (CheckBox) findViewById(R.id.autoLoginChk);
    	saveInfo = (CheckBox) findViewById(R.id.saveInfoChk);
    	//loginBtn = (Button) findViewById(R.id.loginBtn);
    	//获取设备id,保存到共享文件中
    	mDeviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
    	sp.setString("mDeviceID", mDeviceID);
    	UserSession.set("mDeviceID", mDeviceID);
    	pushSP = new SharePre(pushServ);
    	pushSP.setString(PushService.PREF_DEVICE_ID, mDeviceID);
    	UserSession.set(PushService.PREF_DEVICE_ID, mDeviceID);
    	//开启消息推送服务
        PushService.actionStart(getApplicationContext());
    	//判断记住密码多选框的状态
    	if(sp.getBoolean("SAVE_ISCHECK", false)){
    		//设置密码多选框为记住
    		saveInfo.setChecked(true);
    		username.setText(sp.getString("USERNAME", ""));
    		password.setText(sp.getString("PASSWORD", ""));
        	UserSession.set("USERNAME", sp.getString("USERNAME", ""));
        	UserSession.set("PASSWORD", sp.getString("PASSWORD", ""));
    		//判断自动登录多选框状态
    		if(sp.getBoolean("AUTO_ISCHECK", false)){
    			//设置默认的登录状态
    			autoLogin.setChecked(true);
    			
    	//		startGpsService();
    			//跳转正在登录界面
    			/*Intent intent = new Intent();
		        intent.setAction("org.testxxx.helloworld.TaskCategoryActivity");
		        startActivity(intent);*/
    			Intent login = new Intent(MainActivity.this,TaskCategoryActivity.class);
    			MainActivity.this.startActivity(login);
    		}
    	} 
    	
    	 // 登录监听事件  现在默认为用户名为：liu 密码：123  
/*        loginBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
               
            }  
        });
        */ 
        
        //监听记住密码多选框按钮事件  
        saveInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {  
                if (saveInfo.isChecked()) {
                    System.out.println("记住密码已选中"); 
                    sp.setBoolean("SAVE_ISCHECK", true);
                }else {
                    System.out.println("记住密码没有选中");  
                    sp.setBoolean("SAVE_ISCHECK", false);
                }
            }  
        });
        
      //监听自动登录多选框按钮事件  
        autoLogin.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {  
                if (autoLogin.isChecked()) {
                    System.out.println("自动登录已选中");
                    sp.setBoolean("AUTO_ISCHECK", true);
                }else {
                    System.out.println("自动登录没有选中");
                    sp.setBoolean("AUTO_ISCHECK", false);
                }
            }  
        });
        //加入activity列表
        CloseActivityClass.activityList.add(this);
        scd = new SrvrCommDelegate(this);
		scd.bindService();  
    }
    
    //按钮点击事件
    public void doLogin(View v){
    	usernameValue = username.getText().toString();  
        passwordValue = password.getText().toString();
        Hashtable<String, Object> senddata = new Hashtable<String, Object>();
        CommIBinder binder = scd.getBinder();
     	senddata.put("username", usernameValue);
     	senddata.put("password", passwordValue);
     	System.out.println("username----------"+usernameValue);
     	System.out.println("password----------"+passwordValue);
     	senddata.put("action", ActionType.LOGIN);
     	senddata.put("method", ActionType.CHECK_LOGIN);
     	commasynctask = new CommAsyncTask(this, binder){
 			@Override
 			public void onRecvData(Hashtable<String, Object> recvData) {
 		    	//Toast.makeText(getApplicationContext(), (recvData.isEmpty()?"empty ret":recvData.toString()),
 		    		    // Toast.LENGTH_SHORT).show();
 		    	String isLogin = recvData.get("isLogin").toString();
 		    	if(isLogin.equals("yes")){  
 		             Toast.makeText(MainActivity.this,"登录成功", Toast.LENGTH_SHORT).show();  
 		             //登录成功和记住密码框为选中状态才保存用户信息  
 		             if(saveInfo.isChecked()){
 		             	//记住用户名、密码、
 		             	sp.setString("USERNAME", usernameValue);
 		             	sp.setString("PASSWORD", passwordValue); 
 		             } 
		             UserSession.set("USERNAME", usernameValue);
		             UserSession.set("PASSWORD", passwordValue);
 		             String role_level = recvData.get("role_level").toString();
 		             String uid = recvData.get("uid").toString();
 		             String realname = recvData.get("realname").toString();
 		             //保存权限
 		             sp.setString("ROLE_LEVEL", role_level);
 		             sp.setString("UID", uid);
 		             sp.setString("REALNAME", realname);
		             UserSession.set("ROLE_LEVEL", role_level);
		             UserSession.set("REALNAME", realname);
		             UserSession.set("UID", uid);
 		             System.out.println("-----------role_level-----"+role_level);
 		             //消息订阅
 		             //订阅自己
		             /*PushService.subscribeMsg(usernameValue);
		             PushService.subscribeMsg(realname);
 		             if(role_level.equals("manager")){
 		            	 //订阅leader组
 		            	 PushService.subscribeMsg(PushService.NET_LEADER_GROUP);
 		             }else{
 		            	//订阅网管组
 		            	 PushService.subscribeMsg(PushService.NET_MASTER_GROUP);
 		             }
 		             //订阅客户端AI
 		             PushService.subscribeMsg(PushService.MQTT_CLIENT_AI_ID);*/
 		    //         startGpsService();
 		             //跳转界面  
 		          /* Intent intent = new Intent();
 		           intent.setAction("org.testxxx.helloworld.TaskCategoryActivity");
 		           startActivity(intent);*/
 		            Intent intent = new Intent(MainActivity.this,TaskCategoryActivity.class);
 		            //Intent intent = new Intent(MainActivity.this,GroupMessageActivity.class);
 		            MainActivity.this.startActivity(intent);  
 		             
 		             //finish();
 		         }else{
 		             Toast.makeText(MainActivity.this,"用户名或密码错误!", Toast.LENGTH_LONG).show();  
 		         }
 			}
     	};
     	commasynctask.execute(senddata);
    }  
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hello_world, menu);
        return true;
    }
    
    protected void onDestroy(){
        super.onDestroy();
        scd.unbindService();
    }
}
