package com.itheima.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtils {
	/**
	 * 校验某个服务是否还活着 
	 * serviceName :传进来的服务的名称(带上包名的全称)
	 */
	public static boolean isServiceRunning(Context context,String serviceName){
		ActivityManager am=(ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos= am.getRunningServices(100);  //若当前系统开启的服务数小于100,则返回该服务数,若超过100,则只返回100个服务
		for(RunningServiceInfo info:infos){
			String name=info.service.getClassName();  //迭代获取系统每一个正在运行的服务(全名)
			if(serviceName.equals(name)){
				//服务仍在
				return true;
			}
		}
		return false;
	}
}
