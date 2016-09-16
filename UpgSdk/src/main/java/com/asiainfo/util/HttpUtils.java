package com.asiainfo.util;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 
 * @author jh
 *
 */
public class HttpUtils {
	
	//public static String HOST = "";
	public static final int METHOD_GET = 1; 
	public static final int METHOD_POST = 2;

    //**统一支付接口**//
	 public static String unionPayUrl="/apk/selfpay/pay.do";
	 //**支付宝支付接口**//
	 public static String alipayUrl="/apk/alipaytradepre/pay.do";
	 //**支付结果查询接口**//
	 public static String queryPayUrl="/apk/selfpay/queryPayResult.do";
	 //**取消支付**//
	 public static String cancleUrl="/apk/selfpay/cancelOrder.do";
	 //**二维码在线地址**//
	  public static String qrCodeUrl="/apk/selfpay/getQrCodeImage.do?qrCode=";
	  
	  
	 public static HttpEntity getEntity(String url,ArrayList<BasicNameValuePair> params, int method) {
		HttpEntity entity=null;
		// 创建客户端对象
		try {
			HttpClient client =new DefaultHttpClient();
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000); 
			// 创建请求对象
			HttpUriRequest request =null;
			switch (method) {
			case METHOD_GET:
				StringBuilder sb=new StringBuilder(url);
				if(params!=null&&!params.isEmpty()){
					sb.append('?');
					for(BasicNameValuePair pair :params){
						sb.append(pair.getName())
						.append('=')
						.append(pair.getValue())
						.append('&');
					}
					sb.deleteCharAt(sb.length()-1);
				}
				request =new HttpGet(sb.toString());
				break;

			case METHOD_POST:
				request =new HttpPost(url);
				if(params!=null&&!params.isEmpty()){
					UrlEncodedFormEntity reqEntity=new UrlEncodedFormEntity(params,HTTP.UTF_8); //表单
					((HttpPost)request).setEntity(reqEntity);
				}
				break;
			}
			// 执行请求获得响应对象
			HttpResponse response =client.execute(request);
			// 如果响应吗为200获取响应实体
			if(response.getStatusLine().getStatusCode()==200){
				entity=response.getEntity();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity;
	}
	 //获取网络图片
	public static Bitmap getImage(String urls,String qrCode) throws Exception {
		URL url = new URL(urls + qrCode);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(8000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200) {
			InputStream inStream = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(inStream);
			return bitmap;
		}
		return null;
	}
	
}
