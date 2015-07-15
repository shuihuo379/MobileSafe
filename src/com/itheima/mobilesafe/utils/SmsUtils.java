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
 * 短信的工具类
 * @author Administrator
 */
public class SmsUtils {
	private static SharedPreferences sp;
	
	/**
	 * 提供备份短信的回调接口(实现解耦)
	 */
	public interface BackUpCallBack{
		/**
		 * 开始备份的时候，设置进度的最大值
		 * @param max 总进度
		 */
		public void beforeBackup(int max);

		/**
		 * 备份过程中，增加进度
		 * @param progress 当前进度
		 */
		public void onSmsBackup(int progress);
	}
	
	public interface RestoreCallBack{
		/**
		 * 开始还原的时候,设置进度的最大值
		 * @param max 总进度
		 */
		public void beforeRestore(int max);
		
		/**
		 * 还原过程中,增加进度
		 * @param progress 当前进度
		 */
		public void onSmsRestore(int progress);
	}
	
	
	/**
	 * 备份用户的短信到XML文件中(利用XML序列化器写入数据)
	 * @param context
	 * @param BackUpCallBack 备份短信的接口
	 * @throws Exception 
	 */
	public static void backupSms(Context context,BackUpCallBack callBack) throws Exception{
		ContentResolver resolver=context.getContentResolver();
		File file=new File(Environment.getExternalStorageDirectory(),"backup.xml");
		FileOutputStream fos=new FileOutputStream(file);
		
		// 把用户的短信一条一条读出来，按照一定的格式写到XML文件里
		XmlSerializer serializer=Xml.newSerializer(); //获取xml文件的生成器（序列化器）
		serializer.setOutput(fos, "UTF-8"); //初始化生成器
		
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
			serializer.text(body); //标签中的文本内容
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
			
			//备份过程中,增加进度
			process++;
//			pd.setProgress(process);
			callBack.onSmsBackup(process);
		}
		
		serializer.endTag(null, "smss");
		serializer.endDocument();
		
		fos.close();
	}
	
	/**
	 * 短信的还原
	 * @throws Exception
	 * @param flag 是否清理原来的短信 
	 */
	public static void restoreSms(Context context,boolean flag,RestoreCallBack callBack) throws Exception{
		Uri uri=Uri.parse("content://sms/");
		if(flag){
			context.getContentResolver().delete(uri, null, null); //清除原来的短信
		}
		
		//pull解析SD卡上的XML文件
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
						body=parser.nextText(); //获取body节点中的内容
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
						//把短信插入到系统短信应用中
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
