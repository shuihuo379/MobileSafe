package com.itheima.mobilesafe;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.db.NumberAddressQueryUtils;

public class NumberAddressQueryActivity extends Activity {
	private EditText ed_phone;
	private TextView result;
	private Vibrator vibrator; //ϵͳ�ṩ���񶯷���
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_address_query);
		ed_phone = (EditText) findViewById(R.id.ed_phone);
		result = (TextView) findViewById(R.id.result);
		vibrator=(Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		ed_phone.addTextChangedListener(new TextWatcher() {
			/**
			 * ���ı������仯��ʱ��ص�
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s!=null && s.length()>=3){
					//��ѯ���ݿ⣬������ʾ���
					String address = NumberAddressQueryUtils.queryNumber(s.toString());
					result.setText(address);
				}else if(s.length()==0){
					result.setText("��ʾ���");
				}else{
					result.setText("");
				}
			}
			
			/**
			 * ���ı������仯֮ǰ�ص�
			 */
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			/**
			 * ���ı������仯֮��ص�
			 */
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}
	
	/**
	 * ��ѯ���������
	 * @param view
	 */
	public void numberAddressQuery(View view){
		String phone = ed_phone.getText().toString().trim();
		if(TextUtils.isEmpty(phone)){
			Toast.makeText(this, "����Ϊ��", 0).show();
			//���ض���Ч��
			Animation shake=AnimationUtils.loadAnimation(this,R.anim.shake);
			ed_phone.startAnimation(shake);
			
			//���ֻ������û�
			vibrator.vibrate(2000);
			long[] pattern={200,300,300,400,1000,2000}; //ͣ200ms,��200ms,ͣ300ms,��300ms...
			vibrator.vibrate(pattern, -1); //-1��ʾ���ظ�,0��ʾѭ����,1��ʾ��300��ʼѭ����
			
			return;
		}else{
			//ȥ���ݿ��ѯ���������
			String address=NumberAddressQueryUtils.queryNumber(phone);
			result.setText(address);
		}
	}
}
