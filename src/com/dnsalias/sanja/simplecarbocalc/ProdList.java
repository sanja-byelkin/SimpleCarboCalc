package com.dnsalias.sanja.simplecarbocalc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class ProdList {
	private static final String LOGTAG = "SimpleCarboCalcActivity.ProdList";

	/**
	 * Field names
	 */
	static final String PROD_ID = "prod_id";
	static final String PROD__ID = "_id";
	static final String PROD_CARB = "prod_carb";
	static final String PROD_LANG = "prod_lang";
	static final String PROD_NAME = "prod_name";
	static final String PROD_LANGNAME = "prod_langname";

	/**
	 * tables names
	 */
	/* PROD_ID, PROD_CARB */
	static final String FULLPRODLIST_TABLE_NAME = "fullprodlist";
	/* PROD_ID, PROD_LANG, PROD_NAME */
	static final String FULLPRODLISTNAME_TABLE_NAME = "fullprodlistname";
	/* PROD__ID, PROD_CARB, PROD_NAME (for current locale) */
	static final String PRODLIST_TABLE_NAME = "prodlist";
	/* PROD_LANG, PROD_LANGNAME */
	static final String LANGLIST_TABLE_NAME = "langlist";

	private static final ProdList sInstance = new ProdList();

	/**
	 * Returns the only instance of database management object
	 * 
	 * @return database management object
	 */
	public static ProdList getInstance() {
		return sInstance;
	}

	/**
	 * Connection to activity (for configuration and context).
	 */
	private SimpleCarboCalcActivity mActivity = null;
	/**
	 * Connection to database
	 */
	private ProdListOpenHelper mDbHelper = null;
	/**
	 * State of external storage (for backup).
	 */
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;
	private File mExternalMyDirectory = null;

	/**
	 * empty constructor
	 */
	public ProdList() {
		mActivity = null;
		mDbHelper = null;
	}

	/**
	 * Assign activity
	 * 
	 * @param activity_arg
	 *            reference to activity the database connected to
	 */
	public void setActivity(SimpleCarboCalcActivity activity_arg) {
		mActivity = activity_arg;
		mDbHelper = new ProdListOpenHelper(mActivity);
	}

	/**
	 * Parse 'units' config option parameters
	 * 
	 * @param langLine
	 *            - the line to parse
	 * @return true in case of error, false if everything is OK
	 */
	private boolean parseUnitsLine(String unitParam) {
		int units;
		try {
			units = Integer.valueOf(unitParam).intValue();
		} catch (NumberFormatException ex2) {
			units = -1;
		}
		if (units < 0 && units > 2) {
			Log.e(LOGTAG, "Unrecognized 'units' config value: '" + unitParam
					+ "'");
			return true;
		}
		mActivity.setUnits(units);
		return false;
	}

	/**
	 * Parse 'lang' config option parameters
	 * 
	 * @param langLine
	 *            - the line to parse
	 * @param db
	 *            - database to write changes
	 * @return true in case of error, false if everything is OK
	 */
	private boolean parseLangLine(String langLine, SQLiteDatabase db) {
		String langsDesc[] = langLine.split("\\|");
		if (langsDesc.length < 1) {
			Log.e(LOGTAG, "Unrecognized config lang line (|): '" + langLine
					+ "'");
			return true;
		}
		for (int i = 0; i < langsDesc.length; i++) {
			String lang[] = langsDesc[i].split("\\~");
			if (lang.length != 2 || lang[0].length() != 2) {
				Log.e(LOGTAG, "lang.length: " + lang.length
						+ "  lang[0].length():" + lang[0].length() + "  '"
						+ langsDesc[i] + "'");
				Log.e(LOGTAG, "Unrecognized config lang line(~): '" + langLine
						+ "'");
				return true;
			}
			ContentValues vals = new ContentValues(2);
			vals.put(PROD_LANG, lang[0]);
			vals.put(PROD_LANGNAME, lang[1]);
			db.insert(LANGLIST_TABLE_NAME, null, vals);
		}
		return false;
	}

	/**
	 * Parse 'prod' config option parameters
	 * 
	 * @param langLine
	 *            - the line to parse
	 * @param db
	 *            - database to write changes
	 * @return true in case of error, false if everything is OK
	 */
	private boolean parseProdLine(String prodLine, SQLiteDatabase db) {
		String prodsDesc[] = prodLine.split("\\|");
		if (prodsDesc.length < 2) {
			Log.e(LOGTAG, "Unrecognized config prod line (|): '" + prodLine
					+ "'");
			return true;
		}
		double proc;
		long id;
		try {
			proc = Double.valueOf(prodsDesc[0]);
		} catch (NumberFormatException ex2) {
			proc = -1;
		}
		if (proc < 0 && proc > 100) {
			Log.e(LOGTAG, "Unrecognized config prod line (0): '" + prodLine
					+ "'");
			return true;
		}
		{
			ContentValues vals = new ContentValues(2);
			vals.put(PROD_CARB, proc);
			vals.putNull(PROD_ID);
			db.insert(FULLPRODLIST_TABLE_NAME, null, vals);
			Cursor res = db.rawQuery("SELECT last_insert_rowid()", null);
			res.moveToFirst();
			id = res.getLong(0);
			Log.v(LOGTAG, "id: " + id + "  '" + prodLine + "'");
			res.close();
		}
		for (int i = 1; i < prodsDesc.length; i++) {
			String prod[] = prodsDesc[i].split("\\~");
			if (prod.length != 2 || prod[0].length() != 2) {
				Log.e(LOGTAG, "prod.length: " + prod.length
						+ "  prod[0].length():" + prod[0].length() + "  '"
						+ prodsDesc[i] + "'");
				Log.e(LOGTAG, "Unrecognized config prod line (~): '" + prodLine
						+ "'");
				return true;
			}
			ContentValues vals = new ContentValues(3);
			vals.put(PROD_ID, id);
			vals.put(PROD_LANG, prod[0]);
			vals.put(PROD_NAME, prod[1]);

			db.insert(FULLPRODLISTNAME_TABLE_NAME, null, vals);
		}
		return false;
	}

	/**
	 * Parse configuration/backup file line
	 * 
	 * @param line
	 *            - the line t parse
	 * @param db
	 *            - database to write changes
	 * @return true in case of error, false if everything is OK
	 */
	private boolean parseConfigLine(String line, SQLiteDatabase db) {
		String[] conf = TextUtils.split(line, "=");
		if (conf.length != 2) {
			Log.e(LOGTAG, "Unrecognized config line: '" + line + "'");
		} else {
			if (conf[0].equals("units")) {
				return parseUnitsLine(conf[1]);
			} else if (conf[0].equals("lang")) {
				return parseLangLine(conf[1], db);
			} else if (conf[0].equals("prod")) {
				return parseProdLine(conf[1], db);
			}
		}
		return true;
	}

	/**
	 * Loads configuration file.
	 * 
	 * @param merge
	 *            true if we need merge (unimplemented yet)
	 * @param inputStream
	 *            stream with configuration
	 * @return true in case of error, false if everything is OK
	 */
	synchronized boolean loadBackupFile(boolean merge, InputStream inputStream) {
		boolean ok = true;
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		if (!merge) {
			// cleanup the database
			db.execSQL("DELETE FROM " + FULLPRODLIST_TABLE_NAME + ";");
			db.execSQL("DELETE FROM " + FULLPRODLISTNAME_TABLE_NAME + ";");
			db.execSQL("DELETE FROM " + PRODLIST_TABLE_NAME + ";");
			db.execSQL("DELETE FROM " + LANGLIST_TABLE_NAME + ";");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				Log.v(LOGTAG, "read: '" + line + "'");
				if (line.length() != 0 && line.charAt(0) != '#') {
					String[] conf = TextUtils.split(line, "=");
					if (conf.length != 2) {
						Log.e(LOGTAG, "Unrecognized config line:'" + line + "'");
					} else {
						if (parseConfigLine(line, db)) {
							Log.e(LOGTAG,
									"There was errors in parsing backup file. Rollback.");
							ok = false;
						}
					}
				}
			}
			/* Create your language table */
			Locale.getDefault().getLanguage();
			Cursor res = db.rawQuery("SELECT " + FULLPRODLISTNAME_TABLE_NAME
					+ "." + PROD_ID + "," + PROD_CARB + "," + PROD_NAME
					+ " FROM " + FULLPRODLIST_TABLE_NAME + ","
					+ FULLPRODLISTNAME_TABLE_NAME + " WHERE "
					+ FULLPRODLIST_TABLE_NAME + "." + PROD_ID + "="
					+ FULLPRODLISTNAME_TABLE_NAME + "." + PROD_ID + " and "
					+ PROD_LANG + "='" + Locale.getDefault().getLanguage()
					+ "'", null);
			Log.v(LOGTAG, "count for '" + Locale.getDefault().getLanguage()
					+ "': " + res.getCount());
			if (res.getCount() == 0) {
				Log.e(LOGTAG, "Empty result for languge: '"
						+ Locale.getDefault().getLanguage() + "'");
				ok = false;
			} else
				while (res.moveToNext()) {
					Log.v(LOGTAG,
							"id: " + res.getLong(0) + "  carb: "
									+ res.getDouble(1) + " name: '"
									+ res.getString(2) + "'");
					ContentValues vals = new ContentValues(3);
					vals.put(PROD__ID, res.getLong(0));
					vals.put(PROD_CARB, res.getDouble(1));
					vals.put(PROD_NAME, res.getString(2));
					db.replace(PRODLIST_TABLE_NAME, null, vals);
				}
			;
			res.close();
			if (ok)
				db.setTransactionSuccessful();
		} catch (IOException ex) {
			Log.e(LOGTAG, "IO error: " + ex.toString());
			ok = false;
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
			}
			db.endTransaction();
		}
		db.execSQL("VACUUM;");
		return !ok;
	}

	/**
	 * Loads initial configuration file in case if database is empty
	 * 
	 * @param resources
	 *            resources
	 * @return true in case of error, false if everything is OK
	 */
	boolean loadInitFileIfEmpty(Resources resources) {
		boolean rc = false; // OK
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor res = db.rawQuery("select exists(select * from "
				+ FULLPRODLIST_TABLE_NAME + ");", null);
		res.moveToFirst();
		if (res.getInt(0) == 0)
			rc = loadInitFile(resources);
		res.close();
		return rc;
	}

	/**
	 * Loads initial configuration file
	 * 
	 * @param resources
	 *            resources
	 * @return true in case of error, false if everything is OK
	 */
	boolean loadInitFile(Resources resources) {
		InputStream inputStream = resources
				.openRawResource(R.raw.initial_backup);
		return loadBackupFile(false, inputStream);
	}

	void checkExternal() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		Log.v(LOGTAG, "external storage:"
				+ (mExternalStorageAvailable ? "available " : "unavaliable ")
				+ (mExternalStorageWriteable ? "writable" : "non-writable"));
		if (mExternalStorageWriteable) {
			File extDir = Environment.getExternalStorageDirectory();
			if (mExternalMyDirectory == null)
				mExternalMyDirectory = new File(extDir, "SimpleCarboCalc");
			if (!mExternalMyDirectory.isDirectory()) {
				if (!mExternalMyDirectory.mkdir()) {
					Log.e(LOGTAG, "Can't create directory: "
							+ mExternalMyDirectory);
					mExternalStorageWriteable = false;
				} else
					Log.v(LOGTAG, "Directory created: " + mExternalMyDirectory);
			} else
				Log.v(LOGTAG, "Directory: " + mExternalMyDirectory);
		}
	}

	void writeUnits(OutputStream out) throws IOException {
		StringBuffer str = new StringBuffer(10);
		str.append("units=");
		str.append(mActivity.getUnits());
		str.append('\n');
		out.write(str.toString().getBytes());
	}

	void writeLangs(OutputStream out, SQLiteDatabase db) throws IOException {
		StringBuffer str = new StringBuffer(100);
		str.append("lang=");
		String fields[] = { PROD_LANG, PROD_LANGNAME };
		Cursor res = db.query(LANGLIST_TABLE_NAME, fields, null, null, null,
				null, null);
		if (res.getCount() == 0) {
			Log.e(LOGTAG, "No laguages in DB");
		}
		boolean first = true;
		while (res.moveToNext()) {
			if (!first)
				str.append('|');
			str.append(res.getString(0));
			str.append('~');
			str.append(res.getString(1));
			first = false;
		}
		res.close();
		str.append('\n');
		out.write(str.toString().getBytes());
	}

	void writeProds(OutputStream out, SQLiteDatabase db) throws IOException {
		String fieldsHeader[] = { PROD_ID, PROD_CARB };
		String fields[] = { PROD_LANG, PROD_NAME };

		Cursor resHeader = db.query(FULLPRODLIST_TABLE_NAME, fieldsHeader,
				null, null, null, null, null);
		if (resHeader.getCount() == 0) {
			Log.e(LOGTAG, "No Prods in DB");
		}
		while (resHeader.moveToNext()) {
			StringBuffer str = new StringBuffer(100);
			str.append("prod=");
			str.append(resHeader.getDouble(1));
			Cursor res = db.query(FULLPRODLISTNAME_TABLE_NAME, fields, PROD_ID
					+ "=" + resHeader.getLong(0), null, null, null, null, null);
			if (res.getCount() == 0) {
				Log.e(LOGTAG, "No Prod Names in DB :" + resHeader.getLong(0));
			}
			while (res.moveToNext()) {
				str.append('|');
				str.append(res.getString(0));
				str.append('~');
				str.append(res.getString(1));
			}
			str.append('\n');
			out.write(str.toString().getBytes());
		}
		resHeader.close();
	}

	boolean backupConfig() {
		checkExternal();
		if (!mExternalStorageWriteable)
			return true;
		File backup = new File(mExternalMyDirectory, "backup.txt");
		try {
			if (!backup.isFile() && !backup.createNewFile()) {
				Log.e(LOGTAG, "Can't create file " + backup);
				return true;
			}
		} catch (IOException ex) {
			Log.e(LOGTAG, "Can't create file '" + backup + "': " + ex);
			return true;
		}
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(backup));
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			writeUnits(out);
			writeLangs(out, db);
			writeProds(out, db);
			out.flush();
		}

		catch (FileNotFoundException ex) {
			Log.e(LOGTAG, "Can't find file '" + backup + "': " + ex);
			return false;
		}

		catch (IOException ex) {
			Log.e(LOGTAG, "Can't write file '" + backup + "': " + ex);
			return false;
		}

		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
				}
				;
			}
		}
		return false;
	}

	Cursor getCoursorForRequest(String req) {
		String where = null;
		String vars[] = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String fields[] = { PROD__ID, PROD_NAME, PROD_CARB };
		if (req != null) {
			vars = new String[1];
			vars[0] = req + "*";
			where = PROD_NAME + " MATCH ?";
		}
		return db.query(PRODLIST_TABLE_NAME, fields, where, vars, null, null,
				null);
	}
	
	double getCarbProc(long id)
	{
		double proc;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String fields[] = { PROD_CARB };
		Cursor res= db.query(FULLPRODLIST_TABLE_NAME, fields, PROD_ID + "=" + id,
				null, null, null, null);
		res.moveToFirst();
		proc= res.getFloat(0);
		res.close();
		return proc;
	}
}
