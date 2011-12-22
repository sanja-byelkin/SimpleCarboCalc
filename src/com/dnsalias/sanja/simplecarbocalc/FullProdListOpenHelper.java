package com.dnsalias.sanja.simplecarbocalc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FullProdListOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION= 1;
    private static final String FULLPRODLIST_TABLE_NAME= "fullprodlist";
    private static final String FULLPRODLIST_TABLE_CREATE=
                "CREATE TABLE " + FULLPRODLIST_TABLE_NAME + " (" +
                FullProdList.PROD_ID + " INTEGER PRIMARY KEY, " +
                FullProdList.PROD_CARB + " REAL);";
	FullProdListOpenHelper(Context context) {
		super(context, FULLPRODLIST_TABLE_NAME, null, DATABASE_VERSION);
    }	

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(FULLPRODLIST_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		 * No new version yet :)
		 */
	}

}
