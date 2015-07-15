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
 * 实现对本地数据库的增删改查
 * 
 * @author Administrator
 */
public class BlackNumberDao {
	private BlackNumberDBOpenHelper dbhelper;

	/**
	 * 构造方法
	 * @param context 上下文
	 */
	public BlackNumberDao(Context context) {
		dbhelper = new BlackNumberDBOpenHelper(context);
	}

	/**
	 * 查询黑名单号码是是否存在
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
	 * 查询黑名单号码是是否存在
	 * @param number
	 * @return 返回号码的拦截模式(1.电话拦截,2.短信拦截,3.全部拦截),不是黑名单号码,返回null
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
	 * 查询全部黑名单号码
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
	 * 查询部分的黑名单号码(分批查询时用到)
	 * @param offset 从哪个位置开始获取数据
	 * @param maxnumber 一次最多获取多少条记录
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
	 * 添加黑名单号码
	 * @param number
	 *            黑名单号码
	 * @param mode
	 *            拦截模式 1.电话拦截 2.短信拦截 3.全部拦截
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
	 * 修改黑名单号码的拦截模式
	 * 
	 * @param number
	 *            要修改的黑名单号码
	 * @param newmode
	 *            新的拦截模式
	 */
	public void update(String number, String newmode) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newmode);
		db.update("blacknumber", values, "number=?", new String[] { number });
		db.close();
	}

	/**
	 * 删除黑名单号码
	 * 
	 * @param number
	 *            要删除的黑名单号码
	 */
	public void delete(String number) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.delete("blacknumber", "number=?", new String[] { number });
		db.close();
	}
}
