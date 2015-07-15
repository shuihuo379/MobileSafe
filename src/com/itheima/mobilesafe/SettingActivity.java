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
	private SettingItemView siv_update; // ������Ͽؼ���ʵ��
	private SharedPreferences sp;
	
	private SettingItemView siv_show_address; // �����Ƿ�����ʾ������
	private Intent showAddress;
	private SettingClickView scv_changebg;  //���ù�������ʾ�򱳾�
	
	//��������������
	private SettingItemView siv_callsms_safe;
	private Intent callSmsSafeIntent;
	
	//���������Ź�����
	private SettingItemView siv_watchdog;
	private Intent watchDogIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// ��ʼ�����Ƿ����Զ�����(�����Զ�����Ͽؼ���ͼ)
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		siv_watchdog=(SettingItemView) findViewById(R.id.siv_watchdog);

		/**
		 * ȡ���ϴ��˳�ʱ�����״̬(��һ�ν�ȥ,Ĭ����false)
		 */
		boolean update = sp.getBoolean("update", false);
		if (update) {
			// �Զ������Ѿ�����
			siv_update.setChecked(true);
		} else {
			// �Զ������Ѿ��ر�
			siv_update.setChecked(false);
		}

		/**
		 * ������Ͽؼ��ĵ���¼�
		 */
		siv_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Editor editor = sp.edit();
				// �ж��Ƿ���ѡ��
				if (siv_update.ischecked()) {
					// �Ѿ����Զ�����,�����ر�
					siv_update.setChecked(false);
					editor.putBoolean("update", false);
				} else {
					// û�д��Զ�����,�������
					siv_update.setChecked(true);
					editor.putBoolean("update", true);
				}
				editor.commit(); // ��������˳�ʱ��״̬
			}
		});

		// ���ú����������ʾ�ؼ�
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddress = new Intent(this, AddressService.class);

		// ������������״̬
		boolean isServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.AddressService");
		if(isServiceRunning){
			//��������ķ����ǿ�����
			siv_show_address.setChecked(true);
		}else{
			siv_show_address.setChecked(false);
		}
		
		// �������Ź������״̬
		boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.WatchDogService");
		siv_watchdog.setChecked(isWatchDogServiceRunning);

		siv_show_address.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv_show_address.ischecked()) {
					// ��Ϊ��ѡ��״̬
					siv_show_address.setChecked(false);
					stopService(showAddress); // �ر���ʾ�����������صķ���
				} else {
					// ѡ��״̬
					siv_show_address.setChecked(true);
					startService(showAddress); // ������ʾ�����������صķ���
				}
			}
		});
		
		//���ú�������صı���
		scv_changebg=(SettingClickView) findViewById(R.id.scv_changebg);
		scv_changebg.setTitle("��������ʾ��ķ��");
		//���弸�ֱ�������
		final String [] items = {"��͸��","������","��ʿ��","������","ƻ����"};
		//ȡ����һ�α����ѡ��ı�������
		int which=sp.getInt("which",0);
		scv_changebg.setDesc(items[which]);
		
		scv_changebg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int dd=sp.getInt("which",0);
				//����һ����ѡ�Ի���
				AlertDialog.Builder builder=new Builder(SettingActivity.this);
				builder.setSingleChoiceItems(items,dd,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//����ѡ�����
						Editor editor=sp.edit();
						editor.putInt("which",which);
						editor.commit();
						scv_changebg.setDesc(items[which]);
						//ȡ���Ի���
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("cancel",null);
				builder.show();
			}
		});
		
		
		//��������������
		siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
		siv_callsms_safe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv_callsms_safe.ischecked()) {
					// ��Ϊ��ѡ��״̬
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					// ѡ��״̬
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);   //������������������
				}
			}
		});
		
		
		//����������
		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);
		watchDogIntent = new Intent(this, WatchDogService.class);
		siv_watchdog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv_watchdog.ischecked()) {
					// ��Ϊ��ѡ��״̬
					siv_watchdog.setChecked(false);
					stopService(watchDogIntent);
				} else {
					// ѡ��״̬
					siv_watchdog.setChecked(true);
					startService(watchDogIntent); //�������ӹ�����
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showAddress = new Intent(this, AddressService.class);
		
		// ������������״̬
		boolean isServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.AddressService");
		if(isServiceRunning){
			//��������ķ����ǿ�����
			siv_show_address.setChecked(true);
		}else{
			siv_show_address.setChecked(false);
		}
		
		//�����������������ط����״̬
		boolean isCallSmsServiceRunning = ServiceUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.CallSmsSafeService");
		siv_callsms_safe.setChecked(isCallSmsServiceRunning);
	}
}
