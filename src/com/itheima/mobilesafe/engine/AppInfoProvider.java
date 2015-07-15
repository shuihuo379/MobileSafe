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
 * 业务方法，提供手机里面安装的所有的应用程序信息
 */
public class AppInfoProvider {
	/**
	 * 获取所有的安装的应用程序信息。
	 * @param context 上下文
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context){
		PackageManager pm=context.getPackageManager();
		List<PackageInfo> packageInfos=pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		
		for(PackageInfo packInfo:packageInfos){
			AppInfo appInfo = new AppInfo();
			String packname=packInfo.packageName; 
			Drawable icon=packInfo.applicationInfo.loadIcon(pm); //获取APP的图标
			String appname=packInfo.applicationInfo.loadLabel(pm).toString(); //获取APP的名称
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
				//用户程序
				appInfo.setUserApp(true);
			}else{
				//系统程序
				appInfo.setUserApp(false);
			}
			if((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE)==0){
				//手机的内存
				appInfo.setInRom(true);
			}else{
				//手机的外存储设备
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
