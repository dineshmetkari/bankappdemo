package a.matt.lcl.util;

import android.util.Log;

public class Ulog {
	private final static boolean D=true;
	private final static String app="LCL";
	
	public static void d(String msg){
		if (D)Log.d(app,msg);
	}
	
	public static void e(String msg){
		if (D)Log.d(app,msg);
	}
	
}
