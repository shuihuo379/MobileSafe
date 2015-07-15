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
	private Vibrator vibrator; //系统提供的振动服务
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_address_query);
		ed_phone = (EditText) findViewById(R.id.ed_phone);
		result = (TextView) findViewById(R.id.result);
		vibrator=(Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		ed_phone.addTextChangedListener(new TextWatcher() {
			/**
			 * 当文本发生变化的时候回调
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s!=null && s.length()>=3){
					//查询数据库，并且显示结果
					String address = NumberAddressQueryUtils.queryNumber(s.toString());
					result.setText(address);
				}else if(s.length()==0){
					result.setText("显示结果");
				}else{
					result.setText("");
				}
			}
			
			/**
			 * 当文本发生变化之前回调
			 */
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			/**
			 * 当文本发生变化之后回调
			 */
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}
	
	/**
	 * 查询号码归属地
	 * @param view
	 */
	public void numberAddressQuery(View view){
		String phone = ed_phone.getText().toString().trim();
		if(TextUtils.isEmpty(phone)){
			Toast.makeText(this, "号码为空", 0).show();
			//加载动画效果
			Animation shake=AnimationUtils.loadAnimation(this,R.anim.shake);
			ed_phone.startAnimation(shake);
			
			//振动手机提醒用户
			vibrator.vibrate(2000);
			long[] pattern={200,300,300,400,1000,2000}; //停200ms,振动200ms,停300ms,振动300ms...
			vibrator.vibrate(pattern, -1); //-1表示不重复,0表示循环振动,1表示从300开始循环振动
			
			return;
		}else{
			//去数据库查询号码归属地
			String address=NumberAddressQueryUtils.queryNumber(phone);
			result.setText(address);
		}
	}
}
