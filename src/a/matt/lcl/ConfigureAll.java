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

public class ConfigureAll extends Activity implements OnClickListener {
	AccountLoginData accountData=null;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Have the system blur any windows behind this one.
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        
        accountData = (AccountLoginData)getIntent().getSerializableExtra(MainApp.E_ACCOUNT_DATA);
        
        if (null==accountData) return;
        
        Ulog.d("AccountData reçu par configureAll : " + accountData.toString());
        
        setContentView(R.layout.config_all);
        EditText editAgence=(EditText)findViewById(R.id.AgenceEdit);
        editAgence.setText(accountData.getAgenceId());
        
        EditText editCompte=(EditText)findViewById(R.id.CompteEdit);
        EditText editCompteCle=(EditText)findViewById(R.id.CompteEditkey);
        String compte=accountData.getCompteId();
        try{
	        editCompte.setText(compte.substring(0, compte.length()-1));
	        editCompteCle.setText(compte.substring(compte.length()-1, compte.length()));
	    }
        catch (Exception e){
	        editCompte.setText("");
	        editCompteCle.setText("");
        }
        EditText editPass=(EditText)findViewById(R.id.PassEdit);
        editPass.setText(accountData.getPassword());
        
        CheckBox saveAcc=(CheckBox)findViewById(R.id.SaveAccount);
        saveAcc.setChecked(accountData.isStoreAccount());
        
        CheckBox savePass=(CheckBox)findViewById(R.id.SavePass);
        savePass.setChecked(accountData.isStorePass());
        
        Button configOk = (Button)findViewById(R.id.ConfigAllSave);
        configOk.setOnClickListener(this);
        
        Button configCancel = (Button)findViewById(R.id.Cancel);
        configCancel.setOnClickListener(this);
        
    }

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.ConfigAllSave) {
			Ulog.d("ConfigureAll click sur OK");

			EditText editAgence = (EditText) findViewById(R.id.AgenceEdit);
			accountData.setAgenceId(editAgence.getText().toString());

			EditText editCompte = (EditText) findViewById(R.id.CompteEdit);
			EditText editCompteCle = (EditText) findViewById(R.id.CompteEditkey);
			accountData.setCompteId(editCompte.getText().toString()
					+ editCompteCle.getText().toString());

			EditText editPass = (EditText) findViewById(R.id.PassEdit);
			accountData.setPassword(editPass.getText().toString());

			CheckBox saveAccount = (CheckBox) findViewById(R.id.SaveAccount);
			accountData.setStoreAccount(saveAccount.isChecked());

			CheckBox savePass = (CheckBox) findViewById(R.id.SavePass);
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
