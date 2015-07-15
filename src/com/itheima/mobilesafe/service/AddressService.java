package com.itheima.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.NumberAddressQueryUtils;

/**
 * 监听来电号码的归属地的服务
 * 
 * @author Administrator
 */
public class AddressService extends Service {
	/**
	 * 监听去电
	 */
	private TelephonyManager tm; // 电话服务
	private MyListenerPhone listener;
	private OutCallReceiver receiver;
	private WindowManager wm; // 窗体管理者
	private View view;
	private WindowManager.LayoutParams params;
	private SharedPreferences sp;
	private long[] mHits = new long[2];  //数组下标与号码归属地的吐司点击次数有关(即双击事件)
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// 服务里面的内部类
	// 广播接收者的生命周期和服务一样
	class OutCallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 这就是我们拿到的拨出去的电话号码
			String phone = getResultData();
			// 查询数据库
			String address = NumberAddressQueryUtils.queryNumber(phone);
			// Toast.makeText(context,address,1).show();
			myToast(address);
		}
	}

	private class MyListenerPhone extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// state：状态，incomingNumber：来电号码
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: // 来电铃声响起
				// 查询数据库,获取来电地址
				String address = NumberAddressQueryUtils
						.queryNumber(incomingNumber);
				// Toast.makeText(getApplicationContext(), address, 1).show();
				myToast(address);
				break;
			case TelephonyManager.CALL_STATE_IDLE: // 电话的空闲状态:挂电话,来电拒绝等
				// 把这个View(自定义的吐司)移除,否则会出现挂断电话,显示来电或去电的归属地的吐司还停留在主界面上
				if (view != null) {
					wm.removeView(view); //挂断电话时,移除来电归属视图
				}
			default:
				break;
			}
		}
	}

	/**
	 * 自定义吐司(使用窗体管理者实现)
	 * @param address
	 */
	private void myToast(String address) {
		view = View.inflate(this, R.layout.address_show, null);
		TextView tv = (TextView) view.findViewById(R.id.tv_address);
		tv.setText(address);
		
		/**
		 * 双击点击监听事件(双击后吐司居中显示的功能)
		 */
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();
				if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					// 双击居中了...
					params.x=wm.getDefaultDisplay().getWidth()/2-view.getWidth()/2;  //得到距屏幕左边的距离
					wm.updateViewLayout(view, params); //更新布局视图
					Editor editor = sp.edit();
					editor.putInt("lastx", params.x); 
					editor.commit();  //保存居中的位置,下次显示来电信息,则让吐司居中显示 
				}
			}
		});
		
		
		/**
		 * 添加自定义吐司的触摸事件
		 */
		view.setOnTouchListener(new OnTouchListener() {
			//定义手指初始化的位置
			int startX;
			int startY;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: //手指按下屏幕
					startX=(int) event.getRawX();
					startY=(int) event.getRawY();
					System.out.println("开始的位置:"+startX+","+startY);
					break;
				case MotionEvent.ACTION_MOVE: //手指在屏幕上移动
					int newX=(int) event.getRawX();
					int newY=(int) event.getRawY();
					System.out.println("新的位置:"+newX+","+newY);
					
					int dx=newX-startX;
					int dy=newY-startY;
					System.out.println("手指的偏移量:"+dx+","+dy);
					params.x+=dx;
					params.y+=dy;
					
					//考虑边界问题
					if(params.x<0){
						//超出窗体左边
						params.x=0;
					}
					if(params.y<0){
						//超出窗体上边
						params.y=0;
					}
					if(params.x>(wm.getDefaultDisplay().getWidth()-view.getWidth())){
						//超出窗体右边
						params.x=wm.getDefaultDisplay().getWidth()-view.getWidth();
					}
					if(params.y>(wm.getDefaultDisplay().getHeight()-view.getHeight())){
						//超出窗体下边
						params.y=wm.getDefaultDisplay().getHeight()-view.getHeight();
					}
					
					//更新一个view对象的布局(因为接受到来电时,吐司已经加入到了窗体上,位置已经初始化了,当移动吐司时，需要更新)
					wm.updateViewLayout(view,params); 
					
					//重新初始化手指的开始结束的位置
					startX=(int) event.getRawX();
					startY=(int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP:  //手指离开屏幕一瞬间
					//记录控件最后一次移动后的位置
					Editor editor=sp.edit();
					editor.putInt("lastX",params.x);
					editor.putInt("lastY",params.y);
					editor.commit();
					break;

				default:
					break;
				}
				return false; //若返回值为true,则表示:事件处理完毕,不要让父控件或父布局响应触摸事件
			}
		});
		
		
		// "半透明","活力橙","卫士蓝","金属灰","苹果绿"
		int[] ids = { R.drawable.call_locate_white,
				R.drawable.call_locate_orange, R.drawable.call_locate_blue,
				R.drawable.call_locate_gray, R.drawable.call_locate_green };
		sp=getSharedPreferences("config",MODE_PRIVATE);
		view.setBackgroundResource(ids[sp.getInt("which",0)]);
		

		// 窗体的参数设置(吐司的大小以及吐司的位置)
		params = new WindowManager.LayoutParams();

		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.gravity=Gravity.TOP+Gravity.LEFT;  //设置吐司的对齐方式(与窗体左上角对齐)
		params.x=sp.getInt("lastX",100);
		params.y=sp.getInt("lastY",100); //初始位置:距左边100个像素,距顶部100个像素
		
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					 | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		//注意:之前使用的TYPE_TOAST类型天生不具备焦点,不可触摸
		//android 系统中具有电话优先级的一种窗体类型(记得添加权限)
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE; 

		wm.addView(view, params);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		// 监听来电
		listener = new MyListenerPhone();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

		// 服务创建时,用代码去注册广播接收者(动态)
		receiver = new OutCallReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		registerReceiver(receiver, filter);

		// 实例化窗体
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消监听来电
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;

		// 服务结束时,用代码取消注册广播接收者
		unregisterReceiver(receiver);
		receiver = null;
	}
}
