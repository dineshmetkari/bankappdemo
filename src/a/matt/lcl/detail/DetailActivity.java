package a.matt.lcl.detail;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import a.matt.lcl.R;
import a.matt.lcl.account.AbstractAccountLine;
import a.matt.lcl.account.AccountLoginData;
import a.matt.lcl.util.Ulog;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DetailActivity extends ListActivity implements Runnable{

	private List<AbstractDetailLine> detailList;
	private ListActivity la=this;
	private String nom="";
    
	// Dialogues
    public static final int DIALOG_LOAD =1;
    public static final int DIALOG_ERR =2;
    
    //Globals for thread
    private AccountLoginData loginData;
    private String url;
    private String cookie;
	
	@Override
	public void run() {
		detailList=null;
		int err=0;
		do {	
			err++;
			detailList=DetailHelper.getDetailInfos(loginData, url, cookie, true);
			if (null==detailList){ 
				SystemClock.sleep(1000);
				Ulog.e("Retry " + err);
			}
		}while ((null==detailList)&&(err<5));
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (null == detailList){
				showDialog(DIALOG_ERR);
				return;
			}
			dismissDialog(DIALOG_LOAD);
			DetailAdapter detailAdapter=new DetailAdapter(la);
			setListAdapter(detailAdapter);
			TextView titre=(TextView)findViewById(R.id.detailTitle);
			titre.setText("Détail " + nom);
		}
	};
	
	private void getDetailData(){
		showDialog(DIALOG_LOAD);
		
		Thread t = new Thread(this);
		t.start();
	}    
    
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Ulog.d ("DetailActivity onCreate - Called");
		Intent i = getIntent();
		BundleDetailDatas detail= (BundleDetailDatas)(i.getSerializableExtra("bundle"));
		this.cookie=detail.getCookie();
		this.url="https://particuliers.secure.lcl.fr" + detail.getUrl();
		this.loginData=detail.getLoginData();
		this.nom=detail.getNom();
		
		Ulog.d("Detail Activity url " + url);
		Ulog.d("Detail Activity cookie " + cookie);
		
		setContentView(R.layout.detail);
	}

	@Override
	public void onStart() {
		super.onStart();
		Ulog.d ("DetailActivity onStart - Called");
		getDetailData();
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
            	        	   DetailActivity.this.finish();
            	           }
            	       })
            	       .setNegativeButton("Réessayer", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   DetailActivity.this.getDetailData();
            	           }
            	       });
            	AlertDialog alert = builder.create();
                return alert;
        }
        return null;
    }

	class DetailAdapter extends ArrayAdapter<AbstractDetailLine> {
		Activity context;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM");
		DecimalFormat df=new DecimalFormat("#,##0.00");
		
		DetailAdapter(Activity context) {
			super(context, R.layout.detail_row, detailList);
			this.context=context;
		}
		
		public View getView(int position, View convertView,	ViewGroup parent) {
			View row;
			AbstractDetailLine line=detailList.get(position);
			
			if (line.getClass()==DetailLine.class){
				LayoutInflater inflater=context.getLayoutInflater();
				row=inflater.inflate(R.layout.detail_row, null);
				
				if (position%2!=0)
					row.setBackgroundColor(0xFFEEEEEE);
				
				TextView detailDate=(TextView)row.findViewById(R.id.detailDate);
				detailDate.setText(format.format(((DetailLine)line).getDate().getTime()));
				
				TextView detailIntitule=(TextView)row.findViewById(R.id.detailIntitule);
				detailIntitule.setText(((DetailLine)line).getIntitule());
				
				TextView detailMontant=(TextView)row.findViewById(R.id.detailMontant);
				detailMontant.setText(df.format(((DetailLine)line).getMontant()));

				return(row);
			}
			else if (line.getClass()==DetailHeader.class){
				if (line.getType()==AbstractAccountLine.TYPE_GLOBAL_HEADER){
					LayoutInflater inflater=context.getLayoutInflater();
					row=inflater.inflate(R.layout.detail_g_header, null);
					//row.setBackgroundColor(0x88555555);
					return(row);
				}
//				else if (line.getType()==AbstractAccountLine.TYPE_SECTION_HEADER){
//					LayoutInflater inflater=context.getLayoutInflater();
//					row=inflater.inflate(R.layout.main_s_header, null);
//					TextView label=(TextView)row.findViewById(R.id.main_s_label);
//					label.setText(((AccountHeader)line).getLabel());
//					row.setBackgroundColor(0x88555555);
//					return(row);
//				}
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
