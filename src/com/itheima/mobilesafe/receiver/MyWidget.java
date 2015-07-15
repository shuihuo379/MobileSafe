package com.itheima.mobilesafe.receiver;

import com.itheima.mobilesafe.service.UpdateWidgetService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class MyWidget extends AppWidgetProvider{
	
	//只要操作widget,就会有广播事件产生,就会执行onReceive方法
	@Override
	public void onReceive(Context context, Intent intent) {
		//目的:防止用户关闭Widget更新服务
		Intent i=new Intent(context,UpdateWidgetService.class);
		context.startService(i);
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	//第一个Widget控件拿到桌面时调用
	@Override
	public void onEnabled(Context context) {
		Intent intent=new Intent(context,UpdateWidgetService.class);
		context.startService(intent);
		super.onEnabled(context);
	}
	
	//最后一个Widget控件拿到桌面时调用
	@Override
	public void onDisabled(Context context) {
		Intent intent=new Intent(context,UpdateWidgetService.class);
		context.stopService(intent);
		super.onDisabled(context);
	}
}
