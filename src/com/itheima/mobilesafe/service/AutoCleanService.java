package com.itheima.mobilesafe.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * 锁屏清理进程的服务
 */
public class AutoCleanService extends Service {
	private ScreenOffReceiver receiver; 
	private ActivityManager am;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		am=(ActivityManager) getSystemService(ACTIVITY_SERVICE);
		receiver=new ScreenOffReceiver();
		registerReceiver(receiver,new IntentFilter(Intent.ACTION_SCREEN_OFF)); //服务开启,注册锁屏广播接受者
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver=null;
		super.onDestroy();
	}
	
	/**
	 * 锁屏的广播接受者(注意:此广播接受者在清单文件配置广播接收者是不会生效的,只能在代码里面注册里面才会生效)
	 * @author Administrator
	 */
	private class ScreenOffReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			List<RunningAppProcessInfo> infos=am.getRunningAppProcesses();
			for(RunningAppProcessInfo info:infos){
				int code=info.importance;
				//杀死后台进程与空进程
				if(code==RunningAppProcessInfo.IMPORTANCE_EMPTY || code==RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
					am.killBackgroundProcesses(info.processName);
				}
			}
		}
	}
}
