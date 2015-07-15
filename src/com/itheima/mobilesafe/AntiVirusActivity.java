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
				tv_scan_status.setText("����ɨ��:"+scanInfo.name);
				TextView tv=new TextView(getApplicationContext());
				if(scanInfo.isvirus){
					tv.setTextColor(Color.RED);
					tv.setText("���ֲ�����"+scanInfo.name);
				}else{
					tv.setTextColor(Color.BLACK);
					tv.setText("ɨ�谲ȫ��"+scanInfo.name);
				}
				ll_container.addView(tv,0);  //ָ������0,ÿ�μ��ص��������Ϸ�
				break;
			case FINISH:
				tv_scan_status.setText("ɨ�����");
				iv_scan.clearAnimation();  //���ɨ�趯��
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
		
		//������ת����
		RotateAnimation ra=new RotateAnimation(0, 360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		ra.setDuration(1000); //һ����תһȦ
		ra.setRepeatCount(Animation.INFINITE); //����ת��
		iv_scan.startAnimation(ra);
		
		scanVirus();
	}
	
	/**
	 * ɨ�財��
	 */
	private void scanVirus(){
		//��ʼ������
		pm=getPackageManager();
		tv_scan_status.setText("���ڳ�ʼ��ɱ������...");
		
		new Thread(){
			public void run() {
				SystemClock.sleep(300);
				List<PackageInfo> infos=pm.getInstalledPackages(0);
				progressBar.setMax(infos.size());
				int progress=0;
				for(PackageInfo info:infos){
					String sourceDir=info.applicationInfo.sourceDir; //apk�ļ���װ������·��
					String md5=getFileMd5(sourceDir);
//					System.out.println(info.applicationInfo.loadLabel(pm)+":"+md5);
					ScanInfo scanInfo=new ScanInfo();
					scanInfo.name=info.applicationInfo.loadLabel(pm).toString();
					scanInfo.packname=info.packageName;
					if(AntiVirusDao.isVirus(md5)){
						//���ֲ���
						scanInfo.isvirus=true;
					}else{
						//ɨ�谲ȫ
						scanInfo.isvirus=false;
					}
					Message msg=Message.obtain();
					msg.obj=scanInfo; //ÿ��ɨ��Ӧ�ó���
					msg.what=SCANNING;
					handler.sendMessage(msg);
					
					progress++;
					progressBar.setProgress(progress);
					SystemClock.sleep(150);
				}
				
				//ɨ�����,���͸���UI�������Ϣ
				Message msg=Message.obtain();
				msg.what=FINISH;
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	
	/**
	 * ɨ����Ϣ���ڲ���
	 */
	class ScanInfo{
		String packname; //Ӧ�ó���İ���
		String name;  //Ӧ�ó��������
		boolean isvirus; //�ж��Ƿ��ǲ���
	}
	
	/**
	 * ��ȡ�ļ���md5ֵ
	 * @param path APK�ļ���ȫ·������
	 * @return
	 */
	private String getFileMd5(String path){
		try {
			// ��ȡһ���ļ���������Ϣ��ǩ����Ϣ��
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
				// ������
				int number = b & 0xff;// ����
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
