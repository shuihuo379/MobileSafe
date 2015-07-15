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
	private ScreenOnReceiver onreceiver;  //�������������Ĺ㲥������,���������ĺ�̨����Widget�ķ������ĵ���(�𵽻�����Ч��)
	
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
					//���÷�����Ƹ�����һ������(����С�ؼ�)�е�UI����
					ComponentName provider=new ComponentName(getApplicationContext(),MyWidget.class);
					RemoteViews views=new RemoteViews(getPackageName(), R.layout.process_widget);
					views.setTextViewText(R.id.process_count,"�������еĽ���:"+
							SystemInfoUtils.getRunningProcessCount(getApplicationContext())+"��");
					views.setTextViewText(R.id.process_memory,"�����ڴ�:"+
							Formatter.formatFileSize(getApplicationContext(), 
							SystemInfoUtils.getAvailMem(getApplicationContext())));
					
					// ����һ������,����������������һ��Ӧ�ó���ִ�е�(PendingIntent)
					// �Զ���һ���㲥�¼�,ɱ����̨���ȵ��¼�
					Intent intent=new Intent();
					intent.setAction("com.itheima.mobilesafe.killall");
					PendingIntent pendingIntent=PendingIntent.getBroadcast(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
					views.setOnClickPendingIntent(R.id.btn_clear,pendingIntent); //�����ť,�����������͹㲥
					awm.updateAppWidget(provider, views);
				}
			};
			timer.schedule(task, 0, 3000);
		}
	}
	
	/**
	 * �����Ĺ㲥������
	 * @author Administrator
	 */
	private class ScreenOffReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("��Ļ������...");
			stopTimer(); //����ֹͣ��ʱ
		}
	}
	
	/**
	 * ������Ļ�Ĺ㲥������
	 * @author Administrator
	 */
	private class ScreenOnReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("��Ļ������...");
			startTimer(); //������ʼ��ʱ
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
