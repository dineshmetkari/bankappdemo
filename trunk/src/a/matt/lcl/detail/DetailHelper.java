package a.matt.lcl.detail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import a.matt.lcl.account.AbstractAccountLine;
import a.matt.lcl.account.AccountLoginData;
import a.matt.lcl.https.HttpsHelper;
import a.matt.lcl.https.PostFields;
import a.matt.lcl.https.RequestProperties;
import a.matt.lcl.https.WebContent;
import a.matt.lcl.util.Ulog;

public class DetailHelper {
	
	private final static String markerDetailDebut="<WAP>";
	private final static String markerDetailFin="</WAP>";
	
	public static ArrayList<AbstractDetailLine> getDetailInfos (AccountLoginData data,String url, String cookie, boolean relog){
		
		
		String cookieNew=cookie;
		
		RequestProperties props=new RequestProperties();
		props.addProperty("host", "particuliers.secure.lcl.fr");
		props.addProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6 GTB5 (.NET CLR 3.5.30729)");
		props.addProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		props.addProperty("Accept-Language", "fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3");
		//props.addProperty("Accept-Encoding", "gzip,deflate");
		props.addProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		props.addProperty("Keep-Alive", "300");
		props.addProperty("Connection", "keep-alive");
		props.addProperty("Referer", "https://particuliers.secure.lcl.fr/index.html");
		props.addProperty("Content-Type",	"application/x-www-form-urlencoded");
		
		if (relog){
			String urlLogin = "https://particuliers.secure.lcl.fr/everest/UWBI/UWBIAccueil?DEST=IDENTIFICATION";

			PostFields fields=new PostFields();
			fields.addField("agenceId", data.getAgenceId());
			fields.addField("compteId", data.getCompteId());
			fields.addField("CodeId", data.getPassword());
			fields.addField("x", "2");
			fields.addField("y", "32");
			
			WebContent content = HttpsHelper.doPost(urlLogin, null, props, fields, false);
			if ((null==content)||(null==content.getCookie())){
				Ulog.e("Login post failed");
				return null;
			}
			cookieNew=content.getCookie();
		}
		
		WebContent content1= HttpsHelper.doGet(url, cookieNew,props);
		if (null==content1){
			Ulog.e("Pas de contenu");
			return null;
		}
		
		if (relog){
			url = "https://particuliers.secure.lcl.fr/everest/UWBI/UWBIAccueil?DEST=LOGOUT";
			HttpsHelper.doGet(url,cookieNew, props);
		}
				
		int start = content1.getPage().indexOf(markerDetailDebut);
		if (-1==start){
			Ulog.e("Pas de marker de debut");
			return null;
		}
		int end=content1.getPage().indexOf(markerDetailFin, start);
		if (-1==end){
			Ulog.e("Pas de marker de fin");
			return null;
		}
		String detail= content1.getPage().substring(start + markerDetailDebut.length(), end);
		ArrayList<AbstractDetailLine> detailArray=new ArrayList<AbstractDetailLine>();
		DetailLine detailLine;
		
		DetailHeader gHeader = new DetailHeader();
		gHeader.setType(AbstractAccountLine.TYPE_GLOBAL_HEADER);
		detailArray.add(gHeader);
		
		int i=0;
		while (null!=(detailLine=parseTrDetail(detail,1 + i++))){
			detailArray.add(detailLine);
		}
		return detailArray;
	}
	
	private static DetailLine parseTrDetail(String htmlBloc,int n){
		int index=0;
		for (int i=0;i<n;i++){
			index=htmlBloc.indexOf("<tr class=",index);
			if (-1==index){
				Ulog.d("Plus de details");
				return null;
			}
			index++;
		}
		index=htmlBloc.indexOf("<td", ++index);
		index=htmlBloc.indexOf(">", ++index);
		int debutDate=index+1;
		index=htmlBloc.indexOf("&nbsp;</td>", ++index);
		int finDate=index;
		index+="&nbsp;</td>".length();
		index=htmlBloc.indexOf("<", ++index);
		index=htmlBloc.indexOf(">", ++index);
		index=htmlBloc.indexOf(">", ++index);
		int debutIntitule=index+1;
		index=htmlBloc.indexOf("<", ++index);
		int finIntitule=index;
		
		index=htmlBloc.indexOf("<td align=\"right\">", ++index);
		index=htmlBloc.indexOf(">", ++index);		
		int debutDebit=index+1;
		index=htmlBloc.indexOf("</td>", ++index);
		int finDebit=index;
		index=htmlBloc.indexOf("<td", ++index);
		index=htmlBloc.indexOf(">", ++index);		
		int debutCredit=index+1;
		index=htmlBloc.indexOf("</td>", ++index);
		int finCredit=index;		
		
		String date=htmlBloc.substring(debutDate, finDate);
		date=date.replaceAll("&nbsp;", "").replaceAll("<b>", "").replaceAll("/b>", "");;
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
		GregorianCalendar cal=new GregorianCalendar();
		try {
			cal.setTime(format.parse(date));
		} catch (ParseException e1) {}
		//Ulog.d("Date " + format.format(cal.getTime()));
		
		String intitule=htmlBloc.substring(debutIntitule, finIntitule);
		intitule=intitule.trim();
		intitule=intitule.replaceAll("\r", "");
		intitule=intitule.replaceAll("\t", "");
		intitule=intitule.replaceAll(" +", " ");
		intitule=intitule.replaceAll("&nbsp;", "");
		//Ulog.d("Intitule " + intitule);
		
		
		float fDebit;
		try{
			String debit=htmlBloc.substring(debutDebit, finDebit);
			index=debit.indexOf("<b>");
			if (-1!=index) {
				debutDebit=index+3;
				index=debit.indexOf("</b>");
				if (-1!=index) {
					finDebit=index;
					debit=debit.substring(debutDebit, finDebit);
				}
			}
			debit=debit.trim();
			debit=debit.replaceAll(" ", "");
			debit=debit.replaceAll("\r", "");
			debit=debit.replaceAll("\t", "");
			debit=debit.replaceAll("[^0-9]", "");

			//Ulog.d("Debit lu " + debit);
			int iDebit=Integer.parseInt(debit);
			fDebit=iDebit/-100f;
		} catch (Exception e){
			fDebit=0f;
		}
		//Ulog.d("Debit " + fDebit);

		float fCredit;
		try{
			String credit=htmlBloc.substring(debutCredit, finCredit);
			credit=credit.trim();
			credit=credit.replaceAll(" ", "");
			credit=credit.replaceAll("\r", "");
			credit=credit.replaceAll("\t", "");
			credit=credit.replaceAll("[^0-9]", "");
			//Ulog.d("Credit lu " + credit);
			int iCredit=Integer.parseInt(credit);
			fCredit=iCredit/100f;
		} catch (Exception e){
			fCredit=0f;
		}
		//Ulog.d("credit " + fCredit);		
		
		float fMontant;
		if ((fCredit==0f)&&(fDebit!=0f))fMontant=fDebit;
		else if ((fCredit!=0f)&&(fDebit==0f))fMontant=fCredit;
		else fMontant=0f;

		DetailLine detailLine=new DetailLine();
		
		detailLine.setDate(cal);
		detailLine.setIntitule(intitule);
		detailLine.setMontant(fMontant);

		return detailLine;
	}
}
