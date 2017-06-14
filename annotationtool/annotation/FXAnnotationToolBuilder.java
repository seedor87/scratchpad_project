package annotation;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class FXAnnotationToolBuilder extends Application {
	
	private Stage stage;
	private GraphicsContext gc;
	private Process proc;
	
	private Object[] windowInfo;
    
    private double xPos1;
    private double yPos1;
    private double xPos2;
    private double yPos2;
    private double xPosCurr;
    private double yPosCurr;
    
    private boolean dragging;
    private boolean building;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	this.stage = stage;
    	this.stage.initStyle(StageStyle.UNDECORATED);
    	this.stage.setOpacity(0.2d);
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
				if(event.getButton() != MouseButton.SECONDARY) {
					xPos1 = event.getX();
					yPos1 = event.getY();
					dragging = true;
				}
			}
			
			if(event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				if(event.getButton() == MouseButton.SECONDARY) {
					build(true);
				}
				xPos2 = event.getX();
				yPos2 = event.getY();
				dragging = false;
				build(false);
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
	
	private void build(boolean snapToWindow) {
		if(building) {
			return;
		}
		building = true;
		
		if(snapToWindow) {
			this.stage.setOpacity(0f);
			this.stage.setIconified(true);
			
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
			    	getWindowInfo();
			    	if(windowInfo != null) {
			    		String programID = getProgramID((String)windowInfo[0]);
			    		String programName = getProgramName(programID);
			    		System.out.println(programName);
			    		
						Stage newStage = new Stage(); 		
						Stage newSecondaryStage = new Stage();
						newStage.setWidth((Double)windowInfo[1]);			
						newSecondaryStage.setWidth((Double)windowInfo[1]);
						newStage.setHeight((Double)windowInfo[2]);			
						newSecondaryStage.setHeight((Double)windowInfo[2]);
						double x = (Double)windowInfo[3];
						double y = (Double)windowInfo[4];
						
						System.out.println(windowInfo[0]);
						
						AnnotationToolApplication app = new AnnotationToolApplication(newStage, newSecondaryStage, x, y, true);
			    	}
			    }
			});
		}
		else {
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
				
			} else if (w < 25 && h < 25) {
				AnnotationToolApplication app = new AnnotationToolApplication(new Stage(), new Stage(), 0, 0, false);
			}
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
	
	/**
	 * Builds a terminal process from the array of arguments given, then returns a BufferedReader for its output.
	 * 
	 * @param args An array of Strings that form the terminal command. 
	 * @return The BufferedReader of the process's output.
	 * @throws IOException
	 */
	private BufferedReader runProcess(String[] args) throws IOException {
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
	private void getWindowInfo() {
		
		String windowID = null;
		Double width = -1d;
		Double height = -1d;
		Double x = -1d;
		Double y = -1d;

		try {
			String[] xWinInfoArgs = {"xwininfo"};
			BufferedReader br = runProcess(xWinInfoArgs);
			
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
		windowInfo = new Object[] {windowID, width, height, x, y};
	}
	
	/**
	 * Gets the ID number of the program.
	 * 
	 * @param windowID The ID number of the window as a string.
	 * @return The ID number of the program as a string.
	 */
	private String getProgramID(String windowID) {
		try {
			String[] xPropArgs = {"xprop", "-id", windowID, "_NET_WM_PID"};
			BufferedReader br = runProcess(xPropArgs);
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
	private String getProgramName(String programID) {
		try {
			String[] llArgs = {"ls", "-l", MessageFormat.format("/proc/{0}/exe", programID)};
			BufferedReader br = runProcess(llArgs);
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
