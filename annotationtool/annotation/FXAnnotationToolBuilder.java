package annotation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.WindowInfo;
import util.X11InfoGatherer;

/**
 * Both the main class as well as the builder for the application. Using this you can create a maximized window, a window of a specific size
 * or a window that snaps to a running window (in Linux).
 */
public class FXAnnotationToolBuilder extends Application {
	
	private X11InfoGatherer gatherer = X11InfoGatherer.getX11InfoGatherer();
	private Stage stage;
	private Stage tableStage;
	private GraphicsContext gc;
	private Process proc;
	private TableView table;
	
	private ArrayList<WindowInfo> windows;
	private int[] windowAttributes;
	
	private static String programRestore;
    
    private double xPos1;
    private double yPos1;
    private double xPos2;
    private double yPos2;
    private double xPosCurr;
    private double yPosCurr;
    private static double widthRestore = -1;
    private static double heightRestore = -1;
    private static double xRestore = -1;
    private static double yRestore = -1;
    
    private boolean dragging;
    private boolean building;

    public static void main(String[] args) {
    	if(args.length > 0) {
    		BufferedReader br = null;
    		FileReader fr = null;

    		try {

    			fr = new FileReader("state.txt");
    			br = new BufferedReader(fr);

    			String sCurrentLine;

    			br = new BufferedReader(new FileReader("state.txt"));

    			while ((sCurrentLine = br.readLine()) != null) {
    				String[] coords = sCurrentLine.split(" ");
    				widthRestore = Double.valueOf(coords[0]);
    				heightRestore = Double.valueOf(coords[1]);
    				xRestore = Double.valueOf(coords[2]);
    				yRestore = Double.valueOf(coords[3]);
    			}

    		} catch (IOException e) {

    			e.printStackTrace();

    		} finally {

    			try {

    				if (br != null)
    					br.close();

    				if (fr != null)
    					fr.close();

    			} catch (IOException ex) {

    				ex.printStackTrace();

    			}

    		}
    	} 
    	
		launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	if(widthRestore > 0) {
    		xPos1 = xRestore;
    		xPos2 = xPos1 + widthRestore;
    		yPos1 = yRestore;
    		yPos2 = yPos1 + heightRestore;
    		if(programRestore != null) {
    			//TODO: Snap into that slim jim
    		} else {
    			build(false);
    		}
    		return;
    	}
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
    		windows = gatherer.getAllWindows();
    		tableStage = new Stage();
    		createWindowList(tableStage);
    	}
    }
    
    private void createWindowList(Stage stage) {
    	final ObservableList<WindowInfo> windows = FXCollections.observableArrayList(this.windows);
    	
    	Scene scene = new Scene(new Group());
        stage.setTitle("Windows");
        stage.setWidth(500);
        stage.setHeight(500);
 
        final Label label = new Label("Windows");
        label.setFont(new Font("Arial", 20));
 
        table = new TableView(); 
        table.setEditable(false);
 
        TableColumn<WindowInfo, String> windowTitles = new TableColumn<WindowInfo, String>("Available Windows");
        windowTitles.setCellValueFactory(new PropertyValueFactory<WindowInfo, String>("Title"));
        windowTitles.setPrefWidth(500);
        
        table.setItems(windows);
        table.getColumns().addAll(windowTitles);
 
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 20));
        
        Button selectWindowButton = new Button();
        selectWindowButton.setText("Annotate Selected Window");
        selectWindowButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				WindowInfo selectedItem = (WindowInfo)table.getSelectionModel().getSelectedItem();
				if(selectedItem != null) {
					windowAttributes = selectedItem.getDimensions();
					buildFromInfo(selectedItem);
					stage.close();
				}
			}
		});
        Button annotateDesktopButton = new Button();
        annotateDesktopButton.setText("Annotate Entire Screen");
        annotateDesktopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
        
        HBox hbox = new HBox();
        hbox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        hbox.getChildren().addAll(selectWindowButton, annotateDesktopButton);
        
        vbox.getChildren().addAll(table, hbox);
 
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        
        table.setPrefWidth(440);
        table.setPrefHeight(400);
 
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
	
	private void buildFromInfo(WindowInfo window) {
		building = true;
		this.stage.setOpacity(0f);
		this.stage.setIconified(true);
		
		Stage newStage = new Stage();
		Stage newSecondaryStage = new Stage();
		newStage.setWidth(Double.valueOf(windowAttributes[0]));
		newSecondaryStage.setWidth(Double.valueOf(windowAttributes[0]));
		newStage.setHeight(Double.valueOf(windowAttributes[1]));
		newSecondaryStage.setHeight(Double.valueOf(windowAttributes[1]));
		double x = Double.valueOf(windowAttributes[2]);
		double y = Double.valueOf(windowAttributes[3]);
		AnnotationToolApplication app = new AnnotationToolApplication(
				newStage, 
				newSecondaryStage, 
				x, 
				y, 
				true,
				window
				);
	}
	
	private void build(boolean snapToWindow) {
		if(building) {
			return;
		}
		building = true;
		
		if(snapToWindow) {
			
		
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
		    	if(stage != null) {
		    		stage.close();
		    	}
		    	if(tableStage != null) {
		    		tableStage.close();
		    	}
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
	
	private static void restoreSession(String[] args) {
		if(args.length > 2) {
			double width = Double.valueOf(args[0]);
			double height = Double.valueOf(args[1]);
			double x = Double.valueOf(args[2]);
			double y = Double.valueOf(args[3]);
			Stage newStage = new Stage(); 		
			Stage newSecondaryStage = new Stage();
			newStage.setWidth(width);			
			newSecondaryStage.setWidth(width);
			newStage.setHeight(height);			
			newSecondaryStage.setHeight(height);
			
			if(args.length > 3) {
				//TODO: Launch program whose name is listed here.
			} else {
				AnnotationToolApplication app = new AnnotationToolApplication(
						newStage, 
						newSecondaryStage, 
						x, 
						y, 
						true
						);
			}
			
		} else {
			launch(args);
		}
		
	}
}
