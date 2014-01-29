package org.testxxx.helloworld;

import org.testxxx.service.PushService;
import org.testxxx.util.SharePre;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class GroupMessageActivity extends Activity implements org.testxxx.helloworld.PathView.OnItemClickListener{
	private SharedPreferences shared;
	private Button pushmsgBtn;
	private CheckBox sunxin,guokai,haitao,dengbiao;
	private EditText messagecnt;
	private Intent recevIntent;
	private String recevMsg;
	private TextView fromMsg;
	
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //去除标题
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.group_message);
	        //获取的实例对象
	        shared = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
	        pushmsgBtn = (Button) findViewById(R.id.push_msg);
	        sunxin = (CheckBox) findViewById(R.id.suixin);
	        guokai = (CheckBox) findViewById(R.id.guokai);
	        haitao = (CheckBox) findViewById(R.id.haitao);
	        dengbiao = (CheckBox) findViewById(R.id.dengbiao);
	        messagecnt = (EditText) findViewById(R.id.messagecnt);
	        recevIntent = getIntent();
	        recevMsg = recevIntent.getStringExtra("msg");
	        fromMsg = (TextView) findViewById(R.id.groupmessage);
	        System.out.println("----------------recevMsg---------"+recevMsg);
	        fromMsg.setText(recevMsg);
	        //为消息发送按钮绑定click事件
	        pushmsgBtn.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {  
	               String msg = messagecnt.getText().toString();
	               String realname = shared.getString("REALNAME", "访客");
	               msg = realname+":"+msg;
	               if(msg.isEmpty()){
	            	   Toast.makeText(getApplicationContext(), "亲，不要闹，敢不敢把消息写上？",
	    		    		     Toast.LENGTH_LONG).show();
	               }else{
	            	   String msgFrom = shared.getString("USERNAME", "");
	            	   if(sunxin.isChecked()){
	            		   PushService.publishMsg("leader1", msgFrom, msg, "groupmsg", 2);
	            	   }
	            	   
	            	   if(guokai.isChecked()){
	            		   PushService.publishMsg("leader2", msgFrom, msg, "groupmsg", 2);
	            	   }
	            	   
	            	   if(haitao.isChecked()){
	            		   PushService.publishMsg("testuser1", msgFrom, msg, "groupmsg", 2);
	            	   }
	            	   
	            	   if(dengbiao.isChecked()){
	            		   PushService.publishMsg("testuser2", msgFrom, msg, "groupmsg", 2);
	            	   }
	            	   
	            	   if(!sunxin.isChecked()&&!guokai.isChecked()&&!haitao.isChecked()&&!dengbiao.isChecked()){
	            		   Toast.makeText(getApplicationContext(), "亲，不要闹，敢不敢把接收人选上？",
		    		    		     Toast.LENGTH_LONG).show();
	            	   }else{
	            		   Toast.makeText(getApplicationContext(), "亲,发送成功！",
		    		    		     Toast.LENGTH_LONG).show();
	            	   }
	               }
	               
	            }  
	        }); 
	        
	      //右下角拇指导航
	       setupView();
	     //加入activity列表
	        CloseActivityClass.activityList.add(this);
	    }
	 
	 private void setupView() {
			PathView mPathView = (PathView) findViewById(R.id.mPathView);
			ImageButton startMenu = new ImageButton(this);
			startMenu.setBackgroundResource(R.drawable.start_menu_btn);
			mPathView.setStartMenu(startMenu);

			int[] drawableIds = new int[] { R.drawable.start_menu_scan_normal,
					R.drawable.start_menu_call_normal,
					R.drawable.start_menu_sms_normal,
					R.drawable.start_menu_chat_normal };
			View[] items = new View[drawableIds.length];
			for (int i = 0; i < drawableIds.length; i++) {
				ImageButton button = new ImageButton(this);
				button.setBackgroundResource(drawableIds[i]);
				items[i] = button;
			}
			mPathView.setItems(items);
			mPathView.setOnItemClickListener(this);
		}

		@Override
		public void onItemClick(View view, int position) {
			Intent intent = new Intent();
			if(position==3){
				intent.setAction("org.testxxx.helloworld.GroupMessageActivity");
				startActivity(intent);
				finish();
			}else if(position==2){
				Intent login = new Intent(this,TaskCategoryActivity.class);
				this.startActivity(login);
				//intent.setAction("org.testxxx.helloworld.TaskCategoryActivity");
				//startActivity(intent);
				finish();
			}else if(position==1){
				SharedPreferences shared = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
				SharePre sp = new SharePre(shared);
				sp.setBoolean("AUTO_ISCHECK", false);
				CloseActivityClass.exitClient(this);
			}else{
				Toast.makeText(this, "您单击了第"+position+"项", Toast.LENGTH_SHORT).show();
			}
		}
	 
	 protected void onDestroy(){
	        super.onDestroy();
	 }
}
