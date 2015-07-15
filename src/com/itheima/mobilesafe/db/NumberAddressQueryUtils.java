package com.itheima.mobilesafe.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumberAddressQueryUtils {
	private static String path = "data/data/com.itheima.mobilesafe/files/address.db";
	/**
	 * 传一个号码进来，返回一归属地回去
	 * @param number
	 * @return
	 */
	public static String queryNumber(String number) {
		String address="";  //默认查询地址为空
		// path 把address.db这个数据库拷贝到data/data/《包名》/files/address.db
		//打开一个现有的数据库
		SQLiteDatabase db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
		
		if(number.matches("^1[34568]\\d{9}$")){
			String sql="select location from data2 where id = (select outkey from data1 where id = ?)";
			Cursor cursor=db.rawQuery(sql,new String[]{number.substring(0, 7)});
			System.out.println(cursor.getColumnIndex("location")+"--->");  //结果为0
			while(cursor.moveToNext()){
				String location=cursor.getString(cursor.getColumnIndex("location"));
				address=location;
			}
			cursor.close();
		}else{
			//其它电话号码
			switch (number.length()) {
				case 3:
					address = "匪警号码";  // 110
					break;
				case 4:
					address = "模拟器";  // 5554
					break;
				case 5:
					address = "客服电话";  // 10086
					break;
				case 7:
					address = "本地号码";
					break;
				case 8:
					address = "本地号码";
					break;
				default:
					// 处理长途电话 10
					if (number.length() > 10 && number.startsWith("0")) {
						// 010-59790386
						Cursor cursor = db.rawQuery(
								"select location from data2 where area = ?",
								new String[] { number.substring(1, 3) });

						while (cursor.moveToNext()) {
							String location = cursor.getString(cursor.getColumnIndex("location"));
							address = location.substring(0, location.length()-2); //取出末尾的联通或移动或电信等字段
						}
						cursor.close();

						// 0855-59790386
						cursor = db.rawQuery(
								"select location from data2 where area = ?",
								new String[] { number.substring(1, 4) });
						while (cursor.moveToNext()) {
							String location = cursor.getString(cursor.getColumnIndex("location"));
							address = location.substring(0, location.length() - 2);
						}
					}

					break;	
			}
		}
		return address;
	}
}
