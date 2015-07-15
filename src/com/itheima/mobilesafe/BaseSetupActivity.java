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
	private GestureDetector detector;  //定义一个手势识别器
	protected SharedPreferences sp;
	
	public abstract void showNext();  //定义两个抽象方法,提供给子类去实现,以完成不同的功能
	public abstract void showPre();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp=getSharedPreferences("config",MODE_PRIVATE);
		
		detector=new GestureDetector(this,new OnGestureListener(){
			/**
			 * 当我们手指在上面滑动的时候回调
			 */
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				//屏蔽斜滑这种情况
				if(Math.abs(e2.getRawY()-e1.getRawY())>100){
					Toast.makeText(getApplicationContext(),"不能这样滑",Toast.LENGTH_SHORT).show();
					return true;
				}
				//屏蔽在X轴滑动很慢的情况
				if(Math.abs(velocityX)<200){
					Toast.makeText(getApplicationContext(),"滑动太慢了",Toast.LENGTH_SHORT).show();
					return true;
				}
				
				if((e2.getRawX()-e1.getRawX())>150){
					System.out.println("显示上一个页面,从左向右滑动");
					showPre(); //抽象方法,依据不同的子类实现不同的功能
					return true;
				}
				if((e1.getRawX()-e2.getRawX())>150){
					System.out.println("显示下一个页面,从右向左滑动");
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
	 * 使用手势识别器
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
