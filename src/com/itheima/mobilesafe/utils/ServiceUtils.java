package com.itheima.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtils {
	/**
	 * У��ĳ�������Ƿ񻹻��� 
	 * serviceName :�������ķ��������(���ϰ�����ȫ��)
	 */
	public static boolean isServiceRunning(Context context,String serviceName){
		ActivityManager am=(ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos= am.getRunningServices(100);  //����ǰϵͳ�����ķ�����С��100,�򷵻ظ÷�����,������100,��ֻ����100������
		for(RunningServiceInfo info:infos){
			String name=info.service.getClassName();  //������ȡϵͳÿһ���������еķ���(ȫ��)
			if(serviceName.equals(name)){
				//��������
				return true;
			}
		}
		return false;
	}
}
