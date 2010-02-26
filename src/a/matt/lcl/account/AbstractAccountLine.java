package a.matt.lcl.account;

public abstract class AbstractAccountLine {
	public final static int TYPE_GLOBAL_HEADER = 1;
	public final static int TYPE_SECTION_HEADER = 2;
	public final static int TYPE_MAIN_ACCOUNT = 3;

	private int type; 

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
