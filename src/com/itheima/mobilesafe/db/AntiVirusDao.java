package com.itheima.mobilesafe.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * �������ݿ�Ĳ�ѯҵ����
 * @author Administrator
 */
public class AntiVirusDao {
	/**
	 * ��ѯһ��md5�Ƿ��ڲ������ݿ��������
	 * @param md5 
	 * @return
	 */
	public static boolean isVirus(String md5){
		String path = "/data/data/com.itheima.mobilesafe/files/antivirus.db";
		boolean result = false;
		SQLiteDatabase db=SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		String sql="select * from datable where md5=?";
		Cursor cursor=db.rawQuery(sql, new String[]{md5});
		if(cursor.moveToNext()){
			result=true; //����
		}
		db.close();
		cursor.close();
		return result;
	}
}
