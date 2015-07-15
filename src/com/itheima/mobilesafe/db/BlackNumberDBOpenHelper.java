package com.itheima.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BlackNumberDBOpenHelper extends SQLiteOpenHelper {
	private static final String DBName = "black.db";
	private static final int version = 1;

	public BlackNumberDBOpenHelper(Context context) {
		super(context, DBName, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table blacknumber (_id integer primary key autoincrement,number varchar(20),mode varchar(2))";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
