package a.matt.lcl.detail;

public abstract class AbstractDetailLine {
	public final static int TYPE_GLOBAL_HEADER = 1;
	public final static int TYPE_SECTION_HEADER = 2;
	public final static int TYPE_DATA = 3;

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
