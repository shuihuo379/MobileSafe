package com.itheima.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.service.GPSService;

public class SMSReceiver extends BroadcastReceiver {
	private SharedPreferences sp;
	private DevicePolicyManager dpm; // �豸���Է���
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Object[] objs=(Object[]) intent.getExtras().get("pdus");
		sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
		for(Object obj:objs){
			SmsMessage sms=SmsMessage.createFromPdu((byte[])obj);  //��þ����ĳһ������
			String sender=sms.getOriginatingAddress();  //��ȡ���ͷ��ĺ���
			String body=sms.getMessageBody();
			String safenumber=sp.getString("safenumber","");
			System.out.println(sender+"===>"+body);
			
			if(sender.contains(safenumber)){
				if("#*location*#".equals(body)){
					//�õ��ֻ���GPS
					System.out.println("�õ��ֻ���GPS");
					Intent i=new Intent(context,GPSService.class);
					context.startService(i);
					SharedPreferences sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
					String lastlocation=sp.getString("lastlocation",null);
					if(TextUtils.isEmpty(lastlocation)){
						SmsManager.getDefault().sendTextMessage(sender,null,"getting location...",null,null);
					}else{
						SmsManager.getDefault().sendTextMessage(sender,null,lastlocation,null,null);
					}
					abortBroadcast();  //������㲥��ֹ��,�����û��յ���������(�뽫�ù㲥���ȼ�����Ϊ��)
					
				}else if("#*alarm*#".equals(body)){
					//���ű���Ӱ��
					System.out.println("���ű���Ӱ��");
					MediaPlayer player=MediaPlayer.create(context,R.raw.shaoniankuang);
					player.setLooping(false);
					player.setVolume(1.0f,1.0f);
					player.start();
					abortBroadcast();
					
				}else if("#*wipedata*#".equals(body)){
					//Զ���������
					System.out.println("Զ���������");
					wipePersonalData(context);
					abortBroadcast();
					
				}else if("#*lockscreen*#".equals(body)){
					//Զ������
					System.out.println("Զ������");
					lockScreen(context);
					abortBroadcast();
				}
			}
		}
	}
	
	/**
	private void installApp(Context context) {
		String uri="android.resource://"+context.getPackageName()+"/"+R.raw.lock;
		Intent intent=new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.parse(uri),"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	private void openAdmin(Context context) {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		ComponentName mDeviceAdminSample=new ComponentName(context,MyAdmin.class);  //Ҫ����Ķ���
		
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,mDeviceAdminSample);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,context.getString(R.string.alert_information));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	*/
	
	private void lockScreen(Context context) {
		dpm = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
		ComponentName who=new ComponentName(context,MyAdmin.class);
		
		if(dpm.isAdminActive(who)){
			dpm.lockNow();
//			dpm.resetPassword("123",0);  //���ý�������
//			dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);  //���sdcard�ϵ�����
//			dpm.wipeData(0);  //�ָ���������
		}
	}
	
	private void wipePersonalData(Context context){
		dpm = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
		ComponentName who=new ComponentName(context,MyAdmin.class);
		if(dpm.isAdminActive(who)){
			dpm.resetPassword("123",0);
			dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE); //���sdcard�ϵ�����
			dpm.wipeData(0);  //�ָ���������
		}
	}
}
