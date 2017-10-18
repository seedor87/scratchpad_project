package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessRunner {
	/**
	 * Builds a terminal process from the array of arguments given, then returns a BufferedReader for its output.
	 * 
	 * @param args An array of Strings that form the terminal command. 
	 * @param proc The process to run this on.
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
	 * Brings a window to the front.
	 * 
	 * @param windowTitle The complete or partial title of the window. This is how the window is identified.
	 * @param proc The process to run this on.
	 */
	public static void focusWindow(String windowTitle, Process proc) {
		String[] wmCtrlArgs = {"wmctrl", "-a", windowTitle};
		try {
			BufferedReader br = runProcess(wmCtrlArgs, proc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Resizes a window.
	 * 
	 * @param windowTitle The complete or partial title of the window. This is how the window is identified.
	 * @param x X position of the window
	 * @param y Y position of the window
	 * @param width Width of the window
	 * @param height Height of the window
	 * @param proc The process to run this on.
	 */
	public static void resizeWindow(String windowTitle, double x, double y, double width, double height, Process proc) {
		String[] wmCtrlArgs = {"wmctrl", "-r", windowTitle, "-b", "remove,maximized_vert,maximized_horz"};
		try {
			BufferedReader br = runProcess(wmCtrlArgs, proc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String dimensions = "0," + String.valueOf((int)x) + "," + String.valueOf((int)y) + "," + String.valueOf((int)width) + "," + String.valueOf((int)height);
		String[] wmCtrlArgs2 = {"wmctrl", "-r", windowTitle, "-e", dimensions};
		try {
			BufferedReader br = runProcess(wmCtrlArgs2, proc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

