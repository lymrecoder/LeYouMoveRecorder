package org.testxxx.helloworld;

import java.util.Hashtable;

import org.testxxx.itfc.CommAsyncTask;
import org.testxxx.itfc.CommIBinder;
import org.testxxx.service.PushService;
import org.testxxx.util.UserSession;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Message extends CommActivity
{
	private String mantis_num; 
	private String handler_name;
	private EditText mantis_text;
	private EditText content_text;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);
		
		Intent intent_from = getIntent();
		mantis_num = intent_from.getStringExtra("mantis_num");
		handler_name = intent_from.getStringExtra("handler_login");
		
		mantis_text = (EditText)findViewById(R.id.mantis_num_msg);
		mantis_text.setText(mantis_num);
		
		content_text = (EditText)findViewById(R.id.content_msg);
		
		Button submit_btn = (Button)findViewById(R.id.submit_msg);

		final Context ctx = this;
		submit_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String msg_content = content_text.getText().toString();
				Hashtable<String, Object> senddata = new Hashtable<String, Object>();
		     	CommIBinder binder = scd.getBinder();
		    	senddata.put("action", "3");
		    	senddata.put("method", "2");
		    	senddata.put("mantis_num", mantis_num);
		    	senddata.put("msg_content", msg_content);
		    	commasynctask = new CommAsyncTask(ctx, binder){
					@SuppressWarnings("unchecked")
					@Override
					public void onRecvData(Hashtable<String, Object> recvData) {
				    	String display_str;
				    	boolean retval = false;
				    	String msgTo, msgFrom=UserSession.get("USERNAME"), realname = UserSession.get("REALNAME");
				    	String msgContent = realname+"在任务mantis:"+mantis_num+"中留言了";
				    	int msgType = 1;
				    	if(UserSession.get("ROLE_LEVEL").equals("manager")){
				    		msgTo = handler_name;
				    	}else{
				    		msgTo = PushService.NET_LEADER_GROUP;
				    	}
				    	if(recvData.get("retval").toString().equals("true")){
				    		display_str = "留言成功";
				    		if(!msgTo.isEmpty()){
				    			PushService.publishMsg(msgTo, msgFrom, msgContent, mantis_num, msgType);
				    		}
				    		retval = true;
				    	}else{
				    		display_str = "留言失败";
				    	}
						Toast.makeText(getApplicationContext(), display_str, Toast.LENGTH_SHORT).show();
				    	if(retval){
				    		finish();
				    	}
					}
				};
				commasynctask.execute(senddata);
			}
		});
	}
}