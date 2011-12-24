package com.dnsalias.sanja.simplecarbocalc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProdListOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION= 1;
	private static final String DATABASE_NAME= "ProdList";
	
	static final String PROD_ID= "prod_id";
	static final String PROD_CARB= "prod_carb";
	static final String PROD_LANG= "prod_lang";
	static final String PROD_NAME= "prod_name";
	
    private static final String FULLPRODLIST_TABLE_NAME= "fullprodlist";
	private static final String FULLPRODLISTNAME_TABLE_NAME= "fullprodlistname";		
	private static final String PRODLIST_TABLE_NAME= "prodlist";
	
    private static final String FULLPRODLIST_TABLE_CREATE=
                "CREATE TABLE " + FULLPRODLIST_TABLE_NAME + " (" +
                PROD_ID + " INTEGER PRIMARY KEY, " +
                PROD_CARB + " REAL);";
    private static final String FULLPRODLISTNAME_TABLE_CREATE=
            "CREATE TABLE " + FULLPRODLISTNAME_TABLE_NAME + " (" +
            PROD_ID + " INTEGER, " +
            PROD_LANG + " TEXT(2), " +
            PROD_NAME + " TEXT, " +
            "PRIMARY KEY (" + PROD_ID + "," + PROD_LANG + ")," +
            "UNIQUE (" + PROD_ID + "," + PROD_NAME + ") );";
    private static final String PRODLIST_TABLE_CREATE=
            "CREATE VIRTUAL TABLE " + PRODLIST_TABLE_NAME + " USING fts3 (" +
            PROD_NAME + " TEXT PRIMARY KEY, " +
            PROD_CARB + " REAL);";
    
	ProdListOpenHelper(Context context) {
		super(context, FULLPRODLIST_TABLE_NAME, null, DATABASE_VERSION);
    }	

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(FULLPRODLIST_TABLE_CREATE);
		db.execSQL(FULLPRODLISTNAME_TABLE_CREATE);
		db.execSQL(PRODLIST_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		 * No new version yet :)
		 */
	}

}
