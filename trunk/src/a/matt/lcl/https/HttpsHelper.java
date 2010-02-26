package a.matt.lcl.https;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import a.matt.lcl.util.Ulog;
import android.os.SystemClock;

public class HttpsHelper {
	
	private static String readCookie(HttpsURLConnection conn) {
		String key;
		String value;
		for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; ++i) {
			key = key.toLowerCase();
			if (key.equalsIgnoreCase("Set-Cookie")) {
				Ulog.d("Cookie value is " + conn.getHeaderField(i));
				value = conn.getHeaderField(i);
				return value;
			}
		}
		return null;
	}

	public static WebContent doGet(String url,String cookie,RequestProperties requestProperties) {
		InputStream stream = null;
		BufferedReader buffer = null;
		StringBuilder sb = new StringBuilder();
		WebContent webContent;
		
		Ulog.d("WebHelper.doGet Called");

		try {
			URL uurl = new URL(url);
			HttpsURLConnection conn;
			MyHostnameVerifier mhnv = new MyHostnameVerifier();
			HttpsURLConnection.setDefaultHostnameVerifier(mhnv);
			MyX509TrustManager[] x509TM = new MyX509TrustManager[] { new MyX509TrustManager() };
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, x509TM, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			HttpsURLConnection.setFollowRedirects(false);
			

			conn = (HttpsURLConnection) uurl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);
		
			if( cookie != null )      
				conn.setRequestProperty("Cookie",cookie); 
			
			if (null!=requestProperties)
				requestProperties.setProperties(conn);
			
			conn.connect();
			
			for (int err=0;(-1==conn.getResponseCode())&&(err<10);err++){
				Ulog.e("No response code... waiting...");
				SystemClock.sleep(100);
			}
			if (conn.getResponseCode()!=HttpsURLConnection.HTTP_OK){
				Ulog.e("Response Code " + conn.getResponseCode());
				return null;
			}
			webContent=new WebContent();
			webContent.setCookie(readCookie(conn));
			
			stream = conn.getInputStream();
			buffer = new BufferedReader(new InputStreamReader(stream));
			String inputLine;
						
			while ((inputLine = buffer.readLine()) != null) {
				    //Ulog.d("---> " + inputLine);
					sb.append(inputLine);
			}
			webContent.setPage(sb.toString());
			buffer.close();
			return webContent;
		} catch (Exception e) {
			Ulog.e("WebHelper.doGet Exception " + e.getClass().getSimpleName() + " "
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public static WebContent doPost(String url,String cookie,RequestProperties requestProperties, PostFields postFields,boolean noContent) {
		HttpsURLConnection conn;
		WebContent webContent=null;

		Ulog.d("doPost Called");
		try {			
			URL uurl = new URL(url);
			MyHostnameVerifier mhnv = new MyHostnameVerifier();
			HttpsURLConnection.setDefaultHostnameVerifier(mhnv);
			MyX509TrustManager[] x509TM = new MyX509TrustManager[] { new MyX509TrustManager() };
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, x509TM, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			HttpsURLConnection.setFollowRedirects(false);

			conn = (HttpsURLConnection) uurl.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			if( cookie != null )      
				conn.setRequestProperty("Cookie",cookie); 
			
			if (null!=requestProperties)
				requestProperties.setProperties(conn);

			conn.connect();
			
			// Send Post fields
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(postFields.getPostParameters());
			wr.flush();
			wr.close();
			
			for (int err=0;(-1==conn.getResponseCode())&&(err<10);err++){
				Ulog.e("No response code... waiting...");
				SystemClock.sleep(100);
			}
			if (conn.getResponseCode()!=HttpsURLConnection.HTTP_OK){
				Ulog.e("Response Code " + conn.getResponseCode());
				return null;
			}
			
			webContent=new WebContent();
			webContent.setCookie(readCookie(conn));
			if (noContent){
				webContent.setPage(null);
				return webContent;
			}
			else{
				InputStream stream = conn.getInputStream();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
				String inputLine;
				StringBuilder sb=new StringBuilder();
	
				while ((inputLine = buffer.readLine()) != null) {
					//Ulog.d( "---> " + inputLine);
					sb.append(inputLine);
				}
				webContent.setPage(sb.toString());
				buffer.close();
				return webContent;
			}
		} catch (Exception e) {
			Ulog.e("Exception doPost" + e.getClass().getSimpleName() + " "
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}	