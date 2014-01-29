package org.testxxx.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.testxxx.itfc.CommIBinder;
import org.testxxx.util.ScktComm;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SrvrComm extends Service {

	final String RemoteAddr = "119.161.160.70";
//	final static String RemoteAddr = "172.16.2.202";
//	final static String RemoteAddr = "192.168.1.101";
	final int RemotePort = 35430;
	
	final static String field_seperator = " ";
	final static String msg_seperator = "\r\n";
	
	private int count = 0;
	private boolean quit = false;
	private ScktComm CommunicationComp;
	private Hashtable<String, String> SendMsgs = new Hashtable<String, String>();  // TODO，这俩hashtable可以指定泛型
	private Hashtable<String, String> RecvMsgs = new Hashtable<String, String>();
	
	private SrvrCommBinder conn_binder = new SrvrCommBinder();
	
	public class SrvrCommBinder extends Binder implements CommIBinder{
		public synchronized String sendMessage(String msg) throws InterruptedException{
			String ret = null;
			Random rd = new Random();
			String curr_key = String.valueOf(rd.nextInt());
			SendMsgs.put(curr_key, msg);
			while(true){
				ret = (String)RecvMsgs.get(curr_key);
				if(null == ret){
					wait();
				}else {
					break;
				}
			}
			RecvMsgs.remove(curr_key);
			return ret;
		}
		
		/**
		 * 这个方法的意义是，只有通过synchronized方法或语句块在该类中显式的获得锁，才能进行notify操作
		 */
		public synchronized void notifyAllByThread(){
			notifyAll();
		}
		
	}
	
	/**
	 * 把接收到的数据拆分为key value形式
	 * @param recvData
	 * @return
	 */
	protected Hashtable<String, String> _analyzeRecvData(String recvData){
		Hashtable<String, String> msg_arr_map = new Hashtable<String, String>();
		String [] msg_list = recvData.split(msg_seperator);
		String [] msg_key_val_tmp;
		
		for(int i=0; i<msg_list.length; i++){
			msg_list[i] = msg_list[i].trim();
			if(!msg_list[i].isEmpty()){
				msg_key_val_tmp = msg_list[i].split(field_seperator, 2);
				if(2!=msg_key_val_tmp.length) continue;
				msg_arr_map.put(msg_key_val_tmp[0].trim(), msg_key_val_tmp[1].trim());
			}
		}
		
		return msg_arr_map;
	}
	
	/**
	 * 分解分析后的消息列表，并放到RecvMsgs数组中
	 * @param msg_arr_map
	 */
	protected ArrayList<String> _dispatchRecvData(Hashtable<String, String> msg_arr_map){
		Set<String> to_dispatch_keyset = msg_arr_map.keySet();
		ArrayList<String> toDel = new ArrayList<String>();
		if(to_dispatch_keyset.isEmpty()){
			return toDel;
		}
		for (Iterator<String> key_it = to_dispatch_keyset.iterator(); key_it.hasNext();){
			String key = (String)key_it.next();
			if(msg_arr_map.get(key).isEmpty()){
				RecvMsgs.put(key, "");
			}else{
				RecvMsgs.put(key, msg_arr_map.get(key));
			}
			toDel.add(key);
		}
		return toDel;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return conn_binder;
	}
	
	// Service被创建时回调该方法。
	@Override
	public void onCreate()
	{
		super.onCreate();
		CommunicationComp = new ScktComm(RemoteAddr, RemotePort);
		System.out.println("Service is Created");
		// 启动一条线程、动态地修改count状态值
		new Thread()
		{
			@Override
			public void run()
			{
				while (!quit)
				{
					try
					{
						Thread.sleep(1000);
						Set<String> to_send_keyset = SendMsgs.keySet();
						if(to_send_keyset.isEmpty()){
							continue;
						}
						boolean recv_data_process_result = false;
						ArrayList<String> toDel = new ArrayList<String>();
						for (Iterator<String> key_it = to_send_keyset.iterator(); key_it.hasNext();){
							Hashtable<String, String> recv_data_hash;
							String key = (String)key_it.next();
							String ret = CommunicationComp.sendMessage(key+field_seperator+(String)SendMsgs.get(key)+msg_seperator);
							/**
							 * TODO 这里需要根据字符串进行拆分，把不同的结果放到不同的结果key中
							 */
							recv_data_hash = _analyzeRecvData(ret);
							
					//		recv_data_process_result = _dispatchRecvData(recv_data_hash);
							toDel.addAll(_dispatchRecvData(recv_data_hash));
						}
						for (Iterator<String> key_del = toDel.iterator(); key_del.hasNext();){
							SendMsgs.remove(key_del.next());
						}
						conn_binder.notifyAllByThread();
					}
					catch (InterruptedException e)
					{
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					count++;
				}
			}
		}.start();
	}

}
