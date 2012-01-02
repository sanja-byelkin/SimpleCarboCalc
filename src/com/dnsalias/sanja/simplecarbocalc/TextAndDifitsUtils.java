package com.dnsalias.sanja.simplecarbocalc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import android.widget.EditText;

public class TextAndDifitsUtils {
	
	/**
	 * Checks that given value is positive otherwise turns it to Double.NaN
	 * 
	 * @param val
	 *            - value to check
	 * @return val or Double.NaN if val was negative
	 */
	public static  Double checkNegative(Double val)
	{
		if (val.isNaN() || val < 0)
			val = Double.NaN;
		return val;
	}

	/**
	 * Fetches numeric value from the given text field and check it
	 * 
	 * @param txt
	 *            - text field to fetch value from
	 * @return Double - positive numeric value of the field or Double.NaN in
	 *         case of error
	 */
	public static  Double getPositiveDoubleValue(EditText txt)
	{
		Double val;
		try {
			val = new Double(txt.getText().toString());
			val = checkNegative(val);
		} catch (NumberFormatException ex) {
			/*
			 * It could be mess with , and . in different locale setting => try
			 * to fix
			 */
			try {
				val = new Double(txt.getText().toString().replace(',', '.'));
				val = checkNegative(val);
			} catch (NumberFormatException ex2) {
				val = Double.NaN;
			}
		}
		return val;
	}
	
	/**
	 * Convert double to a string with correct separator and precision
	 * @param val - double to convert
	 * @return
	 */
	public static String getStringDouble(Double val)
	{
		DecimalFormatSymbols df = new DecimalFormatSymbols();
		df.setDecimalSeparator('.'); // Numeric keyboard has only point, so we
										// should use it
		DecimalFormat twoDigitsFormat = new DecimalFormat("#.##", df);
		return twoDigitsFormat.format(val);
	}
	
	/**
	 * Sets EditText with Double value
	 * 
	 * @param txt
	 *            - where to put the value
	 * @param val
	 *            - the value to be put
	 */
	public static  void setDoubleValue(EditText txt, Double val)
	{
		txt.setText(getStringDouble(val));
	}

	/**
	 * Fetches and checks percent from its field
	 * 
	 * @return Double - value of the percent field or Double.NaN in case of
	 *         error
	 */
	public static Double getProcent(EditText txt) {
		Double val = getPositiveDoubleValue(txt);
		val = checkNegative(val);
		if (val.isNaN() || val > 100.00)
			val = Double.NaN;
		else
			val /= 100;
		return val;
	}

	/**
	 * Shows value in the fields that indicates inability to calculate it
	 * 
	 * @param txt
	 *            The field where to show error
	 */
	public static void setToError(EditText txt) {
		if (txt.getText().toString().compareTo("#") != 0)
			txt.setText("#");
	}
}
