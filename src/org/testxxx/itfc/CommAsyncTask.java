package org.testxxx.itfc;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;
import org.testxxx.util.AbstractDataParser;
import org.testxxx.util.JsonDataParser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class CommAsyncTask extends AsyncTask<String, Integer, String> {
//	public class CommAsyncTask extends AsyncTask<Hashtable, Progress, Result> {

	private Context ctx;
	private CommIBinder bd;
	private AbstractDataParser dp;
	private ProgressDialog pdialog;
	
	public CommAsyncTask(CommIBinder bd){
		super();
		this.bd = bd;
		this.dp = new JsonDataParser();
	}
	
	public CommAsyncTask(Context ctx, CommIBinder bd){
		super();
		this.ctx = ctx;
		this.bd = bd;
		this.dp = new JsonDataParser();
	}
	
	public CommAsyncTask(Context ctx, CommIBinder bd, AbstractDataParser dp){
		super();
		this.ctx = ctx;
		this.bd = bd;
		this.dp = dp;
	}
	
	/**
	 * 应用该类时重写该方法
	 * @param recvData
	 */
//	abstract public void onRecvData(String recvData);
	abstract public void onRecvData(Hashtable<String, Object> recvData);
	
	protected Hashtable<String, Object> decodeRecvdata(String ret){
		return dp.decode(ret);
	}
	
	@Override
	public void onPostExecute(String ret){
		Hashtable<String, Object> b = decodeRecvdata(ret);
		if(null == b){
			Toast.makeText(ctx, "receive data fail",
	    		     Toast.LENGTH_SHORT).show();
		}
		pdialog.dismiss();
		onRecvData(b);
	}
	
	public void execute(Hashtable<String, Object> sendData){
		JSONObject obj = new JSONObject(sendData);
		String str = obj.toString();
		pdialog = new ProgressDialog(ctx);
		// 设置对话框的标题
		pdialog.setTitle("任务正在执行中");
		// 设置对话框 显示的内容
		pdialog.setMessage("任务正在执行中，敬请等待...");
		// 设置对话框不能用“取消”按钮关闭
		pdialog.setCancelable(false);
		// 设置该进度条的最大进度值
		pdialog.setMax(202);
		// 设置对话框的进度条风格
		pdialog.setProgressStyle(ProgressDialog.THEME_HOLO_LIGHT);
		// 设置对话框的进度条是否显示进度
		pdialog.setIndeterminate(false);
		pdialog.show();
		execute(str);
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		String ret = "";
		try {
			ret = bd.sendMessage(arg0[0]);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

}
