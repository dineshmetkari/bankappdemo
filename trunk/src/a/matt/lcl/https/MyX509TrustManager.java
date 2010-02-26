package a.matt.lcl.https;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MyX509TrustManager implements javax.net.ssl.X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		X509Certificate[] retVal = new X509Certificate[0];
		return retVal;
	}

}
