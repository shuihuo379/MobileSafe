package com.itheima.mobilesafe;

import android.content.Intent;
import android.os.Bundle;

public class Setup1Activity extends BaseSetupActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//注意:此处调用的是BaseSetupActivity类中的onCreate方法,初始化父类的activity,父类的activity中有手指滑动的事件
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_setup1);
		
	}
	
	@Override
	public void showNext() {
		Intent intent=new Intent(this,Setup2Activity.class);
		startActivity(intent);
		finish();
		//要求在finish或者startActivity方法后面执行
		overridePendingTransition(R.anim.tran_in,R.anim.tran_out);
		
	}

	@Override
	public void showPre() {
		
	}
}
