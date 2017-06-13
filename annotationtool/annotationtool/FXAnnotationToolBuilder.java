package annotationtool;

import java.awt.Canvas;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.corba.se.pept.transport.EventHandler;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class FXAnnotationToolBuilder extends Application {
	
	
	private Stage stage;
	private GraphicsContext gc;
    
    private String instructions = "Click and drag to select a portion of the screen to annotate.";
    
    private double xPos1;
    private double yPos1;
    private double xPos2;
    private double yPos2;
    private double xPosCurr;
    private double yPosCurr;
    
    private boolean dragging;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	this.stage = stage;
    	this.stage.initStyle(StageStyle.UNDECORATED);
    	this.stage.setOpacity(0.004);
    	this.stage.setMaximized(true);
    	Group root = new Group();
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        stage.setScene(new Scene(root));
        stage.getScene().setFill(Color.rgb(0, 0, 0, 1d / 255d));
        this.stage.show();
        
		stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, new BuilderKeyHandler());
		stage.getScene().addEventHandler(MouseEvent.ANY, new BuilderMouseHandler());
		
		gc.setStroke(Color.RED);
    	gc.setLineWidth(4);
    }
    
    private void highlight(GraphicsContext gc) {
    	gc.clearRect(0, 0, Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
    	gc.setStroke(new Color(1, 0, 0, 0.5f));
    	gc.setLineWidth(4);
    	double beginX, beginY, width, height;
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

	private class BuilderMouseHandler implements javafx.event.EventHandler<MouseEvent>
	{

		@Override
		public void handle(MouseEvent event) {
			if(event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				xPos1 = event.getX();
				yPos1 = event.getY();
				dragging = true;
			}
			
			if(event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				xPos2 = event.getX();
				yPos2 = event.getY();
				dragging = false;
				build();
			}
			
			if(event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				xPosCurr = event.getX();
				yPosCurr = event.getY();
				highlight(gc);
			}
			
		}
		
	}
	
	private class BuilderKeyHandler implements javafx.event.EventHandler<KeyEvent>
	{

		@Override
		public void handle(KeyEvent event) {
			if(event.getCode() == KeyCode.ESCAPE) {
				close();
			}
		}
		
	}
	
	private void build() {
		double x, y, w, h;
		
		x = Double.min(xPos1, xPos2);
		y = Double.min(yPos1, yPos2);
		
		w = Double.max(xPos1, xPos2) - x;
		h = Double.max(yPos1, yPos2) - y;
		
		if(w >= 50 && h >= 50) {
			String[] windowSize = new String[] {String.valueOf(w), String.valueOf(h), String.valueOf(x), String.valueOf(y)};
			
			Stage newStage = new Stage(); 	Stage newSecondaryStage = new Stage();
			newStage.setWidth(w);			newSecondaryStage.setWidth(w);
			newStage.setHeight(h);			newSecondaryStage.setHeight(h);
			
			AnnotationToolApplication app = new AnnotationToolApplication(newStage, newSecondaryStage, x, y, true);

		} else if (w < 50 && h < 50) {
			AnnotationToolApplication app = new AnnotationToolApplication(new Stage(), new Stage(), 0, 0, false);
		}
		
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		        stage.close();
		    }
		});
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
