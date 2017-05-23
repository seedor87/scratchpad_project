package annotationtool;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

public class AnnotationToolBuilder extends JFrame {
	
	NativeMouseListener mListener;
	NativeKeyListener kListener;
	
    private static Color mostlyClearPaint = new Color(0f,0f,0f,0.1f);
    
    private int xPos1;
    private int yPos1;
    private int xPos2;
    private int yPos2;
    
    private boolean dragging;
	
	public static void main(String[] args) {
		JFrame builder = new AnnotationToolBuilder();
	}
	
	public AnnotationToolBuilder() {
		super("Frame Builder");
		setUndecorated(true);
		setAlwaysOnTop(true);
		setExtendedState(MAXIMIZED_BOTH);
		setBackground(mostlyClearPaint);
		
		try {
			Logger keyListenerLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        	keyListenerLogger.setLevel(Level.WARNING);
        	
        	keyListenerLogger.setUseParentHandlers(false);
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

		mListener = new BuilderMouseListener();
		kListener = new BuilderKeyListener();
		GlobalScreen.addNativeMouseListener(mListener);
		GlobalScreen.addNativeKeyListener(kListener);
		
		setVisible(true);
	}
	
	@Override
    protected void processEvent(AWTEvent evt) {
        super.processEvent(evt);
        
        if (evt instanceof MouseEvent) {
    		MouseEvent me = (MouseEvent) evt;
    		if (me.getID() == MouseEvent.MOUSE_PRESSED)
    		{
    			System.out.println("this part is working");
    			xPos1 = me.getXOnScreen();
    			yPos1 = me.getYOnScreen();
    			dragging = true;
    		} 
    		else if (me.getID() == MouseEvent.MOUSE_DRAGGED) 
    		{
    			//make a marquee rectangle or something I dunno
    		} 
    		else if (me.getID() == MouseEvent.MOUSE_RELEASED && dragging) 
    		{
    			System.out.println("getting releases");
    			xPos2 = me.getXOnScreen() - xPos1;
    			yPos2 = me.getYOnScreen() - yPos1;
    			dragging = false;
    		}
    	}
	}
	
	private class BuilderMouseListener implements NativeMouseListener {

		@Override
		public void nativeMouseClicked(NativeMouseEvent me) {		}

		@Override
		public void nativeMousePressed(NativeMouseEvent me) {
			if(!dragging) {
				System.out.println("this part is working");
				xPos1 = me.getX();
				yPos1 = me.getY();
				dragging = true;
			}
		}

		@Override
		public void nativeMouseReleased(NativeMouseEvent me) {
			if(dragging) {
				System.out.println("getting releases");
				xPos2 = me.getX();
				yPos2 = me.getY();
				dragging = false;
				build();
			}
		}
		
	}
	
	private class BuilderKeyListener implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent ke) {
			if(ke.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
				close();
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent ke) {}

		@Override
		public void nativeKeyTyped(NativeKeyEvent ke) {}
		
	}
	
	private void build() {
		int x;
		int y;
		int w;
		int h;
		
		x = Integer.min(xPos1, xPos2);
		y = Integer.min(yPos1, yPos2);
		
		w = Integer.max(xPos1, xPos2) - x;
		h = Integer.max(yPos1, yPos2) - y;
		
		if(w >= 50 && h >= 50) {
			String[] windowSize = new String[] {String.valueOf(w), String.valueOf(h), String.valueOf(x), String.valueOf(y)};
			AnnotationTool.main(windowSize);
			GlobalScreen.removeNativeKeyListener(kListener);
			GlobalScreen.removeNativeMouseListener(mListener);
			this.dispose();
		}
	}
	
	private void close() {
		System.exit(0);
	}
}
