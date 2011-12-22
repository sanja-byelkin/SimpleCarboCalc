package com.dnsalias.sanja.simplecarbocalc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FullProdListNameOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION= 1;
    private static final String FULLPRODLISTNAME_TABLE_NAME= "fullprodlistname";
    private static final String FULLPRODLISTNAME_TABLE_CREATE=
                "CREATE TABLE " + FULLPRODLISTNAME_TABLE_NAME + " (" +
                FullProdList.PROD_ID + " INTEGER, " +
                FullProdList.PROD_LANG + " TEXT(2), " +
                FullProdList.PROD_NAME + " TEXT, " +
                "PRIMARY KEY (" + FullProdList.PROD_ID + "," + FullProdList.PROD_LANG + ")," +
                "UNIQUE (" + FullProdList.PROD_ID + "," + FullProdList.PROD_NAME + ") );";
    FullProdListNameOpenHelper(Context context) {
		super(context, FULLPRODLISTNAME_TABLE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(FULLPRODLISTNAME_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(FULLPRODLISTNAME_TABLE_CREATE);
	}

}
