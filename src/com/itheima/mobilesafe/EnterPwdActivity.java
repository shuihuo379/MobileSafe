package com.itheima.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 程序锁弹出的输入密码的界面
 * @author Administrator
 */
public class EnterPwdActivity extends Activity {
	private EditText et_password;
	private String packname;
	private TextView tv_name;
	private ImageView iv_icon;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enter_pwd);
		et_password = (EditText) findViewById(R.id.et_password);
		
		Intent intent=getIntent();
		packname=intent.getStringExtra("packname"); //获取从服务中传过来的数据(应用程序的包名)
		tv_name = (TextView) findViewById(R.id.tv_name);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		
		PackageManager pm=getPackageManager();
		try {
			ApplicationInfo info=pm.getApplicationInfo(packname, 0);
			tv_name.setText(info.loadLabel(pm));
			iv_icon.setImageDrawable(info.loadIcon(pm));  //显示
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
	   //回桌面(模仿小房子键)
	   /**
	    <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.MONKEY"/>
        */
		
		Intent intent=new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addCategory("android.intent.category.MONKEY");
		startActivity(intent);
		//实质是:所有的activity最小化 不会执行ondestory 只执行 onstop方法,故须重写onstop方法,执行finish操作,彻底关闭当前界面
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
	
	public void click(View view){
		String pwd = et_password.getText().toString().trim();
		if(TextUtils.isEmpty(pwd)){
			Toast.makeText(this, "密码不能为空", 0).show();
			return;
		}
		if("123".equals(pwd)){
			//告诉看门狗这个程序密码输入正确了,可以临时的停止保护
			//自定义的广播,临时停止保护
			Intent intent=new Intent();
			intent.setAction("com.itheima.mobilesafe.tempstop");
			intent.putExtra("packname",packname); //发送广播，携带数据
			sendBroadcast(intent);
			finish();
		}else{
			Toast.makeText(this,"密码错误",0).show();
		}
	}
}
