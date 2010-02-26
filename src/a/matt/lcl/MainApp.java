package a.matt.lcl;

import java.util.GregorianCalendar;
import java.util.List;

import a.matt.lcl.account.AbstractAccountLine;
import a.matt.lcl.account.AccountHeader;
import a.matt.lcl.account.AccountHelper;
import a.matt.lcl.account.AccountLoginData;
import a.matt.lcl.account.AccountMainData;
import a.matt.lcl.account.SecuredStorage;
import a.matt.lcl.detail.BundleDetailDatas;
import a.matt.lcl.detail.DetailActivity;
import a.matt.lcl.util.Ulog;
import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainApp extends ListActivity implements Runnable{

    // Intent request codes
    public static final int I_REQUEST_FULL_CONFIG = 10;
    public static final int I_REQUEST_PASSWORD = 11;
    public static final int I_ACCOUNT_DETAILS = 12;
    
    // Extra codes
    public static final String E_ACCOUNT_DATA = "accdata";
    
    // Dialogues
    public static final int DIALOG_LOAD =1;
    public static final int DIALOG_ERR =2;
    
    // Globals
    private AccountLoginData accountLogin;
    private SecuredStorage securedStorage;
    private List<AbstractAccountLine> accountList;
    private AccountAdapter accountAdapter;
    private ListActivity la=this;
    
    // Menu
    private static final int MENU_CONFIG =1;
    
    
//    private  String  getStorageSecret(){
//    	TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
//    	return tm.getSubscriberId();
//    }
	@SuppressWarnings("unused")
	private boolean testTryPeriodExpired() {
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar exp = new GregorianCalendar(2010, 1, 31);

		Ulog.d("Test d'expiration ");

		if (now.getTimeInMillis() > exp.getTimeInMillis()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Application expirée").setMessage(
					"La durée d'évaluation de cette application a expiré")
					.setCancelable(false).setPositiveButton("Quitter",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									MainApp.this.finish();
								}
							});
			Ulog.d("Expiree");
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		Ulog.d("Pas expirée");
		return false;
	}
    
	private void callConfig(){
		Ulog.d("Appel de  callConfig");
		//showDialog(DIALOG_LOAD);
        Intent configIntent = new Intent(this, ConfigureAll.class);
        configIntent.putExtra(E_ACCOUNT_DATA, accountLogin);
        startActivityForResult(configIntent, I_REQUEST_FULL_CONFIG);
	}

	private void callConfigPass(){
		Ulog.d("Appel de  callConfigPass");
		//showDialog(DIALOG_LOAD);
        Intent configIntent = new Intent(this, ConfigurePass.class);
        configIntent.putExtra(E_ACCOUNT_DATA, accountLogin);
        startActivityForResult(configIntent, I_REQUEST_PASSWORD);
	}	
	
	private void persistAccountData(){
		AccountLoginData toStore = new AccountLoginData("","","");
		if (accountLogin.isStoreAccount()){
			toStore.setAgenceId(accountLogin.getAgenceId());
			toStore.setCompteId(accountLogin.getCompteId());
			toStore.setStoreAccount(true);
		}
		if (accountLogin.isStorePass()){
			toStore.setPassword(accountLogin.getPassword());
			toStore.setStorePass(true);
		}
		try {
			Ulog.d("Sauvegarde des données " + toStore.toString());
			securedStorage.storeObject(toStore);
		} catch (Exception e) {
			Ulog.e("Impossible de sauver les données");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		accountList=null;
		int err=0;
		do {	
			err++;
			accountList=AccountHelper.getAccountInfos(accountLogin);
			if (null==accountList){ 
				SystemClock.sleep(1000);
				Ulog.e("Retry " + err);
			}
		}while ((null==accountList)&&(err<3));
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			dismissDialog(DIALOG_LOAD);
			if (null == accountList){
				showDialog(DIALOG_ERR);
				return;
			}
			accountAdapter=new AccountAdapter(la);
			setListAdapter(accountAdapter);
		}
	};
	
	private void getAccountsData(){
		showDialog(DIALOG_LOAD);
		
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case I_REQUEST_FULL_CONFIG:
			if (resultCode == Activity.RESULT_OK) {
				this.accountLogin = (AccountLoginData) intent.getExtras().getSerializable(E_ACCOUNT_DATA);
				Ulog.d("retour accountData " + accountLogin.toString());
				persistAccountData();
				getAccountsData();
			}
		case I_REQUEST_PASSWORD:
			if (resultCode == Activity.RESULT_OK) {
				AccountLoginData partial=(AccountLoginData) intent.getExtras().getSerializable(E_ACCOUNT_DATA);
				this.accountLogin.setPassword(partial.getPassword());
				this.accountLogin.setStorePass(partial.isStorePass());
				Ulog.d("retour accountData " + accountLogin.toString());
				persistAccountData();
				getAccountsData();
			}			
			break;
		default:
		}
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_CONFIG, 0, "Config").setIcon(drawable.ic_menu_agenda);
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_CONFIG:
	    	callConfig();
	        return true;
	    }
	    return false;
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Ulog.d("MainApp onCreate - Called");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		setContentView(R.layout.main);
		//if (testTryPeriodExpired())	return;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Ulog.d("MainApp onStart - Called");
		//if (testTryPeriodExpired())	return;
		//On commence par regarder si on a les infos bancaires sauvegardées
		try {
			securedStorage= new SecuredStorage(this, "lcl.des", "passwdlcl");
		} catch (Exception e) {
			Ulog.e("Impossible de créer le securedstorage");
			e.printStackTrace();
			return;
		}
		try {
			accountLogin = (AccountLoginData)securedStorage.retrieveObject();
			Ulog.d("Account restauré " + accountLogin.toString());
		} catch (Exception e) {
			Ulog.e("Pas de données bancaires lues");
			accountLogin=new AccountLoginData("","","");
		}
				
		if ((accountLogin.getAgenceId().equals("")) || (accountLogin.getCompteId().equals(""))){
			Ulog.d("Donnees bancaires incompletes. Appel de la configuration");
			callConfig();
			return;
		}
		if ((accountLogin.getPassword().equals(""))){
			Ulog.d("Pas de mot de passe. Appel de la saisie du mot de passe");
			callConfigPass();
			return;
		}
		Ulog.d("onCreate accountData OK");		
		callConfigPass();		
	}
		
	@Override
	protected void onStop() {
		super.onStop();
		Ulog.d("MainApp onStop - Called");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Ulog.d("MainApp onPause - Called");
	}	
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	
    	switch (id) {
            case DIALOG_LOAD: 
            	Ulog.d("onCreateDialog Load");
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("Chargement...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
                 
            case DIALOG_ERR: 
            	Ulog.d("onCreateDialog Erreur");
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setTitle("Erreur")
            		   .setMessage("Vérifiez vos identifiants ou réessayez plus tard")
            	       .setCancelable(false)
            	       .setPositiveButton("Quitter", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   MainApp.this.finish();
            	           }
            	       })
            	       .setNegativeButton("Réessayer", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   MainApp.this.getAccountsData();
            	           }
            	       })
            	       .setNeutralButton("Identifiants", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   MainApp.this.callConfig();
            	           }
            	       });
            	AlertDialog alert = builder.create();
                return alert;
        }
        return null;
    }

     
	class AccountAdapter extends ArrayAdapter<AbstractAccountLine> {
		Activity context;
		
		AccountAdapter(Activity context) {
			super(context, R.layout.main_row, accountList);
			this.context=context;
		}
		
		public View getView(int position, View convertView,	ViewGroup parent) {
			View row;
			AbstractAccountLine line=accountList.get(position);
			
			if (line.getClass()==AccountMainData.class){
				LayoutInflater inflater=context.getLayoutInflater();
				row=inflater.inflate(R.layout.main_row, null);
				
				if (position%2!=0)
					row.setBackgroundColor(0xFFEEEEEE);
				
				TextView accountName=(TextView)row.findViewById(R.id.accountName);
				accountName.setText(((AccountMainData)line).getNom());
				
				TextView accountSolde=(TextView)row.findViewById(R.id.accountSolde);
				accountSolde.setText(Integer.toString(((AccountMainData)line).getBalance()));
				accountSolde.setTag(new Integer(position));
				accountSolde.setOnClickListener(new OnClickListener(){
		            public void onClick(View v) {
		            	
		                Integer position  = (Integer)(v.getTag());
		                Integer balance = ((AccountMainData)accountList.get(position)).getBalance();
		               
		                Ulog.d("Click Detail " + position + " " + balance);
		                BundleDetailDatas bundleDetail = new BundleDetailDatas();
		                bundleDetail.setCookie(((AccountHeader)accountList.get(0)).getCookie());
		                bundleDetail.setLoginData(accountLogin);
		                bundleDetail.setUrl(((AccountMainData)accountList.get(position)).getLienDetail());
		                bundleDetail.setNom(((AccountMainData)accountList.get(position)).getNom());
		                
		                Intent detailIntent=new Intent(getApplicationContext(), DetailActivity.class);
		                detailIntent.putExtra("bundle", bundleDetail);
		                startActivity(detailIntent);
		            }
		        } );


				
				TextView accountCb=(TextView)row.findViewById(R.id.accountCb);
				int cb=((AccountMainData)line).getEnCoursCb();
				if (0!=cb){
					accountCb.setText(Integer.toString((cb)));
				}else{
					accountCb.setText("");
				}
				return(row);
			}
			else if (line.getClass()==AccountHeader.class){
				if (line.getType()==AbstractAccountLine.TYPE_GLOBAL_HEADER){
					LayoutInflater inflater=context.getLayoutInflater();
					row=inflater.inflate(R.layout.main_g_header, null);
					//row.setBackgroundColor(0x88555555);
					return(row);
				}
				else if (line.getType()==AbstractAccountLine.TYPE_SECTION_HEADER){
					LayoutInflater inflater=context.getLayoutInflater();
					row=inflater.inflate(R.layout.main_s_header, null);
					TextView label=(TextView)row.findViewById(R.id.main_s_label);
					label.setText(((AccountHeader)line).getLabel());
					//row.setBackgroundColor(0x88555555);
					return(row);
				}
				return null;
			}
			else {
				return null;
			}
		}
		
		@Override
		public boolean  areAllItemsEnabled() {
		    return false;                       
		}
		
		@Override
		public boolean isEnabled(int position) {
		        return false;
		}
	}
}
