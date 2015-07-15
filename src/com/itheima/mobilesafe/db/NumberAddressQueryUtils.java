package com.itheima.mobilesafe.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumberAddressQueryUtils {
	private static String path = "data/data/com.itheima.mobilesafe/files/address.db";
	/**
	 * ��һ���������������һ�����ػ�ȥ
	 * @param number
	 * @return
	 */
	public static String queryNumber(String number) {
		String address="";  //Ĭ�ϲ�ѯ��ַΪ��
		// path ��address.db������ݿ⿽����data/data/��������/files/address.db
		//��һ�����е����ݿ�
		SQLiteDatabase db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
		
		if(number.matches("^1[34568]\\d{9}$")){
			String sql="select location from data2 where id = (select outkey from data1 where id = ?)";
			Cursor cursor=db.rawQuery(sql,new String[]{number.substring(0, 7)});
			System.out.println(cursor.getColumnIndex("location")+"--->");  //���Ϊ0
			while(cursor.moveToNext()){
				String location=cursor.getString(cursor.getColumnIndex("location"));
				address=location;
			}
			cursor.close();
		}else{
			//�����绰����
			switch (number.length()) {
				case 3:
					address = "�˾�����";  // 110
					break;
				case 4:
					address = "ģ����";  // 5554
					break;
				case 5:
					address = "�ͷ��绰";  // 10086
					break;
				case 7:
					address = "���غ���";
					break;
				case 8:
					address = "���غ���";
					break;
				default:
					// ����;�绰 10
					if (number.length() > 10 && number.startsWith("0")) {
						// 010-59790386
						Cursor cursor = db.rawQuery(
								"select location from data2 where area = ?",
								new String[] { number.substring(1, 3) });

						while (cursor.moveToNext()) {
							String location = cursor.getString(cursor.getColumnIndex("location"));
							address = location.substring(0, location.length()-2); //ȡ��ĩβ����ͨ���ƶ�����ŵ��ֶ�
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
