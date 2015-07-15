package com.itheima.mobilesafe.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import com.itheima.mobilesafe.domain.BlackNumberInfo;

/**
 * ʵ�ֶԱ������ݿ����ɾ�Ĳ�
 * 
 * @author Administrator
 */
public class BlackNumberDao {
	private BlackNumberDBOpenHelper dbhelper;

	/**
	 * ���췽��
	 * @param context ������
	 */
	public BlackNumberDao(Context context) {
		dbhelper = new BlackNumberDBOpenHelper(context);
	}

	/**
	 * ��ѯ�������������Ƿ����
	 * @param number
	 * @return
	 */
	public boolean find(String number) {
		boolean result = false;
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from blacknumber where number=?",
				new String[] { number });
		if (cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * ��ѯ�������������Ƿ����
	 * @param number
	 * @return ���غ��������ģʽ(1.�绰����,2.��������,3.ȫ������),���Ǻ���������,����null
	 */
	public String findMode(String number) {
		String result = null;
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select mode from blacknumber where number=?",
				new String[] { number });
		if (cursor.moveToNext()) {
			result = cursor.getString(cursor.getColumnIndex("mode"));
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * ��ѯȫ������������
	 * @return
	 */
	public List<BlackNumberInfo> findAll() {
		SystemClock.sleep(5000);
		List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select number,mode from blacknumber order by _id desc", null);
		while (cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(cursor.getColumnIndex("number"));
			String mode = cursor.getString(cursor.getColumnIndex("mode"));
			info.setMode(mode);
			info.setNumber(number);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * ��ѯ���ֵĺ���������(������ѯʱ�õ�)
	 * @param offset ���ĸ�λ�ÿ�ʼ��ȡ����
	 * @param maxnumber һ������ȡ��������¼
	 * @return
	 */
	public List<BlackNumberInfo> findPart(int offset, int maxnumber) {
		SystemClock.sleep(500);
		List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String sql="select number,mode from blacknumber order by _id desc limit ? offset ?";
		Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(maxnumber),String.valueOf(offset)});
		while (cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(cursor.getColumnIndex("number"));
			String mode = cursor.getString(cursor.getColumnIndex("mode"));
			info.setMode(mode);
			info.setNumber(number);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * ��Ӻ���������
	 * @param number
	 *            ����������
	 * @param mode
	 *            ����ģʽ 1.�绰���� 2.�������� 3.ȫ������
	 */
	public void add(String number, String mode) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		db.insert("blacknumber", null, values);
		db.close();
	}

	/**
	 * �޸ĺ��������������ģʽ
	 * 
	 * @param number
	 *            Ҫ�޸ĵĺ���������
	 * @param newmode
	 *            �µ�����ģʽ
	 */
	public void update(String number, String newmode) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newmode);
		db.update("blacknumber", values, "number=?", new String[] { number });
		db.close();
	}

	/**
	 * ɾ������������
	 * 
	 * @param number
	 *            Ҫɾ���ĺ���������
	 */
	public void delete(String number) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.delete("blacknumber", "number=?", new String[] { number });
		db.close();
	}
}
