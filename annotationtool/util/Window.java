package util;

public class Window {
	private String id = "";
	private String title = "";
	private String programID = "";
	private String programName = "";
	
	public Window(String id, String title, String programID, String programName) {
		this.setId(id);
		this.setTitle(title);
		this.setProgramID(programID);
		this.setProgramName(programName);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getProgramID() {
		return programID;
	}

	public void setProgramID(String programID) {
		this.programID = programID;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}
	
	
}
