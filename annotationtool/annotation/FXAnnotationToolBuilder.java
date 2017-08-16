package annotation;

import java.io.IOException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
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
import util.ProcessRunner;
import util.WindowInfo;
import util.X11InfoGatherer;

import javax.swing.*;

import static javax.swing.UIManager.get;

/**
 * Both the main class as well as the builder for the application. Using this you can create a maximized window, a window of a specific size
 * or a window that snaps to a running window (in Linux).
 */
public class FXAnnotationToolBuilder extends Application {

	private X11InfoGatherer gatherer;// = X11InfoGatherer.getX11InfoGatherer();
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
	private static String workingPath;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		workingPath = promptDialogBox();
		System.out.println("from start" + workingPath);

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
			gatherer = X11InfoGatherer.getX11InfoGatherer();
			windows = gatherer.getAllWindows();
			tableStage = new Stage();
			createWindowList(tableStage);
		}
	}

	private String promptDialogBox() throws IOException {


		String path = getFileName();
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.setTitle("Scratchpad.exe");
		dialog.setHeaderText("Welcome to Scratchpad");
		dialog.setContentText("");
		ButtonType buttonTypeOne = new ButtonType("Create New Project");
		ButtonType buttonTypeTwo = new ButtonType("Import Project");
		ButtonType buttonTypeThree = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree);
		//Set extension filter
		chooser.setInitialFileName(path);
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Annotation Files (*.jnote)", "*.jnote");
		chooser.getExtensionFilters().add(extFilter);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get() == buttonTypeOne) { //create new project/file

			chooser.getExtensionFilters().add(extFilter);


			//Show save file dialog
			File file = chooser.showSaveDialog(stage);
			path = file.getAbsoluteFile().toString();



		} else if (result.get() == buttonTypeTwo) { //import from file
			// ... user chose "Two"
			path = chooser.showOpenDialog(null).getPath();
		} else {
			// ... user chose CANCEL or closed the dialog
			System.exit(0);
		}

		System.out.println(path);
		return path;
	}

	/**
	 * Generates a file name based on date and time
	 *
	 * @return File name as a string.
	 */
	public String getFileName() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
		timeStamp += ".jnote";
		//return timeStamp;
		return timeStamp;
	}


	/**
	 * Creates a table of all visible windows to annotate.
	 *
	 * @param stage A stage to hold the table of windows/
	 */
	private void createWindowList(Stage stage) {
		final ObservableList<WindowInfo> windows = FXCollections.observableArrayList(this.windows);

		Scene scene = new Scene(new Group());
		stage.setTitle("Windows");
		stage.setWidth(600);
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

		Button restoreSessionButton = new Button();
		restoreSessionButton.setText("Restore Previous Session");
		restoreSessionButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double x = 200;
				double y = 150;
				double width = 900;
				double height = 750;
				//TODO: Open JSON with info from prev. session, update annotation window size variables.
				Process proc = null;
				WindowInfo selectedItem = (WindowInfo)table.getSelectionModel().getSelectedItem();
				if(selectedItem != null) {
					ProcessRunner.focusWindow(selectedItem.getTitle(), proc);
					ProcessRunner.resizeWindow(selectedItem.getTitle(), x, y, width, height, proc);



					windowAttributes = selectedItem.getDimensions();

					try {
						buildFromInfo(selectedItem);
					} catch (IOException e) {
						e.printStackTrace();
					}
					stage.close();
				} else {
					try {
						restoreSession(new String[] {String.valueOf(width), String.valueOf(height), String.valueOf(x), String.valueOf(y)});
					} catch (IOException e) {
						e.printStackTrace();
					}
					stage.close();
				}
			}
		});

		Button selectWindowButton = new Button();
		selectWindowButton.setText("Annotate Selected Window");
		selectWindowButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				WindowInfo selectedItem = (WindowInfo)table.getSelectionModel().getSelectedItem();
				if(selectedItem != null) {
					windowAttributes = selectedItem.getDimensions();

					try {
						buildFromInfo(selectedItem);
					} catch (IOException e) {
						e.printStackTrace();
					}



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
		hbox.getChildren().addAll(restoreSessionButton, selectWindowButton, annotateDesktopButton);

		vbox.getChildren().addAll(table, hbox);

		((Group) scene.getRoot()).getChildren().addAll(vbox);

		table.setPrefWidth(440);
		table.setPrefHeight(400);

		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Creates a visible box over the region of the screen the user intends to annotate.
	 *
	 * @param gc
	 */
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
					try {
						build();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				xPos2 = event.getX();
				yPos2 = event.getY();
				dragging = false;
				try {
					build();
				} catch (IOException e) {
					e.printStackTrace();
				}
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

	/**
	 * Creates an annotation window around a given window.
	 *
	 * @param window The window to annotate.
	 */
	private void buildFromInfo(WindowInfo window) throws IOException {
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
				window,
				workingPath
		);
	}

	/**
	 * Creates an annotation tool window over a region of the screen
	 */
	private void build() throws IOException {
		if(building) {
			return;
		}
		building = true;
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

			AnnotationToolApplication app = new AnnotationToolApplication(newStage, newSecondaryStage, x, y, true,workingPath);

		} else if (w < 25 && h < 25) {
			AnnotationToolApplication app = new AnnotationToolApplication(new Stage(), new Stage(), 0, 0, false,workingPath);
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

	/**
	 * Closes the entire application.
	 */
	private void close() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				System.exit(0);
			}
		});
	}

	/**
	 * Restores the annotation tool to a saved size and position.
	 *
	 * @param args Array containing attributes of the saved window.
	 */
	private static void restoreSession(String[] args) throws IOException {
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
						true,
						workingPath);
			}

		} else {
			launch(args);
		}

	}
}
