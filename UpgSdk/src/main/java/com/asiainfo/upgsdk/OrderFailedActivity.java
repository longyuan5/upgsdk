package com.asiainfo.upgsdk;

import com.asiainfo.util.ConstantUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OrderFailedActivity extends Activity{

	private String failString;
	private TextView tv_message;
	private Button btn_order_failed;
	private View view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sdk_activity_order_failure);
		
		view = getWindow().getDecorView();
		failString = getIntent().getExtras().getString("msg");
		
		tv_message = (TextView)findViewById(R.id.tv_message);
		tv_message.setText(failString);
		
		btn_order_failed = (Button)findViewById(R.id.btn_order_failure);
	}
	
	
	@Override
	public void onBackPressed() {
		try {
			returnActivity(view);
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}


	public void returnActivity(View v) throws ClassNotFoundException {
		
		Intent intent=new Intent();
//		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.setClass(OrderFailedActivity.this, Class.forName(backActivity));
		setResult(ConstantUtil.RESULT_ORDER_FAIL, intent);
		finish();
	}
	

}
