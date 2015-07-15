package com.itheima.mobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setup3Activity extends BaseSetupActivity{
	private static final int REQUEST_CODE = 1;
	private EditText et_setup3_phone;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup3);
		et_setup3_phone=(EditText) findViewById(R.id.et_setup3_phone);
		et_setup3_phone.setText(sp.getString("safenumber",""));
	}

	@Override
	public void showNext() {
		String phone=et_setup3_phone.getText().toString().trim();
		if(TextUtils.isEmpty(phone)){
			Toast.makeText(this, "��ȫ���뻹û������", Toast.LENGTH_SHORT).show();
			return;
		}
		//���氲ȫ����
		Editor editor=sp.edit();  //�˴���ʵ���ڸ����г�ʼ����
		editor.putString("safenumber",phone);
		editor.commit();
		
		Intent intent=new Intent(this,Setup4Activity.class);
		startActivity(intent);
		finish();
		//Ҫ����finish����startActivity��������ִ��(ʹ��XML�ļ����嶯��Ч����ִ��)
		overridePendingTransition(R.anim.tran_in,R.anim.tran_out);
	}

	@Override
	public void showPre() {
		Intent intent=new Intent(this,Setup2Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in,R.anim.tran_pre_out);
	}
	
	/**
	 * ѡ����ϵ�˵ĵ���¼�
	 * @param view
	 */
	public void selectContact(View view){
		Intent intent=new Intent(this,SelectContactActivity.class);
		startActivityForResult(intent,REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data==null){
			return;
		}
		if(requestCode==REQUEST_CODE && resultCode==RESULT_OK){
			String phone=data.getStringExtra("phone").replace("-","");
			et_setup3_phone.setText(phone);
		}
	}
}