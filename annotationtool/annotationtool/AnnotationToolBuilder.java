package annotationtool;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;

import com.sun.awt.AWTUtilities;
import com.sun.swing.internal.plaf.basic.resources.basic;

public class AnnotationToolBuilder extends JFrame {
	
	NativeMouseListener mListener;
	NativeMouseMotionListener dragListener;
	NativeKeyListener kListener;
	
	private static Color clearPaint = new Color(0f,0f,0f,0f);
    private static Color mostlyClearPaint = new Color(0f,0f,0f,0.1f);
    
    private String instructions = "Click and drag to select a portion of the screen to annotate.";
    
    private int xPos1;
    private int yPos1;
    private int xPos2;
    private int yPos2;
    private int xPosCurr;
    private int yPosCurr;
    
    private boolean dragging;
	
	public static void main(String[] args) {
		JFrame builder = new AnnotationToolBuilder();
	}
	
	public AnnotationToolBuilder() {
		super("Frame Builder");
		setUndecorated(true);
		setAlwaysOnTop(true);
		setExtendedState(MAXIMIZED_BOTH);
		
		AWTUtilities.setWindowOpaque(this, true);
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
		dragListener = new BuilderMouseDragListener();
		kListener = new BuilderKeyListener();
		GlobalScreen.addNativeMouseListener(mListener);
		GlobalScreen.addNativeMouseMotionListener(dragListener);
		GlobalScreen.addNativeKeyListener(kListener);
		
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.Src);
		g2d.setPaint(mostlyClearPaint);
		g2d.setStroke(new BasicStroke(4));
		g2d.fill(this.getBounds());
		int beginX, beginY, width, height;
		if (dragging == true) {
			beginX = Math.min(xPos1, xPosCurr);
			beginY = Math.min(yPos1, yPosCurr);
			width = Math.abs(xPosCurr - xPos1);
			height = Math.abs(yPosCurr - yPos1);

			g2d.setColor(Color.RED);
			g2d.drawRect(beginX, beginY, width, height);
		}
		g2d.setFont(g2d.getFont().deriveFont(3, 25f));
		g2d.setColor(Color.BLACK);
		g2d.drawString(instructions, 26, 26);
		g2d.setColor(Color.RED);
		g2d.drawString(instructions, 25, 25);
	}
	
	private class BuilderMouseListener implements NativeMouseListener {

		@Override
		public void nativeMouseClicked(NativeMouseEvent me) {		}

		@Override
		public void nativeMousePressed(NativeMouseEvent me) {
			if(!dragging) {
				xPos1 = me.getX();
				yPos1 = me.getY();
				dragging = true;
			}
		}

		@Override
		public void nativeMouseReleased(NativeMouseEvent me) {
			if(dragging) {
				xPos2 = me.getX();
				yPos2 = me.getY();
				dragging = false;
				build();
			}
		}
		
	}

	private class BuilderMouseDragListener implements NativeMouseMotionListener {

		@Override
		public void nativeMouseDragged(NativeMouseEvent me) {
			xPosCurr = me.getX();
			yPosCurr = me.getY();
			repaint();
		}

		@Override
		public void nativeMouseMoved(NativeMouseEvent me) {}
		
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
			GlobalScreen.removeNativeMouseMotionListener(dragListener);
			GlobalScreen.removeNativeMouseListener(mListener);
			this.dispose();
		}
	}
	
	private void close() {
		System.exit(0);
	}
}
