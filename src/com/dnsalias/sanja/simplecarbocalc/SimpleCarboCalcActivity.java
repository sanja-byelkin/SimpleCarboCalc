package com.dnsalias.sanja.simplecarbocalc;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * 
 * @author Oleksander "Sanja" Byelkin
 *
 * @brief Simple Carbohydrates calculator: calculate 1 of 3 parameters by other 2 (% of Carbohydrates, Total
 * 		  weight of the product, wiegth of carbohydrates in it in units (1 unit = 12gr)).
 * 
 * @license GPL V2
 * 
 */

public class SimpleCarboCalcActivity extends Activity {
	public static final String PREFS_NAME = "MyState";
	
	/**
	 * Constants of the 3 calculated parameters
	 */
	public static final int pProcN= 0;  // % of Carbohydrates
	public static final int pTotalN= 1; // Total weight of the product
	public static final int pCarbN= 2;  // wiegth of carbohydrates
	
	/**
	 * Text fields
	 */
	private EditText mText[]= new EditText[3];
	/**
	 * Radio buttons which shows which parametr will be calculated
	 */
	private RadioButton mRadioButton[]= new RadioButton[3];
	/**
	 * Sequence of the parameters in which they was touched
	 * (first (index 0) parameter is in focus, last (index 2) parameter is calculating parameter
	 */
	private int mSequence[]= {pProcN, pTotalN, pCarbN};

	/**
	 * Finds index of the View object in the given array
	 * @param v      - View object to find 
	 * @param array  - array of View objects where to search
	 * @return index of the found element or -1
	 */
	private int getElementIndex(View v, View[] array)
	{
		for(int i=0; i < array.length; i++)
			if (v == array[i])
				return i;
		return -1;
	}
	
	/**
	 * Listener of focus changing for text fields to detect which parameter should be calculated
	 */
	private OnFocusChangeListener mTextListener = new OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			int i= getElementIndex(v, mText);
			if (hasFocus)
			{
				setFocusTo(i);
			}
	    }
	};
	
	/**
	 * Listener of checked state changes for radio buttons for direct pointing of calculated value
	 */
	private OnCheckedChangeListener mRadioListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			int i= getElementIndex(v, mRadioButton);
			
			if (isChecked)
				setCalculatorTo(i);
	    }
	};
	
	/**
	 * Checks that given value is positive otherwise turns it to Double.NaN
	 * @param val  - value to check
	 * @return val or Double.NaN if val was negative
	 */
	private Double checkNegative(Double val)
	{
		if (val.isNaN() || val < 0)
			val= Double.NaN;
		return val;
	}
	
	/**
	 * Fetches numeric value from the given text field and check it
	 * @param txt - text field to fetch value from
	 * @return Double - positive numeric value of the field or Double.NaN in case of error
	 */
	private Double getPositiveDoubleValue(EditText txt)
	{
		Double val;
		try {
			val= new Double(txt.getText().toString());
			val= checkNegative(val);
		}
		catch (NumberFormatException ex)
		{
			val= Double.NaN;
		}
		return val;
	}
	
	/**
	 * Fetches and checks percent from its field
	 * @return Double - value of the percent field or Double.NaN in case of error
	 */
	private Double getProcent()
	{
		Double val= getPositiveDoubleValue(mText[pProcN]);
		val= checkNegative(val);
		if (val.isNaN() || val > 100.00)
		    val= Double.NaN;
		else
			val/= 100;
		return val;
	}
	
	/**
	 * Shows value inthe fields that indicates inability to calculate it
	 * @param txt The field where to show error
	 */
	private void setToError(EditText txt)
	{
		if (txt.getText().toString().compareTo("#") != 0)
			txt.setText("#");
	}
	
	/**
	 * Watcher of text fields changes which recalculate last touched field by other two
	 */
	private TextWatcher mTextWatcher= new TextWatcher() {
	   public void afterTextChanged(Editable s)
	   {
		   Double total;
		   Double carb;
		   Double proc;
		   DecimalFormat twoDigitsFormat = new DecimalFormat("#.##");
		   if (s == mText[mSequence[2]].getText())
			   return; // Avoid infinite loop
		   switch (mSequence[2])
		   {
		   case pProcN:
			   total= getPositiveDoubleValue(mText[pTotalN]);
			   carb= getPositiveDoubleValue(mText[pCarbN]);
			   if (carb < 0.001 || total.isNaN() || carb.isNaN())
				   setToError(mText[pProcN]);
			   else
			   {
				   carb*= 12;
				   mText[pProcN].setText(twoDigitsFormat.format(new Double(carb * 100 / total)));
			   }
			   break;
		   case pTotalN:
			   proc= getProcent();
			   carb= getPositiveDoubleValue(mText[pCarbN]);
			   if (proc < 0.00001 || proc.isNaN() || carb.isNaN())
				   setToError(mText[pTotalN]);
			   else
			   {
				   carb*= 12;
				   mText[pTotalN].setText(twoDigitsFormat.format(new Double(carb/proc)));
			   }
			   break;
		   case pCarbN:
			   proc= getProcent();
			   total= getPositiveDoubleValue(mText[pTotalN]);
			   if (proc.isNaN() || total.isNaN())
				   setToError(mText[pCarbN]);
			   else
				   mText[pCarbN].setText(twoDigitsFormat.format(new Double(total*proc/12)));
			   break;
		   }
	
	   }
	   public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	   public void onTextChanged(CharSequence s, int start, int before, int count) {}
	};
	
	/**
	 * Checks and sets all Radio buttons according to current state of mSequence
	 * @return
	 */
	private boolean setRadio()
	{
		boolean changed= false;
		for(int i= 0; i < mRadioButton.length; i++)
			if (mRadioButton[i].isChecked() != (i == mSequence[2]))
			{
				mRadioButton[i].setChecked(i == mSequence[2]);
				changed= true;
			}
		return changed;
	}
	
	/**
	 * Moves Focus to the given field
	 * @param focus - number of the field (pProcN, pTotalN, pCarbN)
	 * @return true if there was changes
	 */
	private boolean setFocusTo(int focus)
	{
		if (mSequence[0] != focus)
		{
			if (mSequence[1] != focus)
				mSequence[2]= mSequence[1];
			mSequence[1]= mSequence[0];
			mSequence[0]= focus;
			if (setRadio() && !mText[mSequence[0]].isFocused())
				mText[mSequence[0]].requestFocus();
			return true;
	    }
		return false;
	}
	
	/**
	 * Moves calculating pointer to the given field
	 * @param calc - number of the field (pProcN, pTotalN, pCarbN)
	 * @return true if there was changes
	 */
	private boolean setCalculatorTo(int calc)
	{
		if (mSequence[2] != calc)
		{
			if (mSequence[1] != calc)
				mSequence[0]= mSequence[1];
			mSequence[1]= mSequence[2];
			mSequence[2]= calc;
		
			if (setRadio() && !mText[mSequence[0]].isFocused())
				mText[mSequence[0]].requestFocus();
			return true;
		}
		return false;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
         * Find our fields
         */
        mText[pProcN]= (EditText)findViewById(R.id.editTextProc);
        mText[pTotalN]= (EditText)findViewById(R.id.editTextTotal);
        mText[pCarbN]= (EditText)findViewById(R.id.editTextCarb);
        mRadioButton[pProcN]= (RadioButton) findViewById(R.id.radioButtonProc);
        mRadioButton[pTotalN]= (RadioButton) findViewById(R.id.radioButtonTotal);
        mRadioButton[pCarbN]= (RadioButton) findViewById(R.id.radioButtonCarb);
        
        /*
         * Restore state of the application
         */
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mSequence[0] = settings.getInt("Sequence0", 0);
        mSequence[1] = settings.getInt("Sequence1", 1);
        mSequence[2] = settings.getInt("Sequence2", 2);
        if (mSequence[0] < 0 || mSequence[1] < 0 || mSequence[2] < 0 ||
        		mSequence[0] > 2 || mSequence[1] > 2 || mSequence[2] > 2 ||
        		mSequence[0] == mSequence[1] || mSequence[0] == mSequence[2] ||
        		mSequence[1] == mSequence[2])
        {
        	for (int i= 0; i < 3; i++)
        		mSequence[i]= i;
        }
        mText[pProcN].setText(settings.getString("Proc", "12"));
        mText[pTotalN].setText(settings.getString("Total", "100"));
        mText[pCarbN].setText(settings.getString("Carb", "1"));
        
		/*
		 * Set listeners
		 */
        for(int i= 0; i < mText.length; i++)
        {
        	mText[i].setOnFocusChangeListener(mTextListener);
        	mText[i].addTextChangedListener(mTextWatcher);
        	mRadioButton[i].setOnCheckedChangeListener(mRadioListener);
        }
        /*
         * Set initial state of focus and ratio buttons
         */
        setRadio();
        mText[mSequence[0]].requestFocus();
    }
    
    
    protected void onStop(){
        super.onStop();

       /*
        * Store state of the application
        */
       SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       SharedPreferences.Editor editor = settings.edit();
       editor.putInt("Sequence0", mSequence[0]);
       editor.putInt("Sequence1", mSequence[1]);
       editor.putInt("Sequence2", mSequence[2]);
       editor.putString("Proc", mText[pProcN].getText().toString());
       editor.putString("Total", mText[pTotalN].getText().toString());
       editor.putString("Carb", mText[pCarbN].getText().toString());
       // Commit the edits!
       editor.commit();
     }
}