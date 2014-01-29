package org.testxxx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Hashtable;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

/**
 * 远程
 * @author yufulou
 * 
 */
public class ScktComm extends AbstractComm{
	String RemoteAddr;
//	final static String RemoteAddr = "172.16.2.202";
//	final static String RemoteAddr = "192.168.1.103";
	int RemotePort;
	static private Socket SckComm;
	
	public ScktComm(String remote_addr, int remote_port){
		RemoteAddr = remote_addr;
		RemotePort = remote_port;
		try {
			chkAndReConn();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	Runnable initSckRunnable = new Runnable(){
		public void run(){
			try{
				SckComm = new Socket(RemoteAddr, RemotePort);
				SckComm.setKeepAlive(true);
			}
			catch (SocketTimeoutException e1)
			{
				System.out.println("网络连接超时！！");
			}
			catch(ConnectException e2){
				e2.printStackTrace();
				Log.v("helloworld", e2.getMessage());
			}catch (SocketException e)
			{
				e.printStackTrace();
				Log.v("helloworld", e.getMessage());
			}/*catch(NetworkOnMainThreadException e4){
				e4.printStackTrace();
				Log.v("helloworld", e4.getMessage());
			}*/catch(Exception e3){
				e3.printStackTrace();
				Log.v("helloworld", e3.getMessage());
			}
		}
	
	};
	
	private synchronized void iniSckComm() throws IOException{
		if(null == SckComm){
			new Thread(initSckRunnable).start();
		}
	}
	
	public ScktComm() throws IOException{
		chkAndReConn();
		return;
	}
	
	/**
	 * 检查并重建连接
	 * @throws IOException 
	 */
	private boolean chkAndReConn() throws IOException{
		if(null == SckComm){
			iniSckComm();
		}
		return true;
	}
	
	@Override
	public String sendMessage(String msg) throws UnsupportedEncodingException, IOException{
		String server_ret = "";
		if(chkAndReConn()){
			try{
				OutputStream os = SckComm.getOutputStream();
				os.write(msg
						.getBytes("utf-8"));
				BufferedReader br = new BufferedReader(new InputStreamReader(
						SckComm.getInputStream() , "utf-8"));
				server_ret = br.readLine();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(null!=server_ret)System.out.println(server_ret);
		}
		return server_ret;
	}
	/*
	@Override
	public void finalize() throws Throwable{
		super.finalize();
		SckComm.close();
	} */
}
