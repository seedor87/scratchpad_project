package util;

import java.awt.event.InputEvent;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;



public class GlobalInputListener implements 
											NativeKeyListener, 
											NativeMouseInputListener, 
											NativeMouseWheelListener {

	private static final String JSON_FILE_NAME = "./ser.json";

	private static Gson gson = new Gson();
	private static FileWriter fw = null;

	private ScheduledExecutorService moveDelay;
	private LinkedHashMap<NativeInputEvent, Long> inputEvents = new LinkedHashMap<NativeInputEvent, Long>();
	private long startTime = System.currentTimeMillis();
	private boolean saveMove = true;
	
	public GlobalInputListener() {
		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);

		// Don't forget to disable the parent handlers.
		logger.setUseParentHandlers(false);

		try{
			fw = new FileWriter(JSON_FILE_NAME);
		}catch(FileNotFoundException fileNotFound){
			System.out.println("ERROR: While Creating or Opening the File " + JSON_FILE_NAME);
		}catch (IOException ex) {
			ex.printStackTrace();
		}

		
		moveDelay = Executors.newScheduledThreadPool(1);
		moveDelay.scheduleAtFixedRate(()-> {
				saveMove = true;
			}, 0, 10, TimeUnit.MILLISECONDS);
	}

	private long getTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	public LinkedHashMap<NativeInputEvent, Long> getInputEvents() {
		return inputEvents;
	}

	public void record() {}
	
	public void saveInputEvents() {
		try {
			FileOutputStream fileOut = new FileOutputStream("inputEvents.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(inputEvents);
			System.out.println("wrote it");
			out.close();
			fileOut.close();
			System.out.println("Input data saved.");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeEvent) {}

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeEvent) {
		inputEvents.put(nativeEvent, getTime());
		gson.toJson(new InputRecord(nativeEvent, getTime()), fw);
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
		inputEvents.put(nativeEvent, getTime());
		gson.toJson(new InputRecord(nativeEvent, getTime()), fw);
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent nativeEvent) {
		if(saveMove) {
			inputEvents.put(nativeEvent, getTime());
			saveMove = false;
			gson.toJson(new InputRecord(nativeEvent, getTime()), fw);
		}
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent nativeEvent) {}

	@Override
	public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeEvent) {
		inputEvents.put(nativeEvent, getTime());
		gson.toJson(new InputRecord(nativeEvent, getTime()), fw);
		saveInputEvents();
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		inputEvents.put(nativeEvent, getTime());
		gson.toJson(new InputRecord(nativeEvent, getTime()), fw);
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
		inputEvents.put(nativeEvent, getTime());
		gson.toJson(new InputRecord(nativeEvent, getTime()), fw);
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}
	
	public static void main(String[] args) {
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.exit(1);
		}
		
		GlobalInputListener inputListener = new GlobalInputListener();

		GlobalScreen.addNativeKeyListener(inputListener);
		GlobalScreen.addNativeMouseListener(inputListener);
		GlobalScreen.addNativeMouseWheelListener(inputListener);
		GlobalScreen.addNativeMouseMotionListener(inputListener);
	}
}
