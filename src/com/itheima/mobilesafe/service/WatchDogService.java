package com.itheima.mobilesafe.service;

import java.util.List;

import com.itheima.mobilesafe.EnterPwdActivity;
import com.itheima.mobilesafe.db.ApplockDao;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * ���Ź����� ����ϵͳ���������״̬
 * ʵ��:�ں�̨�����û�������һ��Ӧ��
 * @author Administrator
 */
public class WatchDogService extends Service {
	private ActivityManager am;
	private boolean flag;
	private ApplockDao dao;
	private InnerReceiver receiver;
	private ScreenOffReceiver offreceiver; 
	private DataChangeReceiver dataChangeReceiver;
	private String tempStopProtectPackname;
	private List<String> protectPacknames;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class ScreenOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			tempStopProtectPackname = null;  //����ʱ�����ʱ��������,���ٴο�����Ļʱ,��Ҫ���뱣������
		}
	}
	
	private class InnerReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("���յ�����ʱֹͣ�����Ĺ㲥�¼�...");
			tempStopProtectPackname=intent.getStringExtra("packname"); //����㲥�¼�,��ȡ�����������Activity�д��ݵ�����
		}
	}
	
	private class DataChangeReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("���ݿ�����ݱ仯��...");
			protectPacknames = dao.findAll();
		}
	}
	
	@Override
	public void onCreate() {
		//����ע�������㲥������
		offreceiver=new ScreenOffReceiver();
		registerReceiver(offreceiver,new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		//����ע����ʱֹͣ������ͼ�Ĺ㲥������
		receiver=new InnerReceiver();
		IntentFilter filter=new IntentFilter("com.itheima.mobilesafe.tempstop");
		registerReceiver(receiver, filter);
		
		dataChangeReceiver=new DataChangeReceiver();
		registerReceiver(dataChangeReceiver,new IntentFilter("com.itheima.mobilesafe.applockchange"));
		
		am=(ActivityManager) getSystemService(ACTIVITY_SERVICE);
		dao=new ApplockDao(this);
		protectPacknames=dao.findAll();
		flag=true;
		
		new Thread(){
			public void run(){
				while(flag){
					List<RunningTaskInfo> alltasks=am.getRunningTasks(1);  //���ص�ǰ�������е�����ջ�ļ���
					String packname=alltasks.get(0).topActivity.getPackageName();
					
					//����:���ȸ�Logcat��־è��һ����Ϣ,ͨ��socket�����ӵ�Logcat��־����,Ȼ������д���ڴ�Ļ�������
//					System.out.println("packname="+packname); 
//					if(dao.find(packname)){ //��ѯ���ݿ�̫����,������Դ,�ĳɴ��ڴ��в�ѯ(����һ��)
					if(protectPacknames.contains(packname)){
						//�ж����Ӧ�ó����Ƿ���Ҫ��ʱ��ֹͣ����
						if(packname.equals(tempStopProtectPackname)){
							
						}else{
							// ��ǰӦ����Ҫ����,������һ����������Ľ���
							Intent intent=new Intent(getApplicationContext(),EnterPwdActivity.class);
							// ע��:������û������ջ��Ϣ��,�ڷ�����activity,Ҫָ�����activity���е�����ջ,��������Ҫָ��״̬
							//(����ָ��activity���µ�����ջ������)
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra("packname",packname); //����Ҫ��������İ���
							startActivity(intent);
						}
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver=null;
		unregisterReceiver(offreceiver);
		offreceiver=null;
		unregisterReceiver(dataChangeReceiver);
		dataChangeReceiver=null;
		flag=false;
		super.onDestroy();
	}
}
