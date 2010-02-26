package a.matt.lcl.https;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

public class RequestProperties {
	private Map<String,String> properties;


	public RequestProperties() {
		super();
		properties=new HashMap<String,String>();
	}

	public void addProperty(String key, String value){
		properties.put(key, value);
	}
	
	public void clear(){
		properties.clear();
	}
	
	public void setProperties(HttpsURLConnection conn){
	    Iterator<Entry<String, String>> it = properties.entrySet().iterator();
	    Map.Entry<String,String> pairs;
	    while (it.hasNext()) {
	         pairs = it.next();
	         conn.setRequestProperty(pairs.getKey(),pairs.getValue());
	    }
	}
}
