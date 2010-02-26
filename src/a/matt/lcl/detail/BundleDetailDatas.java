package a.matt.lcl.detail;

import java.io.Serializable;

import a.matt.lcl.account.AccountLoginData;

public class BundleDetailDatas implements Serializable {

	private static final long serialVersionUID = 1L;
	private AccountLoginData loginData ;
	private String url;
	private String cookie;
	private String nom;
	
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public AccountLoginData getLoginData() {
		return loginData;
	}
	public void setLoginData(AccountLoginData loginData) {
		this.loginData = loginData;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
}
