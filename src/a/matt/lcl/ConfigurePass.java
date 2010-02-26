package a.matt.lcl;

import a.matt.lcl.account.AccountLoginData;
import a.matt.lcl.util.Ulog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class ConfigurePass extends Activity implements OnClickListener {
	AccountLoginData accountData=null;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Have the system blur any windows behind this one.
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        
        accountData = (AccountLoginData)getIntent().getSerializableExtra(MainApp.E_ACCOUNT_DATA);
        
        if (null==accountData) return;
        
        Ulog.d("AccountData reçu par configurePass : " + accountData.toString());
        
        setContentView(R.layout.config_pass);
 
        EditText editPass=(EditText)findViewById(R.id.PassEdit1);
        editPass.setText(accountData.getPassword());
                
        CheckBox savePass=(CheckBox)findViewById(R.id.SavePass1);
        savePass.setChecked(accountData.isStorePass());
        
        Button configOk = (Button)findViewById(R.id.ConfigPass);
        configOk.setOnClickListener(this);
        
        Button configCancel = (Button)findViewById(R.id.Cancel1);
        configCancel.setOnClickListener(this);        
        
    }

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.ConfigPass) {
			Ulog.d("ConfigurePass click sur OK");

			EditText editPass = (EditText) findViewById(R.id.PassEdit1);
			accountData.setPassword(editPass.getText().toString());

			CheckBox savePass = (CheckBox) findViewById(R.id.SavePass1);
			accountData.setStorePass(savePass.isChecked());
			
			Bundle bundle = new Bundle();
			bundle.putSerializable(MainApp.E_ACCOUNT_DATA, accountData);

			Intent mIntent = new Intent();
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
			finish();
		}
		setResult(RESULT_CANCELED);
		finish();
	}
}
