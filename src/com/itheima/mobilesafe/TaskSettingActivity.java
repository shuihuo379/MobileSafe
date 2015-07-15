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
		
		cb_show_system.setChecked(sp.getBoolean("showsystem",false)); //Ĭ�ϲ���ʾϵͳ����
		cb_show_system.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor=sp.edit();
				editor.putBoolean("showsystem",isChecked);
				editor.commit();
			}
		});
		
		/**
		//����ʱ5��,ÿ��1sִ��OnTick����
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
				//ע��:�����Ĺ㲥�¼���һ������Ĺ㲥�¼�,���嵥�ļ����ù㲥�������ǲ�����Ч��,ֻ���ڴ�������ע������Ż���Ч
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
