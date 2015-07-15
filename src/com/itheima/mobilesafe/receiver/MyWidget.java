package com.itheima.mobilesafe.receiver;

import com.itheima.mobilesafe.service.UpdateWidgetService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class MyWidget extends AppWidgetProvider{
	
	//ֻҪ����widget,�ͻ��й㲥�¼�����,�ͻ�ִ��onReceive����
	@Override
	public void onReceive(Context context, Intent intent) {
		//Ŀ��:��ֹ�û��ر�Widget���·���
		Intent i=new Intent(context,UpdateWidgetService.class);
		context.startService(i);
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	//��һ��Widget�ؼ��õ�����ʱ����
	@Override
	public void onEnabled(Context context) {
		Intent intent=new Intent(context,UpdateWidgetService.class);
		context.startService(intent);
		super.onEnabled(context);
	}
	
	//���һ��Widget�ؼ��õ�����ʱ����
	@Override
	public void onDisabled(Context context) {
		Intent intent=new Intent(context,UpdateWidgetService.class);
		context.stopService(intent);
		super.onDisabled(context);
	}
}
