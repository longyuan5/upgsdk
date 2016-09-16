package com.asiainfo.util;



import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.asiainfo.upgsdk.R;

public class LoadingFrameDialog extends Dialog{
	private static LoadingFrameDialog m_LoadingDialog = null;
	private static ImageView iv_loading=null;
	
	
	public LoadingFrameDialog(Context context) {
		super(context);
	}

	public LoadingFrameDialog(Context context, int theme) {
		super(context, theme);
	}

	public static LoadingFrameDialog createDialog(Context context , int type) {
		m_LoadingDialog = new LoadingFrameDialog(context,
				R.style.CreateFrameDialog);
		m_LoadingDialog.setCancelable(false);
		m_LoadingDialog.setContentView(R.layout.sdk_dialog_loading);
		return m_LoadingDialog;
	}


	public static void setFrameLoading(int Drawable){
		if (m_LoadingDialog == null) {
			return;
		}
		 iv_loading = (ImageView) m_LoadingDialog
				.findViewById(R.id.iv_loading);
		iv_loading.setBackgroundResource(Drawable);
		AnimationDrawable animationDrawable = (AnimationDrawable) iv_loading
				.getBackground();
		animationDrawable.start();
		
	}
	

	/**�رռ���Ȧ�͵�ǰ����*/
	public static void DiaLogCilck(final Context context , final int type) {
		if (m_LoadingDialog == null)
			return;
		m_LoadingDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					switch (type) {
					case DialogCloseType.CLOSEALL:
						dialog.dismiss();
						((Activity) context).finish();
						break;
					case DialogCloseType.CLOSEDIALOG:
						dialog.dismiss();
						break;
					case DialogCloseType.NOTCLOSE:
						 
						break;
					}
					return true;
				} else {
					return false;
				}
			}
		});
	}

	public LoadingFrameDialog setMessage(String strMessage) {
		TextView tvMsg = (TextView) m_LoadingDialog
				.findViewById(R.id.tv_loading_text);
		if (tvMsg != null) {
			tvMsg.setText(strMessage);
		}
		return m_LoadingDialog;
	}

	public  void ShowDialog(Context context , int type) {
		try {
			DiaLogCilck(context, type);
			m_LoadingDialog.show();
		} catch (Exception e) {

		}
	}
	public void removeLoading(){
		try {
			iv_loading.clearAnimation();
			m_LoadingDialog.dismiss();
			m_LoadingDialog = null;
		} catch (Exception e) {
		}
	}

}
