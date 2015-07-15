package com.itheima.mobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.itheima.mobilesafe.ui.SettingItemView;

public class Setup2Activity extends BaseSetupActivity {
	private SettingItemView siv_setup2_sim;
	private TelephonyManager tm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);
		tm=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		siv_setup2_sim=(SettingItemView) findViewById(R.id.siv_setup2_sim);
		
		//还原上一次的状态
		String sim=sp.getString("sim",null);  //此处SharedPreferences的实例在父类中初始化了
		if(TextUtils.isEmpty(sim)){
			//没有绑定
			siv_setup2_sim.setChecked(false);
		}else{
		   //已经绑定
			siv_setup2_sim.setChecked(true);
		}
		
		siv_setup2_sim.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor=sp.edit();
				if(siv_setup2_sim.ischecked()){
					//需要解除绑定
					siv_setup2_sim.setChecked(false);
					editor.putString("sim",null);
				}else{
					//需要绑定
					siv_setup2_sim.setChecked(true);
					String sim=tm.getSimSerialNumber();  //得到sim卡的序列号
					editor.putString("sim",sim);
				}
				editor.commit();
			}
		});
	}
	
	@Override
	public void showNext() {
		String sim=sp.getString("sim",null);
		if(TextUtils.isEmpty(sim)){
			Toast.makeText(this,"sim卡没有绑定",Toast.LENGTH_LONG).show();
			return;
		}
		
		Intent intent=new Intent(this,Setup3Activity.class);
		startActivity(intent);
		finish();
		//要求在finish或者startActivity方法后面执行
		overridePendingTransition(R.anim.tran_in,R.anim.tran_out);
	}

	@Override
	public void showPre() {
		Intent intent=new Intent(this,Setup1Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in,R.anim.tran_pre_out);
	}
}
