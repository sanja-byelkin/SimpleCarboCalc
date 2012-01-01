package com.dnsalias.sanja.simplecarbocalc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;

/**
 * 
 * @author Oleksander "Sanja" Byelkin
 * 
 * @brief Simple Carbohydrates calculator: calculate 1 of 3 parameters by other
 *        2 (% of Carbohydrates, Total weight of the product, weight of
 *        carbohydrates in it in units (1 unit = 1, 10 or 12gr)).
 * 
 * @license GPL V2
 * 
 */

public class SimpleCarboCalcActivity extends Activity {
	/**
	 * Name to save preferences of the application (application state)
	 */
	public static final String PREFS_NAME = "MyState";

	private static final String LOGTAG = "SimpleCarboCalcActivity";
	/**
	 * Constants to identify saved state
	 */
	public static final String STATE_UNIT = "Unit";
	public static final String STATE_LANG = "Lang";
	public static final String STATE_SEQ0 = "Sequence0";
	public static final String STATE_SEQ1 = "Sequence1";
	public static final String STATE_SEQ2 = "Sequence1";
	public static final String STATE_PROC = "Proc";
	public static final String STATE_TOTAL = "Total";
	public static final String STATE_CARB = "Carb";

	/**
	 * Constant for Activity request code
	 */
	private static final int ACTIVITY_SETUP = 1;
	private static final int ACTIVITY_EDIT= 2;

	/**
	 * Menu constants
	 */
	private static final int MENU_SETUP = Menu.FIRST + 1;
	private static final int MENU_ABOUT = MENU_SETUP + 1;

	/**
	 * Constants to identify configure options
	 */
	public static final String CONFIG_UNIT = "unit_conf";
	public static final String CONFIG_LANG = "lang_conf";

	/**
	 * Constants of the 3 calculated parameters
	 */
	public static final int N_PROC = 0; // % of Carbohydrates
	public static final int N_TOTAL = 1; // Total weight of the product
	public static final int N_CARB = 2; // wiegth of carbohydrates

	public static final int UNIT_FACTOR[] = { 1, 10, 12 };

	private int mUnitSetup;
	private String mProdLangSetup;

	private boolean mIsSetupProcess = false;

	int getUnits() {
		return mUnitSetup;
	}

	String getProdLang() {
		return mProdLangSetup;
	}

	/**
	 * Text fields
	 */
	private EditText mText[] = new EditText[3];
	/**
	 * Radio buttons which shows which parametr will be calculated
	 */
	private RadioButton mRadioButton[] = new RadioButton[3];
	/**
	 * Sequence of the parameters in which they was touched (first (index 0)
	 * parameter is in focus, last (index 2) parameter is calculating parameter
	 */
	private int mSequence[] = { N_PROC, N_TOTAL, N_CARB };

	/**
	 * ID of last touched product in the DB or -1
	 */
	private long mLastTouched = -1;

	private ListView mProdList;
	private ImageButton mPlusButton;
	private ImageButton mEditButton;
	private ImageButton mMinusButton;
	private ImageButton mSearchButton;
	private SimpleCursorAdapter mListAdapter;

	/**
	 * Finds index of the View object in the given array
	 * 
	 * @param v
	 *            - View object to find
	 * @param array
	 *            - array of View objects where to search
	 * @return index of the found element or -1
	 */
	private int getElementIndex(View v, View[] array) {
		for (int i = 0; i < array.length; i++)
			if (v == array[i])
				return i;
		return -1;
	}

	/**
	 * Listener of focus changing for text fields to detect which parameter
	 * should be calculated
	 */
	private OnFocusChangeListener mTextListener = new OnFocusChangeListener()
	{
		public void onFocusChange(View v, boolean hasFocus) {
			int i = getElementIndex(v, mText);
			if (hasFocus && !mIsSetupProcess) {
				setFocusTo(i);
			}
		}
	};

	/**
	 * Listener on product list items click
	 */
	private OnItemClickListener mProductListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			double proc = ProdList.getInstance().getCarbProc(id);
			mText[N_PROC].requestFocus();
			TextAndDifitsUtils.setDoubleValue(mText[N_PROC], new Double(proc));
			mLastTouched = id;
			checkLastTouched();
		}
	};

	/**
	 * Listener of checked state changes for radio buttons for direct pointing
	 * of calculated value
	 */
	private OnCheckedChangeListener mRadioListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			int i = getElementIndex(v, mRadioButton);

			if (isChecked && !mIsSetupProcess)
				setCalculatorTo(i);
		}
	};

	private OnClickListener onAddClickListener= new OnClickListener()
	{
		public void onClick (View v)
		{
			long target= mLastTouched;
			if (target > 0 && v == mPlusButton)
				target= -1; // adding
			Log.v(LOGTAG, "Target: " + target);
			String[][] lists= ProdList.getInstance().getLangList();
	        String[] langShort= lists[0];
	        String[] langLong= lists[1];
	        Intent intent= new Intent(getBaseContext(), ProductEdit.class);
	        intent.putExtra(ProductEdit.EDIT_ID, target);
			intent.putExtra(ProductEdit.EDIT_LANGS_SHORT, langShort);
			intent.putExtra(ProductEdit.EDIT_LANGS_LONG, langLong);
			intent.putExtra(ProductEdit.EDIT_MAIN_LANG, getProdLang());
			Double proc= getProcent();
			String procStr;
			if (proc.isNaN())
				procStr= "";
			else
				procStr= TextAndDifitsUtils.getStringDouble(proc * 100);
			intent.putExtra(ProductEdit.EDIT_PROC, procStr);

			if (target > 0)
			{
				// Editing
				String names[]= new String[langShort.length];
				ProdList.getInstance().getNames(target, langShort, names);
				intent.putExtra(ProductEdit.EDIT_NAMES, names);
			}
			else
			{
				// Adding
				String names[]= new String[langShort.length];
				intent.putExtra(ProductEdit.EDIT_NAMES, names);
			}
			startActivityForResult(intent, ACTIVITY_EDIT);
		}
	};

	/**
	 * Fetches and checks percent from its field
	 * 
	 * @return Double - value of the percent field or Double.NaN in case of
	 *         error
	 */
	private Double getProcent() {
		return TextAndDifitsUtils.getProcent(mText[N_PROC]);
	}

	/**
	 * Watcher of text fields changes which recalculate last touched field by
	 * other two
	 */
	private TextWatcher mTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			Double total;
			Double carb;
			Double proc;
			if (s == mText[mSequence[2]].getText() || mIsSetupProcess)
				return; // Avoid infinite loop or setup problems
			switch (mSequence[2]) {
			case N_PROC:
				total= TextAndDifitsUtils.getPositiveDoubleValue(mText[N_TOTAL]);
				carb= TextAndDifitsUtils.getPositiveDoubleValue(mText[N_CARB]);
				if (carb < 0.001 || total.isNaN() || carb.isNaN())
					TextAndDifitsUtils.setToError(mText[N_PROC]);
				else {
					carb*= UNIT_FACTOR[mUnitSetup];
					TextAndDifitsUtils.setDoubleValue(mText[N_PROC],
							new Double(carb * 100 / total));
				}
				Log.v(LOGTAG, "mTextWatcher afterTextChanged N_PROC:"
						+ mText[N_PROC].getText().toString());
				break;
			case N_TOTAL:
				proc= getProcent();
				carb= TextAndDifitsUtils.getPositiveDoubleValue(mText[N_CARB]);
				if (proc < 0.00001 || proc.isNaN() || carb.isNaN())
					TextAndDifitsUtils.setToError(mText[N_TOTAL]);
				else {
					carb*= UNIT_FACTOR[mUnitSetup];
					TextAndDifitsUtils.setDoubleValue(mText[N_TOTAL], new Double(carb / proc));
				}
				Log.v(LOGTAG, "mTextWatcher afterTextChanged N_TOTAL:"
						+ mText[N_TOTAL].getText().toString());
				break;
			case N_CARB:
				proc= getProcent();
				total= TextAndDifitsUtils.getPositiveDoubleValue(mText[N_TOTAL]);
				if (proc.isNaN() || total.isNaN())
					TextAndDifitsUtils.setToError(mText[N_CARB]);
				else
					TextAndDifitsUtils.setDoubleValue(mText[N_CARB],
							new Double(total * proc
							/ UNIT_FACTOR[mUnitSetup]));
				Log.v(LOGTAG, "mTextWatcher afterTextChanged N_CARB:"
						+ mText[N_CARB].getText().toString());
				break;
			}

		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};

	private TextWatcher mTextWatcherRemoveTouch = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			mLastTouched = -1;
			checkLastTouched();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};

	/**
	 * Checks and sets all Radio buttons according to current state of mSequence
	 * 
	 * @return
	 */
	private boolean setRadio() {
		boolean changed = false;
		for (int i = 0; i < mRadioButton.length; i++)
			if (mRadioButton[i].isChecked() != (i == mSequence[2])) {
				mRadioButton[i].setChecked(i == mSequence[2]);
				changed = true;
			}
		Log.v(LOGTAG, "setRadio " + (changed ? "changed" : "non-changed"));
		return changed;
	}

	/**
	 * Moves Focus to the given field
	 * 
	 * @param focus
	 *            - number of the field (pProcN, pTotalN, pCarbN)
	 * @return true if there was changes
	 */
	private boolean setFocusTo(int focus) {
		Log.v(LOGTAG, "setFocusTo " + focus);
		if (mSequence[0] != focus) {
			if (mSequence[1] != focus)
				mSequence[2] = mSequence[1];
			mSequence[1] = mSequence[0];
			mSequence[0] = focus;
			if (setRadio() && !mText[mSequence[0]].isFocused())
				mText[mSequence[0]].requestFocus();
			return true;
		}
		return false;
	}

	/**
	 * Moves calculating pointer to the given field
	 * 
	 * @param calc
	 *            - number of the field (pProcN, pTotalN, pCarbN)
	 * @return true if there was changes
	 */
	private boolean setCalculatorTo(int calc) {
		Log.v(LOGTAG, "setCalculatorTo " + calc);
		if (mSequence[2] != calc) {
			if (mSequence[1] != calc)
				mSequence[0] = mSequence[1];
			mSequence[1] = mSequence[2];
			mSequence[2] = calc;

			if (setRadio() && !mText[mSequence[0]].isFocused())
				mText[mSequence[0]].requestFocus();
			return true;
		}
		return false;
	}

	/**
	 * Assigns correct carbohydrates unit text
	 */
	void setCarbUnitsName() {
		Resources res = getResources();
		CharSequence names[] = res.getTextArray(R.array.UnitName);
		mRadioButton[N_CARB].setText(names[mUnitSetup]);
	}

	void checkLastTouched() {
		Log.v(LOGTAG, "mLastTouched: " + mLastTouched);
		mMinusButton.setEnabled(mLastTouched > 0);
		mEditButton.setEnabled(mLastTouched > 0);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/*
		 * Find our fields
		 */
		mText[N_PROC] = (EditText) findViewById(R.id.editTextProc);
		mText[N_TOTAL] = (EditText) findViewById(R.id.editTextTotal);
		mText[N_CARB] = (EditText) findViewById(R.id.editTextCarb);
		mRadioButton[N_PROC] = (RadioButton) findViewById(R.id.radioButtonProc);
		mRadioButton[N_TOTAL] = (RadioButton) findViewById(R.id.radioButtonTotal);
		mRadioButton[N_CARB] = (RadioButton) findViewById(R.id.radioButtonCarb);
		mProdList = (ListView) findViewById(R.id.listProd);
		mPlusButton = (ImageButton) findViewById(R.id.add);
		mEditButton = (ImageButton) findViewById(R.id.edit);
		mMinusButton = (ImageButton) findViewById(R.id.remove);
		mSearchButton = (ImageButton) findViewById(R.id.search);

		/*
		 * Restore state of the application
		 */
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mSequence[0] = settings.getInt(STATE_SEQ0, 0);
		mSequence[1] = settings.getInt(STATE_SEQ1, 1);
		mSequence[2] = settings.getInt(STATE_SEQ2, 2);
		if (mSequence[0] < 0 || mSequence[1] < 0 || mSequence[2] < 0
				|| mSequence[0] > 2 || mSequence[1] > 2 || mSequence[2] > 2
				|| mSequence[0] == mSequence[1] || mSequence[0] == mSequence[2]
				|| mSequence[1] == mSequence[2]) {
			for (int i = 0; i < 3; i++)
				mSequence[i] = i;
		}
		mText[N_PROC].setText(settings.getString(STATE_PROC, "12"));
		mText[N_TOTAL].setText(settings.getString(STATE_TOTAL, "100"));
		mText[N_CARB].setText(settings.getString(STATE_CARB, "1"));
		mUnitSetup = settings.getInt(STATE_UNIT, 2);
		mProdLangSetup = settings.getString(STATE_LANG, Locale.getDefault()
				.getLanguage());
		mPlusButton.setOnClickListener(onAddClickListener);
		mEditButton.setOnClickListener(onAddClickListener);

		/*
		 * Set listeners
		 */
		for (int i = 0; i < mText.length; i++) {
			mText[i].setOnFocusChangeListener(mTextListener);
			mText[i].addTextChangedListener(mTextWatcher);
			mRadioButton[i].setOnCheckedChangeListener(mRadioListener);
		}
		mText[N_PROC].addTextChangedListener(mTextWatcherRemoveTouch);

		/*
		 * Load configuration and db if it is needed
		 */
		ProdList.getInstance().setActivity(this);
		ProdList.getInstance().loadInitFileIfEmpty(getResources());

		/*
		 * Set initial state of focus and ratio buttons
		 */
		setCarbUnitsName();
		setRadio();
		checkLastTouched();
		mText[mSequence[0]].requestFocus();

		mListAdapter = new SimpleCursorAdapter(getBaseContext(),
				android.R.layout.two_line_list_item, ProdList.getInstance()
						.getCoursorForRequest(null), new String[] {
						ProdList.PROD_NAME, ProdList.PROD_CARB }, new int[] {
						android.R.id.text1, android.R.id.text2 });
		ListView prodList = (ListView) findViewById(R.id.listProd);
		prodList.setAdapter(mListAdapter);
		prodList.setOnItemClickListener(mProductListener);

		// ProdList.getInstance().loadInitFile(getResources());
		// ProdList.getInstance().backupConfig();
	}

	public void setUnits(int new_unit_idx) {
		if (new_unit_idx != mUnitSetup) {
			int old_unit = UNIT_FACTOR[mUnitSetup];
			if (new_unit_idx != -1) {
				int new_unit = UNIT_FACTOR[new_unit_idx];
				Double carb = TextAndDifitsUtils.getPositiveDoubleValue(mText[N_CARB]);
				mUnitSetup = new_unit_idx;
				if (!carb.isNaN()) {
					mIsSetupProcess = true;
					TextAndDifitsUtils.setDoubleValue(mText[N_CARB], new Double((carb * old_unit)
							/ new_unit));
					mIsSetupProcess = false;
					Log.v(LOGTAG, "onActivityResult reset N_CARB");
				}
				saveAppState(); // Save new unit (and everything else)
				setCarbUnitsName();
			}
		}
	}

	public void setProdLang(String new_lang) {
		Log.v(LOGTAG, "setProdLang: '" + new_lang + "'");
		if (new_lang == null || new_lang.length() != 2)
			new_lang = Locale.getDefault().getLanguage();

		if (new_lang.equals(mProdLangSetup))
			return; /* nothing changed */
		mProdLangSetup = ProdList.getInstance().setNewProdLang(new_lang);
		mListAdapter.changeCursor(ProdList.getInstance().getCoursorForRequest(
				null));

		saveAppState();
	}

	/**
	 * Process results of setup
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		Log.v(LOGTAG, "onActivityResult...");
		if (intent != null) {
			if (requestCode == ACTIVITY_SETUP && resultCode == RESULT_OK) {
				Bundle bundle = intent.getExtras();
				int new_unit_idx = bundle.getInt(CONFIG_UNIT, -1);
				String new_lang = bundle.getString(CONFIG_LANG);
				setUnits(new_unit_idx);
				setProdLang(new_lang);
			}
			else if (requestCode == ACTIVITY_EDIT && resultCode == RESULT_OK)
			{
				mListAdapter.changeCursor(ProdList.getInstance().getCoursorForRequest(
						null));
			}
		}
	}

	/*
	 * Store state of the application
	 */
	private void saveAppState() {
		Log.v(LOGTAG, "saveAppState...");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(STATE_SEQ0, mSequence[0]);
		editor.putInt(STATE_SEQ0, mSequence[1]);
		editor.putInt(STATE_SEQ0, mSequence[2]);
		editor.putString(STATE_PROC, mText[N_PROC].getText().toString());
		editor.putString(STATE_TOTAL, mText[N_TOTAL].getText().toString());
		editor.putString(STATE_CARB, mText[N_CARB].getText().toString());
		editor.putInt(STATE_UNIT, mUnitSetup);
		editor.putString(STATE_LANG, mProdLangSetup);
		editor.commit();
		Log.v(LOGTAG, "saveAppState done");
	}

	/**
	 * Save state on stopping the application and everything else
	 */
	protected void onStop() {
		super.onStop();

		saveAppState();
	}

	/**
	 * Define menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SETUP, 0, R.string.MenuSetup).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ABOUT, 0, R.string.MenuAbout).setIcon(
				android.R.drawable.ic_menu_help);
		return true;
	}

	/**
	 * Menu actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case MENU_SETUP:
			intent = new Intent(this, SimpleCarboCalcSetup.class);
			intent.putExtra(CONFIG_UNIT, mUnitSetup);
			intent.putExtra(CONFIG_LANG, mProdLangSetup);
			startActivityForResult(intent, ACTIVITY_SETUP);
			return true;
		case MENU_ABOUT:
			Resources res = getResources();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					String.format("%s (%s): %s",
							res.getString(R.string.app_name),
							res.getString(R.string.app_ver),
							res.getString(R.string.About)))
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			;
			AlertDialog alert = builder.create();
			alert.show();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}