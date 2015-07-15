package com.itheima.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ApplockDBOpenHelper extends SQLiteOpenHelper {
	private static final String DBName = "applock.db";
	private static final int version = 1;

	public ApplockDBOpenHelper(Context context) {
		super(context, DBName, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table applock (_id integer primary key autoincrement,packname varchar(20))";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
