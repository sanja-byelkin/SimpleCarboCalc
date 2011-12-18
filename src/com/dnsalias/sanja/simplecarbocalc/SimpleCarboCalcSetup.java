/**
 * 
 */
package com.dnsalias.sanja.simplecarbocalc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

/**
 * @author Oleksander "Sanja" Byelkin
 *
 */
public class SimpleCarboCalcSetup extends Activity {

	private Spinner mSetupUnit;
	private Button mConfirm;
	private Button mCancel;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simplecarbocalcsetup);
        setTitle(R.string.setup_name);

        mSetupUnit= (Spinner) findViewById(R.id.setup_unit);
        mConfirm= (Button) findViewById(R.id.setup_confirm);
        mCancel= (Button) findViewById(R.id.setup_cancel);

        Bundle extras= getIntent().getExtras();
        if (extras != null) {
        	mSetupUnit.setSelection(extras.getInt(SimpleCarboCalcActivity.CONFIG_UNIT));
        }
        
        mConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Bundle bundle= new Bundle();

                bundle.putInt(SimpleCarboCalcActivity.CONFIG_UNIT, mSetupUnit.getSelectedItemPosition());

                Intent intent = new Intent();
                intent.putExtras(bundle);
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
