package com.dnsalias.sanja.simplecarbocalc;

import java.util.Locale;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

public class SimpleCarboCalcApp extends Application
{
	SharedPreferences mPreferences;
	String mLang;
	Locale mLocale;

	@Override
    public void onCreate()
	{
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mLang= mPreferences.getString(SimpleCarboCalcActivity.STATE_ALANG, "default");	
		if (mLang.equals("default"))
            mLang= getResources().getConfiguration().locale.getCountry();
		mLocale= new Locale(mLang);
        Locale.setDefault(mLocale);
        Configuration config= new Configuration();
        config.locale= mLocale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mLocale= new Locale(mLang);
        Locale.setDefault(mLocale);
        Configuration config= new Configuration();
        config.locale = mLocale;
        getBaseContext().getResources().updateConfiguration(config, null);     
    }
}
