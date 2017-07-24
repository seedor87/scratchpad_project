package util;

public class WindowInfo {
	private long parentWindowId;
	private long titleWindowId;
	private String title = "";
	
	public WindowInfo(long parentWindowId, long titleWindowId, String title) {
		this.parentWindowId = parentWindowId;
		this.titleWindowId = titleWindowId;
		this.title = title;
	}
	
	public int[] getDimensions() {
		X11InfoGatherer gatherer = X11InfoGatherer.getX11InfoGatherer();
		return gatherer.getWindowAttributes(parentWindowId);
	}
	
	public String updateTitle() {
		X11InfoGatherer gatherer = X11InfoGatherer.getX11InfoGatherer();
		title = gatherer.getWindowName(titleWindowId);
		return title;
	}
	
	public long getParentWindowId() {
		return parentWindowId;
	}
	
	public long getTitleWindowId() {
		return titleWindowId;
	}
	
	public String getTitle() {
		return title;
	}
}
