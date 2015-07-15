package com.itheima.mobilesafe;

import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;

/**
 * ����ͳ��(Google�ṩ��һЩ�����API)
 * ʵ����:��д�ļ�/proc/uid_statĿ¼�µ������ļ�tcp_rcv,tcp_snd
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
			TrafficStats.getUidTxBytes(uid); //ÿ��Ӧ�ó����͵� �ϴ�������byte
			TrafficStats.getUidRxBytes(uid); //ÿ��Ӧ�ó������ص����� byte
		}
		
		TrafficStats.getMobileTxBytes(); //��ȡ�ֻ�3g/2g�����ϴ���������
		TrafficStats.getMobileRxBytes(); //�ֻ�2g/3g���ص�������
		
		TrafficStats.getTotalTxBytes(); //�ֻ�ȫ������ӿ� ����wifi��3g��2g�ϴ���������
		TrafficStats.getTotalRxBytes(); //�ֻ�ȫ������ӿ� ����wifi��3g��2g���ص�������
	}
}
