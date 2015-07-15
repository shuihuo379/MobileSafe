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
		tv_splash_version.setText("�汾��:" + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		
		installShortCut();
		copyDB("address.db");
		copyDB("antivirus.db"); //�������ݿ�
		
		boolean isupdate=sp.getBoolean("update",false);
		if(isupdate){
			//�������
			checkUpdate();
		}else{
			//�Զ������Ѿ��ر�(�ӳ��������������)
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					enterHome();
				}
			},2000);
		}
	
		// ������ʵĶ���Ч��(ʹ��tween����ʵ��͸���ȱ仯)
		AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
		animation.setDuration(1000);
		findViewById(R.id.rl_root_splash).startAnimation(animation);
	}
	

	/**
	 * �㲥������Ϣ������Ӧ�ó���,�������ͼ��
	 */
	private void installShortCut() {
		boolean shortcut=sp.getBoolean("shortcut",false);
		if(shortcut){
			return;
		}
		Editor editor=sp.edit();
		
		//���͹㲥����ͼ,�������棬Ҫ�������ͼ����
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		//��ݷ�ʽ  Ҫ����3����Ҫ����Ϣ 1������ 2.ͼ�� 3.��ʲô����(��ͼ)
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,"�ֻ�С��ʿ");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher));
		//������ͼ���Ӧ����ͼ
		Intent shortcutIntent=new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		shortcutIntent.setClassName(getPackageName(),"com.itheima.mobilesafe.SplashActivity");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,shortcutIntent);
		
		sendBroadcast(intent);
		
		editor.putBoolean("shortcut",true); //�����Ѱ�װ
		editor.commit();
	}

	/**
	 * �������ݿ⵽���Է��ʵĵط�,�˴�Ϊ data/data/����/files
	 */
	private void copyDB(String filename) {
		try {
			InputStream in=getAssets().open(filename);
			File file=new File(getFilesDir(),filename);  //����getFilesDir�������   data/data/����/files ���Ŀ¼
			//Ŀ��:ֻ���俽��һ�����ݿ�,��������
			if(file.exists() && file.length()>0){
				//������,���追��
				System.out.println("������,���追��...");
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
			case SHOW_UPDATE_DIALOG:// ��ʾ�����ĶԻ���
				Log.i(TAG, "��ʾ�����ĶԻ���");
				showUpdateDialog();
				break;
			case ENTER_HOME:// ������ҳ��
				enterHome();
				break;

			case URL_ERROR:// URL����
				enterHome();
				Toast.makeText(getApplicationContext(), "URL����", 0).show();
				break;

			case NETWORK_ERROR:// �����쳣
				enterHome();
				Toast.makeText(SplashActivity.this, "�����쳣", 0).show();
				break;

			case JSON_ERROR:// JSON��������
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON��������", 0).show();
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
						// �����ɹ�
						InputStream in = conn.getInputStream();
						String result = StreamTools.readFromStream(in);
						Log.i(TAG, "�����ɹ�" + result);

						// JSON����
						JSONObject obj = new JSONObject(result);
						String version = obj.getString("version"); // �õ��������İ汾��Ϣ
						description = obj.getString("description");
						apkurl = obj.getString("apkurl");

						// У���Ƿ����°汾
						if (getVersionName().equals(version)) {
							// �汾һ�£�û���°汾��������ҳ��
							mes.what = ENTER_HOME;
						} else {
							// ���°汾������һ�����Ի���
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
	 * ���������Ի���
	 */
	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("��ʾ����");
//		builder.setCancelable(false);  //�������,���öԻ�����ʧ(������ǿ������)
		builder.setMessage(description);
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				enterHome();
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton("��������", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ����APK,�����滻��װ
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					FinalHttp finalHttp = new FinalHttp();
					finalHttp.download(apkurl, Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/mobilesafe2.0.apk", new AjaxCallBack<File>() {
								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									Toast.makeText(getApplicationContext(), "����ʧ��", 1).show();
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									super.onLoading(count, current);
									tv_update_info.setVisibility(View.VISIBLE);  //��ʾ���ؽ����ı���
									//��ǰ���ذٷֱ�
									int progress=(int)(current * 100/count);
									tv_update_info.setText("���ؽ���:"+progress+"%");
								}

								@Override
								public void onSuccess(File t) {
									super.onSuccess(t);
									installAPK(t);
								}
								
								/**
								 * ��װAPK
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
					Toast.makeText(getApplicationContext(), "û��SD��", 0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("�´���˵", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	/**
	 * �õ�Ӧ�ó���İ汾����
	 */
	private String getVersionName() {
		PackageManager pm = getPackageManager(); // ���������ֻ���APK(��������)
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
}
