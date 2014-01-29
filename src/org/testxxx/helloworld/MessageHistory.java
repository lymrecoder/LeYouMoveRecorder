package org.testxxx.helloworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MessageHistory extends CommActivity {

	// 创建一个List集合，List集合的元素是Map
	private List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
	private SimpleAdapter simpleAdapter;
	private Intent intentFrom;

	private String[] msgs = new String[]
			{ "msg1", "msg2", "msg3", "msg4"};
	private String[] msg_persons = new String[]
			{ "person1", "person1", "person2", "person2"};
	private String[] msg_times = new String[]
			{ "2014-01-14 10:00:00", "2014-01-13 10:00:00"
				,  "2014-01-12 10:00:00",  "2014-01-11 10:00:00"};
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_history);
		Intent intt = getIntent();
		intentFrom = intt;
		initMessageList(intt);
		initButton(intt);
	}
	
	private void initButton(Intent intt){
		Button btn = (Button) findViewById(R.id.msglistbtn);
		
		btn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
            	Intent intt_to = new Intent();
            	intt_to.putExtras(intentFrom);
				intt_to.setAction("org.testxxx.helloworld.MESSAGE");
				startActivity(intt_to);
				finish();
			}
		});
	}
	
	private void initMessageList(Intent intt){
		Map<String, Object> listItem = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		ArrayList <HashMap<String, String>> list_intt = (ArrayList<HashMap<String, String>>) intt.getSerializableExtra("list");
		
		listItem.put("msg_person", "留言人");
		listItem.put("msg_content", "内容");
		listItem.put("msg_time", "时间");
		listItems.add(listItem);
		// 创建一个SimpleAdapter
		simpleAdapter = new SimpleAdapter(this, listItems,
			R.layout.msg_item, 
			new String[] { "msg_person", "msg_content", "msg_time"},
			new int[] { R.id.msg_person, R.id.msg_content, R.id.msg_time });
		ListView list = (ListView) findViewById(R.id.msglist);
		// 为ListView设置Adapter
		list.setAdapter(simpleAdapter);
		// 为ListView的列表项单击事件绑定事件监听器
		list.setOnItemClickListener(new OnItemClickListener()
		{
			// 第position项被单击时激发该方法。
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				/*System.out.println(msgs[position]
					+ "被单击了");*/
			}
		});
		list.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			// 第position项被选中时激发该方法。
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id)
			{
			/*	System.out.println(msgs[position]
						+ "被选中了");*/
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		// 添加测试数据
		addToMsgList(list_intt);
	}
	
	public void addToMsgList(ArrayList <HashMap<String, String>>  list){

		HashMap<String, String> data_item;
		if(null != list && !list.isEmpty()){
			Iterator<HashMap<String, String>> it = list.iterator();
			while(it.hasNext()){
				data_item = it.next();
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("msg_content", data_item.get("note"));
				listItem.put("msg_person", data_item.get("reporter_name"));
				listItem.put("msg_time", data_item.get("last_modified"));
				listItems.add(listItem);
			}
		}
	/*	for (int i = 0; i < msgs.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("msg_content", msgs[i]);
			listItem.put("msg_person", msg_persons[i]);
			listItem.put("msg_time", msg_times[i]);
			listItems.add(listItem);
		} */
		simpleAdapter.notifyDataSetChanged();
	}
}
