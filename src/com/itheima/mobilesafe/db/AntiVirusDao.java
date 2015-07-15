package com.itheima.mobilesafe.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 病毒数据库的查询业务类
 * @author Administrator
 */
public class AntiVirusDao {
	/**
	 * 查询一个md5是否在病毒数据库里面存在
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
			result=true; //存在
		}
		db.close();
		cursor.close();
		return result;
	}
}
