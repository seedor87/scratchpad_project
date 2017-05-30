package annotationtool;

import java.awt.Canvas;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;

public class FXAnnotationToolBuilder extends Application {
	
	NativeMouseListener mListener;
	NativeMouseMotionListener dragListener;
	NativeKeyListener kListener;
	
	private Stage stage;
	private GraphicsContext gc;
    
    private String instructions = "Click and drag to select a portion of the screen to annotate.";
    
    private int xPos1;
    private int yPos1;
    private int xPos2;
    private int yPos2;
    private int xPosCurr;
    private int yPosCurr;
    
    private boolean dragging;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	this.stage = stage;
    	this.stage.initStyle(StageStyle.TRANSPARENT);
    	this.stage.setMaximized(true);
    	Group root = new Group();
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        stage.setScene(new Scene(root));
        stage.getScene().setFill(Color.rgb(0, 0, 0, 1d / 255d));
        this.stage.show();
        
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
		
		gc.setStroke(Color.RED);
    	gc.setLineWidth(4);
    }
    
    private void highlight(GraphicsContext gc) {
    	gc.clearRect(0, 0, Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
    	gc.setStroke(new Color(1, 0, 0, 0.5f));
    	gc.setLineWidth(4);
    	int beginX, beginY, width, height;
    	if (dragging == true) {
			beginX = Math.min(xPos1, xPosCurr);
			beginY = Math.min(yPos1, yPosCurr);
			width = Math.abs(xPosCurr - xPos1);
			height = Math.abs(yPosCurr - yPos1);
			
			double[] xPoints = {beginX, beginX + width, beginX + width, beginX};
			double[] yPoints = {beginY, beginY, beginY + height, beginY + height};
			
			System.out.println(xPosCurr + " " + yPosCurr);
			
			gc.strokePolygon(xPoints, yPoints, 4);
    	}
    	
    	
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
			highlight(gc);
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
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
			        stage.close();
			    }
			});
		}
	}
	
	private void close() {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	System.exit(0);
		    }
		});
	}
}
