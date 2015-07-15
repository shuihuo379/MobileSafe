package com.itheima.mobilesafe;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CleanCacheActivity extends Activity {
	private PackageManager pm;
	private ProgressBar pb;
	private TextView tv_scan_status;
	private LinearLayout ll_container;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_cache);
		pb = (ProgressBar) findViewById(R.id.pb);
		tv_scan_status=(TextView) findViewById(R.id.tv_scan_status);
		ll_container=(LinearLayout) findViewById(R.id.ll_container);
		scanCache();
	}
	
	/**
	 * ɨ���ֻ���������Ӧ�ó���Ļ�����Ϣ(����ִ��Դ���е����ط���)
	 */
	private void scanCache() {
		pm=getPackageManager();
		new Thread(){
			public void run() {
				Method getPackageSizeInfoMethod=null;
				Method[] methods=PackageManager.class.getMethods();
				for(Method method:methods){
//					System.out.println(method.getName());
					//һ�����ڸ÷���,��ִ�з���ʱ���������ָ���쳣
					if("getPackageSizeInfo".equals(method.getName())){
						getPackageSizeInfoMethod=method;  //�õ��÷����ķ������
					}
				}
				List<PackageInfo> infos=pm.getInstalledPackages(0);
				pb.setMax(infos.size());  //ע:�������ؼ��ڲ���װ����Handler��Message��Ϣ����,�ʿ���ֱ�Ӹ���UI����
				int progress=0;
				for(PackageInfo info:infos){
					try {
						getPackageSizeInfoMethod.invoke(pm,info.packageName,new MyDataObserver());
						Thread.sleep(150);
					} catch (Exception e) {
						e.printStackTrace();
					}
					progress++;
					pb.setProgress(progress);
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv_scan_status.setText("ɨ�����...");
					}
				});
			}
		}.start();
	}
	
	private class MyDataObserver extends IPackageStatsObserver.Stub{
		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			final long cache=pStats.cacheSize;
			long code=pStats.codeSize;
			long data=pStats.dataSize;
			final String packname=pStats.packageName;
			final ApplicationInfo appInfo;
			try{
				appInfo = pm.getApplicationInfo(packname, 0);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv_scan_status.setText("����ɨ��:"+appInfo.loadLabel(pm));
						if(cache>0){
							View view = View.inflate(getApplicationContext(), R.layout.list_item_cacheinfo, null);
							
							TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache_size);
							tv_cache.setText("�����С:"+Formatter.formatFileSize(getApplicationContext(), cache));
							TextView tv_name = (TextView) view.findViewById(R.id.tv_app_name);
							tv_name.setText(appInfo.loadLabel(pm));
							ImageView iv_delete=(ImageView) view.findViewById(R.id.iv_delete);
							
							iv_delete.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									try {
										Method method=PackageManager.class.getMethod("deleteApplicationCacheFiles",
												String.class,IPackageDataObserver.class);
										method.invoke(pm,packname,new MyPackDataObserver());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
							
							ll_container.addView(view, 0);
						}
					}
				});
			}catch (NameNotFoundException e){
				e.printStackTrace();
			}
		}
	}
	
	private class MyPackDataObserver extends IPackageDataObserver.Stub{
		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
			System.out.println(packageName+"--->"+succeeded);
		}
	}
	
	/**
	 * ����ֻ���ȫ������
	 * ��PackageManager���Դ��,���÷���������ط���
	 * @param view
	 */
	public void clearAll(View view){
		Method[] methods=PackageManager.class.getMethods();
		for(Method method:methods){
			if("freeStorageAndNotify".equals(method.getName())){
				try {
					method.invoke(pm,Short.MAX_VALUE,new MyPackDataObserver());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		}
	}
}
