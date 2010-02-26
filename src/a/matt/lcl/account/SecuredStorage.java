package a.matt.lcl.account;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import a.matt.lcl.util.Ulog;
import android.app.Activity;
import android.app.Service;

public class SecuredStorage {

	private String fileName;
	private SecretKey secretKey;
	private Cipher desCipher;
	Service service;
	Activity activity;

	private void constructorCommon(String fileName, String password)
			throws Exception {
		try {
			this.fileName = fileName;
			// Create Key
			// Password must be at least 8 characters
			byte key[] = password.getBytes();
			DESKeySpec desKeySpec = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			secretKey = keyFactory.generateSecret(desKeySpec);

			// Create Cipher
			desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		} catch (Exception e) {
			Ulog.e("SecuredStorage.SecuredStorage"
					+ e.getClass().getSimpleName() + " " + e.getMessage());
			throw (e);
		}
	}

	public SecuredStorage(Activity activity, String fileName, String password)
			throws Exception {
		try {
			this.activity = activity;
			this.service = null;
			constructorCommon(fileName, password);
		} catch (Exception e) {
			throw (e);
		}
	}

	public SecuredStorage(Service service, String fileName, String password)
			throws Exception {
		try {
			this.service = service;
			this.activity = null;
			constructorCommon(fileName, password);
		} catch (Exception e) {
			throw (e);
		}
	}

	public void storeObject(Serializable object) throws Exception {
		try {
			desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			// Create stream
			OutputStream os;
			if (null != service) {
				os = service.openFileOutput(fileName, 0);
			} else {
				os = activity.openFileOutput(fileName, 0);
			}
			BufferedOutputStream bos = new BufferedOutputStream(os);
			CipherOutputStream cos = new CipherOutputStream(bos, desCipher);
			ObjectOutputStream oos = new ObjectOutputStream(cos);
			
			// Write objects
			oos.writeObject(object);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			Ulog.e("SecuredStorage.storeObject"
					+ e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
	}

	public Serializable retrieveObject() throws Exception {
		try {
			desCipher.init(Cipher.DECRYPT_MODE, secretKey);
			// Create stream
			InputStream is;
			if (null != service) {
				is = service.openFileInput(fileName);
			} else {
				is = activity.openFileInput(fileName);
			}
			BufferedInputStream bis = new BufferedInputStream(is);
			CipherInputStream cis = new CipherInputStream(bis, desCipher);
			ObjectInputStream ois = new ObjectInputStream(cis);

			// Read objects
			Serializable obj = (Serializable) ois.readObject();
			return obj;
		} catch (Exception e) {
			Ulog.e("SecuredStorage.retrieveObject"
					+ e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
	}
}