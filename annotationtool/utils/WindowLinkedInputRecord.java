package utils;

public class WindowLinkedInputRecord extends InputRecord {
	
	private double windowXPos = -1;
	private double windowYPos = -1;
	private double windowWidth = -1;
	private double windowHeight = -1;
	
	/**
	 * Creates an Input Record that also stores information about the window linked to the annotation tool.
	 * 
	 * @param inputRecord The input record to build off of.
	 * @param windowXPos The X position of the linked window.
	 * @param windowYPos The Y position of the linked window.
	 * @param windowWidth The width of the linked window.
	 * @param windowHeight The height of the linked window.
	 */
	public WindowLinkedInputRecord(InputRecord inputRecord, double windowXPos, double windowYPos, double windowWidth, double windowHeight) {
		super(inputRecord.getEventType(), inputRecord.getInputTime(), inputRecord.getxPos(), inputRecord.getyPos(), inputRecord.getInput());
		this.setRecordType("WindowLinkedInputRecord");
		this.windowXPos = windowXPos;
		this.windowYPos = windowYPos;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}
	
	/**
	 * Gets the relative X position of the mouse, based on the size of the window.
	 * 
	 * @return the relative X position of the mouse, based on the size of the window.
	 */
	public double getXPosRatio() {
		double adjustedInputXPos = this.getxPos() - windowXPos;
		return adjustedInputXPos/(double)windowWidth;
	}
	
	/**
	 * Gets the relative Y position of the mouse, based on the size of the window.
	 * 
	 * @return the relative Y position of the mouse, based on the size of the window.
	 */
	public double getYPosRatio() {
		double adjustedInputYPos = this.getyPos() - windowYPos;
		return adjustedInputYPos/(double)windowWidth;
	}
	
	/**
	 * Gets the leftmost position of the window.
	 * 
	 * @return the leftmost position of the window.
	 */
	public double getWindowXPos() {
		return windowXPos;
	}
	
	/**
	 * Gets the topmost position of the window.
	 * 
	 * @return the topmost position of the window.
	 */
	public double getWindowYPos() {
		return windowYPos;
	}
	
	/**
	 * Gets the width of the window.
	 * 
	 * @return the width of the window.
	 */
	public double getWindowWidth() {
		return windowWidth;
	}
	
	/**
	 * Gets the height of the window.
	 * 
	 * @return the height of the window.
	 */
	public double getWindowHeight() {
		return windowHeight;
	}
}
