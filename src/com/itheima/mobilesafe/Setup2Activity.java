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
		
		//��ԭ��һ�ε�״̬
		String sim=sp.getString("sim",null);  //�˴�SharedPreferences��ʵ���ڸ����г�ʼ����
		if(TextUtils.isEmpty(sim)){
			//û�а�
			siv_setup2_sim.setChecked(false);
		}else{
		   //�Ѿ���
			siv_setup2_sim.setChecked(true);
		}
		
		siv_setup2_sim.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor=sp.edit();
				if(siv_setup2_sim.ischecked()){
					//��Ҫ�����
					siv_setup2_sim.setChecked(false);
					editor.putString("sim",null);
				}else{
					//��Ҫ��
					siv_setup2_sim.setChecked(true);
					String sim=tm.getSimSerialNumber();  //�õ�sim�������к�
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
			Toast.makeText(this,"sim��û�а�",Toast.LENGTH_LONG).show();
			return;
		}
		
		Intent intent=new Intent(this,Setup3Activity.class);
		startActivity(intent);
		finish();
		//Ҫ����finish����startActivity��������ִ��
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
