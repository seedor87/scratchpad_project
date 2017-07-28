package util;

import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseWheelEvent;

import java.lang.reflect.Field;

public class InputRecord {
	private String recordType = "InputRecord";
	private String eventType;
	private Long inputTime;
	private int xPos = -1;
	private int yPos = -1;
	private int input;
	
	public InputRecord() {
		
	}
	
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

	public InputRecord(NativeInputEvent nativeEvent, Long inputTime) {
		this.eventType = nativeEvent.getClass().getSimpleName();
		this.inputTime = inputTime;
		if (nativeEvent instanceof NativeMouseWheelEvent) {
			this.input = ((NativeMouseWheelEvent) nativeEvent).getWheelRotation();
		}
		else if (nativeEvent instanceof NativeKeyEvent) {
			this.input = ((NativeKeyEvent) nativeEvent).getKeyCode();
		}
		else { // (nativeEvent instanceof NativeMouseEvent) {
			this.xPos = ((NativeMouseEvent) nativeEvent).getX();
			this.yPos = ((NativeMouseEvent) nativeEvent).getY();
			this.input = ((NativeMouseEvent) nativeEvent).getButton();
		}

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
	
	public int getInput() {
		return input;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append( this.getClass().getName() );
		result.append( " Object {" );
		result.append(newLine);

		//determine fields declared in this class only (no fields of superclass)
		Field[] fields = this.getClass().getDeclaredFields();

		//print field names paired with their values
		for ( Field field : fields  ) {
			result.append("  ");
			try {
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				result.append( field.get(this) );
			} catch ( IllegalAccessException ex ) {
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("}");

		return result.toString();
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}
	
}
