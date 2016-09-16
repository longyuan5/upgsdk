package com.asiainfo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asiainfo.upgsdk.R;
import com.asiainfo.util.HttpUtils;

import java.util.List;
import java.util.Map;

public class UPGAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<Map<String, Object>> upgList;
	private int viewId;
	private String requestUrl;

	public UPGAdapter(Context context,List<Map<String, Object>> upgList,int viewId,String requestUrl) {
		this.upgList = upgList;
		System.out.println("nihao"+upgList);
		if(this.upgList == null){
			this.upgList = upgList;
		}
		this.inflater = LayoutInflater.from(context);
		this.viewId = viewId;
		this.requestUrl=requestUrl;
	}

	@Override
	public int getCount() {
		return upgList.size();
	}

	@Override
	public Object getItem(int position) {
		return upgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflater.inflate(R.layout.sdk_imageitem, null);

		TextView payurl=(TextView) view.findViewById(R.id.payurl);
		ImageView imageitem=(ImageView) view.findViewById(R.id.imageitems);
        payurl.setText(upgList.get(position).get("paymentName")+"");
        try {
			imageitem.setImageBitmap(HttpUtils.getImage(requestUrl,"/"+upgList.get(position).get("paymentLogoUrl")+""));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}
}