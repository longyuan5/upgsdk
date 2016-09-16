package com.asiainfo.upgsdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.asiainfo.util.ApkSignUtil;
import com.asiainfo.util.ConstantUtil;
import com.asiainfo.util.HttpUtils;
import com.asiainfo.util.LoadingFrameDialog;

public class PayActivity extends Activity implements OnClickListener {
	
	public static final int SUCCESS_INPUT = 1;
	public static final int FAIL_INPUT = 2;
	public static final int PAYMENTCODE_EMPTY = 0; //paymentcode不为空
	
	private int paymentCode_Status;
	private String charger, busCode, amount, settleCode, appCode, notifyUrl,
			orderCode, productName, productInfo, paymentCode, nonce_str, sign;
	
	private String tradeSequence;//交易流水号
	private TextView productNames;
	private TextView payMoney;
	private LinearLayout ordersLayout;
	private String key;
	private LoadingFrameDialog mLoadingFrame = null;
	private boolean checks = false;
	private String msg = "支付宝下单失败,请重新下单！";
	private String sign_type = "MD5";
	private String input_charset = "UTF-8";
	private String requestUrl;
	private List<Map<String, Object>> upgList;
	private String paymentPaths, paymentCodes, paymentImageUrl;
	private String userId;  //用户编码
	

	@SuppressLint({ "NewApi", "WrongViewCast" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sdk_activtiy_pay);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
				.penaltyLog().penaltyDeath().build());
		// **参数**//
		parameter();
		
		MyTask mTask = new MyTask();
		mTask.execute();
	}

	//根据结算区域编码区分地区获取requestUrl   "QS410000", "http://111.13.30.84/netpay"
	private String getUrl(String settleCode) {
		HashMap<String, String> requestUrlMap = new HashMap<String, String>();
		requestUrlMap.put("QS410000", "http://10.254.15.31:8080/upg_netpay/");
		//TODO
		return requestUrlMap.get(settleCode);
	}
	


	// 初始化入参
	@SuppressWarnings("deprecation")
	private void parameter() {
		ordersLayout = (LinearLayout) findViewById(R.id.ordersLayout);
		payMoney = (TextView) findViewById(R.id.payMoney);
		productNames = (TextView) findViewById(R.id.productNames);
		// **账务方编码**//
		charger = getIntent().getExtras().getString("charger");
		// **业务编号**//
		busCode = getIntent().getExtras().getString("busCode");
		// **缴费金额**//
		amount = getIntent().getExtras().getString("amount");
		// **结算区域编码**//
		settleCode = getIntent().getExtras().getString("settleCode");
		// **交易类型**//
		appCode = getIntent().getExtras().getString("appCode");
		// **外围通知url**//
		notifyUrl = getIntent().getExtras().getString("notifyUrl");
		// **外围订单号**//
		orderCode = getIntent().getExtras().getString("orderCode");
		// ***产品名称**//
		productName = getIntent().getExtras().getString("productName");
		// **产品信息**//
		productInfo = getIntent().getExtras().getString("productInfo");
		// **付费方编码**//
		paymentCode = getIntent().getExtras().getString("paymentCode");
		requestUrl = getUrl(settleCode);
		//  **加密key。每个应用分配一个key，测试key：test123**//
		key = getIntent().getExtras().getString("key");
		
		//用户编码
		userId = getIntent().getExtras().getString("userId");
		
		//随机生成1个32位的随机数
		nonce_str = ApkSignUtil.randomString(32);
		
		// 加密生成sign签名
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("charger", charger);
		reqMap.put("busCode", busCode);
		reqMap.put("amount", amount);
		reqMap.put("settleCode", settleCode);
		reqMap.put("appCode", appCode);
		reqMap.put("notifyUrl", notifyUrl);
		reqMap.put("notifyUrl", notifyUrl);
		try {
			
			reqMap.put("productName", URLEncoder.encode(URLEncoder.encode(
					productName, input_charset)));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		reqMap.put("nonce_str", nonce_str);
		reqMap.put("orderCode", orderCode);
		reqMap.put("sign_type", sign_type);
		reqMap.put("input_charset", input_charset);
		reqMap.put("channelType", "TV");
		reqMap.put("paymentCode", paymentCode);
		reqMap.put("userId", userId);
		try {
			ApkSignUtil.sign(reqMap, sign_type, key, input_charset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sign = reqMap.get("sign");
		// **编码类型**//
		productNames.setText(productName);
		payMoney.setText(Integer.parseInt(amount) / 100.0 + "元");

	}

	// 统一支付接口
	private class MyTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			ShowFrameDialog(R.anim.progress_round);
		}

		@SuppressLint("NewApi")
		@Override
		protected String doInBackground(String... param) {
			String productsName = "";
			ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			try {
				productsName = URLEncoder.encode(
						URLEncoder.encode(productName, input_charset),
						input_charset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 初始化入参
			params.add(new BasicNameValuePair("charger", charger));
			params.add(new BasicNameValuePair("busCode", busCode));
			params.add(new BasicNameValuePair("amount", amount));
			params.add(new BasicNameValuePair("settleCode", settleCode));
			params.add(new BasicNameValuePair("appCode", appCode));
			params.add(new BasicNameValuePair("notifyUrl", notifyUrl));
			params.add(new BasicNameValuePair("orderCode", orderCode));
			params.add(new BasicNameValuePair("productName", productsName));
			params.add(new BasicNameValuePair("productInfo", productInfo));
			params.add(new BasicNameValuePair("paymentCode", paymentCode));
			params.add(new BasicNameValuePair("nonce_str", nonce_str));
			params.add(new BasicNameValuePair("sign_type", sign_type));
			params.add(new BasicNameValuePair("sign", sign));
			params.add(new BasicNameValuePair("channelType", "TV"));
			params.add(new BasicNameValuePair("input_charset", input_charset));
			params.add(new BasicNameValuePair("userId", userId));
			System.out.println(HttpUtils.unionPayUrl);
			HttpEntity entity = HttpUtils.getEntity(requestUrl
					+ HttpUtils.unionPayUrl, params, HttpUtils.METHOD_POST);
			Log.i("ha1", requestUrl
					+ HttpUtils.unionPayUrl);
			String content=null;
			JSONObject json = null;
			try {
				content = EntityUtils.toString(entity);
				Log.i("content", content);
				
				if(!paymentCode .trim().isEmpty() ){
					if(null!=content&&!"".equals(content)){
						json = JSON.parseObject(content);
		    			 if(json.get("trade_status").equals("SUCCESS")){
		    			tradeSequence=json.get("tradeSequence").toString();
		    			String paymentName=new String(json.getString("paymentName")
    							.toString().getBytes("iso-8859-1"), "GBK");
		    			 //订单超时时间
		    			String paymentImageUrl=json.getString("paymentImageUrl");
		    			String tradeSequence=json.getString("tradeSequence");
		    			Message message = handler.obtainMessage();
						message.what = PAYMENTCODE_EMPTY;
						Bundle bundle = new Bundle();
						bundle.putString("content", content);
						bundle.putString("paymentName", paymentName);
						bundle.putString("paymentImageUrl", paymentImageUrl);
						bundle.putString("tradeSequence", tradeSequence);
						message.setData(bundle);
						handler.sendMessage(message);
		    			 }else{
		    					Toast.makeText(PayActivity.this,new String(json.getString("message")
		    							.toString().getBytes("iso-8859-1"), "GBK"), Toast.LENGTH_SHORT).show(); 
		    			 }
	    			}else{
	    				Toast.makeText(PayActivity.this, "网络异常，请返回重新支付", Toast.LENGTH_SHORT).show();
	    			}
					
				} else if(null != content && !"".equals(content)) {
					json = JSON.parseObject(content);
					if ("SUCCESS".equals(json.get("trade_status"))) {
						// 支付流水号
						tradeSequence = json.getString("tradeSequence");
						// 解析返回支付类型
						JSONArray jsonArray = JSONArray.parseArray(json
								.getString("merList"));
						upgList = new ArrayList<Map<String, Object>>();
						for (int i = 0; i < jsonArray.size(); i++) {
							JSONObject jsonObject = JSON.parseObject(jsonArray
									.getString(i));
							Map<String, Object> map = new HashMap<String, Object>();
							// ****名称：如支付宝*****//
							map.put("paymentName",
									new String(jsonObject.get("paymentName")
											.toString().getBytes("iso-8859-1"),
											"GBK"));
							// ****支付接口地址--用于返回支付二维码**//
							map.put("paymentPath",
									jsonObject.get("paymentPath"));
							// **扫码页面图片路径**//
							map.put("paymentImageUrl",
									jsonObject.get("paymentImageUrl"));
							// **图标地址**//
							map.put("paymentLogoUrl",
									jsonObject.get("paymentLogoUrl"));
							// ***付费方编码**//
							map.put("paymentCode",
									jsonObject.get("paymentCode"));
							upgList.add(map);
						}
						checks = true;
						Message message = handler.obtainMessage();
						message.what = SUCCESS_INPUT;
						handler.sendMessage(message);
						
					} else {
						// 失败原因
						msg = new String(json.get("message").toString()
								.getBytes("iso-8859-1"), "GBK");
						Message message = handler.obtainMessage();
						message.what = FAIL_INPUT;
						message.obj = msg;
						handler.sendMessage(message);
						
					}
//					checks = true;
//					Message msg = handler.obtainMessage();
//					handler.sendMessage(msg);
				} else {
					checks = false;
					msg = new String(json.get("message").toString()
							.getBytes("iso-8859-1"), "GBK");
					Message message = handler.obtainMessage();
					message.what = FAIL_INPUT;
					message.obj = msg;
					handler.sendMessage(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return tradeSequence;
		}

		@Override
		protected void onPostExecute(String result) {

			removesDialog(R.anim.progress_round);
		}

		@Override
		protected void onCancelled() {
			removesDialog(R.anim.progress_round);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			case SUCCESS_INPUT:
				for (int i = 0; i < upgList.size(); i++) {
					View convertView = LayoutInflater.from(PayActivity.this)
							.inflate(R.layout.sdk_imageitem, null);
					TextView payurl = (TextView) convertView
							.findViewById(R.id.payurl);
					ImageView imageitem = (ImageView) convertView
							.findViewById(R.id.imageitems);
					try {
						payurl.setText(new String(upgList.get(i).get("paymentName")
								.toString().getBytes("iso-8859-1"), "GBK"));
						imageitem.setImageBitmap(HttpUtils.getImage(requestUrl, "/"
								+ upgList.get(i).get("paymentLogoUrl")));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					imageitem.setFocusable(true);
					imageitem.requestFocus();
					ordersLayout.addView(convertView);
					final int j=i;
					imageitem.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							//Toast.makeText(PayActivity.this, j+"", Toast.LENGTH_SHORT).show();
							Intent intent = new Intent();
							intent.putExtra("tradeSequence", tradeSequence);
							intent.putExtra("key", key);
							intent.putExtra("appCode", appCode);
							intent.putExtra("requestUrl", requestUrl);
							intent.putExtra("paymentName", upgList.get(j).get("paymentName").toString());
							intent.putExtra("amount",  getIntent().getExtras().getString("amount"));
							intent.putExtra("paymentPath", upgList.get(j).get("paymentPath").toString());
							intent.putExtra("paymentImageUrl", upgList.get(j).get("paymentImageUrl").toString());
							intent.putExtra("paymentCode",upgList.get(j).get("paymentCode").toString());
							intent.setClass(PayActivity.this, AliPayActivity.class);
							Log.i("w3", tradeSequence+ key +appCode+requestUrl+upgList.get(j).get("paymentName").toString()
									+getIntent().getExtras().getString("amount")+upgList.get(j).get("paymentPath").toString()
									+ upgList.get(j).get("paymentPath").toString()+upgList.get(j).get("paymentImageUrl").toString()
									+upgList.get(j).get("paymentCode").toString());
							startActivityForResult(intent, 1);
							
						}
					});
					
				}
				break;
			case FAIL_INPUT:
				String msg = (String)message.obj;
				Intent intent = new Intent(PayActivity.this,OrderFailedActivity.class);
				intent.putExtra("msg",msg);
				startActivityForResult(intent, 3);
				break;
			case PAYMENTCODE_EMPTY:
				Bundle bundle = message.getData();
				Intent intent1 = new Intent(PayActivity.this,AliPayActivity.class);
				intent1.putExtra("content",bundle.getString("content"));
				intent1.putExtra("paymentCode_Status",1);
				intent1.putExtra("tradeSequence", bundle.getString("tradeSequence"));
				intent1.putExtra("key", key);
				intent1.putExtra("appCode", appCode);
				intent1.putExtra("requestUrl", requestUrl);
				intent1.putExtra("paymentName", bundle.getString("paymentName"));
				intent1.putExtra("amount",  getIntent().getExtras().getString("amount"));
				intent1.putExtra("paymentImageUrl", bundle.getString("paymentImageUrl"));
				intent1.putExtra("paymentCode",paymentCode);
				startActivityForResult(intent1, 2);
				break;
			default:
				break;
			}
		}
	};
	
	// 延时加载
	private void ShowFrameDialog(int type) {
		mLoadingFrame = LoadingFrameDialog.createDialog(this, type);
		mLoadingFrame.setFrameLoading(R.anim.progress_round);//设置对话框加载动画
		mLoadingFrame.ShowDialog(this, type);
		mLoadingFrame.setMessage("正在加载，请等待。。。");
	}

	private void removesDialog(int type) {
		mLoadingFrame.dismiss();
		mLoadingFrame = null;
	}

	//回调参数
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		switch (resultCode) {
		case ConstantUtil.RESULT_PAY_SUCCESS:
			Intent intentSuccess = new Intent();
			intentSuccess.putExtra("isSuccess", data.getStringExtra("isSuccess"));
			intentSuccess.putExtra("tradeSequence", data.getStringExtra("tradeSequence"));
			setResult(ConstantUtil.RESULT_PAY_SUCCESS, intentSuccess);
			PayActivity.this.finish();
			break;
		case ConstantUtil.RESULT_PAY_FAIL:
			Intent intentFail = new Intent();
			intentFail.putExtra("isSuccess", data.getStringExtra("isSuccess"));
			intentFail.putExtra("tradeSequence", data.getStringExtra("tradeSequence"));
			setResult(ConstantUtil.RESULT_PAY_FAIL, intentFail);
			PayActivity.this.finish();
			break;
		case ConstantUtil.RESULT_ORDER_FAIL:
			PayActivity.this.finish();
			break;
		case ConstantUtil.RESULT_ORDER_CANCEL:
			PayActivity.this.finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		
	}

	


}
