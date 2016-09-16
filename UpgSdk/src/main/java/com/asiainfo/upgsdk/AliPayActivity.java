package com.asiainfo.upgsdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asiainfo.util.ApkSignUtil;
import com.asiainfo.util.ConstantUtil;
import com.asiainfo.util.HttpUtils;
import com.asiainfo.util.LoadingFrameDialog;

public class AliPayActivity extends Activity{
	public static final int ALIPAY_NET = 1;
	
	public int paymentCode_Status;
	private ImageView alipayUrl;
	private String tradeSequence;
	private String nonce_str;
	private String requestUrl;
	private String sign_type="MD5";
	private String input_charset="UTF-8";
	private String key;
	private String appCode;
	private String paymentName;
	private int time_out;
	private int query_interval;
	private Timer timer;
	private String paymentPath, paymentCode, paymentImageUrl;
	int i=0;
	private String msg;
	private boolean isPay=false;
	private LoadingFrameDialog mLoadingFrame=null;
	private ImageView bgimg;
	private TextView paymentname;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			//支付完成
	          if(isPay){
	        	 timer.cancel();
	        	 returnActivity(ConstantUtil.RESULT_PAY_SUCCESS);
	        	 Intent intent=new Intent();
	        	 intent.setClass(AliPayActivity.this, PaySuccessActivity.class);
	        	 intent.putExtra("productName", getIntent().getExtras().getString("productName"));
	        	 intent.putExtra("amount", getIntent().getExtras().getString("amount"));
	        	 intent.putExtra("tradeSequence", tradeSequence);
	        	 startActivity(intent);
	        	 
	        	 //订单超时
	          }else if(i*query_interval>=time_out){
	        	  timer.cancel();
	        	  cancle();
	        	  returnActivity(ConstantUtil.RESULT_PAY_FAIL);
	        	  Intent intent=new Intent();
	         	 intent.setClass(AliPayActivity.this, PayFaildActivity.class);
	         	 intent.putExtra("productName", getIntent().getExtras().getString("productName"));
	         	 intent.putExtra("amount", getIntent().getExtras().getString("amount"));
	         	 intent.putExtra("tradeSequence", tradeSequence);
	         	 startActivity(intent); 
	         	 
	          }	
		}
	};
	

	
	private void returnActivity(int status) {
		switch (status) {
		case ConstantUtil.RESULT_PAY_SUCCESS:  //支付成功
			Intent intentSuccess = new Intent();
			intentSuccess.putExtra("isSuccess", "SUCCESS");
			intentSuccess.putExtra("tradeSequence", tradeSequence);
			setResult(ConstantUtil.RESULT_PAY_SUCCESS, intentSuccess);
			AliPayActivity.this.finish();
			break;
		case ConstantUtil.RESULT_PAY_FAIL:  //支付失败
			Intent intentFail = new Intent();
			intentFail.putExtra("isSuccess", "FAIL");
			intentFail.putExtra("tradeSequence", tradeSequence);
			setResult(ConstantUtil.RESULT_PAY_FAIL, intentFail);
			AliPayActivity.this.finish();
			break;
		default:
			break;
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sdk_activity_alipay);
		//初始化基本信息
		try {
			init();
			MyTask mTask = new MyTask();  
			mTask.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
  @Override
	public void onBackPressed() {
		timer.cancel();
		cancle();
		Intent intent = new Intent();
		setResult(ConstantUtil.RESULT_ORDER_CANCEL,intent);
		AliPayActivity.this.finish();
	}	
		


	//初始化基本数据
	private void init() throws Exception {
		alipayUrl=(ImageView) findViewById(R.id.alipayUrl);
		tradeSequence=getIntent().getExtras().getString("tradeSequence");
		paymentPath=getIntent().getExtras().getString("paymentPath");
		paymentCode=getIntent().getExtras().getString("paymentCode");
		paymentImageUrl=getIntent().getExtras().getString("paymentImageUrl");
		paymentCode_Status=getIntent().getExtras().getInt("paymentCode_Status");
	
		key=getIntent().getExtras().getString("key");
		appCode=getIntent().getExtras().getString("appCode");
		requestUrl=getIntent().getExtras().getString("requestUrl");
		
	}
	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what = 2;
			try {
				queryPay();
			} catch (Exception e) {
				e.printStackTrace();
			}
			handler.sendMessage(message);
		}
	};
	//取消订单接口
	private void cancle() {
		nonce_str= ApkSignUtil.randomString(32);
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("appCode", appCode);
		reqMap.put("nonce_str", nonce_str);
		reqMap.put("sign_type", sign_type);
		reqMap.put("input_charset", input_charset);
		reqMap.put("tradeSequence", tradeSequence);
		reqMap.put("paymentCode", paymentCode);
		try {
			ApkSignUtil.sign(reqMap, sign_type, key, input_charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tradeSequence", tradeSequence));
		params.add(new BasicNameValuePair("appCode", appCode));
		params.add(new BasicNameValuePair("nonce_str", nonce_str));
		params.add(new BasicNameValuePair("sign_type", sign_type));
		params.add(new BasicNameValuePair("sign", reqMap.get("sign")));
		params.add(new BasicNameValuePair("input_charset", input_charset));
		params.add(new BasicNameValuePair("paymentCode", paymentCode));
		HttpEntity entity = HttpUtils.getEntity(requestUrl+HttpUtils.cancleUrl, params,HttpUtils.METHOD_GET);
		String content;
		try {
			content = EntityUtils.toString(entity);
			JSONObject demoJson = new JSONObject(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//查询是否支付接口
	private void queryPay() {
		i=i+1;
		nonce_str= ApkSignUtil.randomString(32);
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("appCode", appCode);
		reqMap.put("nonce_12str", nonce_str);
		reqMap.put("sign_type", sign_type);
		reqMap.put("input_charset", input_charset);
		reqMap.put("tradeSequence", tradeSequence);
		reqMap.put("paymentCode", paymentCode);
		try {
			ApkSignUtil.sign(reqMap, sign_type, key, input_charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tradeSequence", tradeSequence));
		params.add(new BasicNameValuePair("appCode", appCode));
		params.add(new BasicNameValuePair("paymentCode", paymentCode));
		params.add(new BasicNameValuePair("nonce_str", nonce_str));
		params.add(new BasicNameValuePair("sign_type", sign_type));
		params.add(new BasicNameValuePair("sign", reqMap.get("sign")));
		params.add(new BasicNameValuePair("input_charset", input_charset));
		HttpEntity entity = HttpUtils.getEntity(requestUrl+HttpUtils.queryPayUrl, params,HttpUtils.METHOD_GET);
		String content;
		try {
			content = EntityUtils.toString(entity);
			JSONObject demoJson = new JSONObject(content);
			if(demoJson.get("trade_status").toString().equals("SUCCESS")){
				isPay=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void ShowFrameDialog(int type){
		 mLoadingFrame=LoadingFrameDialog.createDialog(this, type);
		mLoadingFrame.setFrameLoading(R.anim.progress_round);
		mLoadingFrame.ShowDialog(this, type);
		mLoadingFrame.setMessage("正在加载，请等待。。。");
	}
	private void removesDialog(int type){
		mLoadingFrame.dismiss();
		mLoadingFrame=null;
	}
	
	//支付下单接口 返回二维码地址用于用户支付
	 private class MyTask extends AsyncTask<String, Integer, String> {  
	        @Override  
	        protected void onPreExecute() {  
	    		ShowFrameDialog(R.anim.progress_round);
	        }  
	          
	        @Override  
	        protected String doInBackground(String... param) {  
	        	nonce_str= ApkSignUtil.randomString(32);
	        	String qrCode="";
	    		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	    		Map<String, String> reqMap = new HashMap<String, String>();
	    		reqMap.put("appCode", appCode);
	    		reqMap.put("nonce_str", nonce_str);
	    		reqMap.put("sign_type", sign_type);
	    		reqMap.put("input_charset", input_charset);
	    		reqMap.put("tradeSequence", tradeSequence);
	    		reqMap.put("paymentCode", paymentCode);
	    		try {
					ApkSignUtil.sign(reqMap, sign_type, key, input_charset);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    		params.add(new BasicNameValuePair("tradeSequence", tradeSequence));
	    		params.add(new BasicNameValuePair("paymentCode", paymentCode));
	    		params.add(new BasicNameValuePair("appCode", appCode));
	    		params.add(new BasicNameValuePair("nonce_str", nonce_str));
	    		params.add(new BasicNameValuePair("sign_type", sign_type));
	    		params.add(new BasicNameValuePair("sign", reqMap.get("sign")));
	    		params.add(new BasicNameValuePair("input_charset", input_charset));
	    		String content;
	    		try {
	    			if (paymentCode_Status == 1){
	    				content = getIntent().getExtras().getString("content");
	    				Log.i("long1", content);
	    			} else {
						HttpEntity entity = HttpUtils.getEntity(requestUrl+"/"+paymentPath, params,HttpUtils.METHOD_GET);
	    				content = EntityUtils.toString(entity);
	    				Log.i("long2", requestUrl+"/"+paymentPath);
	    				Log.i("content2", content);
	    				Log.i("params", params.toString());
					}
	    			if(null!=content&&!"".equals(content)){
		    			 JSONObject demoJson = new JSONObject(content);
		    			 if(demoJson.get("trade_status").equals("SUCCESS")){
		    				 
		    			  //**支付二维码地址**//
		    			 qrCode=demoJson.get("qrCode").toString();
		    			 //**定时任务多久调一次查询接口**//
		    			 query_interval=Integer.parseInt(demoJson.getString("query_interval"));
		    			 //订单超时时间
		    			 time_out=Integer.parseInt(demoJson.getString("time_out"));
		    			 }else{
		    					Toast.makeText(AliPayActivity.this,new String(demoJson.getString("message")
		    							.toString().getBytes("iso-8859-1"), "GBK"), Toast.LENGTH_SHORT).show(); 
		    			 }
	    			}else{
	    				Toast.makeText(AliPayActivity.this, "网络异常，请返回重新支付", Toast.LENGTH_SHORT).show();
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		return qrCode;
	        }  
	          
	        //onProgressUpdate方法用于更新进度信息  
	        @Override  
	        protected void onProgressUpdate(Integer... progresses) {  
	        }  
	          
	        @Override  
	        protected void onPostExecute(String qrCode) { 
	        	try {
	        		if(!"".equals(qrCode)&&null!=qrCode){
	        		System.out.println(requestUrl+HttpUtils.qrCodeUrl+":::"+qrCode);
					alipayUrl.setImageBitmap(HttpUtils.getImage(requestUrl+HttpUtils.qrCodeUrl,qrCode));
					timer = new Timer();
					timer.schedule(task, 0,query_interval*1000);
	        		}else{
	        	     Toast.makeText(AliPayActivity.this, "网络异常,"+paymentName+"失败，请返回重新支付", Toast.LENGTH_SHORT).show();	
	        		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	removesDialog(R.anim.progress_round);
	        }  
	          
	        @Override  
	        protected void onCancelled() { 
	        	removesDialog(R.anim.progress_round);
	        }  
	    }
	 
//	 public boolean onKeyDown(int keyCode, KeyEvent event) {
//		 
//         if (keyCode == KeyEvent.KEYCODE_BACK
//                   && event.getRepeatCount() == 0) {
//             System.out.println("返回按钮");
//               return true;
//           }
//           return super.onKeyDown(keyCode, event);
//       }
}
