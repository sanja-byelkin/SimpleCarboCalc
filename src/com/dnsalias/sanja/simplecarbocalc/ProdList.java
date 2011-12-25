package com.dnsalias.sanja.simplecarbocalc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class ProdList
{
	private static final String LOGTAG = "SimpleCarboCalcActivity.ProdList";
	
	static final String PROD_ID= "prod_id";
	static final String PROD__ID= "_ID";
	static final String PROD_CARB= "prod_carb";
	static final String PROD_LANG= "prod_lang";
	static final String PROD_NAME= "prod_name";
	static final String PROD_LANGNAME= "prod_langname";
	
    static final String FULLPRODLIST_TABLE_NAME= "fullprodlist";
	static final String FULLPRODLISTNAME_TABLE_NAME= "fullprodlistname";		
	static final String PRODLIST_TABLE_NAME= "prodlist";
	static final String LANGLIST_TABLE_NAME= "langlist";

	private static final ProdList sInstance = new ProdList();

    public static ProdList getInstance()
    {
        return sInstance;
    }
	
	private SimpleCarboCalcActivity activity;
	private ProdListOpenHelper dbHelper;
	
    public ProdList()
    {
    	activity= null;
    	dbHelper= null;
    }
    
    public void setActivity(SimpleCarboCalcActivity activity_arg)
    {
    	activity= activity_arg;
    	dbHelper= new ProdListOpenHelper(activity);
    }
    
    /**
     * Parse 'units' config option parameters
     * @param langLine - the line to parse
     * @return true in case of error, false if everything is OK
     */  
    private boolean parseUnitsLine(String unitParam)
    {
    	int units;
		try {
			units= Integer.valueOf(unitParam).intValue();
		}
		catch (NumberFormatException ex2)
        {
            units= -1;
		}
		if (units < 0 && units > 2)
		{
			Log.e(LOGTAG, "Unrecognized 'units' config value: '" + unitParam + "'");
			return true;
		}
		activity.setUnits(units);
		return false;
    }
    
    /**
     * Parse 'lang' config option parameters
     * @param langLine - the line to parse
     * @param db - database to write changes
     * @return true in case of error, false if everything is OK
     */
    private boolean parseLangLine(String langLine, SQLiteDatabase db)
    {
    	String langsDesc[]= langLine.split("\\|");
    	if (langsDesc.length < 1)
    	{
    		Log.e(LOGTAG, "Unrecognized config lang line (|): '" + langLine + "'");
    		return true;
    	}
   		for(int i= 0; i < langsDesc.length; i++)
    	{
   			String lang[]= langsDesc[i].split("\\~");
    		if (lang.length != 2 || lang[0].length() !=2)
    		{
    			Log.e(LOGTAG, "lang.length: " + lang.length + "  lang[0].length():" +
    				lang[0].length() + "  '" + langsDesc[i] + "'");
    			Log.e(LOGTAG, "Unrecognized config lang line(~): '" + langLine + "'");
    			return true;
    		}
    		ContentValues vals= new ContentValues(2);
    		vals.put(PROD_LANG, lang[0]);
    		vals.put(PROD_LANGNAME, lang[1]);
    		db.insert(LANGLIST_TABLE_NAME, null, vals);
    	}
   		return false;
    }
    /**
     * Parse 'prod' config option parameters
     * @param langLine - the line to parse
     * @param db - database to write changes
     * @return true in case of error, false if everything is OK
     */
    private boolean parseProdLine(String prodLine, SQLiteDatabase db)
    {
    	String prodsDesc[]= prodLine.split("\\|");
    	if (prodsDesc.length < 2)
    	{
    		Log.e(LOGTAG, "Unrecognized config prod line (|): '" + prodLine + "'");
    		return true;
    	}
    	double proc;
    	long id;
    	try
    	{
    		proc= Double.valueOf(prodsDesc[0]);
    	}
    	catch (NumberFormatException ex2)
        {
    		proc= -1;
        }
    	if (proc < 0 && proc > 100)
    	{
    		Log.e(LOGTAG, "Unrecognized config prod line (0): '" + prodLine + "'");
			return true;
		}
    	{
    		ContentValues vals= new ContentValues(2);
    		vals.put(PROD_CARB, proc);
    		vals.putNull(PROD_ID);
    		db.insert(FULLPRODLIST_TABLE_NAME, null, vals);
    		Cursor res= db.rawQuery("SELECT last_insert_rowid()", null);
    		res.moveToFirst();
    		id= res.getLong(0);
    		Log.v(LOGTAG, "id: " + id + "  '" + prodLine + "'");
    		res.close();
    	}
   		for(int i= 1; i < prodsDesc.length; i++)
    	{
   			String prod[]= prodsDesc[i].split("\\~");
    		if (prod.length != 2 || prod[0].length() !=2)
    		{
        		Log.e(LOGTAG, "prod.length: " + prod.length + "  prod[0].length():" +
        				prod[0].length() + "  '" + prodsDesc[i] + "'");
    			Log.e(LOGTAG, "Unrecognized config prod line (~): '" + prodLine + "'");
    			return true;
    		}
    		ContentValues vals= new ContentValues(3);
    		vals.put(PROD_ID, id);
    		vals.put(PROD_LANG, prod[0]);
    		vals.put(PROD_NAME, prod[1]);
    		
    		db.insert(FULLPRODLISTNAME_TABLE_NAME, null, vals);
    	}
   		return false;
    }
    /**
     * Parse configuration/backup file line
     * @param line - the line t parse
     * @param db - database to write changes
     * @return true in case of error, false if everything is OK
     */
    private boolean parseConfigLine(String line, SQLiteDatabase db)
    {
    	String[] conf= TextUtils.split(line, "=");
		if (conf.length != 2)
		{
			Log.e(LOGTAG, "Unrecognized config line: '" + line + "'");
		}
		else
		{
			if (conf[0].equals("units"))
			{
				return parseUnitsLine(conf[1]);
			}
			else if (conf[0].equals("lang"))
			{
				return parseLangLine(conf[1], db);
			}
			else if (conf[0].equals("prod"))
			{
				return parseProdLine(conf[1], db);
			}
		}
		return true;
    }
    
    synchronized boolean loadBackupFile(boolean merge, InputStream inputStream)
    {
    	boolean ok= true;
    	SQLiteDatabase db= dbHelper.getWritableDatabase();
    	db.beginTransaction();
    	if (!merge)
    	{
    		// cleanup the database
    		db.execSQL("DELETE FROM " + FULLPRODLIST_TABLE_NAME + ";");
    		db.execSQL("DELETE FROM " + FULLPRODLISTNAME_TABLE_NAME+ ";");
    		db.execSQL("DELETE FROM " + PRODLIST_TABLE_NAME+ ";");
    		db.execSQL("DELETE FROM " + LANGLIST_TABLE_NAME+ ";");
    	}
    	
    	BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream));

    	try {
            String line;
            while ((line= reader.readLine()) != null)
            {
            	Log.v(LOGTAG, "read: '" + line + "'");
            	if (line.length() != 0 &&
            		line.charAt(0) != '#')
            	{
            		String[] conf= TextUtils.split(line, "=");
            		if (conf.length != 2)
            		{
            			Log.e(LOGTAG, "Unrecognized config line:'" + line + "'");
            		}
            		else
            		{
            			if (parseConfigLine(line, db))
            			{
            				Log.e(LOGTAG, "There was errors in parsing backup file. Rollback.");
            				ok= false;
            			}
            		}
            	}
            }
       		/* Create your language table */
       		Locale.getDefault().getLanguage();
       		Cursor res= db.rawQuery("SELECT " + FULLPRODLISTNAME_TABLE_NAME + "." + PROD_ID + "," +
       				PROD_CARB + "," + PROD_NAME + " FROM " + FULLPRODLIST_TABLE_NAME + "," +
       				FULLPRODLISTNAME_TABLE_NAME + " WHERE " +
       				FULLPRODLIST_TABLE_NAME + "." + PROD_ID + "=" +
       				FULLPRODLISTNAME_TABLE_NAME + "." + PROD_ID + " and " +
       				PROD_LANG + "='" + Locale.getDefault().getLanguage() + "'", null);
       		Log.v(LOGTAG, "count for '"  + Locale.getDefault().getLanguage() + "': " +
       				res.getCount());
       		if (res.getCount() == 0)
       		{
       			Log.e(LOGTAG, "Empty result for languge: '" + Locale.getDefault().getLanguage() +
       					"'");
       			ok= false;
       		}
       		else
       			while (res.moveToNext())
       			{
       				Log.v(LOGTAG, "id: " + res.getLong(0) + "  carb: " + res.getDouble(1) +
       						" name: '" + res.getString(2) + "'");
       				ContentValues vals= new ContentValues(3);
       				vals.put(PROD__ID, res.getLong(0));
       				vals.put(PROD_CARB, res.getDouble(1));
       				vals.put(PROD_NAME, res.getString(2));
       				db.replace(PRODLIST_TABLE_NAME, null, vals);
       			};
       		res.close();
            if (ok)
            	db.setTransactionSuccessful();
        }
    	catch (IOException ex)
    	{
    		Log.e(LOGTAG, "IO error: " + ex.toString());
    		ok= false;
    	}
    	finally
    	{
    		try { reader.close(); } catch (IOException ex) {}
    		db.endTransaction();
        }
    	db.execSQL("VACUUM;");
    	return !ok;
    }
    
    boolean loadInitFile(Resources resources)
    {
    	InputStream inputStream= resources.openRawResource(R.raw.initial_backup);
    	return loadBackupFile(false, inputStream);
    }
    
}
