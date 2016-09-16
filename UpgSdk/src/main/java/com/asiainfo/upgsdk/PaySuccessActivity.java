package com.asiainfo.upgsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.asiainfo.util.ConstantUtil;

public class PaySuccessActivity extends Activity{
	
	private View view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sdk_activity_pay_success);
		
		view = getWindow().getDecorView();
		String maney=getIntent().getExtras().getString("amount");
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



	public void returnActivity(View v) throws ClassNotFoundException{
		
//		Intent intent=new Intent();
////		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
////		intent.setClass(PaySuccessActivity.this, Class.forName(backActivity));
//		intent.putExtra("isSuccess", "SUCCESS");
//		intent.putExtra("tradeSequence", getIntent().getExtras().getString("tradeSequence"));
//		setResult(ConstantUtil.RESULT_PAY_SUCCESS, intent);
		finish();
	}
	
	
}
