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
	private DevicePolicyManager dpm; // 设备策略服务
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Object[] objs=(Object[]) intent.getExtras().get("pdus");
		sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
		for(Object obj:objs){
			SmsMessage sms=SmsMessage.createFromPdu((byte[])obj);  //获得具体的某一条短信
			String sender=sms.getOriginatingAddress();  //获取发送方的号码
			String body=sms.getMessageBody();
			String safenumber=sp.getString("safenumber","");
			System.out.println(sender+"===>"+body);
			
			if(sender.contains(safenumber)){
				if("#*location*#".equals(body)){
					//得到手机的GPS
					System.out.println("得到手机的GPS");
					Intent i=new Intent(context,GPSService.class);
					context.startService(i);
					SharedPreferences sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
					String lastlocation=sp.getString("lastlocation",null);
					if(TextUtils.isEmpty(lastlocation)){
						SmsManager.getDefault().sendTextMessage(sender,null,"getting location...",null,null);
					}else{
						SmsManager.getDefault().sendTextMessage(sender,null,lastlocation,null,null);
					}
					abortBroadcast();  //把这个广播终止掉,避免用户收到短信内容(须将该广播优先级设置为高)
					
				}else if("#*alarm*#".equals(body)){
					//播放报警影音
					System.out.println("播放报警影音");
					MediaPlayer player=MediaPlayer.create(context,R.raw.shaoniankuang);
					player.setLooping(false);
					player.setVolume(1.0f,1.0f);
					player.start();
					abortBroadcast();
					
				}else if("#*wipedata*#".equals(body)){
					//远程清除数据
					System.out.println("远程清除数据");
					wipePersonalData(context);
					abortBroadcast();
					
				}else if("#*lockscreen*#".equals(body)){
					//远程锁屏
					System.out.println("远程锁屏");
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
		ComponentName mDeviceAdminSample=new ComponentName(context,MyAdmin.class);  //要激活的对象
		
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
//			dpm.resetPassword("123",0);  //设置解锁密码
//			dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);  //清除sdcard上的数据
//			dpm.wipeData(0);  //恢复出厂设置
		}
	}
	
	private void wipePersonalData(Context context){
		dpm = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
		ComponentName who=new ComponentName(context,MyAdmin.class);
		if(dpm.isAdminActive(who)){
			dpm.resetPassword("123",0);
			dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE); //清除sdcard上的数据
			dpm.wipeData(0);  //恢复出厂设置
		}
	}
}
