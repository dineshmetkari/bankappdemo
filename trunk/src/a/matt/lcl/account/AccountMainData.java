package a.matt.lcl.account;

public class AccountMainData extends AbstractAccountLine {
	private String nom;
	private int balance;
	private int enCoursCb;
	private String lienDetail;
	private String lienDetailCb;

	public AccountMainData() {
		super();
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getEnCoursCb() {
		return enCoursCb;
	}

	public void setEnCoursCb(int enCoursCb) {
		this.enCoursCb = enCoursCb;
	}

	public String getLienDetail() {
		return lienDetail;
	}

	public void setLienDetail(String lienDetail) {
		this.lienDetail = lienDetail;
	}

	public String getLienDetailCb() {
		return lienDetailCb;
	}

	public void setLienDetailCb(String lienDetailCb) {
		this.lienDetailCb = lienDetailCb;
	}
}
