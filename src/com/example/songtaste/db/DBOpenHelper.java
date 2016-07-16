package com.example.songtaste.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

	private static final String DBNAME = "songlist.db";
	private static final int VERSION = 1;

	public DBOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS netsonglist ("
				+ "id integer primary key autoincrement,"
				+ "songName varchar(40), "
				+ "songID varchar(10),"
				+ "userName varchar(20),"
				+ "userPic varchar(15),"
				+ "currentNum varchar(10))");
		db.execSQL("CREATE TABLE IF NOT EXISTS lovesonglist ("
				+ "id integer primary key autoincrement,"
				+ "songName varchar(40), "
				+ "songID varchar(10),"
				+ "userName varchar(20),"
				+ "userPic varchar(15))");//歌曲所在的页面数值

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL("DROP TABLE IF EXISTS netsonglist");
		db.execSQL("DROP TABLE IF EXISTS lovesonglist");
		onCreate(db);

	}

}
