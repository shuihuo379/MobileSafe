package com.itheima.mobilesafe;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itheima.mobilesafe.db.AntiVirusDao;

public class AntiVirusActivity extends Activity {
	protected static final int SCANNING = 0;
	protected static final int FINISH = 1;
	private ImageView iv_scan;
	private TextView tv_scan_status;
	private ProgressBar progressBar;
	private PackageManager pm;
	private LinearLayout ll_container;
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCANNING:
				ScanInfo scanInfo=(ScanInfo) msg.obj;
				tv_scan_status.setText("正在扫描:"+scanInfo.name);
				TextView tv=new TextView(getApplicationContext());
				if(scanInfo.isvirus){
					tv.setTextColor(Color.RED);
					tv.setText("发现病毒："+scanInfo.name);
				}else{
					tv.setTextColor(Color.BLACK);
					tv.setText("扫描安全："+scanInfo.name);
				}
				ll_container.addView(tv,0);  //指定参数0,每次加载到界面最上方
				break;
			case FINISH:
				tv_scan_status.setText("扫描完毕");
				iv_scan.clearAnimation();  //清除扫描动画
				break;
			default:
				break;
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_vitrus);
		
		iv_scan=(ImageView) findViewById(R.id.iv_scan);
		tv_scan_status = (TextView) findViewById(R.id.tv_scan_status);
		progressBar=(ProgressBar) findViewById(R.id.progressBar1);
		ll_container=(LinearLayout) findViewById(R.id.ll_container);
		
		//定义旋转动画
		RotateAnimation ra=new RotateAnimation(0, 360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		ra.setDuration(1000); //一秒钟转一圈
		ra.setRepeatCount(Animation.INFINITE); //永久转动
		iv_scan.startAnimation(ra);
		
		scanVirus();
	}
	
	/**
	 * 扫描病毒
	 */
	private void scanVirus(){
		//初始化工作
		pm=getPackageManager();
		tv_scan_status.setText("正在初始化杀毒引擎...");
		
		new Thread(){
			public void run() {
				SystemClock.sleep(300);
				List<PackageInfo> infos=pm.getInstalledPackages(0);
				progressBar.setMax(infos.size());
				int progress=0;
				for(PackageInfo info:infos){
					String sourceDir=info.applicationInfo.sourceDir; //apk文件安装的完整路径
					String md5=getFileMd5(sourceDir);
//					System.out.println(info.applicationInfo.loadLabel(pm)+":"+md5);
					ScanInfo scanInfo=new ScanInfo();
					scanInfo.name=info.applicationInfo.loadLabel(pm).toString();
					scanInfo.packname=info.packageName;
					if(AntiVirusDao.isVirus(md5)){
						//发现病毒
						scanInfo.isvirus=true;
					}else{
						//扫描安全
						scanInfo.isvirus=false;
					}
					Message msg=Message.obtain();
					msg.obj=scanInfo; //每个扫描应用程序
					msg.what=SCANNING;
					handler.sendMessage(msg);
					
					progress++;
					progressBar.setProgress(progress);
					SystemClock.sleep(150);
				}
				
				//扫描完毕,发送更新UI界面的消息
				Message msg=Message.obtain();
				msg.what=FINISH;
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	
	/**
	 * 扫描信息的内部类
	 */
	class ScanInfo{
		String packname; //应用程序的包名
		String name;  //应用程序的名称
		boolean isvirus; //判断是否是病毒
	}
	
	/**
	 * 获取文件的md5值
	 * @param path APK文件的全路径名称
	 * @return
	 */
	private String getFileMd5(String path){
		try {
			// 获取一个文件的特征信息，签名信息。
			File file = new File(path);
			// md5
			MessageDigest digest = MessageDigest.getInstance("md5");
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = fis.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			byte[] result = digest.digest();
			StringBuffer sb  = new StringBuffer();
			for (byte b : result) {
				// 与运算
				int number = b & 0xff;// 加盐
				String str = Integer.toHexString(number);
				// System.out.println(str);
				if (str.length() == 1) {
					sb.append("0");
				}
				sb.append(str);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
