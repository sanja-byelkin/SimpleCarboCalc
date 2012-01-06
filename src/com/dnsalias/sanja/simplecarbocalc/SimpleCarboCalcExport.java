package com.dnsalias.sanja.simplecarbocalc;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

public class SimpleCarboCalcExport extends Activity
{
	private static final String LOGTAG= "SimpleCarboCalcExport";
	
	RadioGroup mType;
	EditText mFile;
	CheckBox mUnits;
	CheckBox mLangs;
	CheckBox mProds;
	
    RadioGroup.OnCheckedChangeListener typechangeListener= new RadioGroup.OnCheckedChangeListener()
    {
    	public void onCheckedChanged(RadioGroup group, int checkedId)
        {
        	File dir= new File(Environment.getExternalStorageDirectory(), "SimpleCarboCalc");
        	if (checkedId == R.id.backup)
        	{
        		File file= new File(dir, "backup.txt");
        		mFile.setText(file.toString());
        		mFile.setEnabled(false);
        		mUnits.setChecked(true); mUnits.setEnabled(false);
        		mLangs.setChecked(true); mLangs.setEnabled(false);
        		mProds.setChecked(true); mProds.setEnabled(false);
        	}
        	else
        	{
        		
        		Calendar cl= Calendar.getInstance();
        		File file= new File(dir, String.format("backup%04d%02d%02d%02d%02d.txt",
        				cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1, cl.get(Calendar.DAY_OF_MONTH),
        				cl.get(Calendar.HOUR), cl.get(Calendar.MINUTE)));
        		mFile.setText(file.toString());
        		mFile.setEnabled(true);
        		mUnits.setChecked(false); mUnits.setEnabled(true);
        		mLangs.setChecked(false); mLangs.setEnabled(true);
        		mProds.setChecked(true); mProds.setEnabled(true);
        	}
        }
    };
    
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.export);
        setTitle(R.string.export_name);
        
        mType= (RadioGroup) findViewById(R.id.radioExportType);
        mFile= (EditText) findViewById(R.id.exportFile);
        mUnits= (CheckBox) findViewById(R.id.exportUnits);
        mLangs= (CheckBox) findViewById(R.id.exportLanguages);
        mProds= (CheckBox) findViewById(R.id.exportProducts);
        
 
        mType.setOnCheckedChangeListener(typechangeListener);
        mType.check(R.id.backup);
	}
}
