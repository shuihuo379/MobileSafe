package com.itheima.mobilesafe.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.receiver.MyWidget;
import com.itheima.mobilesafe.utils.SystemInfoUtils;

public class UpdateWidgetService extends Service {
	private Timer timer;
	private TimerTask task;
	private AppWidgetManager awm;
	private ScreenOffReceiver offreceiver; 
	private ScreenOnReceiver onreceiver;  //定义锁屏解屏的广播接受者,避免锁屏的后台更新Widget的服务消耗电量(起到环保的效果)
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		onreceiver=new ScreenOnReceiver();
		offreceiver=new ScreenOffReceiver();
		registerReceiver(onreceiver,new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(offreceiver,new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		awm=AppWidgetManager.getInstance(this);
		startTimer();
		super.onCreate();
	}

	private void startTimer() {
		if(timer==null && task==null){
			timer=new Timer();
			task=new TimerTask() {
				@Override
				public void run() {
					//利用反射机制更新另一个进程(桌面小控件)中的UI界面
					ComponentName provider=new ComponentName(getApplicationContext(),MyWidget.class);
					RemoteViews views=new RemoteViews(getPackageName(), R.layout.process_widget);
					views.setTextViewText(R.id.process_count,"正在运行的进程:"+
							SystemInfoUtils.getRunningProcessCount(getApplicationContext())+"个");
					views.setTextViewText(R.id.process_memory,"可用内存:"+
							Formatter.formatFileSize(getApplicationContext(), 
							SystemInfoUtils.getAvailMem(getApplicationContext())));
					
					// 描述一个动作,这个动作是由另外的一个应用程序执行的(PendingIntent)
					// 自定义一个广播事件,杀死后台进度的事件
					Intent intent=new Intent();
					intent.setAction("com.itheima.mobilesafe.killall");
					PendingIntent pendingIntent=PendingIntent.getBroadcast(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
					views.setOnClickPendingIntent(R.id.btn_clear,pendingIntent); //点击按钮,由桌面来发送广播
					awm.updateAppWidget(provider, views);
				}
			};
			timer.schedule(task, 0, 3000);
		}
	}
	
	/**
	 * 锁屏的广播接受者
	 * @author Administrator
	 */
	private class ScreenOffReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("屏幕锁屏了...");
			stopTimer(); //锁屏停止计时
		}
	}
	
	/**
	 * 解锁屏幕的广播接受者
	 * @author Administrator
	 */
	private class ScreenOnReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("屏幕解锁了...");
			startTimer(); //解屏开始计时
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(offreceiver);
		unregisterReceiver(onreceiver);
		offreceiver=null;
		onreceiver=null;
		stopTimer();
	}

	private void stopTimer() {
		if(timer!=null && task!=null){
			timer.cancel();
			task.cancel();
			timer=null;
			task=null;
		}
	}
}
