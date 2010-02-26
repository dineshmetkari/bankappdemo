package a.matt.lcl.https;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class PostFields {
	private Map<String,String> fields;
	private final static char ecom='&';
	private final static char equal='=';
	private final static String encodeType="UTF-8";

	public PostFields() {
		super();
		fields=new HashMap<String,String>();
	}

	public void addField(String key, String value){
		fields.put(key, value);
	}
	
	public void clear(){
		fields.clear();
	}
	
	
	public String getPostParameters(){
	    Iterator<Entry<String, String>> it = fields.entrySet().iterator();
	    StringBuilder sb= new StringBuilder();
	    Map.Entry<String,String> pairs;
	    while (it.hasNext()) {
	         pairs = it.next();
	         try {
	        	 sb.append(URLEncoder.encode(pairs.getKey(), encodeType));
	        	 sb.append(equal);
	        	 sb.append(URLEncoder.encode(pairs.getValue(), encodeType));
	         }catch (Exception e){
	 			 Log.e("LCL", "getPostParameters Exception " + e.getClass().getSimpleName() + " "
						+ e.getMessage());
	         }
	         if (it.hasNext()){
	        	 sb.append(ecom);
	         }
	    }
	    return sb.toString();
	}
}
