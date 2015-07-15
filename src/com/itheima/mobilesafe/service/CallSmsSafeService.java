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
 * �������绰��������ط���
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
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // ���ù㲥������ȼ�
		registerReceiver(receiver, filter); // �������������ط�����ʱ��̬ע��㲥�¼�
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
			System.out.println("���ŵ�����...");
			// ��鷢�����Ƿ��Ǻ��������룬���ö�������ȫ������
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object obj : objs) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				String sender = smsMessage.getOriginatingAddress(); // �õ����ŷ�����
				String mode = dao.findMode(sender);
				if ("2".equals(mode) || "3".equals(mode)) {
					System.out.println("���ض���...");
					abortBroadcast();
				}

				/**
				 * //����������ʾ����(��ʵ���ѯ���ݿ�) String body =
				 * smsMessage.getMessageBody(); if(body.contains("fapiao")){
				 * //���Էִʼ��� System.out.println("���ط�Ʊ����"); abortBroadcast(); }
				 */
			}
		}
	}

	private class MyPhoneListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: // ����״̬
				String mode = dao.findMode(incomingNumber);
				if ("1".equals(mode) || "3".equals(mode)) {
					System.out.println("�Ҷϵ绰...");
					// �۲���м�¼���ݿ����ݵı仯(ʹ�����ݹ۲���ʵ��)
					Uri uri = Uri.parse("content://call_log/calls");
					getContentResolver().registerContentObserver(uri, true,
							new CallLogObserver(incomingNumber, new Handler()));
					endCall(); //����һ�������������е� Զ�̷���ķ��� ,�������ú󣬺��м�¼���ܻ�û������
					
					// ����һ��Ӧ�ó�����ϵ�˵�Ӧ�õ�˽�����ݿ�
					// deleteCallLog(incomingNumber);
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * �Ҷϵ绰��ʵ��
	 */
	private void endCall() {
		// IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
		// ����ServiceManager�౻������
		// ���÷�����Ʒ���������ServiceManager�еķ���
		try {
			Class clazz = CallSmsSafeService.class.getClassLoader().loadClass(
						"android.os.ServiceManager");
			Method method = clazz.getDeclaredMethod("getService", String.class); // ֻ��1������,����ΪString
			IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE); // ������Ϊ��̬��,���һ���������ó�null,�������θ��Ϸ��䷽���ĸ�������
			ITelephony.Stub.asInterface(ibinder).endCall(); // AIDL����Զ�̽��̵ķ���
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����¼�����ݹ۲���,���������¼�ı仯,�Ӷ������ص绰�Ĺ�����ɾ�������¼
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
			System.out.println("���ݿ�����ݱ仯��,�����˺��м�¼...");
			deleteCallLog(incomingNumber);
			super.onChange(selfChange);
		}
	}

	/**
	 * ���ú��м�¼�������ṩ��ɾ�����м�¼
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		Uri uri = Uri.parse("content://call_log/calls"); // ���м�¼URI��·��
		resolver.delete(uri, "number=?", new String[] { incomingNumber });
	}
}
