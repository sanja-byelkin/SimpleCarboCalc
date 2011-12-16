package com.dnsalias.sanja.simplecarbocalc;

import java.text.DecimalFormat;

import android.app.Activity;
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

public class SimpleCarboCalcActivity extends Activity {

	public static final int pProcN= 0;
	public static final int pTotalN= 1;
	public static final int pCarbN= 2;
	
	private TextView mHeader;
	private EditText mText[]= new EditText[3];
	private RadioButton mRadioButton[]= new RadioButton[3];
	private int mSequence[]= {pProcN, pTotalN, pCarbN};

	
	private int getElementIndex(View v, View[] array)
	{
		for(int i=0; i < array.length; i++)
			if (v == array[i])
				return i;
		return -1;
	}
	
	private OnFocusChangeListener mTextListener = new OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			int i= getElementIndex(v, mText);
			if (hasFocus)
			{
				setFocusTo(i);
			}
	    }
	};
	
	private OnCheckedChangeListener mRadioListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			int i= getElementIndex(v, mRadioButton);
			
			if (isChecked)
				setCalculatorTo(i);
	    }
	};
	private Double checkNegative(Double val, EditText txt)
	{
		if (val < 0)
		{
			val= -val;
			txt.setText(val.toString());
		}
		return val;
	}	
	
	private Double getPositiveDoubleValue(EditText txt)
	{
		Double val;
		try {
			val= new Double(txt.getText().toString());
			val= checkNegative(val, mText[pProcN]);
		}
		catch (NumberFormatException ex)
		{
			val= 0.0;
		}
		return val;
	}
	
	private Double getProcent()
	{
		Double val= getPositiveDoubleValue(mText[pProcN]);
		val= checkNegative(val, mText[pProcN]);
		if (val > 100)
		{
		    val= 100.0;
			mText[pProcN].setText("100");
		}
		val/= 100;
		return val;
	}
	

	
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
			   if (carb < 0.001)
				   mText[pProcN].setText("0");
			   else
			   {
				   carb*= 12;
				   mText[pProcN].setText(twoDigitsFormat.format(new Double(carb * 100 / total)));
			   }
			   break;
		   case pTotalN:
			   proc= getProcent();
			   if (proc < 0.00001)
				   mText[pTotalN].setText(0);
			   else
			   {
				   carb= getPositiveDoubleValue(mText[pCarbN]);
				   carb*= 12;
				   mText[pTotalN].setText(twoDigitsFormat.format(new Double(carb/proc)));
			   }
			   break;
		   case pCarbN:
			   proc= getProcent();
			   total= getPositiveDoubleValue(mText[pTotalN]);
			   mText[pCarbN].setText(twoDigitsFormat.format(new Double(total*proc/12)));
			   break;
		   }
	
	   }
	   public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	   public void onTextChanged(CharSequence s, int start, int before, int count) {}
	};
	
	private void setRadio()
	{
		for(int i= 0; i < mRadioButton.length; i++)
			if (mRadioButton[i].isChecked() != (i == mSequence[2]))
				mRadioButton[i].setChecked(i == mSequence[2]);
	}
	
	private void setFocusTo(int focus)
	{
		if (mSequence[0] != focus)
		{
			if (mSequence[1] == focus)
			{
				mSequence[1]= mSequence[0];
				mSequence[0]= focus;
			}
			else
			{
				mSequence[2]= mSequence[1];
				mSequence[1]= mSequence[0];
				mSequence[0]= focus;
			}
		}
		setRadio();
	}
	
	private void setCalculatorTo(int calc)
	{
		if (mSequence[2] != calc);
		{
			if (mSequence[1] == calc)
			{
				mSequence[1]= mSequence[2];
				mSequence[2]= calc;
			}
			else
			{
				mSequence[0]= mSequence[1];
				mSequence[1]= mSequence[2];
				mSequence[2]= calc;
			}
		}
		if (!mText[mSequence[0]].isFocused())
			mText[mSequence[0]].requestFocus();
		setRadio();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mHeader= (TextView)findViewById(R.id.textHeader);
        mText[pProcN]= (EditText)findViewById(R.id.editTextProc);
        mText[pTotalN]= (EditText)findViewById(R.id.editTextTotal);
        mText[pCarbN]= (EditText)findViewById(R.id.editTextCarb);
        mRadioButton[pProcN]= (RadioButton) findViewById(R.id.radioButtonProc);
        mRadioButton[pTotalN]= (RadioButton) findViewById(R.id.radioButtonTotal);
        mRadioButton[pCarbN]= (RadioButton) findViewById(R.id.radioButtonCarb);
        

		//mHeader.setText(String.valueOf(mSequence[0]*100+mSequence[1]*10+mSequence[2]));
        for(int i= 0; i < mText.length; i++)
        {
        	mText[i].setOnFocusChangeListener(mTextListener);
        	mText[i].addTextChangedListener(mTextWatcher);
        	mRadioButton[i].setOnCheckedChangeListener(mRadioListener);
        }
        setRadio();
        mText[pProcN].requestFocus();
    }
}