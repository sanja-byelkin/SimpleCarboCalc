package com.dnsalias.sanja.simplecarbocalc;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SimpleCarboCalcPreference extends PreferenceActivity
{
	ListPreference mLang;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  

        addPreferencesFromResource(R.xml.preferences);
        
        String[][] lists= ProdList.getInstance().getLangList();
        mLang= (ListPreference) findPreference(SimpleCarboCalcActivity.STATE_LANG);
        mLang.setEntries(lists[1]);
        mLang.setEntryValues(lists[0]);
    }
}
