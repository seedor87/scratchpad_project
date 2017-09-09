package util;

public class WindowInfo {
	private long parentWindowId;
	private long titleWindowId;
	private String title = "";
	
	/**
	 * The WindowInfo class keeps track of a window's outermost ID, the window that contains its title's ID, and its title.
	 * 
	 * @param parentWindowId The ID of the window that manages the dimensions of the visible window.
	 * @param titleWindowId The ID of the window that manages the title of the visible window.
	 * @param title The title of the window.
	 */
	public WindowInfo(long parentWindowId, long titleWindowId, String title) {
		this.parentWindowId = parentWindowId;
		this.titleWindowId = titleWindowId;
		this.title = title;
	}
	
	/**
	 * Gets the dimensions of this window.
	 * 
	 * @return An array containing the width, height, x, and y attributes of a window, as ints.
	 */
	public int[] getDimensions() {
		X11InfoGatherer gatherer = new X11InfoGatherer();
		return gatherer.getWindowAttributes(parentWindowId);
	}
	
	/**
	 * Returns the most current title of the window, updating the instance variable.
	 * 
	 * @return The most current title of the window.
	 */
	public String updateTitle() {
		X11InfoGatherer gatherer = new X11InfoGatherer();
		title = gatherer.getWindowName(titleWindowId);
		return title;
	}
	
	/**
	 * Returns the ID of the window controlling the dimensions of the visible window.
	 * 
	 * @return the ID of the window controlling the dimensions of the visible window.
	 */
	public long getParentWindowId() {
		return parentWindowId;
	}
	
	/**
	 * Returns the ID of the window controlling the title of the visible window.
	 * 
	 * @return the ID of the window controlling the title of the visible window.
	 */
	public long getTitleWindowId() {
		return titleWindowId;
	}
	
	/**
	 * Returns the title of the window.
	 * 
	 * @return The title of the window.
	 */
	public String getTitle() {
		return title;
	}
}
