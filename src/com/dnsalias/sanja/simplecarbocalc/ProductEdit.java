package com.dnsalias.sanja.simplecarbocalc;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author Oleksander "Sanja" Byelkin
 *
 */
public class ProductEdit extends Activity
{
	private static final String LOGTAG = "SimpleCarboCalcEdit";
	
	private Button mConfirm;
	private Button mCancel;
	private Cursor mLangCursor;
	private Spinner mLang;
	private EditText mProc;
	private EditText[] mNamesEdit= null;
	private String[] mLangListLong= null;
	private String[] mLangListShort= null;
	private String[] mNames= null;
	private String mMainLang= null;
	private int mMainLangIdx= -1;
	
	public static final String EDIT_PROC= "proc_edit";
	public static final String EDIT_ID= "id_edit";
	public static final String EDIT_LANGS_SHORT= "shortlang_edit";
	public static final String EDIT_LANGS_LONG= "longlang_edit";
	public static final String EDIT_NAMES= "name_edit";
	public static final String EDIT_MAIN_LANG= "mainlang_edit";
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.product_edit);
        
        Bundle extras= getIntent().getExtras();
       	long id= extras.getLong(EDIT_ID);
       	double proc= extras.getLong(EDIT_PROC);
       	String names[]= extras.getStringArray(EDIT_NAMES);
       	mLangListShort= extras.getStringArray(EDIT_LANGS_SHORT);
       	mLangListLong= extras.getStringArray(EDIT_LANGS_LONG);
       	mNames= extras.getStringArray(EDIT_NAMES);
       	mMainLang= extras.getString(EDIT_MAIN_LANG);
        
        setTitle((id > 0 ? R.string.edit_name : R.string.add_name));

        
        mProc= (EditText) findViewById(R.id.proc);
        mConfirm= (Button) findViewById(R.id.confirm);
        mCancel= (Button) findViewById(R.id.cancel);

        TableRow rowExample= (TableRow) findViewById(R.id.tableRow3);
        TableLayout table= (TableLayout) findViewById(R.id.tableLayout1);

        TableRow.LayoutParams prm= new  TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        mNamesEdit= new  EditText[mLangListShort.length];
        for(int i= 0; i < mLangListShort.length; i++)
        {
        	Log.v(LOGTAG, "lang: '" + mLangListShort[i] + "'");
        	if (mMainLang.equals(mLangListShort[i]))
        	{
        		mMainLangIdx= i;
        		TextView text= (TextView) findViewById(R.id.mainLangText);
        		text.setText(mLangListLong[i]);
        		mNamesEdit[i]= (EditText) findViewById(R.id.mainLangName);
        	}
        	else
        	{
        		TableRow row= new TableRow(getBaseContext());
        		TextView text= new TextView(getBaseContext());
        		text.setText(mLangListLong[i]);
        		text.setLayoutParams(prm);
        		row.addView(text);
        		mNamesEdit[i]= new EditText(getBaseContext());
        		mNamesEdit[i].setLayoutParams(prm);
         		row.addView(mNamesEdit[i]);
        		table.addView(row);
        	}
        	if (mNames[i] != null)
        		mNamesEdit[i].setText(mNames[i]);
        }
        if (mMainLangIdx == -1)
        	Log.e(LOGTAG, "Main language '" + mMainLang + "' is not found in " + mLangListShort);
        
	}

}
