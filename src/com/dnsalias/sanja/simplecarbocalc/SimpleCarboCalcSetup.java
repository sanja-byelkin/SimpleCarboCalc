/**
 * 
 */
package com.dnsalias.sanja.simplecarbocalc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
	private String[] mLangListLong;
	private String[] mLangListShort;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simplecarbocalcsetup);
        setTitle(R.string.setup_name);

        mSetupUnit= (Spinner) findViewById(R.id.setup_unit);
        mSetupProdLang= (Spinner) findViewById(R.id.setup_prod_lang);
        mConfirm= (Button) findViewById(R.id.setup_confirm);
        mCancel= (Button) findViewById(R.id.setup_cancel);
        
        String[][] lists= ProdList.getInstance().getLangList();
        mLangListShort= lists[0];
        mLangListLong= lists[1];
        
        mSetupProdLang.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, mLangListLong));

        Bundle extras= getIntent().getExtras();
        if (extras != null)
        {
        	int i;
        	mSetupUnit.setSelection(extras.getInt(SimpleCarboCalcActivity.CONFIG_UNIT));
        	String lang= extras.getString(SimpleCarboCalcActivity.CONFIG_LANG);
        	for (i= 0; i < mLangListShort.length; i++)
        	{
        		if (lang.equals(mLangListShort[i]))
        		{
        			Log.v(LOGTAG, "Found " + lang);
        			break;
        		}
        		Log.v(LOGTAG, "Compare: '" + lang + "' and '" + mLangListShort[i] + "'");
        	}
        	
        	if (i < mLangListShort.length)
        	{
        		Log.v(LOGTAG, "Lang set to: " + i + " " + mLangListShort[i]);
        		mSetupProdLang.setSelection(i);
        	}
        	else
        		Log.e(LOGTAG, "Lang not found: " + lang);
        }
        
        mConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Bundle bundle= new Bundle();

                bundle.putInt(SimpleCarboCalcActivity.CONFIG_UNIT, mSetupUnit.getSelectedItemPosition());
                bundle.putString(SimpleCarboCalcActivity.CONFIG_LANG, mLangListShort[mSetupProdLang.getSelectedItemPosition()]);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                Log.v(LOGTAG, "result: " + bundle.toString());
                setResult(RESULT_OK, intent);
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
