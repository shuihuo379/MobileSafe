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
 * 看门狗代码 监视系统程序的运行状态
 * 实质:在后台监视用户操作哪一个应用
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
			tempStopProtectPackname = null;  //锁屏时清除临时保护变量,当再次开启屏幕时,需要输入保护密码
		}
	}
	
	private class InnerReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("接收到了临时停止保护的广播事件...");
			tempStopProtectPackname=intent.getStringExtra("packname"); //处理广播事件,获取从输入密码的Activity中传递的数据
		}
	}
	
	private class DataChangeReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("数据库的内容变化了...");
			protectPacknames = dao.findAll();
		}
	}
	
	@Override
	public void onCreate() {
		//代码注册锁屏广播接受者
		offreceiver=new ScreenOffReceiver();
		registerReceiver(offreceiver,new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		//代码注册临时停止监视意图的广播接受者
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
					List<RunningTaskInfo> alltasks=am.getRunningTasks(1);  //返回当前正在运行的任务栈的集合
					String packname=alltasks.get(0).topActivity.getPackageName();
					
					//过程:首先给Logcat日志猫发一个消息,通过socket绑定连接到Logcat日志包里,然后将数据写到内存的缓存区里
//					System.out.println("packname="+packname); 
//					if(dao.find(packname)){ //查询数据库太慢了,消耗资源,改成从内存中查询(如下一行)
					if(protectPacknames.contains(packname)){
						//判断这个应用程序是否需要临时的停止保护
						if(packname.equals(tempStopProtectPackname)){
							
						}else{
							// 当前应用需要保护,弹出来一个输入密码的界面
							Intent intent=new Intent(getApplicationContext(),EnterPwdActivity.class);
							// 注意:服务是没有任务栈信息的,在服务开启activity,要指定这个activity运行的任务栈,开启它需要指定状态
							//(这里指定activity在新的任务栈中运行)
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra("packname",packname); //设置要保护程序的包名
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
