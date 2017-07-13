package util;

public class InputRecord {
	private String eventType;
	private Long inputTime;
	private int xPos = -1;
	private int yPos = -1;
	private int input;
	
	public InputRecord(String eventType, Long inputTime, int xPos, int yPos, int input) {
		this.eventType = eventType;
		this.inputTime = inputTime;
		this.xPos = xPos;
		this.yPos = yPos;
		this.input = input;
	}
	
	public InputRecord(String eventType, Long inputTime, int input) {
		this.eventType = eventType;
		this.inputTime = inputTime;
		this.input = input;
	}
	
	public String getEventType() {
		return eventType;
	}
	
	public Long getInputTime() {
		return inputTime;
	}
	
	public int getxPos() {
		return xPos;
	}
	
	public int getyPos() {
		return yPos;
	}
	
	public int getEventVar() {
		return input;
	}
	
}
