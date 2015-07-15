package com.itheima.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import com.itheima.mobilesafe.utils.SmsUtils;
import com.itheima.mobilesafe.utils.SmsUtils.BackUpCallBack;
import com.itheima.mobilesafe.utils.SmsUtils.RestoreCallBack;

public class AtoolActivity extends Activity {
	private ProgressDialog pd;
	private boolean isdelete;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atools);
	}

	public void numberQuery(View view) {
		Intent intent = new Intent(this, NumberAddressQueryActivity.class);
		startActivity(intent);
	}

	/**
	 * 点击事件,短信的备份
	 * @param view
	 */
	public void smsBackup(View view) {
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在备份短信");
		pd.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SmsUtils.backupSms(AtoolActivity.this,
							new BackUpCallBack() {
								@Override
								public void onSmsBackup(int progress) {
									pd.setProgress(progress);
								}

								@Override
								public void beforeBackup(int max) {
									pd.setMax(max);
								}
							});
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(AtoolActivity.this, "备份成功", 0).show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(AtoolActivity.this, "备份失败", 0).show();
						}
					});
				} finally {
					pd.dismiss();
				}
			}
		}).start();
	}

	/**
	 * 点击事件,短信的还原
	 * @param view
	 */
	public void smsRestore(View view){
		SharedPreferences sp=getSharedPreferences("config",MODE_PRIVATE);
		int max=sp.getInt("max",0);
		if(max==0){
			Toast.makeText(this,"你尚未备份短信",0).show();
			return;
		}
		
		/**
		 * 给出删除旧的短信的提示对话框
		 */
		AlertDialog.Builder dialog=new Builder(this);
		dialog.setTitle("提示");
		dialog.setMessage("还原短信前,是否删除已有的短信");
		dialog.setPositiveButton("确定",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				isdelete=true;
				new MyThread(isdelete).start();
				pd.show();
			}
		});
		dialog.setNegativeButton("取消",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				isdelete=false;
				new MyThread(isdelete).start();
				pd.show();
			}
		});
		dialog.show();
		
		pd=new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在还原短信");
	}
	
	class MyThread extends Thread{
		private boolean flag;
		public MyThread(boolean isdelete){
			this.flag=isdelete;
		}
		
		@Override
		public void run() {
			try{
				SmsUtils.restoreSms(AtoolActivity.this,flag,new RestoreCallBack() {
					@Override
					public void onSmsRestore(int progress) {
						pd.setProgress(progress);
					}
					
					@Override
					public void beforeRestore(int max) {
						pd.setMax(max);
					}
				});
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(AtoolActivity.this, "还原成功", 0).show();
					}
				});
			}catch (Exception e) {
				e.printStackTrace();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(AtoolActivity.this, "还原失败", 0).show();
					}
				});
			}finally{
				pd.dismiss();
			}
		}
	}
}
