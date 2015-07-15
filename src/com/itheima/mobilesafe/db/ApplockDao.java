package com.itheima.mobilesafe.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ApplockDao {
	private ApplockDBOpenHelper dbhelper;
	private Context context;
	/**
	 * ���췽��
	 * @param context ������
	 */
	public ApplockDao(Context context) {
		dbhelper = new ApplockDBOpenHelper(context);
		this.context=context;
	}
	
	/**
	 * ���һ��Ҫ�����İ���
	 * @param packname
	 */
	public void add(String packname){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("packname", packname);
		db.insert("applock", null, values);
		db.close();
		
		Intent intent=new Intent();
		intent.setAction("com.itheima.mobilesafe.applockchange");
		context.sendBroadcast(intent);
	}
	
	/**
	 * ɾ��һ��Ҫ����Ӧ�ó���İ���
	 * @param packname
	 */
	public void delete(String packname){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.delete("applock", "packname=?", new String[]{packname});
		db.close();
		
		Intent intent=new Intent();
		intent.setAction("com.itheima.mobilesafe.applockchange");
		context.sendBroadcast(intent);
	}
	
	/**
	 * ��ѯһ��������������¼�Ƿ����
	 * @param packname
	 * @return
	 */
	public boolean find(String packname){
		boolean result = false;
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("applock", null, "packname=?", new String[]{packname}, null, null, null);
		if(cursor.moveToNext()){
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	
	/**
	 * ��ѯȫ���İ���
	 * @param packname
	 * @return
	 */
	public List<String> findAll(){
		List<String> protectPacknames=new ArrayList<String>();
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("applock",new String[]{"packname"},null,null, null, null, null);
		while(cursor.moveToNext()){
			protectPacknames.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return protectPacknames;
	}
}
