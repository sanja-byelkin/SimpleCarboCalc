package com.dnsalias.sanja.simplecarbocalc;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SimpleCarboCalcExport extends Activity
{
	private static final String LOGTAG= "SimpleCarboCalcExport";
	
	RadioGroup mType;
	EditText mFile;
	CheckBox mUnits;
	CheckBox mLangs;
	CheckBox mProds;
	CheckBox mAllProds;
	ListView mExportProds;
	Button mExport;
	TextView mResult;
	
	private void checkExport()
	{
		mExport.setEnabled(mUnits.isChecked() ||  mLangs.isChecked() || mProds.isChecked());
	}
	
	CompoundButton.OnCheckedChangeListener prodCangeListener= new CompoundButton.OnCheckedChangeListener()
	{
		public void	 onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked)
			{
				mAllProds.setEnabled(true); mAllProds.setChecked(true);
			}
			else
			{
				mAllProds.setEnabled(false); mAllProds.setChecked(false);
			}
			checkExport();
		}
	};
	
	CompoundButton.OnCheckedChangeListener checkCangeListener= new CompoundButton.OnCheckedChangeListener()
	{
		public void	 onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			checkExport();
		}
	};
	
	CompoundButton.OnCheckedChangeListener allCangeListener= new CompoundButton.OnCheckedChangeListener()
	{
		public void	 onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked || !buttonView.isEnabled())
			{
				mExportProds.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_checked, new String[0]));
				mExportProds.setFocusable(false);
			}
			else
			{
				mExportProds.setAdapter(new SimpleCursorAdapter(getBaseContext(),
						android.R.layout.simple_list_item_multiple_choice,
						ProdList.getInstance().getCoursorForRequest(null, -1),
						new String[] {ProdList.PROD_NAME },
						new int[] { android.R.id.text1 }));
				mExportProds.setFocusable(true);
			}
		}
	};

    RadioGroup.OnCheckedChangeListener typeChangeListener= new RadioGroup.OnCheckedChangeListener()
    {
    	public void onCheckedChanged(RadioGroup group, int checkedId)
        {
        	File dir= new File(Environment.getExternalStorageDirectory(), "SimpleCarboCalc");
        	if (checkedId == R.id.backup)
        	{
        		File file= new File(dir, "backup.txt");
        		mFile.setText(file.toString());
        		mFile.setEnabled(false);
        		mUnits.setEnabled(false); mUnits.setChecked(true);
        		mLangs.setEnabled(false); mLangs.setChecked(true);
        		mProds.setEnabled(false); mProds.setChecked(true);
        		mAllProds.setChecked(true); mAllProds.setEnabled(false);
        	}
        	else if (checkedId == R.id.export)
        	{
        		
        		Calendar cl= Calendar.getInstance();
        		File file= new File(dir, String.format("backup%04d%02d%02d%02d%02d.txt",
        				cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1, cl.get(Calendar.DAY_OF_MONTH),
        				cl.get(Calendar.HOUR), cl.get(Calendar.MINUTE)));
        		mFile.setText(file.toString());
        		mFile.setEnabled(true);
        		mUnits.setEnabled(false); mUnits.setChecked(false);
        		mLangs.setEnabled(true); mLangs.setChecked(false);
        		mProds.setEnabled(true); mProds.setChecked(true);
        		mAllProds.setChecked(true); mAllProds.setEnabled(true);
        	}
        	else
        	{
        		mFile.setText("");
        		mFile.setEnabled(false);
        		mUnits.setEnabled(false); mUnits.setChecked(false);
        		mLangs.setEnabled(true); mLangs.setChecked(false);
        		mProds.setEnabled(true); mProds.setChecked(true);
        		mAllProds.setChecked(true); mAllProds.setEnabled(true);
        	}
        }
    };
    
    View.OnClickListener doExportListener= new View.OnClickListener()
    {
    	public void onClick(View v)
    	{
    		if (mType.getCheckedRadioButtonId() == R.id.share)
    		{
    			String conf= ProdList.getInstance().SaveConfig(mUnits.isChecked(), mLangs.isChecked(), mProds.isChecked(),
        				(mAllProds.isChecked() ? null : mExportProds.getCheckItemIds()));
    			if (conf != null)
    			{
    				Intent sendIntent = new Intent(Intent.ACTION_SEND);
    				sendIntent.putExtra(Intent.EXTRA_TEXT, conf);
    				sendIntent.setType("text/plain");
    				startActivity(Intent.createChooser(sendIntent, "Share via"));
    			}
    			else
    			{
    				mResult.setText(String.format(getResources().getString(R.string.ExportFailure), mFile.getText().toString()));
    				mResult.setTextColor(Color.RED);
    			}
    		}
    		else
    		{
    			if (ProdList.getInstance().SaveConfig(mFile.getText().toString(),
    				mUnits.isChecked(), mLangs.isChecked(), mProds.isChecked(),
    				(mAllProds.isChecked() ? null : mExportProds.getCheckItemIds())))
    			{
    				mResult.setText(String.format(getResources().getString(R.string.ExportFailure), mFile.getText().toString()));
    				mResult.setTextColor(Color.RED);
    			}
    			else
    			{
    				mResult.setText(String.format(getResources().getString(R.string.ExportSuccess), mFile.getText().toString()));
    				mResult.setTextColor(Color.GREEN);
    			}
    		}
    	}
    };
    
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.export_config);
        setTitle(R.string.export_name);
        
        mType= (RadioGroup) findViewById(R.id.radioExportType);
        mFile= (EditText) findViewById(R.id.exportFile);
        mUnits= (CheckBox) findViewById(R.id.exportUnits);
        mLangs= (CheckBox) findViewById(R.id.exportLanguages);
        mProds= (CheckBox) findViewById(R.id.exportProducts);
        mAllProds= (CheckBox) findViewById(R.id.allProds);
        mExportProds= (ListView) findViewById(R.id.exportProdsList);
        mExport= (Button) findViewById(R.id.doExport);
        mResult= (TextView) findViewById(R.id.exportResult);
        
        mExportProds.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_checked, new String[0]));
        mUnits.setOnCheckedChangeListener(checkCangeListener);
        mLangs.setOnCheckedChangeListener(checkCangeListener);
        mProds.setOnCheckedChangeListener(prodCangeListener);
        mAllProds.setOnCheckedChangeListener(allCangeListener);
        mType.setOnCheckedChangeListener(typeChangeListener);
        mType.check(R.id.backup);
        mExport.setOnClickListener(doExportListener);
	}
}
