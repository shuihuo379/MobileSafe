package com.itheima.mobilesafe;

import android.content.Intent;
import android.os.Bundle;

public class Setup1Activity extends BaseSetupActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//ע��:�˴����õ���BaseSetupActivity���е�onCreate����,��ʼ�������activity,�����activity������ָ�������¼�
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_setup1);
		
	}
	
	@Override
	public void showNext() {
		Intent intent=new Intent(this,Setup2Activity.class);
		startActivity(intent);
		finish();
		//Ҫ����finish����startActivity��������ִ��
		overridePendingTransition(R.anim.tran_in,R.anim.tran_out);
		
	}

	@Override
	public void showPre() {
		
	}
}
