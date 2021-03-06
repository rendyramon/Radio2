package org.oucho.radio2.db;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.oucho.radio2.MainActivity;

public class RadiosDatabase extends SQLiteOpenHelper {

	public static final String DB_NAME = "WebRadio";
	public static final String TABLE_NAME =  "WebRadio";
	private static final int DB_VERSION = 1;
	
	public RadiosDatabase() {
		super(MainActivity.getContext(), DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE" + " " + TABLE_NAME + " " + "(url TEXT PRIMARY KEY, name TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This constructor is intentionally empty, pourquoi ? parce que !
	}

}
