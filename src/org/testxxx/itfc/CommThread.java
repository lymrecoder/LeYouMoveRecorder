package org.testxxx.itfc;

import java.util.Hashtable;

import org.testxxx.util.AbstractDataParser;
import org.testxxx.util.JsonDataParser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * 对服务请求的线程类，使用时，重写
 * @author yufulou
 *
 */
public class CommThread implements Runnable {

	protected Handler hd;
	protected String toSendData;
	private AbstractDataParser dataParser;
	private CommIBinder binder;
    
	public void registerSendData(Hashtable<String, Object> data){
		toSendData = dataParser.encode(data);
	}
	
	public void setDataParser(AbstractDataParser parser){
		this.dataParser = parser;
	}
	
	public CommThread(CommSrvrHandler hd, CommIBinder binder){
		super();
		this.hd = hd;
		this.binder = binder;
		this.setDataParser(new JsonDataParser());
	}
	
	public CommThread(CommSrvrHandler hd, CommIBinder binder, AbstractDataParser parser){
		super();
		this.hd = hd;
		this.binder = binder;
		if(null != parser){
			this.setDataParser(parser);
		}
	}
	
	@Override
	public void run(){
		String recvdata = "";
		Hashtable<String, ?> recvdataHash;
		Bundle bundle = new Bundle();
		Message msg = Message.obtain();
		// TODO 通过binder请求数据，返回数据后，通过dataParser.decode转换为Hashtable，
		//      调用handler.sendMessage通知改变界面
		
		try {
			recvdata = binder.sendMessage(toSendData);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		recvdataHash = dataParser.decode(recvdata);
		bundle.putInt("actionCode", Integer.parseInt((String) recvdataHash.get("actionCode")));
		bundle.putSerializable("ret", (Hashtable<String, Object>)recvdataHash.get("ret"));
		msg.setData(bundle);
		msg.what = 0x123;
		hd.sendMessage(msg);
	}
}
