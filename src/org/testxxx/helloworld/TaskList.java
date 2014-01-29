package org.testxxx.helloworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testxxx.itfc.CommAsyncTask;
import org.testxxx.itfc.CommIBinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class TaskList extends CommActivity {

	ArrayList<HashMap<String, String>> task_list_data;
	
	// 创建一个List集合，List集合的元素是Map
	List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
	SimpleAdapter simpleAdapter;
	
	private String[] names = new String[]
			{ "task1", "task2", "task3", "task4"};
	private String[] apppersons = new String[]
			{ "appperson1", "appperson1", "appperson2", "appperson2"};
	private String[] taskpersons = new String[]
			{ "taskperson1", "taskperson1", "taskperson2", "taskperson2"};
	private String[] times = new String[]
			{ "2014-01-14 10:00:00", "2014-01-13 10:00:00"
				,  "2014-01-12 10:00:00",  "2014-01-11 10:00:00"};
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task);
		
		// 创建一个SimpleAdapter
		simpleAdapter = new SimpleAdapter(this, listItems,
			R.layout.task_item, 
			new String[] { "taskName", "appperson", "taskperson", "time"},
			new int[] { R.id.name, R.id.appperson , R.id.taskperson , R.id.time });
		ListView list = (ListView) findViewById(R.id.tasklist);
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
				openTaskDetail(task_list_data.get(position).get("id"));
		/*		System.out.println(names[position]
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
				System.out.println(names[position]
						+ "被选中了");
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		// 添加测试数据
		addToList();
	}
	
	private void openTaskDetail(String mantis_num){
    	Intent intent = new Intent();
    	intent.putExtra("mantis_num", mantis_num);
    	intent.setAction("org.testxxx.helloworld.TaskDetail");
		startActivity(intent);
	}
	
	public void addToList(){
		Intent intent = getIntent();
		HashMap<String, String> data_item;
		task_list_data = (ArrayList<HashMap<String, String>>)intent.getSerializableExtra("list");
		if(null != task_list_data && !task_list_data.isEmpty()){
			Iterator<HashMap<String, String>> it = task_list_data.iterator();
			while(it.hasNext()){
				data_item = it.next();
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("taskName", data_item.get("summary"));
				listItem.put("appperson", "申请人："+data_item.get("reporter_name"));
				listItem.put("taskperson", "办理人："+data_item.get("handler_name"));
				listItem.put("time", data_item.get("last_updated"));
				listItems.add(listItem);
			}
		}
	/*	
		for (int i = 0; i < names.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("taskName", names[i]);
			listItem.put("appperson", "申请人："+apppersons[i]);
			listItem.put("taskperson", "办理人："+taskpersons[i]);
			listItem.put("time", times[i]);
			listItems.add(listItem);
		}*/
		simpleAdapter.notifyDataSetChanged();
	}

}
