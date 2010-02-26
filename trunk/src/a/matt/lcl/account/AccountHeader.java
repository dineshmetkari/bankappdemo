package a.matt.lcl.account;

public class AccountHeader extends AbstractAccountLine {
	private String Label;
	private String cookie;
	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}
	
}
