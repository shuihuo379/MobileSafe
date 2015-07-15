package com.itheima.mobilesafe.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.itheima.mobilesafe.db.BlackNumberDao;

/**
 * 黑名单电话与短信拦截服务
 * @author Administrator
 */
public class CallSmsSafeService extends Service {
	private InnerSmsReceiver receiver;
	private BlackNumberDao dao;
	private TelephonyManager tm;
	private MyPhoneListener listener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		dao = new BlackNumberDao(this);

		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

		receiver = new InnerSmsReceiver();
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // 设置广播最高优先级
		registerReceiver(receiver, filter); // 黑名单短信拦截服务开启时动态注册广播事件
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver = null;
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		super.onDestroy();
	}

	private class InnerSmsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("短信到来了...");
			// 检查发件人是否是黑名单号码，设置短信拦截全部拦截
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object obj : objs) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				String sender = smsMessage.getOriginatingAddress(); // 得到短信发件人
				String mode = dao.findMode(sender);
				if ("2".equals(mode) || "3".equals(mode)) {
					System.out.println("拦截短信...");
					abortBroadcast();
				}

				/**
				 * //智能拦截演示代码(真实须查询数据库) String body =
				 * smsMessage.getMessageBody(); if(body.contains("fapiao")){
				 * //语言分词技术 System.out.println("拦截发票短信"); abortBroadcast(); }
				 */
			}
		}
	}

	private class MyPhoneListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: // 铃响状态
				String mode = dao.findMode(incomingNumber);
				if ("1".equals(mode) || "3".equals(mode)) {
					System.out.println("挂断电话...");
					// 观察呼叫记录数据库内容的变化(使用内容观察者实现)
					Uri uri = Uri.parse("content://call_log/calls");
					getContentResolver().registerContentObserver(uri, true,
							new CallLogObserver(incomingNumber, new Handler()));
					endCall(); //另外一个进程里面运行的 远程服务的方法 ,方法调用后，呼叫记录可能还没有生成
					
					// 另外一个应用程序联系人的应用的私有数据库
					// deleteCallLog(incomingNumber);
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 挂断电话的实现
	 */
	private void endCall() {
		// IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
		// 其中ServiceManager类被隐藏了
		// 利用反射机制反射隐藏类ServiceManager中的方法
		try {
			Class clazz = CallSmsSafeService.class.getClassLoader().loadClass(
						"android.os.ServiceManager");
			Method method = clazz.getDeclaredMethod("getService", String.class); // 只有1个参数,类型为String
			IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE); // 若方法为静态的,则第一个参数设置成null,后面依次跟上反射方法的各个参数
			ITelephony.Stub.asInterface(ibinder).endCall(); // AIDL调用远程进程的方法
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 来电记录的内容观察者,监视来电记录的变化,从而在拦截电话的过程中删除来电记录
	 * @author Administrator
	 */
	private class CallLogObserver extends ContentObserver {
		private String incomingNumber;

		public CallLogObserver(String incomingNumber, Handler handler) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}

		@Override
		public void onChange(boolean selfChange) {
			System.out.println("数据库的内容变化了,产生了呼叫记录...");
			deleteCallLog(incomingNumber);
			super.onChange(selfChange);
		}
	}

	/**
	 * 利用呼叫记录的内容提供者删除呼叫记录
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		Uri uri = Uri.parse("content://call_log/calls"); // 呼叫记录URI的路径
		resolver.delete(uri, "number=?", new String[] { incomingNumber });
	}
}
