package com.itheima.mobilesafe;

import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;

/**
 * 流量统计(Google提供的一些方便的API)
 * 实质是:读写文件/proc/uid_stat目录下的两个文件tcp_rcv,tcp_snd
 * @author Administrator
 */
public class TrafficManagerActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_manager);
		
		PackageManager pm=getPackageManager();
		List<ApplicationInfo> infos=pm.getInstalledApplications(0);
		for(ApplicationInfo applicationInfo:infos){
			int uid=applicationInfo.uid;
			TrafficStats.getUidTxBytes(uid); //每个应用程序发送的 上传的流量byte
			TrafficStats.getUidRxBytes(uid); //每个应用程序下载的流量 byte
		}
		
		TrafficStats.getMobileTxBytes(); //获取手机3g/2g网络上传的总流量
		TrafficStats.getMobileRxBytes(); //手机2g/3g下载的总流量
		
		TrafficStats.getTotalTxBytes(); //手机全部网络接口 包括wifi，3g、2g上传的总流量
		TrafficStats.getTotalRxBytes(); //手机全部网络接口 包括wifi，3g、2g下载的总流量
	}
}
