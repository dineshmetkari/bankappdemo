package a.matt.lcl.account;

import java.io.Serializable;

public class AccountLoginData implements Serializable  {
	
	private static final long serialVersionUID = 1L;

	private String agenceId = "";
	private String compteId = "";
	private String password = "";
	
	private boolean storeAccount=false;
	private boolean storePass=false;
	
	public String toString(){
		return "Agence " + agenceId + " Compte " + compteId + " Pass " + password;
	}
	
	public AccountLoginData(String agenceId, String compteId, String password) {
		super();
		this.agenceId = agenceId;
		this.compteId = compteId;
		this.password = password;
	}

	public AccountLoginData() {
		super();
	}

	public String getAgenceId() {
		return agenceId;
	}

	public void setAgenceId(String agenceId) {
		this.agenceId = agenceId;
	}

	public String getCompteId() {
		return compteId;
	}

	public void setCompteId(String compteId) {
		this.compteId = compteId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isStoreAccount() {
		return storeAccount;
	}

	public void setStoreAccount(boolean storeAccount) {
		this.storeAccount = storeAccount;
	}

	public boolean isStorePass() {
		return storePass;
	}

	public void setStorePass(boolean storePass) {
		this.storePass = storePass;
	}
	
}
