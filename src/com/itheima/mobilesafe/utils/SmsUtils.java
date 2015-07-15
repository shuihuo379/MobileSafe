package com.itheima.mobilesafe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Xml;

/**
 * ���ŵĹ�����
 * @author Administrator
 */
public class SmsUtils {
	private static SharedPreferences sp;
	
	/**
	 * �ṩ���ݶ��ŵĻص��ӿ�(ʵ�ֽ���)
	 */
	public interface BackUpCallBack{
		/**
		 * ��ʼ���ݵ�ʱ�����ý��ȵ����ֵ
		 * @param max �ܽ���
		 */
		public void beforeBackup(int max);

		/**
		 * ���ݹ����У����ӽ���
		 * @param progress ��ǰ����
		 */
		public void onSmsBackup(int progress);
	}
	
	public interface RestoreCallBack{
		/**
		 * ��ʼ��ԭ��ʱ��,���ý��ȵ����ֵ
		 * @param max �ܽ���
		 */
		public void beforeRestore(int max);
		
		/**
		 * ��ԭ������,���ӽ���
		 * @param progress ��ǰ����
		 */
		public void onSmsRestore(int progress);
	}
	
	
	/**
	 * �����û��Ķ��ŵ�XML�ļ���(����XML���л���д������)
	 * @param context
	 * @param BackUpCallBack ���ݶ��ŵĽӿ�
	 * @throws Exception 
	 */
	public static void backupSms(Context context,BackUpCallBack callBack) throws Exception{
		ContentResolver resolver=context.getContentResolver();
		File file=new File(Environment.getExternalStorageDirectory(),"backup.xml");
		FileOutputStream fos=new FileOutputStream(file);
		
		// ���û��Ķ���һ��һ��������������һ���ĸ�ʽд��XML�ļ���
		XmlSerializer serializer=Xml.newSerializer(); //��ȡxml�ļ��������������л�����
		serializer.setOutput(fos, "UTF-8"); //��ʼ��������
		
		serializer.startDocument("UTF-8",true); 
		serializer.startTag(null,"smss");
		
		Uri uri=Uri.parse("content://sms/");
		Cursor cursor=resolver.query(uri, new String[] { "body", "address",
				"type", "date" },null,null,null);
		
		int max=cursor.getCount();
		sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
		Editor editor=sp.edit();
		editor.putInt("max",max);
		editor.commit();
		
//		pd.setMax(max);
		callBack.beforeBackup(max);
		int process=0;
		while(cursor.moveToNext()){
			SystemClock.sleep(500);
			String body=cursor.getString(cursor.getColumnIndex("body"));
			String address=cursor.getString(cursor.getColumnIndex("address"));
			String type=cursor.getString(cursor.getColumnIndex("type"));
			String date=cursor.getString(cursor.getColumnIndex("date"));
			
			serializer.startTag(null, "sms");
			
			serializer.startTag(null, "body");
			serializer.text(body); //��ǩ�е��ı�����
			serializer.endTag(null, "body");
			
			serializer.startTag(null, "address");
			serializer.text(address);
			serializer.endTag(null, "address");

			serializer.startTag(null, "type");
			serializer.text(type);
			serializer.endTag(null, "type");

			serializer.startTag(null, "date");
			serializer.text(date);
			serializer.endTag(null, "date");
			
			serializer.endTag(null, "sms");
			
			//���ݹ�����,���ӽ���
			process++;
//			pd.setProgress(process);
			callBack.onSmsBackup(process);
		}
		
		serializer.endTag(null, "smss");
		serializer.endDocument();
		
		fos.close();
	}
	
	/**
	 * ���ŵĻ�ԭ
	 * @throws Exception
	 * @param flag �Ƿ�����ԭ���Ķ��� 
	 */
	public static void restoreSms(Context context,boolean flag,RestoreCallBack callBack) throws Exception{
		Uri uri=Uri.parse("content://sms/");
		if(flag){
			context.getContentResolver().delete(uri, null, null); //���ԭ���Ķ���
		}
		
		//pull����SD���ϵ�XML�ļ�
		XmlPullParser parser=Xml.newPullParser();
		File file=new File(Environment.getExternalStorageDirectory(),"backup.xml");
		FileInputStream fis=new FileInputStream(file);
		parser.setInput(fis,"UTF-8");
		
		String body = null;
		String date = null;
		String type = null;
		String address = null;
		
		int EventType=parser.getEventType();
		int process=0;
	
		sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
		callBack.beforeRestore(sp.getInt("max",0));
		
		while(EventType!=XmlPullParser.END_DOCUMENT){
			String tagName=parser.getName();
			switch (EventType) {
				case XmlPullParser.START_TAG:
					if("body".equals(tagName)){
						body=parser.nextText(); //��ȡbody�ڵ��е�����
					}else if("date".equals(tagName)){
						date=parser.nextText();
					}else if("type".equals(tagName)){
						type = parser.nextText();
					}else if("address".equals(tagName)){
						address = parser.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					if("sms".equals(tagName)){
						//�Ѷ��Ų��뵽ϵͳ����Ӧ����
						ContentValues values = new ContentValues();
						values.put("body",body);
						values.put("date",date);
						values.put("type",type);
						values.put("address",address);
						context.getContentResolver().insert(uri, values);
						process++;
						callBack.onSmsRestore(process);
					}
				default:
					break;
			}
			EventType=parser.next();
		}
		fis.close();
	}
}
