package annotation;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import util.ProcessRunner;
import util.Window;

public class FXAnnotationToolBuilder extends Application {
	
	private Stage stage;
	private GraphicsContext gc;
	private Process proc;
	
	private ArrayList<Window> windows = new ArrayList<Window>();
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
    	
    	if(System.getProperty("os.name").equals("Linux")) {
    		ArrayList<String> windowIDs = ProcessRunner.getAllWindows(proc);

    		for(String id : windowIDs) {
    			String windowTitle = ProcessRunner.getWindowTitleByID(id, proc);
    			String programID = ProcessRunner.getProgramID(id, proc);
    			String programTitle = ProcessRunner.getProgramName(programID, proc);
    			windows.add(new Window(id, windowTitle, programID, programTitle));
    		}
    		
    		createWindowList(new Stage());
    	}
    }
    
    private void createWindowList(Stage stage) {
    	final ObservableList<Window> windows = FXCollections.observableArrayList(this.windows);
    	
    	Scene scene = new Scene(new Group());
        stage.setTitle("Windows");
        stage.setWidth(500);
        stage.setHeight(500);
 
        final Label label = new Label("Windows");
        label.setFont(new Font("Arial", 20));
 
        TableView table = new TableView<>(); 
        table.setEditable(false);
 
        TableColumn<Window, String> windowTitles = new TableColumn<>("Window Titles");
        windowTitles.setCellValueFactory(new PropertyValueFactory<Window, String>("title"));
        TableColumn<Window, String> programTitles = new TableColumn<>("Program Names");
        programTitles.setCellValueFactory(new PropertyValueFactory<Window, String>("programName"));
        
        table.setItems(windows);
        table.getColumns().addAll(windowTitles, programTitles);
 
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 20));
        vbox.getChildren().addAll(label, table);
 
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
 
        stage.setScene(scene);
        stage.show();
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
			    	windowInfo = ProcessRunner.getWindowInfo(proc);
			    	if(windowInfo != null) {
			    		String programID = ProcessRunner.getProgramID((String)windowInfo[0], proc);
			    		String programName = ProcessRunner.getProgramName(programID, proc);
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
						
						AnnotationToolApplication app = new AnnotationToolApplication(
														newStage, 
														newSecondaryStage, 
														x, 
														y, 
														true, 
														(String)windowInfo[0]
														);
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
}
