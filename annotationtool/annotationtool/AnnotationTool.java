package annotationtool;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.sun.awt.AWTUtilities;

import annotationtool.ControllerBox;
import sun.awt.image.ToolkitImage;

public class AnnotationTool extends JFrame {

    private class ShapeDef
    {

        Shape shape;
        Paint paint;
        Stroke stroke;

        ShapeDef(Stroke stroke, Paint paint, Shape shape) {
            this.stroke = stroke;
            this.paint = paint;
            this.shape = shape;
        }
    }

    private Image backingMain;
    private Image backingScratch;
    private static Color clearPaint = new Color(0, 0, 0, 0);
    private static Color mostlyClearPaint = new Color(0f,0f,0f,0.1f);

    private Paint paint;
    private Stroke stroke;

    private ShapeDef blockOutShapeDef;
    private ShapeDef border;

    private Deque<ShapeDef> undoStack = new ArrayDeque<ShapeDef>();
    private Deque<ShapeDef> redoStack = new ArrayDeque<ShapeDef>();

    private Cursor defaultCursor;
    private Cursor pencilCursor;

    private int saveImageIndex = 0;
    
    private boolean canDraw = true;

    private Path2D.Float borderShape;
    
    private class GlobalKeyListener implements NativeKeyListener {

    	@Override
    	public void nativeKeyPressed(NativeKeyEvent key) {
			if(key.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
				suppressWindow(true);
			}
			if(key.getKeyCode() == NativeKeyEvent.VC_ALT) {
				suppressWindow();
			}
    	}

    	@Override
    	public void nativeKeyReleased(NativeKeyEvent key) {
    		if(key.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
				suppressWindow(false);
			}
    	}

    	@Override
    	public void nativeKeyTyped(NativeKeyEvent ket) {
    		// TODO Auto-generated method stub
    		
    	}
    	
    }

    private abstract class TextBoxListener implements MouseListener
    {
        public boolean makingTextBox = false;
    }
    private TextBoxListener textBoxListener = textBoxListener = new TextBoxListener() {

        @Override
        public void mouseClicked(MouseEvent e)
        {

        }

        @Override
        public void mousePressed(MouseEvent e)
        {


        }

        @Override
        public void mouseReleased(MouseEvent e)
        {

        }

        @Override
        public void mouseEntered(MouseEvent e)
        {

        }

        @Override
        public void mouseExited(MouseEvent e)
        {

        }
    };
    public void setMakingTextBox(boolean set)
    {
        textBoxListener.makingTextBox = set;
    }

    private KeyListener keyListener = new KeyListener()
    {
        private boolean controlPressed = false;
        private boolean zPressed = false;
        private boolean yPressed = false;

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            if(e.isControlDown())
            {
                controlPressed = true;
            }
            if(e.getExtendedKeyCode() == e.VK_Z)
            {
                zPressed = true;
            }
            if(e.getExtendedKeyCode() == e.VK_Y)
            {
                yPressed = true;
            }
            if(controlPressed && zPressed)
            {
                undo();
            }
            if(yPressed && controlPressed)
            {
                redo();
            }
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            if(e.getExtendedKeyCode() == KeyEvent.VK_Z)
            {
                zPressed = false;
            }
            if(e.getExtendedKeyCode() == KeyEvent.VK_CONTROL)
            {
                controlPressed = false;
            }
            if(e.getExtendedKeyCode() == e.VK_Y)
            {
                yPressed = false;
            }
        }
    };

    public AnnotationTool(int x, int y, int w, int h) {

        super("Drawing Frame");
        setUndecorated(true);
        this.addKeyListener(keyListener);

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        setBounds(x - 5, y - 5, w + 10, h + 10);

        Stroke blockOutStroke;
        Path2D.Float blockOutShape;
        blockOutStroke = new BasicStroke(h, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
        blockOutShape = new Path2D.Float();
        blockOutShape.moveTo(0, h / 2);
        blockOutShape.lineTo(w, h / 2);
        blockOutShapeDef = new ShapeDef(blockOutStroke, clearPaint, blockOutShape);

        // make the window transparent
        setBackground(clearPaint);

        backingScratch = new BufferedImage(w,h,BufferedImage.TRANSLUCENT);//createImage(w, h);
        backingMain = new BufferedImage(w,h,BufferedImage.TRANSLUCENT);//createImage(w, h);
        Graphics2D gMain = (Graphics2D) backingMain.getGraphics();
        gMain.setColor(mostlyClearPaint);
        gMain.fillRect(0, 0, this.getBounds().width, this.getBounds().height);

        borderShape = new Path2D.Float();
        borderShape.moveTo(0, 0);
        borderShape.lineTo(w + 10, 0);
        borderShape.lineTo(w + 10, h + 10);
        borderShape.lineTo(0, h + 10);
        borderShape.closePath();
        border = new ShapeDef(
                new BasicStroke(10, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER),
                new Color(255, 128, 0, 255),
                borderShape);

        setPreferredSize(new Dimension(w + 10, h + 10));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        enableEvents(AWTEvent.KEY_EVENT_MASK
                + AWTEvent.MOUSE_EVENT_MASK
                + AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setVisible(true);
        setAlwaysOnTop(true);
        
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

		GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
		



        /*
        @return an off-screen drawable image, which can be used for double buffering.
        The return value may be null if the component is not displayable.
        This will always happen if GraphicsEnvironment.isHeadless() returns true.
        */
        // backingScratch = createImage(w, h);

        //  backingScratch = new BufferedImage(w,h,BufferedImage.TRANSLUCENT);//createImage(w, h);


/*        Path2D.Float borderShape = new Path2D.Float();
        borderShape.moveTo(0, 0);
        borderShape.lineTo(w + 10, 0);
        borderShape.lineTo(w + 10, h + 10);
        borderShape.lineTo(0, h + 10);
        borderShape.closePath();
        border = new ShapeDef(
                new BasicStroke(10, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER),
                new Color(255, 128, 0, 255),
                borderShape);*/
    }


    /**
     * Makes the window click-through, stops drawing.
     * 
     * @param suppression If the window is suppressed
     */
    public void suppressWindow(boolean suppression) {
    	canDraw = !suppression;
    	if(suppression) {
    		AWTUtilities.setWindowOpacity(this, 0.019f);
    	}
    	else {
    		AWTUtilities.setWindowOpacity(this, 1f);
    	}
    }
    
    public void suppressWindow() {
    	if(AWTUtilities.getWindowOpacity(this) < 1f) {
    		canDraw = true;
    		AWTUtilities.setWindowOpacity(this, 1f);
    	}
    	else {
    		canDraw = false;
    		AWTUtilities.setWindowOpacity(this, .019f);
    	}
    }
    
    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public void doClear(Paint paint) {
        blockOutShapeDef.paint = paint;
        commitShape(blockOutShapeDef);
        repaint();
    }

    public void doClear() {
        doClear(mostlyClearPaint);
    }

    public void clearHistory() {
        doClear();
        undoStack.clear();
        redoStack.clear();
    }

    final ClipboardOwner clipboardOwner = new ClipboardOwner() {
        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
        }
    };

    public void doSave() {
        // find filename for use
        File outFile;
        String fname;
        do {
            fname = String.format("image-%06d.png", saveImageIndex++);
            System.out.println("Trying " + fname);
            outFile = new File(fname);
        } while (outFile.exists());

        String imageTag = "<img src='" + fname +"'>";
        Clipboard clip = this.getToolkit().getSystemClipboard();
        clip.setContents(new StringSelection(imageTag), clipboardOwner);
        System.out.println(imageTag);

        try {
            BufferedImage outImg = null;
            if (backingMain instanceof BufferedImage) {
                outImg = (BufferedImage) backingMain;
            } else if (backingMain instanceof ToolkitImage) {
                outImg = ((ToolkitImage) backingMain).getBufferedImage();
            } else {
                System.err.println("Hmm, not one of those two...");
            }

            ImageIO.write(outImg, "png", outFile);
        } catch (IOException ex) {
            System.err.println("Save failed: " + ex.getMessage());
        }
    }

    @Override
    public void paint(Graphics graphics) {
        // Blank out the scratch image
        Graphics2D gScratch = (Graphics2D) backingScratch.getGraphics();
        gScratch.setComposite(AlphaComposite.Src);


        gScratch.setPaint(mostlyClearPaint);
        gScratch.setStroke(new BasicStroke(10));
        gScratch.fill(borderShape);
        gScratch.drawImage(backingMain, 0, 0, null);

        // if there is a "shape in progress" draw it on the scratch image
        if (p2d != null) {
            gScratch.setPaint(paint);
            gScratch.setStroke(stroke);
            gScratch.draw(p2d);
        }

        Graphics2D g = (Graphics2D) graphics;
        g.setComposite(AlphaComposite.Src);
        AffineTransform trans = g.getTransform();
        g.translate(5, 5);
        g.drawImage(backingScratch, 0, 0, null);
        g.setTransform(trans);
        g.setPaint(border.paint);
        g.setStroke(border.stroke);
        g.draw(border.shape);
    }

    private Path2D.Float p2d; // shape in progress...

    public void undo() {
        if (undoStack.size() > 0) {
            ShapeDef sd = undoStack.pop();
            redoStack.push(sd);
            paintFromUndoStack();
        }
    }

    public void redo() {
        if (redoStack.size() > 0) {
            ShapeDef sd = redoStack.pop();
            undoStack.push(sd);
            paintFromUndoStack();
        }
    }

    private void paintFromUndoStack() {
        Graphics2D g = (Graphics2D) backingMain.getGraphics();
        g.setComposite(AlphaComposite.Src);

        g.setPaint(mostlyClearPaint);
        g.setStroke(new BasicStroke(10));
        g.fill(borderShape);

        Iterator<ShapeDef> sdi = undoStack.descendingIterator();
        while (sdi.hasNext()) {
            ShapeDef s = sdi.next();
            g.setPaint(s.paint);
            g.setStroke(s.stroke);
            g.draw(s.shape);
        }

        repaint();
    }

    private void commitShape(ShapeDef s) {
        undoStack.push(s);
        Graphics2D g = (Graphics2D) backingMain.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setPaint(s.paint);
        g.setStroke(s.stroke);
        g.draw(s.shape);
        p2d = null;
    }

    @Override
    protected void processEvent(AWTEvent evt) {
        super.processEvent(evt);
        
        if(canDraw) {
        	if (evt instanceof MouseEvent) {
        		MouseEvent me = (MouseEvent) evt;
        		if (me.getID() == MouseEvent.MOUSE_PRESSED) {
        			p2d = new Path2D.Float();
        			p2d.moveTo(me.getX(), me.getY());
        		} else if (p2d != null && me.getID() == MouseEvent.MOUSE_DRAGGED) {
        			p2d.lineTo(me.getX(), me.getY());
        		} else if (p2d != null && me.getID() == MouseEvent.MOUSE_RELEASED) {
        			ShapeDef sd = new ShapeDef(stroke, paint, p2d);
        			commitShape(sd);
        		}
        		repaint();
        	}
        }
    }

    public static void main(final String[] args)
    {
        System.err.println("Annoation tool by simon@dancingcloudservices.com");
        System.err.println("Icons by www.iconfinder.com");
        
        int x1 = 50, y1 = 50, w1 = 1280, h1 = 720;
        if (args.length == 2 || args.length == 4)
        {
            w1 = Integer.parseInt(args[0]);
            h1 = Integer.parseInt(args[1]);
            if (args.length == 4)
            {
                x1 = Integer.parseInt(args[2]);
                y1 = Integer.parseInt(args[3]);
            }
            System.out.println("AnnotationTool " + w1 + " by " + h1 + " offset: " + x1 + "," + y1);
        }
        else if (args.length != 0)
        {
            System.err.println("Usage: java annotationtool.AnnotationTool "
                    + "[<width> <height> [ <x> <y>]]"
                    + "\nUsing defaults 1280 720 50 50");
        }
        final int x = x1, y = y1, w = w1, h = h1;
        // Create the GUI on the event-dispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                GraphicsEnvironment ge = GraphicsEnvironment
                        .getLocalGraphicsEnvironment();

                // check if the OS supports translucency
                if (ge.getDefaultScreenDevice().isWindowTranslucencySupported(
                        GraphicsDevice.WindowTranslucency.TRANSLUCENT))
                {

                    AnnotationTool annotationTool= new AnnotationTool(x, y, w, h);
                    annotationTool.setBackground(new Color(0,0,0, 64));
                    ControllerBox controllerBox = new ControllerBox(annotationTool);
                    controllerBox.setBounds(x + w + 10, y, 0, 0);
                    controllerBox.pack();
                    controllerBox.setVisible(true);
                }
            }
        });
    }
}