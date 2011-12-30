/**
 * 
 */
package com.dnsalias.sanja.simplecarbocalc;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

/**
 * @author Oleksander "Sanja" Byelkin
 *
 */
public class SimpleCarboCalcSetup extends Activity
{
	private static final String LOGTAG = "SimpleCarboCalcSetup";
	
	private Spinner mSetupUnit;
	private Spinner mSetupProdLang;
	private Button mConfirm;
	private Button mCancel;
	private Cursor mLangCursor;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simplecarbocalcsetup);
        setTitle(R.string.setup_name);

        mSetupUnit= (Spinner) findViewById(R.id.setup_unit);
        mSetupProdLang= (Spinner) findViewById(R.id.setup_prod_lang);
        mConfirm= (Button) findViewById(R.id.setup_confirm);
        mCancel= (Button) findViewById(R.id.setup_cancel);
        
        mLangCursor= ProdList.getInstance().getCoursorForLanguages();
        mSetupProdLang.setAdapter(new SimpleCursorAdapter(getBaseContext(),
        		android.R.layout.simple_spinner_item,
        		mLangCursor,
        		new String[] {"lang"},
        		new int[] {android.R.id.text1, android.R.id.text2}));

        Bundle extras= getIntent().getExtras();
        if (extras != null)
        {
        	int i;
        	mSetupUnit.setSelection(extras.getInt(SimpleCarboCalcActivity.CONFIG_UNIT));
        	String lang= extras.getString(SimpleCarboCalcActivity.CONFIG_LANG);
        	for (i= 0; i < mLangCursor.getCount(); i++)
        	{
        		mLangCursor.moveToPosition(i);
        		if (lang.equals(mLangCursor.getString(1).substring(0, 2)))
        		{
        			Log.v(LOGTAG, "Found " + lang);
        			break;
        		}
        		Log.v(LOGTAG, "Compare: '" + lang + "' and '" + mLangCursor.getString(1).substring(0, 2) + "'");
        	}
        	
        	if (i < mLangCursor.getCount())
        	{
        		Log.v(LOGTAG, "Lang set to: " + i + " " + mLangCursor.getString(1));
        		mSetupProdLang.setSelection(i);
        	}
        	else
        		Log.e(LOGTAG, "Lang not found: " + lang);
        }
        
        mConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Bundle bundle= new Bundle();

                bundle.putInt(SimpleCarboCalcActivity.CONFIG_UNIT, mSetupUnit.getSelectedItemPosition());
                mLangCursor.moveToPosition(mSetupProdLang.getSelectedItemPosition());
                bundle.putString(SimpleCarboCalcActivity.CONFIG_LANG, mLangCursor.getString(1).substring(0, 2));
                Intent intent = new Intent();
                intent.putExtras(bundle);
                Log.v(LOGTAG, "result: " + bundle.toString());
                setResult(RESULT_OK, intent);
                mLangCursor.close();
                finish();
            }
        });
        
        mCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    setResult(RESULT_CANCELED);
                    finish();
                }

        });
    }
}
