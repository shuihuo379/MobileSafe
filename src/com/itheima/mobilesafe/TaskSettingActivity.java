package com.itheima.mobilesafe;

import com.itheima.mobilesafe.service.AutoCleanService;
import com.itheima.mobilesafe.utils.ServiceUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TaskSettingActivity extends Activity {
	private CheckBox cb_show_system;
	private CheckBox cb_auto_clean;
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_setting);
		sp=getSharedPreferences("config",MODE_PRIVATE);
		cb_auto_clean=(CheckBox) findViewById(R.id.cb_auto_clean);
		cb_show_system=(CheckBox) findViewById(R.id.cb_show_system);
		
		cb_show_system.setChecked(sp.getBoolean("showsystem",false)); //默认不显示系统进程
		cb_show_system.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor=sp.edit();
				editor.putBoolean("showsystem",isChecked);
				editor.commit();
			}
		});
		
		/**
		//倒计时5秒,每隔1s执行OnTick方法
		CountDownTimer cdt=new CountDownTimer(5000,1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				System.out.println(millisUntilFinished);
			}
			
			@Override
			public void onFinish() {
				System.out.println("finish...");
			}
		};
		cdt.start();
		*/
		
		cb_auto_clean.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//注意:锁屏的广播事件是一个特殊的广播事件,在清单文件配置广播接收者是不会生效的,只能在代码里面注册里面才会生效
				Intent intent = new Intent(TaskSettingActivity.this,AutoCleanService.class);
				if(isChecked){
					startService(intent);
				}else{
					stopService(intent);
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		boolean isRunning=ServiceUtils.isServiceRunning(this,"com.itheima.mobilesafe.service.AutoCleanService");
		cb_auto_clean.setChecked(isRunning);
		super.onResume();
	}
}
