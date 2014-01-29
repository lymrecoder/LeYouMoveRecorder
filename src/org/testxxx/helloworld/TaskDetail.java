package org.testxxx.helloworld;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.testxxx.itfc.CommAsyncTask;
import org.testxxx.itfc.CommIBinder;
import org.testxxx.itfc.SrvrCommDelegate;
import org.testxxx.service.PushService;
import org.testxxx.util.UserSession;
import org.testxxx.widget.RecallableSpinner;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TaskDetail extends Activity
{
	protected CommAsyncTask commasynctask;
	
	Hashtable<String, String> curr_task_info;
	int button_action = 1; // 默认是1，申请任务，2，同意申请，3，任务完结
	private String mantis_num;
	
	RecallableSpinner spinner;
	
	private CommIBinder binder;
    
    private ServiceConnection conn_srv = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("--Service Connected--");
			binder = (CommIBinder)service;
			initTaskDetailInfo();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			System.out.println("--Service Disconnected--");
		}
    	
    };
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_detail);

		Intent intent = new Intent();
    	intent.setAction("org.testxxx.service.SRVR_COMM");
    	this.bindService(intent, conn_srv, Service.BIND_AUTO_CREATE);
    	
		Intent intent_from = getIntent();
		mantis_num = intent_from.getStringExtra("mantis_num");
	}
	

	
	private void initTaskDetailInfo(){
    	Hashtable<String, Object> senddata = new Hashtable<String, Object>();
    	senddata.put("action", "4");
    	senddata.put("method", "2");
    	senddata.put("mantis_num", mantis_num);
		commasynctask = new CommAsyncTask(this, binder){
			@SuppressWarnings("unchecked")
			@Override
			public void onRecvData(Hashtable<String, Object> recvData) {
		  /*  	Toast.makeText(getApplicationContext(), (recvData.isEmpty()?"empty ret":recvData.toString()),
		    		     Toast.LENGTH_SHORT).show(); */
				curr_task_info = (Hashtable<String, String>)recvData.get("retval");
				displayTaskDetailInfo();
				initRedirectSpinner();
				initSubmitButton();
			}
    	};
    	commasynctask.execute(senddata);
	}
	
	private void redirectModifyHistoryPage(final Intent intt){
		Hashtable<String, Object> senddata = new Hashtable<String, Object>();
    	senddata.put("action", "4");
    	senddata.put("method", "1");
    	senddata.put("mantis_num", curr_task_info.get("id"));
    	commasynctask = new CommAsyncTask(this, binder){
			@SuppressWarnings("unchecked")
			@Override
			public void onRecvData(Hashtable<String, Object> recvData) {
		   /* 	Toast.makeText(getApplicationContext(), (recvData.isEmpty()?"empty ret":recvData.toString()),
		    		     Toast.LENGTH_SHORT).show(); */
		    	Bundle bd_task_list = new Bundle();
		    	bd_task_list.putSerializable("list", (ArrayList<Hashtable<String, String>>)recvData.get("retval"));
		    	bd_task_list.putSerializable("mantis_num", curr_task_info.get("id"));
		    	intt.putExtras(bd_task_list);
				startActivity(intt);
			}
    	};
    	commasynctask.execute(senddata);
	}
	
	private void redirectMsgHistoryPage(final Intent intt){
		Hashtable<String, Object> senddata = new Hashtable<String, Object>();
    	senddata.put("action", "3");
    	senddata.put("method", "1");
    	senddata.put("mantis_num", curr_task_info.get("id"));
    	commasynctask = new CommAsyncTask(this, binder){
			@SuppressWarnings("unchecked")
			@Override
			public void onRecvData(Hashtable<String, Object> recvData) {
		    /*	Toast.makeText(getApplicationContext(), (recvData.isEmpty()?"empty ret":recvData.toString()),
		    		     Toast.LENGTH_SHORT).show(); */
		    	Bundle bd_task_list = new Bundle();
		    	bd_task_list.putSerializable("list", (ArrayList<Hashtable<String, String>>)recvData.get("retval"));
		    	bd_task_list.putSerializable("mantis_num", curr_task_info.get("id"));
		    	intt.putExtras(bd_task_list);
				startActivity(intt);
			}
    	};
    	commasynctask.execute(senddata);
	}
	
	/**
	 * 转到留言页
	 * @param intt
	 */
	private void redirectMsgPage(Intent intt){
		startActivity(intt);
	}
	
	private void initRedirectSpinner(){
		spinner = (RecallableSpinner) findViewById(R.id.task_redirection);
		spinner.setSelection(0, true);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
			public void onItemSelected(AdapterView<?> adpt, View view,
					int position, long id) {
            	String redirect_to = "";
				Bundle task_bd = new Bundle();
				task_bd.putString("mantis_num", curr_task_info.get("id"));
				task_bd.putString("handler_id", curr_task_info.get("handler_id"));
				task_bd.putString("handler_name", curr_task_info.get("handler_name"));
				task_bd.putString("handler_login", curr_task_info.get("handler_login"));
				task_bd.putString("reporter_id", curr_task_info.get("reporter_id"));
				task_bd.putString("reporter_name", curr_task_info.get("reporter_name"));
            	Intent intt = new Intent();
				intt.putExtras(task_bd);
				switch(position){
				case 0:	
					redirect_to = "org.testxxx.helloworld.MESSAGE";
					break;
				case 1:	
					redirect_to = "org.testxxx.helloworld.MessageHistory";
					break;
				case 2:	
					redirect_to = "org.testxxx.helloworld.ModifyHistory";
					break;
				}
				intt.setAction(redirect_to);
				
				switch(position){
				case 0:
					redirectMsgPage(intt);
					break;
				case 1:
					redirectMsgHistoryPage(intt);
					break;
				case 2:
					redirectModifyHistoryPage(intt);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
         });
	}
	
	@SuppressWarnings("unchecked")
	private void displayTaskDetailInfo(){
		TextView task_name = (TextView)findViewById(R.id.task_name);
		TextView demand_person = (TextView)findViewById(R.id.demand_person);
		TextView exec_person = (TextView)findViewById(R.id.exec_person);
		TextView app_time = (TextView)findViewById(R.id.app_time);
	/*	TextView finish_time = (TextView)findViewById(R.id.finish_time);
		TextView finish_period = (TextView)findViewById(R.id.finish_period);*/
		TextView task_detail_content = (TextView)findViewById(R.id.task_detail_content);
		TextView task_comment = (TextView)findViewById(R.id.task_comment);
		TextView task_status = (TextView)findViewById(R.id.task_status);
		
		task_name.setText(curr_task_info.get("summary"));
		demand_person.setText(curr_task_info.get("reporter_name"));
		exec_person.setText(curr_task_info.get("handler_name"));
		app_time.setText(curr_task_info.get("date_submitted"));
	/*	task_name.setTextContent(curr_task_info.get("summary"));
		task_name.setTextContent(curr_task_info.get("summary")); */
		task_detail_content.setText(curr_task_info.get("description"));
		task_comment.setText(curr_task_info.get("additional_information"));
		task_status.setText(curr_task_info.get("task_status_str"));
	}
	
	private void initSubmitButton(){
		String btn_str = curr_task_info.get("btn_str");
		Button submit_button = (Button)findViewById(R.id.app_task);
		if(btn_str.isEmpty() || curr_task_info.get("btn_action").isEmpty()){
			submit_button.setVisibility(View.INVISIBLE);
			return;
		}
		button_action = Integer.parseInt(curr_task_info.get("btn_action").toString());
		submit_button.setText(btn_str);
		
		final Context ctx = this;
		
		submit_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Hashtable<String, Object> senddata = new Hashtable<String, Object>();
		    	senddata.put("action", "4");
		    	senddata.put("method", "3");
		    	senddata.put("mantis_num", curr_task_info.get("id"));
		    	senddata.put("status", button_action);
		    	commasynctask = new CommAsyncTask(ctx, binder){
					@SuppressWarnings("unchecked")
					@Override
					public void onRecvData(Hashtable<String, Object> recvData) {
						String msgTo, msgFrom=UserSession.get("USERNAME"), realname = UserSession.get("REALNAME");
				    	String msgContent = realname+"改变了任务mantis:"+curr_task_info.get("id")+"的状态";
				    	int msgType = 1;
				    	if(UserSession.get("ROLE_LEVEL").equals("manager")){
				    		msgTo = curr_task_info.get("handler_login");
				    	}else{
				    		msgTo = PushService.NET_LEADER_GROUP;
				    	}
				    	
				   /* 	Toast.makeText(getApplicationContext(), (recvData.isEmpty()?"empty ret":recvData.toString()),
				    		     Toast.LENGTH_SHORT).show(); */
				    	if(recvData.get("retval").toString().equals("true")){
				    		if(!msgTo.isEmpty()){
				    			PushService.publishMsg(msgTo, msgFrom, msgContent, curr_task_info.get("id"), msgType);
				    		}
				    		finish();
				    	}else{
				    		Toast.makeText(getApplicationContext(), "提交失败", Toast.LENGTH_SHORT).show();
				    	}
					}
		    	};
		    	commasynctask.execute(senddata);
			}
		});
	}
    
    protected void onDestroy(){
        super.onDestroy();
        unbindService(conn_srv);
    }
}