package a.matt.lcl.account;

import java.util.ArrayList;

import a.matt.lcl.https.HttpsHelper;
import a.matt.lcl.https.PostFields;
import a.matt.lcl.https.RequestProperties;
import a.matt.lcl.https.WebContent;
import a.matt.lcl.util.Ulog;

public class AccountHelper {
	

	private final static String markerDepotDebut="<!-- COMPTES DEPOT -->";
	private final static String markerDepotFin="<!-- << COMPTES DEPOT -->";

	private final static String markerEpargneDebut="<!-- COMPTES EPARGNE >> -->";
	private final static String markerEpargneFin="<!-- << COMPTES EPARGNE -->";
	
	public static ArrayList<AbstractAccountLine> getAccountInfos (AccountLoginData data){
		
		String url = "https://particuliers.secure.lcl.fr/everest/UWBI/UWBIAccueil?DEST=IDENTIFICATION";
		
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
		
		PostFields fields=new PostFields();
		fields.addField("agenceId", data.getAgenceId());
		fields.addField("compteId", data.getCompteId());
		fields.addField("CodeId", data.getPassword());
		fields.addField("x", "2");
		fields.addField("y", "32");
		
		WebContent content = HttpsHelper.doPost(url, null, props, fields, false);
		if ((null==content)||(null==content.getCookie())){
			Ulog.e("Login post failed");
			return null;
		}
		
		url = "https://particuliers.secure.lcl.fr/outil/UWSP/Synthese/accesSynthese";
		WebContent content1= HttpsHelper.doGet(url, content.getCookie(),props);
		if (null==content1){
			Ulog.e("Pas de contenu");
			return null;
		}
		
		url = "https://particuliers.secure.lcl.fr/everest/UWBI/UWBIAccueil?DEST=LOGOUT";
		HttpsHelper.doGet(url, content.getCookie(), props);
		
		int start = content1.getPage().indexOf(markerDepotDebut);
		if (-1==start){
			Ulog.e("Pas de marker de début");
			return null;
		}
		int end=content1.getPage().indexOf(markerDepotFin, start);
		if (-1==end){
			Ulog.e("Pas de marker de fin");
			return null;
		}
		String comptesDepot= content1.getPage().substring(start + markerDepotDebut.length(), end);
		
		ArrayList<AbstractAccountLine> mainDataArray=new ArrayList<AbstractAccountLine>();
		AccountMainData mainData;
		
		AccountHeader gHeader = new AccountHeader();
		gHeader.setType(AbstractAccountLine.TYPE_GLOBAL_HEADER);
		gHeader.setCookie(content.getCookie());
		mainDataArray.add(gHeader);
		
		int i=0;
		while (null!=(mainData=parseTrAccount(comptesDepot,1 + i++))){
			if (1==i){
				AccountHeader header = new AccountHeader();
				header.setType(AbstractAccountLine.TYPE_SECTION_HEADER);
				header.setLabel("Comptes de dépôt");
				mainDataArray.add(header);
			}
			mainDataArray.add(mainData);
		}
		
		
		start = content1.getPage().indexOf(markerEpargneDebut);
		if (-1==start){
			return mainDataArray;
		}
		end=content1.getPage().indexOf(markerEpargneFin, start);
		if (-1==end){
			return mainDataArray;
		}
		String comptesEpargne= content1.getPage().substring(start + markerEpargneDebut.length(), end);
		
		i=0;
		while (null!=(mainData=parseTrAccount(comptesEpargne,1 + i++))){
			if (1==i){
				AccountHeader header = new AccountHeader();
				header.setType(AbstractAccountLine.TYPE_SECTION_HEADER);
				header.setLabel("Comptes d'épargne");
				mainDataArray.add(header);
			}
			mainData.setEnCoursCb(0);
			mainDataArray.add(mainData);
		}
		return mainDataArray;
	}
	
	private static AccountMainData parseTrAccount(String htmlBloc,int n){
		int index=0;
		for (int i=0;i<n;i++){
			index=htmlBloc.indexOf("<tr class=",index);
			if (-1==index){
				Ulog.d("Plus de comptes");
				return null;
			}
			index++;
		}
		index=htmlBloc.indexOf("<td", ++index);
		index=htmlBloc.indexOf("<td", ++index);
		index=htmlBloc.indexOf(">", ++index);
		int debutCompte=index+1;
		index=htmlBloc.indexOf("<", ++index);
		int finCompte=index;
		
		String compte=htmlBloc.substring(debutCompte, finCompte);
		compte=compte.replaceAll("&nbsp;", "");
		Ulog.d("Compte " + compte);
		
		index=htmlBloc.indexOf("href=\"/",++index);
		int debutLien=index+6;
		index=htmlBloc.indexOf(">",++index);
		int finLien=index-1;
		String lien=htmlBloc.substring(debutLien,finLien);
		lien=lien.replace("nature=06", "mode=45");
		
		Ulog.d("Lien " + lien);
		
		int debutSolde=++index;
		index=htmlBloc.indexOf("</a>",++index);
		int finSolde=index;
		String solde=htmlBloc.substring(debutSolde,finSolde);
		solde=solde.trim();
		solde=solde.replaceAll(" ", "");
		solde=solde.replaceAll("\r", "");
		solde=solde.replaceAll("\t", "");
		char sign=solde.charAt(solde.length()-1);
		solde=solde.replaceAll("[^0-9]", "");
		
		int isolde=Integer.parseInt(solde);
		if (sign == '-') isolde=-isolde;
		isolde=isolde/100;
		
		Ulog.d("isolde<"+isolde+">");
		
		index=htmlBloc.indexOf("href=",++index);
		int debutLienCB=index+6;
		index=htmlBloc.indexOf(">",++index);
		int finLienCB=index-1;
		String lienCB=htmlBloc.substring(debutLienCB,finLienCB);
		Ulog.d("LienCB " + lienCB);
				
		int debutEnCours=++index;
		index=htmlBloc.indexOf("</a>",++index);
		int finEnCours=index;
		String enCours=htmlBloc.substring(debutEnCours,finEnCours);
		enCours=enCours.trim();
		enCours=enCours.replaceAll(" ", "");
		enCours=enCours.replaceAll("\r", "");
		enCours=enCours.replaceAll("\t", "" 	);
		
		char signEnCours;
		int ienCours;
		try{
			signEnCours=enCours.charAt(enCours.length()-1);
			enCours=enCours.replaceAll("[^0-9]", "");
			ienCours=Integer.parseInt(enCours);
			if (signEnCours == '-') ienCours=-ienCours;
				ienCours=ienCours/100;
		}catch (Exception e){
			ienCours=0;
		}
		Ulog.d("ienCours<"+ienCours+">");
		AccountMainData mainData=new AccountMainData();
		mainData.setNom(compte);
		mainData.setBalance(isolde);
		mainData.setEnCoursCb(ienCours);
		mainData.setLienDetail(lien);
		mainData.setLienDetailCb(lienCB);
		return mainData;
	}
}
