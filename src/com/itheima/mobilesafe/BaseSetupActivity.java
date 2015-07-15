package com.itheima.mobilesafe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public abstract class BaseSetupActivity extends Activity {
	private GestureDetector detector;  //����һ������ʶ����
	protected SharedPreferences sp;
	
	public abstract void showNext();  //�����������󷽷�,�ṩ������ȥʵ��,����ɲ�ͬ�Ĺ���
	public abstract void showPre();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp=getSharedPreferences("config",MODE_PRIVATE);
		
		detector=new GestureDetector(this,new OnGestureListener(){
			/**
			 * ��������ָ�����滬����ʱ��ص�
			 */
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				//����б���������
				if(Math.abs(e2.getRawY()-e1.getRawY())>100){
					Toast.makeText(getApplicationContext(),"����������",Toast.LENGTH_SHORT).show();
					return true;
				}
				//������X�Ử�����������
				if(Math.abs(velocityX)<200){
					Toast.makeText(getApplicationContext(),"����̫����",Toast.LENGTH_SHORT).show();
					return true;
				}
				
				if((e2.getRawX()-e1.getRawX())>150){
					System.out.println("��ʾ��һ��ҳ��,�������һ���");
					showPre(); //���󷽷�,���ݲ�ͬ������ʵ�ֲ�ͬ�Ĺ���
					return true;
				}
				if((e1.getRawX()-e2.getRawX())>150){
					System.out.println("��ʾ��һ��ҳ��,�������󻬶�");
					showNext();
					return true;
				}
				return false;
			}


			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
				
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				
			}
			
		});
	}
	
	public void next(View view){
		showNext();
	}
	
	public void pre(View view){
		showPre();
	}
	
	/**
	 * ʹ������ʶ����
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
