package utils;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseWheelEvent;



public class InputAutomator {
	
	static boolean quitReplay = false;
    private static final String JSON_FILE_NAME = "./ser.json";


    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
		InputAutomator automator = new InputAutomator();
		
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}
		
		GlobalScreen.addNativeKeyListener(automator.new ReplayController());
		
		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);

		// Don't forget to disable the parent handlers.
		logger.setUseParentHandlers(false);
		
		LinkedHashMap<NativeInputEvent, Long> inputEvents = new LinkedHashMap<NativeInputEvent, Long>();
		try {
			FileInputStream fileIn = new FileInputStream("inputEvents.ser");
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        try {
	        	inputEvents = (LinkedHashMap<NativeInputEvent, Long>)in.readObject();
	        	System.out.println(inputEvents.keySet().size());
	        } catch(EOFException e) {
	        	in.close();
	        	fileIn.close();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

        /* Call This method to use the json file to recreate instead*/
		automator.recreateInputs(true);
		
	}
	
	/**
	 * Recreates inputs as they originally happened, using a Linked Hash Map of NativeInputEvents and Longs.
	 * 
	 * @param inputEvents A linked hashmap containing NativeInputEvents and Longs representing the number of milliseconds passed before the NativeInputEvent.
	 */
	public void recreateInputs(LinkedHashMap<NativeInputEvent, Long> inputEvents) {
		Robot robot;
		try {
			robot = new Robot();
		} catch (AWTException e)
        {
            throw new RuntimeException(e);          //potentially fixes robot working with ubuntu.
        }
		long lastTime = (long)inputEvents.values().toArray()[0];
		HashSet<Integer> pressedMouseButtons = new HashSet<Integer>();
		HashSet<Integer> pressedKeys = new HashSet<Integer>();
		for(Map.Entry<NativeInputEvent, Long> entry : inputEvents.entrySet()) {
			if(quitReplay) {
				quitReplay = false;
				return;
			}
			String eventType = entry.getKey().getClass().getSimpleName();
			robot.delay((int) (entry.getValue() - lastTime));
			lastTime = entry.getValue();
			if(eventType.equals("NativeMouseEvent")) {
				robot.mouseMove(((NativeMouseEvent)(entry.getKey())).getX(), 
								((NativeMouseEvent)(entry.getKey())).getY());
				if(((NativeMouseEvent)(entry.getKey())).getButton() != 0) {
					Integer pressedButton = ((NativeMouseEvent)(entry.getKey())).getButton();
					if(pressedMouseButtons.contains(pressedButton)) {
						robot.mouseRelease(InputEvent.getMaskForButton(pressedButton));
						pressedMouseButtons.remove(pressedButton);
					} else {
						robot.mousePress(InputEvent.getMaskForButton(pressedButton));
						pressedMouseButtons.add(pressedButton);
					}
				}
			}
			else if(eventType.equals("NativeMouseWheelEvent")) {
				robot.mouseMove(((NativeMouseWheelEvent)(entry.getKey())).getX(), 
								((NativeMouseWheelEvent)(entry.getKey())).getY());
				robot.mouseWheel(((NativeMouseWheelEvent)(entry.getKey())).getWheelRotation());
			}
			else if(eventType.equals("NativeKeyEvent")) {
				Integer pressedKey = (convertToKeyCode((NativeKeyEvent)(entry.getKey())));
				if(pressedKeys.contains(pressedKey)) {
					robot.keyRelease(pressedKey);
					pressedKeys.remove(pressedKey);
				} else {
					robot.keyPress(pressedKey);
					pressedKeys.add(pressedKey);
				}
			}
		}
	}
	
    /**
     * Recreates inputs as they originally occurred using a JSON file. Can limit itself specifically to events that occurred within the annotation window.
     * 
     * @param windowOnly Limits recreation to inputs within the annotation window if true.
     */
    public void recreateInputs(boolean windowOnly) {

        ArrayList<InputRecord> inputRecords = new ArrayList<>();

        try {
            InputStream is = new FileInputStream(new File(JSON_FILE_NAME));
            Reader r = new InputStreamReader(is, "UTF-8");
            Gson gson = new GsonBuilder().create();
            JsonStreamParser p = new JsonStreamParser(r);
            while (p.hasNext()) {
                JsonElement e;
                try {
                    e = p.next();
                } catch (Exception ex) {
                    /* break case for malformed json exception */
                    break;
                }
                if (e.isJsonObject()) {
                    Map m = gson.fromJson(e, Map.class);
                    InputRecord event = gson.fromJson(e, InputRecord.class);
                    inputRecords.add(event);
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }

        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e)
        {
            throw new RuntimeException(e);          //potentially fixes robot working with ubuntu.
        }
        long lastTime = inputRecords.get(0).getInputTime();
        HashSet<Integer> pressedMouseButtons = new HashSet<Integer>();
        HashSet<Integer> pressedKeys = new HashSet<Integer>();

        for(InputRecord entry : inputRecords) {
            if(quitReplay) {
                quitReplay = false;
                return;
            }
            
            if(!windowOnly || entry.getRecordType().equals("WindowLinkedInputRecord")) {
            	
            	String eventType = entry.getEventType();
            	robot.delay((int) (entry.getInputTime() - lastTime));
            	lastTime = entry.getInputTime();
            	if(eventType.equals("NativeMouseEvent")) {
            		robot.mouseMove(entry.getxPos(), entry.getyPos());
            		if(entry.getInput() != 0) {
            			Integer pressedButton = entry.getInput();
            			if(pressedMouseButtons.contains(pressedButton)) {
            				robot.mouseRelease(InputEvent.getMaskForButton(pressedButton));
            				pressedMouseButtons.remove(pressedButton);
            			} else {
            				robot.mousePress(InputEvent.getMaskForButton(pressedButton));
            				pressedMouseButtons.add(pressedButton);
            			}
            		}
            	}
            	else if(eventType.equals("NativeMouseWheelEvent")) {
            		robot.mouseMove(entry.getxPos(), entry.getyPos());
            		robot.mouseWheel(entry.getInput());
            	}
            	else if(eventType.equals("NativeKeyEvent")) {
            		Integer pressedKey = entry.getInput();
            		if(pressedKeys.contains(pressedKey)) {
            			robot.keyRelease(pressedKey);
            			pressedKeys.remove(pressedKey);
            		} else {
            			robot.keyPress(pressedKey);
            			pressedKeys.add(pressedKey);
            		}
            	}
            	
            } else { System.out.println(entry.getRecordType()); } 
            
        }
    }
	
	/**
	 * Returns a human-readable string describing an input event at a given time.
	 * 
	 * @param entry An entry containing a Native Input Event and Long
	 * @return a human-readable string describing an input event at a given time.
	 */
	public static String getLongFormInputEvent(Map.Entry<NativeInputEvent, Long> entry) {
		String inputInfo = "";
		String eventType = entry.getKey().getClass().getSimpleName();
		inputInfo += "Event Type: " + eventType + ", ";
		if(eventType.equals("NativeMouseEvent")) {
			inputInfo += "Button: " + ((NativeMouseEvent)(entry.getKey())).getButton() + ", ";
			inputInfo += "Number of clicks: " + ((NativeMouseEvent)(entry.getKey())).getClickCount() + ", ";
			inputInfo += "Position: " + ((NativeMouseEvent)(entry.getKey())).getX() + ", " + ((NativeMouseEvent)(entry.getKey())).getY();
		} else if (eventType.equals("NativeKeyEvent")) {
			inputInfo += "Code: " + ((NativeKeyEvent)(entry.getKey())).getKeyCode() + ", ";
			inputInfo += "Char: " + ((NativeKeyEvent)(entry.getKey())).getKeyChar();
		} else if (eventType.equals("NativeMouseWheelEvent")) {
			inputInfo += "Button: " + ((NativeMouseWheelEvent)(entry.getKey())).getButton() + ", ";
			inputInfo += "Rotation: " + ((NativeMouseWheelEvent)(entry.getKey())).getWheelRotation() + ", ";
			inputInfo += "Position: " + ((NativeMouseWheelEvent)(entry.getKey())).getX() + ", " + ((NativeMouseEvent)(entry.getKey())).getY();
		}
		inputInfo += ", Time since start: " + entry.getValue();
		return inputInfo;
	}
	
    /**
     * Converts a NativeKeyEvent to a keycode usable by the awt.robot class.
     * 
     * @param e The NativeKeyEvent given by jnativehook
     * @return The keycode corresponding to the NativeKeyEvent.
     */
    protected static int convertToKeyCode(NativeKeyEvent e) {
        int keyCode = KeyEvent.VK_UNDEFINED;

        switch (e.getKeyCode()) {
            case NativeKeyEvent.VC_ESCAPE:
                keyCode = KeyEvent.VK_ESCAPE;
                break;

            // Begin Function Keys
            case NativeKeyEvent.VC_F1:
                keyCode = KeyEvent.VK_F1;
                break;

            case NativeKeyEvent.VC_F2:
                keyCode = KeyEvent.VK_F2;
                break;

            case NativeKeyEvent.VC_F3:
                keyCode = KeyEvent.VK_F3;
                break;

            case NativeKeyEvent.VC_F4:
                keyCode = KeyEvent.VK_F4;
                break;

            case NativeKeyEvent.VC_F5:
                keyCode = KeyEvent.VK_F5;
                break;

            case NativeKeyEvent.VC_F6:
                keyCode = KeyEvent.VK_F6;
                break;

            case NativeKeyEvent.VC_F7:
                keyCode = KeyEvent.VK_F7;
                break;

            case NativeKeyEvent.VC_F8:
                keyCode = KeyEvent.VK_F8;
                break;

            case NativeKeyEvent.VC_F9:
                keyCode = KeyEvent.VK_F9;
                break;

            case NativeKeyEvent.VC_F10:
                keyCode = KeyEvent.VK_F10;
                break;

            case NativeKeyEvent.VC_F11:
                keyCode = KeyEvent.VK_F11;
                break;

            case NativeKeyEvent.VC_F12:
                keyCode = KeyEvent.VK_F12;
                break;

            case NativeKeyEvent.VC_F13:
                keyCode = KeyEvent.VK_F13;
                break;

            case NativeKeyEvent.VC_F14:
                keyCode = KeyEvent.VK_F14;
                break;

            case NativeKeyEvent.VC_F15:
                keyCode = KeyEvent.VK_F15;
                break;

            case NativeKeyEvent.VC_F16:
                keyCode = KeyEvent.VK_F16;
                break;

            case NativeKeyEvent.VC_F17:
                keyCode = KeyEvent.VK_F17;
                break;

            case NativeKeyEvent.VC_F18:
                keyCode = KeyEvent.VK_F18;
                break;

            case NativeKeyEvent.VC_F19:
                keyCode = KeyEvent.VK_F19;
                break;

            case NativeKeyEvent.VC_F20:
                keyCode = KeyEvent.VK_F20;
                break;

            case NativeKeyEvent.VC_F21:
                keyCode = KeyEvent.VK_F21;
                break;

            case NativeKeyEvent.VC_F22:
                keyCode = KeyEvent.VK_F22;
                break;

            case NativeKeyEvent.VC_F23:
                keyCode = KeyEvent.VK_F23;
                break;

            case NativeKeyEvent.VC_F24:
                keyCode = KeyEvent.VK_F24;
                break;
            // End Function Keys


            // Begin Alphanumeric Zone
            case NativeKeyEvent.VC_BACKQUOTE:
                keyCode = KeyEvent.VK_BACK_QUOTE;
                break;

            case NativeKeyEvent.VC_1:
                keyCode = KeyEvent.VK_1;
                break;
            case NativeKeyEvent.VC_2:
                keyCode = KeyEvent.VK_2;
                break;
            case NativeKeyEvent.VC_3:
                keyCode = KeyEvent.VK_3;
                break;
            case NativeKeyEvent.VC_4:
                keyCode = KeyEvent.VK_4;
                break;
            case NativeKeyEvent.VC_5:
                keyCode = KeyEvent.VK_5;
                break;
            case NativeKeyEvent.VC_6:
                keyCode = KeyEvent.VK_6;
                break;
            case NativeKeyEvent.VC_7:
                keyCode = KeyEvent.VK_7;
                break;
            case NativeKeyEvent.VC_8:
                keyCode = KeyEvent.VK_8;
                break;
            case NativeKeyEvent.VC_9:
                keyCode = KeyEvent.VK_9;
                break;
            case NativeKeyEvent.VC_0:
                keyCode = KeyEvent.VK_0;
                break;

            case NativeKeyEvent.VC_MINUS:
                keyCode = KeyEvent.VK_MINUS;
                break;
            case NativeKeyEvent.VC_EQUALS:
                keyCode = KeyEvent.VK_EQUALS;
                break;
            case NativeKeyEvent.VC_BACKSPACE:
                keyCode = KeyEvent.VK_BACK_SPACE;
                break;

            case NativeKeyEvent.VC_TAB:
                keyCode = KeyEvent.VK_TAB;
                break;
            case NativeKeyEvent.VC_CAPS_LOCK:
                keyCode = KeyEvent.VK_CAPS_LOCK;
                break;

            case NativeKeyEvent.VC_A:
                keyCode = KeyEvent.VK_A;
                break;
            case NativeKeyEvent.VC_B:
                keyCode = KeyEvent.VK_B;
                break;
            case NativeKeyEvent.VC_C:
                keyCode = KeyEvent.VK_C;
                break;
            case NativeKeyEvent.VC_D:
                keyCode = KeyEvent.VK_D;
                break;
            case NativeKeyEvent.VC_E:
                keyCode = KeyEvent.VK_E;
                break;
            case NativeKeyEvent.VC_F:
                keyCode = KeyEvent.VK_F;
                break;
            case NativeKeyEvent.VC_G:
                keyCode = KeyEvent.VK_G;
                break;
            case NativeKeyEvent.VC_H:
                keyCode = KeyEvent.VK_H;
                break;
            case NativeKeyEvent.VC_I:
                keyCode = KeyEvent.VK_I;
                break;
            case NativeKeyEvent.VC_J:
                keyCode = KeyEvent.VK_J;
                break;
            case NativeKeyEvent.VC_K:
                keyCode = KeyEvent.VK_K;
                break;
            case NativeKeyEvent.VC_L:
                keyCode = KeyEvent.VK_L;
                break;
            case NativeKeyEvent.VC_M:
                keyCode = KeyEvent.VK_M;
                break;
            case NativeKeyEvent.VC_N:
                keyCode = KeyEvent.VK_N;
                break;
            case NativeKeyEvent.VC_O:
                keyCode = KeyEvent.VK_O;
                break;
            case NativeKeyEvent.VC_P:
                keyCode = KeyEvent.VK_P;
                break;
            case NativeKeyEvent.VC_Q:
                keyCode = KeyEvent.VK_Q;
                break;
            case NativeKeyEvent.VC_R:
                keyCode = KeyEvent.VK_R;
                break;
            case NativeKeyEvent.VC_S:
                keyCode = KeyEvent.VK_S;
                break;
            case NativeKeyEvent.VC_T:
                keyCode = KeyEvent.VK_T;
                break;
            case NativeKeyEvent.VC_U:
                keyCode = KeyEvent.VK_U;
                break;
            case NativeKeyEvent.VC_V:
                keyCode = KeyEvent.VK_V;
                break;
            case NativeKeyEvent.VC_W:
                keyCode = KeyEvent.VK_W;
                break;
            case NativeKeyEvent.VC_X:
                keyCode = KeyEvent.VK_X;
                break;
            case NativeKeyEvent.VC_Y:
                keyCode = KeyEvent.VK_Y;
                break;
            case NativeKeyEvent.VC_Z:
                keyCode = KeyEvent.VK_Z;
                break;

            case NativeKeyEvent.VC_OPEN_BRACKET:
                keyCode = KeyEvent.VK_OPEN_BRACKET;
                break;
            case NativeKeyEvent.VC_CLOSE_BRACKET:
                keyCode = KeyEvent.VK_CLOSE_BRACKET;
                break;
            case NativeKeyEvent.VC_BACK_SLASH:
                keyCode = KeyEvent.VK_BACK_SLASH;
                break;

            case NativeKeyEvent.VC_SEMICOLON:
                keyCode = KeyEvent.VK_SEMICOLON;
                break;
            case NativeKeyEvent.VC_QUOTE:
                keyCode = KeyEvent.VK_QUOTE;
                break;
            case NativeKeyEvent.VC_ENTER:
                keyCode = KeyEvent.VK_ENTER;
                break;

            case NativeKeyEvent.VC_COMMA:
                keyCode = KeyEvent.VK_COMMA;
                break;
            case NativeKeyEvent.VC_PERIOD:
                keyCode = KeyEvent.VK_PERIOD;
                break;
            case NativeKeyEvent.VC_SLASH:
                keyCode = KeyEvent.VK_SLASH;
                break;

            case NativeKeyEvent.VC_SPACE:
                keyCode = KeyEvent.VK_SPACE;
                break;
            // End Alphanumeric Zone

            case NativeKeyEvent.VC_PRINTSCREEN:
                keyCode = KeyEvent.VK_PRINTSCREEN;
                break;
            case NativeKeyEvent.VC_SCROLL_LOCK:
                keyCode = KeyEvent.VK_SCROLL_LOCK;
                break;
            case NativeKeyEvent.VC_PAUSE:
                keyCode = KeyEvent.VK_PAUSE;
                break;

            // Begin Edit Key Zone
            case NativeKeyEvent.VC_INSERT:
                keyCode = KeyEvent.VK_INSERT;
                break;
            case NativeKeyEvent.VC_DELETE:
                keyCode = KeyEvent.VK_DELETE;
                break;
            case NativeKeyEvent.VC_HOME:
                keyCode = KeyEvent.VK_HOME;
                break;
            case NativeKeyEvent.VC_END:
                keyCode = KeyEvent.VK_END;
                break;
            case NativeKeyEvent.VC_PAGE_UP:
                keyCode = KeyEvent.VK_PAGE_UP;
                break;
            case NativeKeyEvent.VC_PAGE_DOWN:
                keyCode = KeyEvent.VK_PAGE_DOWN;
                break;
            // End Edit Key Zone


            // Begin Cursor Key Zone
            case NativeKeyEvent.VC_UP:
                keyCode = KeyEvent.VK_UP;
                break;
            case NativeKeyEvent.VC_LEFT:
                keyCode = KeyEvent.VK_LEFT;
                break;
            case NativeKeyEvent.VC_CLEAR:
                keyCode = KeyEvent.VK_CLEAR;
                break;
            case NativeKeyEvent.VC_RIGHT:
                keyCode = KeyEvent.VK_RIGHT;
                break;
            case NativeKeyEvent.VC_DOWN:
                keyCode = KeyEvent.VK_DOWN;
                break;
            // End Cursor Key Zone


            // Begin Numeric Zone
            case NativeKeyEvent.VC_NUM_LOCK:
                keyCode = KeyEvent.VK_NUM_LOCK;
                break;
            case NativeKeyEvent.VC_SEPARATOR:
                keyCode = KeyEvent.VK_SEPARATOR;
                break;
            // End Numeric Zone

            // Begin Modifier and Control Keys
            case NativeKeyEvent.VC_SHIFT:
                keyCode = KeyEvent.VK_SHIFT;
                break;
            case NativeKeyEvent.VC_CONTROL:
                keyCode = KeyEvent.VK_CONTROL;
                break;
            case NativeKeyEvent.VC_ALT:
                keyCode = KeyEvent.VK_ALT;
                break;
            case NativeKeyEvent.VC_META:
                keyCode = KeyEvent.VK_META;
                break;
            case NativeKeyEvent.VC_CONTEXT_MENU:
                keyCode = KeyEvent.VK_CONTEXT_MENU;
                break;
            // End Modifier and Control Keys


            /* Begin Media Control Keys
            case NativeKeyEvent.VC_POWER:
            case NativeKeyEvent.VC_SLEEP:
            case NativeKeyEvent.VC_WAKE:
            case NativeKeyEvent.VC_MEDIA_PLAY:
            case NativeKeyEvent.VC_MEDIA_STOP:
            case NativeKeyEvent.VC_MEDIA_PREVIOUS:
            case NativeKeyEvent.VC_MEDIA_NEXT:
            case NativeKeyEvent.VC_MEDIA_SELECT:
            case NativeKeyEvent.VC_MEDIA_EJECT:
            case NativeKeyEvent.VC_VOLUME_MUTE:
            case NativeKeyEvent.VC_VOLUME_UP:
            case NativeKeyEvent.VC_VOLUME_DOWN:
            case NativeKeyEvent.VC_APP_MAIL:
            case NativeKeyEvent.VC_APP_CALCULATOR:
            case NativeKeyEvent.VC_APP_MUSIC:
            case NativeKeyEvent.VC_APP_PICTURES:
            case NativeKeyEvent.VC_BROWSER_SEARCH:
            case NativeKeyEvent.VC_BROWSER_HOME:
            case NativeKeyEvent.VC_BROWSER_BACK:
            case NativeKeyEvent.VC_BROWSER_FORWARD:
            case NativeKeyEvent.VC_BROWSER_STOP:
            case NativeKeyEvent.VC_BROWSER_REFRESH:
            case NativeKeyEvent.VC_BROWSER_FAVORITES:
            // End Media Control Keys */

            // Begin Japanese Language Keys
            case NativeKeyEvent.VC_KATAKANA:
                keyCode = KeyEvent.VK_KATAKANA;
                break;
            case NativeKeyEvent.VC_UNDERSCORE:
                keyCode = KeyEvent.VK_UNDERSCORE;
                break;
            //case NativeKeyEvent.VC_FURIGANA:
            case NativeKeyEvent.VC_KANJI:
                keyCode = KeyEvent.VK_KANJI;
                break;
            case NativeKeyEvent.VC_HIRAGANA:
                keyCode = KeyEvent.VK_HIRAGANA;
                break;
            //case NativeKeyEvent.VC_YEN:
            // End Japanese Language Keys


            // Begin Sun keyboards
            case NativeKeyEvent.VC_SUN_HELP:
                keyCode = KeyEvent.VK_HELP;
                break;
            case NativeKeyEvent.VC_SUN_STOP:
                keyCode = KeyEvent.VK_STOP;
                break;
            //case NativeKeyEvent.VC_SUN_FRONT:
            //case NativeKeyEvent.VC_SUN_OPEN:
            case NativeKeyEvent.VC_SUN_PROPS:
                keyCode = KeyEvent.VK_PROPS;
                break;
            case NativeKeyEvent.VC_SUN_FIND:
                keyCode = KeyEvent.VK_FIND;
                break;
            case NativeKeyEvent.VC_SUN_AGAIN:
                keyCode = KeyEvent.VK_AGAIN;
                break;
            case NativeKeyEvent.VC_SUN_COPY:
                //case NativeKeyEvent.VC_SUN_INSERT:
                keyCode = KeyEvent.VK_COPY;
                break;
            case NativeKeyEvent.VC_SUN_CUT:
                keyCode = KeyEvent.VK_CUT;
                break;
            // End Sun keyboards
        }
        return keyCode;
    }

    /**
     * @author Brendan
     *
     * Class for listening to keys in order to control the Input Automator.
     */
    private class ReplayController implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
			if(nativeEvent.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
				quitReplay = true;
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent nativeEvent) {}
		@Override
		public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}
    	
    }
}
