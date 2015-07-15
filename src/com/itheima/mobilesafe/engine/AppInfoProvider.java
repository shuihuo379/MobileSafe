package com.itheima.mobilesafe.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.itheima.mobilesafe.domain.AppInfo;

/**
 * ҵ�񷽷����ṩ�ֻ����氲װ�����е�Ӧ�ó�����Ϣ
 */
public class AppInfoProvider {
	/**
	 * ��ȡ���еİ�װ��Ӧ�ó�����Ϣ��
	 * @param context ������
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context){
		PackageManager pm=context.getPackageManager();
		List<PackageInfo> packageInfos=pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		
		for(PackageInfo packInfo:packageInfos){
			AppInfo appInfo = new AppInfo();
			String packname=packInfo.packageName; 
			Drawable icon=packInfo.applicationInfo.loadIcon(pm); //��ȡAPP��ͼ��
			String appname=packInfo.applicationInfo.loadLabel(pm).toString(); //��ȡAPP������
			int uid=packInfo.applicationInfo.uid; 
//			File rcvfile=new File("/proc/uid_stat/"+uid+"tcp_rcv");
//			File sndfile=new File("/proc/uid_stat/"+uid+"tcp_snd");
			appInfo.setUid(uid);
			
			int flags=packInfo.applicationInfo.flags;
			/**
			 * FLAG_SYSTEM = 1<<0
			 * FLAG_DEBUGGABLE = 1<<1
			 * FLAG_HAS_CODE = 1<<2
			 * FLAG_PERSISTENT = 1<<3
			 * ...
			 */
			if((flags & ApplicationInfo.FLAG_SYSTEM)==0){
				//�û�����
				appInfo.setUserApp(true);
			}else{
				//ϵͳ����
				appInfo.setUserApp(false);
			}
			if((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE)==0){
				//�ֻ����ڴ�
				appInfo.setInRom(true);
			}else{
				//�ֻ�����洢�豸
				appInfo.setInRom(false);
			}
			
			appInfo.setPackname(packname);
			appInfo.setIcon(icon);
			appInfo.setName(appname);
			appInfos.add(appInfo);
		}
		
		return appInfos;
	}
}
