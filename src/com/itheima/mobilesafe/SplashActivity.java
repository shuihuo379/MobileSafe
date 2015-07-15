package com.itheima.mobilesafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.utils.StreamTools;

public class SplashActivity extends Activity {
	protected static final String TAG = "SplashActivity";
	protected static final int SHOW_UPDATE_DIALOG = 0;
	protected static final int ENTER_HOME = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;

	private TextView tv_splash_version;
	private TextView tv_update_info;
	private String description;
	private String apkurl;
	private SharedPreferences sp;
	private boolean flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		sp=getSharedPreferences("config",MODE_PRIVATE);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号:" + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		
		installShortCut();
		copyDB("address.db");
		copyDB("antivirus.db"); //拷贝数据库
		
		boolean isupdate=sp.getBoolean("update",false);
		if(isupdate){
			//检查升级
			checkUpdate();
		}else{
			//自动升级已经关闭(延迟两秒进入主界面)
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					enterHome();
				}
			},2000);
		}
	
		// 添加朦胧的动画效果(使用tween动画实现透明度变化)
		AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
		animation.setDuration(1000);
		findViewById(R.id.rl_root_splash).startAnimation(animation);
	}
	

	/**
	 * 广播发送消息给桌面应用程序,创建快捷图标
	 */
	private void installShortCut() {
		boolean shortcut=sp.getBoolean("shortcut",false);
		if(shortcut){
			return;
		}
		Editor editor=sp.edit();
		
		//发送广播的意图,告诉桌面，要创建快捷图标了
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		//快捷方式  要包含3个重要的信息 1，名称 2.图标 3.干什么事情(意图)
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,"手机小卫士");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher));
		//桌面点击图标对应的意图
		Intent shortcutIntent=new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		shortcutIntent.setClassName(getPackageName(),"com.itheima.mobilesafe.SplashActivity");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,shortcutIntent);
		
		sendBroadcast(intent);
		
		editor.putBoolean("shortcut",true); //表明已安装
		editor.commit();
	}

	/**
	 * 拷贝数据库到可以访问的地方,此处为 data/data/包名/files
	 */
	private void copyDB(String filename) {
		try {
			InputStream in=getAssets().open(filename);
			File file=new File(getFilesDir(),filename);  //其中getFilesDir方法获得   data/data/包名/files 这个目录
			//目的:只让其拷贝一次数据库,提升性能
			if(file.exists() && file.length()>0){
				//正常了,无需拷贝
				System.out.println("正常了,无需拷贝...");
			}else{
				FileOutputStream fos=new FileOutputStream(file);
				byte[]buffer=new byte[1024];
				int len=0;
				while((len=in.read(buffer))!=-1){
					fos.write(buffer, 0, len);
				}
				in.close();
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_UPDATE_DIALOG:// 显示升级的对话框
				Log.i(TAG, "显示升级的对话框");
				showUpdateDialog();
				break;
			case ENTER_HOME:// 进入主页面
				enterHome();
				break;

			case URL_ERROR:// URL错误
				enterHome();
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();
				break;

			case NETWORK_ERROR:// 网络异常
				enterHome();
				Toast.makeText(SplashActivity.this, "网络异常", 0).show();
				break;

			case JSON_ERROR:// JSON解析出错
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON解析出错", 0).show();
				break;

			default:
				break;
			}
		}
	};

	private void checkUpdate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message mes = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setReadTimeout(5000);
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						// 联网成功
						InputStream in = conn.getInputStream();
						String result = StreamTools.readFromStream(in);
						Log.i(TAG, "联网成功" + result);

						// JSON解析
						JSONObject obj = new JSONObject(result);
						String version = obj.getString("version"); // 得到服务器的版本信息
						description = obj.getString("description");
						apkurl = obj.getString("apkurl");

						// 校验是否有新版本
						if (getVersionName().equals(version)) {
							// 版本一致，没有新版本，进入主页面
							mes.what = ENTER_HOME;
						} else {
							// 有新版本，弹出一升级对话框
							mes.what = SHOW_UPDATE_DIALOG;
						}
					}
				} catch (MalformedURLException e) {
					mes.what = URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					mes.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					mes.what = JSON_ERROR;
					e.printStackTrace();
				} finally {
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					if (dTime < 2000) {
						try {
							Thread.sleep(2000 - dTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(mes);
				}
			}
		}).start();
	}

	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 弹出升级对话框
	 */
	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示升级");
//		builder.setCancelable(false);  //点击返回,不让对话框消失(对用于强制升级)
		builder.setMessage(description);
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				enterHome();
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton("立刻升级", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 下载APK,并且替换安装
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					FinalHttp finalHttp = new FinalHttp();
					finalHttp.download(apkurl, Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/mobilesafe2.0.apk", new AjaxCallBack<File>() {
								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									Toast.makeText(getApplicationContext(), "下载失败", 1).show();
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									super.onLoading(count, current);
									tv_update_info.setVisibility(View.VISIBLE);  //显示下载进度文本域
									//当前下载百分比
									int progress=(int)(current * 100/count);
									tv_update_info.setText("下载进度:"+progress+"%");
								}

								@Override
								public void onSuccess(File t) {
									super.onSuccess(t);
									installAPK(t);
								}
								
								/**
								 * 安装APK
								 */
								private void installAPK(File t) {
									Intent intent=new Intent();
									intent.setAction("android.intent.action.VIEW");
									intent.addCategory("android.intent.category.DEFAULT");
									intent.setDataAndType(Uri.fromFile(t),"application/vnd.android.package-archive");
									startActivity(intent);
								}
						});
				} else {
					Toast.makeText(getApplicationContext(), "没有SD卡", 0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("下次再说", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	/**
	 * 得到应用程序的版本名称
	 */
	private String getVersionName() {
		PackageManager pm = getPackageManager(); // 用来管理手机的APK(包管理器)
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
}
