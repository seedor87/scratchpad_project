package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

public class ProcessRunner {
	/**
	 * Builds a terminal process from the array of arguments given, then returns a BufferedReader for its output.
	 * 
	 * @param args An array of Strings that form the terminal command. 
	 * @return The BufferedReader of the process's output.
	 * @throws IOException
	 */
	public static BufferedReader runProcess(String[] args, Process proc) throws IOException {
		proc = new ProcessBuilder(args).start();
		InputStream stdin = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		return new BufferedReader(isr);
	}
	
	/**
	 * Gets the position, size, and ID of the clicked window.
	 * 
	 * @return An array of Objects containing the window's ID in a string, followed by its width, height, x position, and y position in Doubles.
	 */
	public static Object[] getWindowInfo(Process proc) {
		
		String windowID = null;
		Double width = -1d;
		Double height = -1d;
		Double x = -1d;
		Double y = -1d;

		try {
			String[] xWinInfoArgs = {"xwininfo"};
			BufferedReader br = runProcess(xWinInfoArgs, proc);
			
			String line = null;
            while ( (line = br.readLine()) != null) {
            	String[] splitLine = line.split(":");
            	
            	if(splitLine.length > 1 && splitLine[1].trim().equals("Window id")) {
            		windowID = splitLine[2].trim().split(" ")[0];
            	}
            	
            	switch(splitLine[0].trim()) {
            		case "Width":
            			width = Double.valueOf(splitLine[1].trim());
            			break;
            		case "Height": 
            			height = Double.valueOf(splitLine[1].trim());
            			break;
            		case "Absolute upper-left X":
            			x = Double.valueOf(splitLine[1].trim());
            			break;
            		case "Absolute upper-left Y":
            			y = Double.valueOf(splitLine[1].trim());
            			break;
            		default:
            			break;
            	}
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Object[] {windowID, width, height, x, y};
	}
	
	/**
	 * Gets the ID number of the program.
	 * 
	 * @param windowID The ID number of the window as a string.
	 * @return The ID number of the program as a string.
	 */
	public static String getProgramID(String windowID, Process proc) {
		try {
			String[] xPropArgs = {"xprop", "-id", windowID, "_NET_WM_PID"};
			BufferedReader br = runProcess(xPropArgs, proc);
			String line = null;
			while ( (line = br.readLine()) != null) {
				String[] splitLine = line.split(" ");
				return splitLine[splitLine.length - 1].trim();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the file name of the program from a given ID number.
	 * 
	 * @param programID The ID number of the program as a string.
	 * @return The file name of a program as a string.
	 */
	public static String getProgramName(String programID, Process proc) {
		try {
			String[] llArgs = {"ls", "-l", MessageFormat.format("/proc/{0}/exe", programID)};
			BufferedReader br = runProcess(llArgs, proc);
			String line = null;
			while ( (line = br.readLine()) != null) {
				String[] splitLine = line.split("/");
				return splitLine[splitLine.length - 1].trim();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

