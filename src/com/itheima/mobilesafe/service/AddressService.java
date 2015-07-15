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
 * �����������Ĺ����صķ���
 * 
 * @author Administrator
 */
public class AddressService extends Service {
	/**
	 * ����ȥ��
	 */
	private TelephonyManager tm; // �绰����
	private MyListenerPhone listener;
	private OutCallReceiver receiver;
	private WindowManager wm; // ���������
	private View view;
	private WindowManager.LayoutParams params;
	private SharedPreferences sp;
	private long[] mHits = new long[2];  //�����±����������ص���˾��������й�(��˫���¼�)
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// ����������ڲ���
	// �㲥�����ߵ��������ںͷ���һ��
	class OutCallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// ����������õ��Ĳ���ȥ�ĵ绰����
			String phone = getResultData();
			// ��ѯ���ݿ�
			String address = NumberAddressQueryUtils.queryNumber(phone);
			// Toast.makeText(context,address,1).show();
			myToast(address);
		}
	}

	private class MyListenerPhone extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// state��״̬��incomingNumber���������
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: // ������������
				// ��ѯ���ݿ�,��ȡ�����ַ
				String address = NumberAddressQueryUtils
						.queryNumber(incomingNumber);
				// Toast.makeText(getApplicationContext(), address, 1).show();
				myToast(address);
				break;
			case TelephonyManager.CALL_STATE_IDLE: // �绰�Ŀ���״̬:�ҵ绰,����ܾ���
				// �����View(�Զ������˾)�Ƴ�,�������ֹҶϵ绰,��ʾ�����ȥ��Ĺ����ص���˾��ͣ������������
				if (view != null) {
					wm.removeView(view); //�Ҷϵ绰ʱ,�Ƴ����������ͼ
				}
			default:
				break;
			}
		}
	}

	/**
	 * �Զ�����˾(ʹ�ô��������ʵ��)
	 * @param address
	 */
	private void myToast(String address) {
		view = View.inflate(this, R.layout.address_show, null);
		TextView tv = (TextView) view.findViewById(R.id.tv_address);
		tv.setText(address);
		
		/**
		 * ˫����������¼�(˫������˾������ʾ�Ĺ���)
		 */
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();
				if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					// ˫��������...
					params.x=wm.getDefaultDisplay().getWidth()/2-view.getWidth()/2;  //�õ�����Ļ��ߵľ���
					wm.updateViewLayout(view, params); //���²�����ͼ
					Editor editor = sp.edit();
					editor.putInt("lastx", params.x); 
					editor.commit();  //������е�λ��,�´���ʾ������Ϣ,������˾������ʾ 
				}
			}
		});
		
		
		/**
		 * ����Զ�����˾�Ĵ����¼�
		 */
		view.setOnTouchListener(new OnTouchListener() {
			//������ָ��ʼ����λ��
			int startX;
			int startY;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: //��ָ������Ļ
					startX=(int) event.getRawX();
					startY=(int) event.getRawY();
					System.out.println("��ʼ��λ��:"+startX+","+startY);
					break;
				case MotionEvent.ACTION_MOVE: //��ָ����Ļ���ƶ�
					int newX=(int) event.getRawX();
					int newY=(int) event.getRawY();
					System.out.println("�µ�λ��:"+newX+","+newY);
					
					int dx=newX-startX;
					int dy=newY-startY;
					System.out.println("��ָ��ƫ����:"+dx+","+dy);
					params.x+=dx;
					params.y+=dy;
					
					//���Ǳ߽�����
					if(params.x<0){
						//�����������
						params.x=0;
					}
					if(params.y<0){
						//���������ϱ�
						params.y=0;
					}
					if(params.x>(wm.getDefaultDisplay().getWidth()-view.getWidth())){
						//���������ұ�
						params.x=wm.getDefaultDisplay().getWidth()-view.getWidth();
					}
					if(params.y>(wm.getDefaultDisplay().getHeight()-view.getHeight())){
						//���������±�
						params.y=wm.getDefaultDisplay().getHeight()-view.getHeight();
					}
					
					//����һ��view����Ĳ���(��Ϊ���ܵ�����ʱ,��˾�Ѿ����뵽�˴�����,λ���Ѿ���ʼ����,���ƶ���˾ʱ����Ҫ����)
					wm.updateViewLayout(view,params); 
					
					//���³�ʼ����ָ�Ŀ�ʼ������λ��
					startX=(int) event.getRawX();
					startY=(int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP:  //��ָ�뿪��Ļһ˲��
					//��¼�ؼ����һ���ƶ����λ��
					Editor editor=sp.edit();
					editor.putInt("lastX",params.x);
					editor.putInt("lastY",params.y);
					editor.commit();
					break;

				default:
					break;
				}
				return false; //������ֵΪtrue,���ʾ:�¼��������,��Ҫ�ø��ؼ��򸸲�����Ӧ�����¼�
			}
		});
		
		
		// "��͸��","������","��ʿ��","������","ƻ����"
		int[] ids = { R.drawable.call_locate_white,
				R.drawable.call_locate_orange, R.drawable.call_locate_blue,
				R.drawable.call_locate_gray, R.drawable.call_locate_green };
		sp=getSharedPreferences("config",MODE_PRIVATE);
		view.setBackgroundResource(ids[sp.getInt("which",0)]);
		

		// ����Ĳ�������(��˾�Ĵ�С�Լ���˾��λ��)
		params = new WindowManager.LayoutParams();

		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.gravity=Gravity.TOP+Gravity.LEFT;  //������˾�Ķ��뷽ʽ(�봰�����ϽǶ���)
		params.x=sp.getInt("lastX",100);
		params.y=sp.getInt("lastY",100); //��ʼλ��:�����100������,�ඥ��100������
		
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					 | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		//ע��:֮ǰʹ�õ�TYPE_TOAST�����������߱�����,���ɴ���
		//android ϵͳ�о��е绰���ȼ���һ�ִ�������(�ǵ����Ȩ��)
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE; 

		wm.addView(view, params);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		// ��������
		listener = new MyListenerPhone();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

		// ���񴴽�ʱ,�ô���ȥע��㲥������(��̬)
		receiver = new OutCallReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		registerReceiver(receiver, filter);

		// ʵ��������
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ȡ����������
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;

		// �������ʱ,�ô���ȡ��ע��㲥������
		unregisterReceiver(receiver);
		receiver = null;
	}
}
