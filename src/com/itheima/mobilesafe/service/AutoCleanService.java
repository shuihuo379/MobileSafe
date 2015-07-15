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
 * ����������̵ķ���
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
		registerReceiver(receiver,new IntentFilter(Intent.ACTION_SCREEN_OFF)); //������,ע�������㲥������
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver=null;
		super.onDestroy();
	}
	
	/**
	 * �����Ĺ㲥������(ע��:�˹㲥���������嵥�ļ����ù㲥�������ǲ�����Ч��,ֻ���ڴ�������ע������Ż���Ч)
	 * @author Administrator
	 */
	private class ScreenOffReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			List<RunningAppProcessInfo> infos=am.getRunningAppProcesses();
			for(RunningAppProcessInfo info:infos){
				int code=info.importance;
				//ɱ����̨������ս���
				if(code==RunningAppProcessInfo.IMPORTANCE_EMPTY || code==RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
					am.killBackgroundProcesses(info.processName);
				}
			}
		}
	}
}
