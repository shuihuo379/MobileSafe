package com.itheima.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.itheima.mobilesafe.service.AddressService;
import com.itheima.mobilesafe.service.CallSmsSafeService;
import com.itheima.mobilesafe.service.WatchDogService;
import com.itheima.mobilesafe.ui.SettingClickView;
import com.itheima.mobilesafe.ui.SettingItemView;
import com.itheima.mobilesafe.utils.ServiceUtils;

public class SettingActivity extends Activity {
	private SettingItemView siv_update; // 创建组合控件的实例
	private SharedPreferences sp;
	
	private SettingItemView siv_show_address; // 设置是否开启显示归属地
	private Intent showAddress;
	private SettingClickView scv_changebg;  //设置归属地显示框背景
	
	//黑名单拦截设置
	private SettingItemView siv_callsms_safe;
	private Intent callSmsSafeIntent;
	
	//程序锁看门狗设置
	private SettingItemView siv_watchdog;
	private Intent watchDogIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// 初始设置是否开启自动更新(加载自定义组合控件视图)
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		siv_watchdog=(SettingItemView) findViewById(R.id.siv_watchdog);

		/**
		 * 取出上次退出时保存的状态(第一次进去,默认是false)
		 */
		boolean update = sp.getBoolean("update", false);
		if (update) {
			// 自动升级已经开启
			siv_update.setChecked(true);
		} else {
			// 自动升级已经关闭
			siv_update.setChecked(false);
		}

		/**
		 * 设置组合控件的点击事件
		 */
		siv_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Editor editor = sp.edit();
				// 判断是否有选中
				if (siv_update.ischecked()) {
					// 已经打开自动升级,点击后关闭
					siv_update.setChecked(false);
					editor.putBoolean("update", false);
				} else {
					// 没有打开自动升级,点击后开启
					siv_update.setChecked(true);
					editor.putBoolean("update", true);
				}
				editor.commit(); // 保存最后退出时的状态
			}
		});

		// 设置号码归属地显示控件
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddress = new Intent(this, AddressService.class);

		// 监听来电服务的状态
		boolean isServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.AddressService");
		if(isServiceRunning){
			//监听来电的服务是开启的
			siv_show_address.setChecked(true);
		}else{
			siv_show_address.setChecked(false);
		}
		
		// 监听看门狗服务的状态
		boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.WatchDogService");
		siv_watchdog.setChecked(isWatchDogServiceRunning);

		siv_show_address.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv_show_address.ischecked()) {
					// 变为非选中状态
					siv_show_address.setChecked(false);
					stopService(showAddress); // 关闭显示来电号码归属地的服务
				} else {
					// 选择状态
					siv_show_address.setChecked(true);
					startService(showAddress); // 开启显示来电号码归属地的服务
				}
			}
		});
		
		//设置号码归属地的背景
		scv_changebg=(SettingClickView) findViewById(R.id.scv_changebg);
		scv_changebg.setTitle("归属地提示框的风格");
		//定义几种背景主题
		final String [] items = {"半透明","活力橙","卫士蓝","金属灰","苹果绿"};
		//取出上一次保存的选择的背景主题
		int which=sp.getInt("which",0);
		scv_changebg.setDesc(items[which]);
		
		scv_changebg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int dd=sp.getInt("which",0);
				//弹出一个单选对话框
				AlertDialog.Builder builder=new Builder(SettingActivity.this);
				builder.setSingleChoiceItems(items,dd,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//保存选择参数
						Editor editor=sp.edit();
						editor.putInt("which",which);
						editor.commit();
						scv_changebg.setDesc(items[which]);
						//取消对话框
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("cancel",null);
				builder.show();
			}
		});
		
		
		//黑名单拦截设置
		siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
		siv_callsms_safe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv_callsms_safe.ischecked()) {
					// 变为非选中状态
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					// 选择状态
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);   //开启黑名单短信拦截
				}
			}
		});
		
		
		//程序锁设置
		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);
		watchDogIntent = new Intent(this, WatchDogService.class);
		siv_watchdog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv_watchdog.ischecked()) {
					// 变为非选中状态
					siv_watchdog.setChecked(false);
					stopService(watchDogIntent);
				} else {
					// 选择状态
					siv_watchdog.setChecked(true);
					startService(watchDogIntent); //开启监视狗服务
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showAddress = new Intent(this, AddressService.class);
		
		// 监听来电服务的状态
		boolean isServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.AddressService");
		if(isServiceRunning){
			//监听来电的服务是开启的
			siv_show_address.setChecked(true);
		}else{
			siv_show_address.setChecked(false);
		}
		
		//监听黑名单短信拦截服务的状态
		boolean isCallSmsServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.CallSmsSafeService");
		siv_callsms_safe.setChecked(isCallSmsServiceRunning);
	}
}
