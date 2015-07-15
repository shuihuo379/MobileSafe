package com.itheima.mobilesafe.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 一键清理(清理后台进程和空进程) 
 * @author Administrator
 */
public class KillAllReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ActivityManager am=(ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
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
