package annotation;
/**
 * Created by remem on 5/30/2017.
 */

import changeItem.*;
import transferableShapes.*;

import com.google.gson.*;

import eventHandlers.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import utils.FilePacker;
import utils.GlobalInputListener;
import utils.InputRecord;
import utils.WindowInfo;
import utils.WindowLinkedInputRecord;
import utils.X11InfoGatherer;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;


/**
 *
 */
public class AnnotationToolApplication extends Application {

    static {
        System.setProperty("java.awt.headless", "false");
        //https://stackoverflow.com/questions/2552371/setting-java-awt-headless-true-programmatically
        //TODO that link might help with getting images in ubuntu.
    }


    //================================================================================
    // Instance Variables
    //================================================================================

    final ClipboardOwner clipboardOwner = new ClipboardOwner() {
        @Override
        public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
        }
    };
    // Colors and Paint
    private final Color clickablyClearPaint = new Color(1, 1, 1, 1d / 255d);
    private final Color clearPaint = new Color(0, 0, 0, 0);
    private final double[] minStageSize = {100, 100};
    public static FileWriter writer = null;
    public static FileWriter windowWriter = null;
    //record keeping
    UUID uuid;
    private Gson gson = new Gson();
    private String last_file_fileName = "lastFile.txt";
    private String jnote_fileName;
    private String temp_jnote_fileName;
    private String json_fileName = "ShapeRecord.json";
    private String window_fileName = "WindowRecord.json";
    private ArrayList prev_shapes = new ArrayList<Custom_Shape>();
    private ScheduledExecutorService windowGrabber;
    private ArrayList<WindowInfo> relevantWindows;
    private ArrayList<String> dataFiles = new ArrayList<String>();
    // Screen Setup and Layout
    private IconControllerBox controllerBox;
    private Stage mouseCatchingStage;
    private Stage pictureStage;
    private Stage textOptionStage;
    private Scene mouseCatchingScene;
    private Scene drawingScene;
    private Group root;
    private Group leaderRoot;
    private VBox box;
    private Color textColor = Color.BLACK;
    private Color borderColor = Color.BLUE;
    private javafx.scene.paint.Paint paint = Color.BLACK;
    // Handlers
    private List<HandlerGroup> eventHandlers = new LinkedList<HandlerGroup>();
    private MovingHandler movingHandler = new MovingHandler(this);
    private DrawingHandler drawingHandler = new DrawingHandler(this);
    private PutControllerBoxOnTopHandler putControllerBoxOnTopHandler = new PutControllerBoxOnTopHandler();
    private ArrowHandler arrowHandler = new ArrowHandler(this);
    private ShortcutHandler shortcutHandler = new ShortcutHandler(this);
    private TextBoxHandler textBoxHandler = new TextBoxHandler(this);
    private TextBoxKeyHandler textBoxKeyHandler = new TextBoxKeyHandler(this);
    private CircleHandler circleHandler = new CircleHandler(this);
    private OutBoundedOvalHandler outBoundedOvalHandler = new OutBoundedOvalHandler(this);
    private EraseHandler eraseHandler = new EraseHandler(this);
    private TwoTouchChangeSizeAndMoveHandler twoTouchChangeSizeAndMoveHandler = new TwoTouchChangeSizeAndMoveHandler(this);
    private ResizeHandler resizeHandler = new ResizeHandler(this);
    private RectangleHandler rectangleHandler = new RectangleHandler(this);
    private RectificationHandler rectificationHandler = new RectificationHandler(this);
    private LineHandler lineHandler = new LineHandler(this);
    private GlobalInputListener globalInputListener = new GlobalInputListener(this);
    // Annotation Objects
    private Path path;
    private Line line;
    private Stroke stroke;
    private Text text;
    private Circle circle;
    private Rectangle borderShape;
    private StringBuffer textBoxText = new StringBuffer(64);
    private Stack<ChangeItem> undoStack = new Stack<>();
    private Stack<ChangeItem> redoStack = new Stack<>();
    // Cursors
    private Cursor pencilCursor = new ImageCursor(new Image("icons/pencil-cursor.png"));
    private Cursor eraserCursor = new ImageCursor(new Image("icons/eraser-cursor.png"));
    private Cursor textCursor = new ImageCursor(new Image("icons/TextIcon.png"));
    private Cursor arrowCursor = new ImageCursor(new Image("icons/arrow-cursor.png"));
    // Settings
    private WindowInfo windowID;
    private String textFont = "Times New Roman";
    private double strokeWidth = 5d;
    private double textSize = 24d;
    private int borderWidth = 5;
    private int boxWidth = 0;
    private int saveImageIndex = 0;
    private boolean mouseTransparent = false;
    private boolean clickable = true;
    private boolean makingTextBox = false;
    private boolean lockedControllerBox = true;
    private boolean recording = false;
    private boolean saveTextBox = false;
    private boolean saveEditText = false;
    private EditText editTextToSave;
    private Stage primaryStage;
    /*
    This number is used to determine how opaque shapes are
    when settransparent is used. The less opaque that they are, the more shapes that can
    overlap without losing the ability to click through the window.
     */
    private final double OPACITY_MULTIPLIER = 0.108;
    private Map<Shape, Color> oldColorMap = new HashMap<>();

    private Map<Shape,Shape> leadersToFollowers = new HashMap<>();
    private Map<Shape,Shape> followersToLeaders = new HashMap<>();

    //================================================================================
    // Constructors/Starts
    //================================================================================

    public AnnotationToolApplication(Stage primaryStage, Stage secondaryStage, double x, double y, boolean sizedWindow, String jnote_fileName) throws IOException {
        start(primaryStage, secondaryStage, x, y, sizedWindow);
        this.jnote_fileName = jnote_fileName;
        System.out.println("From init" + jnote_fileName);
        remakeFromJSON();
        this.primaryStage = primaryStage;
        

    }

    public AnnotationToolApplication(Stage primaryStage, Stage secondaryStage, double x, double y, boolean sizedWindow, WindowInfo windowID, String jnote_fileName) throws IOException {
        this.windowID = windowID;
        this.jnote_fileName = jnote_fileName;
        remakeFromJSON();

        start(primaryStage, secondaryStage, x, y, sizedWindow);
        this.primaryStage = primaryStage;
    }

    public void start(Stage primaryStage) throws IOException {
        start(primaryStage, new Stage(), 0, 0, false);
        this.primaryStage = primaryStage;
    }

    /**
     * The code starts here.
     * @param primaryStage
     */
    public void start(Stage primaryStage, Stage secondaryStage, double x, double y, boolean sizedWindow) throws IOException {
    	temp_jnote_fileName = System.getProperty("user.home") + "/scratchpad/restore/recovery_" + getFileName();
    	Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
    	this.mouseCatchingStage = primaryStage;
    	//this.stage.initStyle(StageStyle.TRANSPARENT);
    	if(!sizedWindow) {
    		//this.mouseCatchingStage.setFullScreen(true);
    		//this.mouseCatchingStage.setMaximized(true);
    		this.mouseCatchingStage.setY(0);
    		this.mouseCatchingStage.setX(0);
    		this.mouseCatchingStage.setHeight(primScreenBounds.getHeight());
    		this.mouseCatchingStage.setWidth(primScreenBounds.getWidth());
    	}
    	else {
    		this.mouseCatchingStage.setX(x);
    		this.mouseCatchingStage.setY(y);
    	}
    	this.mouseCatchingStage.setOpacity(0.004);
    	this.mouseCatchingStage.initStyle(StageStyle.UNDECORATED);
    	
    	root = new Group();

        leaderRoot = new Group();
    	mouseCatchingScene = new Scene(leaderRoot);         // the scene that catches all the mouse events
    	drawingScene = new Scene(root);   // the scene that renders all the drawings.
    	drawingScene.setFill(clearPaint);
    	
    	mouseCatchingScene.setFill(clickablyClearPaint);
    	
    	//notRoot.getChildren().add(bg);
    	
    	pictureStage = secondaryStage;
    	pictureStage.initStyle(StageStyle.TRANSPARENT);
    	pictureStage.setScene(drawingScene);
    	if(!sizedWindow) {
    		//pictureStage.setMaximized(true);
    		//pictureStage.setFullScreen(true);
    		pictureStage.setX(0);
    		pictureStage.setY(0);
    		this.pictureStage.setHeight(primScreenBounds.getHeight());
    		this.pictureStage.setWidth(primScreenBounds.getWidth());
    	}
    	else {
    		pictureStage.setX(x);
    		pictureStage.setY(y);
    	}
    	
    	mouseCatchingStage.setScene(mouseCatchingScene);
    	
    	setupListeners();
    	
    	controllerBox = new IconControllerBox(this);
    	
    	//boxWidth = ((int) controllerBox.getWidth());
    	
    	setUpMoveListeners(pictureStage);
    	
    	mouseCatchingScene.setCursor(pencilCursor);
    	
    	if(pictureStage.isFullScreen() || pictureStage.isMaximized()) {
    		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    		borderShape = new Rectangle(screenSize.getWidth(), screenSize.getHeight());
    	} else {
    		borderShape = new Rectangle( pictureStage.getWidth(), pictureStage.getHeight());
    	}
    	borderShape.setStroke(borderColor);
    	borderShape.setStrokeWidth(borderWidth);
    	borderShape.setFill(clearPaint);
    	root.getChildren().add(borderShape);
    	
    	resetStages();
    	
    	mouseCatchingStage.show();
    	pictureStage.show();
    	
    	this.primaryStage = primaryStage;
    	
    	resnapToWindow(windowID);
    	
    	Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    	logger.setLevel(Level.WARNING);
    	
    	// Don't forget to disable the parent handlers.
    	logger.setUseParentHandlers(false);
    }

    public ObservableList<Node> getLeaderGroup()
    {
        return leaderRoot.getChildren();
    }

    /**
     * Attempts to rebuild the previous session using the shaperecord json
     * 
     * @throws IOException
     */
    private void remakeFromJSON() throws IOException {
        Custom_Shape.setAnnotationToolApplication(this);
    	
    	try
    	{
    		String jsonRecord = FilePacker.retrieveFromZip(jnote_fileName, "ShapeRecord");
    		InputStream is = new FileInputStream(new File(jsonRecord));
    		Reader r = new InputStreamReader(is, "UTF-8");
    		Gson gson = new GsonBuilder().create();
    		JsonStreamParser p = new JsonStreamParser(r);
    		if(p.hasNext()) {
    			JsonElement e = p.next();
    			jnote_fileName = gson.fromJson(e, String.class);
    		}
    		while (p.hasNext()) {
    			JsonElement e;
    			try {
    				e = p.next();
    			} catch (Exception ex) {
    				/* break case for malformed json exception */
    				break;
    			}
    			if (e.isJsonObject()) {
    				Custom_Shape c = gson.fromJson(e, Custom_Shape.class);
    				switch (c.getType()) {
    				case "undo":
    					undo();
    					break;
    				case "redo":
    					redo();
    					break;
    				default:
    					commitChange(c.toChangeItem());
    					break;
    				}
    				
    				prev_shapes.add(c);
    				System.out.println("reading: " + c);
    				
    			}
    		}
    	}
    	catch (FileNotFoundException fnfe) {
    		fnfe.printStackTrace();
    	} catch (Exception exc) {
    		exc.printStackTrace();
    	}
    	
    	
    	try {
    		writer = new FileWriter(json_fileName);
    		dataFiles.add(json_fileName);
    		gson.toJson(jnote_fileName, writer);
    		windowWriter = new FileWriter(window_fileName);
    		dataFiles.add(window_fileName);
    	} catch (FileNotFoundException fileNotFound) {
    		System.out.println("ERROR: While Creating or Opening the File ShapeRecord.json");
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
    	
    	for (Object prev_shape : prev_shapes) {
    		writeJSON((Custom_Shape) prev_shape);
    	}
    	
    }
    
    //================================================================================
    // Mutators
    //================================================================================

    public void saveTextBox()
    {
        addLeaderToFollower(text);
        uuid = UUID.randomUUID();
        Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.TEXT_STRING, text.getText(), textFont);
        custom_shape.setTextSize(text.getFont().getSize() + "");
        custom_shape.setColorString(paint.toString());
        custom_shape.setLocation(new TransferableShapePoint(String.valueOf(text.getX()), String.valueOf(text.getY())));
        try{
            writeJSON(custom_shape);
            Custom_Shape.setUpUUIDMaps(text, uuid);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public void saveEditText()
    {
        Custom_Shape custom_shape = new Custom_Shape(editTextToSave);
        try
        {
            writeJSON(custom_shape);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Shows a list of options for writing text into the annotation tool.
     */
    public void showTextOptionStage() {
        if(makingTextBox) {
            if(textOptionStage == null) {
                textOptionStage = new Stage();
                HBox textOptions = new HBox();

                Label fontSizeLabel = new Label("Font Size:");
                textOptions.getChildren().add(fontSizeLabel);
    			/*
    			List of all possible font sizes
    			 */
                ObservableList<Double> fontSizeList =
                        FXCollections.observableArrayList(5d, 6d, 8d, 10d, 12d, 14d, 16d, 18d, 20d, 24d, 28d, 32d, 36d, 40d, 44d, 48d, 52d, 56d, 60d, 64d, 80d, 100d);
                ComboBox<Double> fontSizes = new ComboBox<>(fontSizeList);
                fontSizes.valueProperty().addListener(new ChangeListener<Double>() {
                    @Override
                    public void changed(ObservableValue<? extends Double> observableValue, Double oldSize, Double newSize) {
                        setTextSize(newSize);
                    }
                });
                fontSizes.setValue(textSize);
                textOptions.getChildren().add(fontSizes);

                Label fontStyleLabel = new Label("Font: ");
                textOptions.getChildren().add(fontStyleLabel);
                ObservableList<String> fontStyleList =
                        FXCollections.observableArrayList(Font.getFamilies());
                ComboBox<String> fontStyles = new ComboBox<>(fontStyleList);
                fontStyles.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String oldFont, String newFont) {
                        setTextFont(newFont);
                    }
                });
                fontStyles.setValue(textFont);
                textOptions.getChildren().add(fontStyles);

                Label fontColorLabel = new Label("Color: ");
                textOptions.getChildren().add(fontColorLabel);
                ColorPicker fontColorPicker = new ColorPicker(textColor);
                fontColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                    @Override
                    public void changed(ObservableValue<? extends Color> observableValue, Color oldColor, Color newColor) {
                        setTextColor(newColor);
                    }
                });
                textOptions.getChildren().add(fontColorPicker);


                Scene textOptionScene = new Scene(textOptions);
                textOptionStage.setScene(textOptionScene);
                textOptionStage.setTitle("Text and Font Options");
                textOptionStage.setX( Math.max(0, mouseCatchingStage.getX() + .1 * mouseCatchingStage.getWidth()));
                textOptionStage.setY( Math.max(0, mouseCatchingStage.getY() + .1 * mouseCatchingStage.getHeight()));
                textOptionStage.setAlwaysOnTop(true);
            }
            textOptionStage.show();
        } else if(textOptionStage != null) {
            textOptionStage.hide();
        }
    }

    /**
     * Clears the current stage. Can be undone (unlike clear history)
     */
    public void doClear() {
        doClear(clickablyClearPaint);
    }

    /**
     * Currently clears the screen transparently.
     * To do for a color, can simply commit the "blockOutShape"
     * If implemented, move the code up to doClear() and then do that.
     * @param color
     */
    public void doClear(Color color) {
        double h = mouseCatchingStage.getHeight();
        double w = mouseCatchingStage.getWidth();
        Path blockOutShape = new Path();
        blockOutShape.setStrokeWidth(h);
        blockOutShape.getElements().add(new MoveTo(0,h/2));
        blockOutShape.getElements().add(new LineTo(w, h/2));
        blockOutShape.setFill(clearPaint);
        EraseShape eraseShape = new EraseShape(blockOutShape);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                commitChange(eraseShape);
            }
        });
        ArrayList<TransferableShapePoint> pathElements = new ArrayList<>();
        pathElements.add(new TransferableShapePoint(0 +"",h/2 +""));
        pathElements.add(new TransferableShapePoint(w + "", h/2 + ""));
        try
        {
            uuid = UUID.randomUUID();
            Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.ERASE_STRING, pathElements);
            shape.setStrokeWidth(String.valueOf(h));

            writeJSON(shape);

        } catch (JsonParseException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace(); }
        redoStack.clear();
    }

    /**
     * commits a change item to the undostack and adds the changes it makes to the main window.
     * @param changeItem
     */
    public void commitChange(ChangeItem changeItem) {
        changeItem.addChangeToStage(this);
        undoStack.push(changeItem);
    }

    /**
     * Redoes the top item on the redo stack, if there is one to be redone.
     */
    public void redo()
    {
        if(redoStack.size() > 0) {
            ChangeItem temp = redoStack.pop();
            temp.redoChangeToStage(this);
            undoStack.push(temp);
        }
    }

    /**
     * Undoes the top item on the undo stack, pushes it onto the redo stack.
     */
    public void undo() {
        if(undoStack.size() > 0) {
            ChangeItem temp = undoStack.pop();
            temp.undoChangeToStage(this);
            redoStack.push(temp);
        }
    }

    /**
     * clears the undo and redo stack and all changes from them.
     */
    public void clearHistory() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                root.getChildren().clear();
                root.getChildren().add(borderShape);
            }
        });
        undoStack.clear();
        redoStack.clear();
        prev_shapes.clear();
        try
        {
            Files.write(new File(json_fileName).toPath(), Arrays.asList(""), StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Sets the state of the program so that it is making circles.
     */
    public void makeCircles() {
        this.resetHandlers();
        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, circleHandler);
    }

    /**
     * Makes it so that the mousecatching stage stops catching mouse events when toggled to unclickable.
     * Calling the method again restores it so that the stage starts catching the events again.
     * If running in linux, it will also cause additions to the picture stage to be clicked through, so long
     * as too many items are not drawn on top of each other.
     */
    public void toggleClickable()
    {
        clickable = !clickable;
        this.resetHandlers();
        if (clickable)
        {
            setNotClickThrough();
            controllerBox.toFront();
        }
        else
        {
            setClickThrough();
        }
    }

    /**
     * Used by toggleClickable to make the stages catch mouse events.
     */
    private void setNotClickThrough()
    {
        mouseCatchingScene.setFill(clickablyClearPaint);
        mouseCatchingStage.setIconified(false);
        resetStages();

        pictureStage.setOpacity(1.0);
        mouseCatchingStage.setOpacity(0.004);
        setShapesNotClickThrough();

        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
    }

    /**
     * Reverts the changes made by setShapesClickThrough. This brings the shapes back to their original color.
     */
    private void setShapesNotClickThrough()
    {
        for(Node node : root.getChildren())
        {
            Shape shape = (Shape) node;
            if(shape != borderShape)
            {
                if(shape.getFill() == null)
                {
                    shape.setStroke(oldColorMap.get(shape));
                }
                else
                {
                    shape.setFill(oldColorMap.get(shape));
                }
            }
        }
    }

    /**
     * Used by toggleClickable. Used to make it so that mouse events pass through the mousecatchingstage.
     * Events pass through the picture stage if too many items are not drawn on top of each other on
     * linux.
     */
    private void setClickThrough()
    {
        mouseCatchingStage.setAlwaysOnTop(false);
        mouseCatchingScene.setFill(clearPaint);
        mouseCatchingStage.setIconified(true);
        mouseCatchingStage.setOpacity(0.0);

        pictureStage.setAlwaysOnTop(false);
        pictureStage.setAlwaysOnTop(true);

        setShapesClickThrough();

        controllerBox.setAlwaysOnTop(false);//resets the controllerbox so that it stays on top.
        controllerBox.setAlwaysOnTop(true);
    }

    /**
     * Makes it so the shapes are a more transparent color. This allows you to click through them
     * if you run this on linux.
     */
    private void setShapesClickThrough()
    {
        for(Node node: root.getChildren())
        {
            if(node != borderShape)
            {
                Shape shape = (Shape) node;
                Color color = (Color) shape.getFill();
                if(color == null)
                {
                    color = (Color) shape.getStroke();
                    Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()*OPACITY_MULTIPLIER);
                    oldColorMap.put(shape, color);
                    shape.setStroke(newColor);
                }
                else
                {
                    Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()*OPACITY_MULTIPLIER);
                    oldColorMap.put(shape, color);
                    shape.setFill(newColor);
                }
            }
        }
    }

    /**
     * Removes all handlers from the scene.
     * Should be called before adding more in to prevent multiple handlers trying to handle the same event(S).
     */
    public void resetHandlers()
    {
        for (HandlerGroup h : eventHandlers)
        {
            this.mouseCatchingScene.removeEventHandler(h.eventType,h.handler);
        }
//        if(this.makingTextBox)
//        {
//            saveTextBox();
//        }
        this.makingTextBox = false;
        AddShape.movingShapes = false;
        resetStages();
        mouseCatchingScene.setCursor(pencilCursor);
    }

    /**
     * Not fully implemented. should get saved state from resethandlers and restore it. currently just resets back to drawing
     */
    public void unResetHandlers()
    {
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
    }


    /**
     * Sets up all default listeners that the code might need.
     * adds all listeners to the list of handlers so all can be removed at once if needed.
     * Should only ever use handlers that are added to eventHandlers
     */
    private void setupListeners()
    {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(globalInputListener);
        GlobalScreen.addNativeMouseListener(globalInputListener);
        GlobalScreen.addNativeMouseWheelListener(globalInputListener);
        GlobalScreen.addNativeMouseMotionListener(globalInputListener);
        GlobalScreen.addNativeMouseListener(resizeHandler);

        drawingScene.addEventHandler(MouseEvent.MOUSE_PRESSED, putControllerBoxOnTopHandler);
        mouseCatchingScene.addEventHandler(MouseEvent.MOUSE_PRESSED,putControllerBoxOnTopHandler);
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
        mouseCatchingScene.addEventHandler(TouchEvent.ANY, twoTouchChangeSizeAndMoveHandler);       //Doesnt need to be added below cause we always wanna be listening for it
        mouseCatchingScene.addEventHandler(KeyEvent.KEY_PRESSED, shortcutHandler);
        mouseCatchingScene.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveTextBoxHandler(this));
        pictureStage.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveTextBoxHandler(this));
        mouseCatchingStage.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveEditTextHandler(this));
        pictureStage.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveEditTextHandler(this));


        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, drawingHandler));
        eventHandlers.add(new HandlerGroup(KeyEvent.KEY_TYPED,textBoxKeyHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.MOUSE_RELEASED, textBoxHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, circleHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, eraseHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, arrowHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, movingHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, outBoundedOvalHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, rectangleHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, rectificationHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, lineHandler));
    }

    public void recordInput()
    {
        if(!recording) {
            recording = true;
        } else {
            recording = false;
            globalInputListener.saveInputEvents();
        }
    }

    private void unMaximize() {
        pictureStage.setMaximized(false);
        pictureStage.setFullScreen(false);
        mouseCatchingStage.setMaximized(false);
        mouseCatchingStage.setFullScreen(false);

    }

    /**
     * Sets up the two stages so that they move and resize with each other.
     */
    private void setUpMoveListeners(Stage pictureStage) {
        mouseCatchingStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pictureStage.setWidth(mouseCatchingStage.getWidth());
                borderShape.setWidth(mouseCatchingStage.getWidth());
                if(lockedControllerBox) {
                    controllerBox.fitScreen();
                }
            }
        });

        mouseCatchingStage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pictureStage.setHeight(mouseCatchingStage.getHeight());
                borderShape.setHeight(mouseCatchingStage.getHeight());
                if(lockedControllerBox) {
                    controllerBox.fitScreen();
                }
            }
        });

        mouseCatchingStage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (!mouseCatchingStage.isIconified())
                {
                    pictureStage.setX(mouseCatchingStage.getX());
                    if (lockedControllerBox) {
                        controllerBox.fitScreen();
                    }
                }
            }
        });

        mouseCatchingStage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(!mouseCatchingStage.isIconified())
                {
                    pictureStage.setY(mouseCatchingStage.getY());
                    if(lockedControllerBox)
                    {
                        controllerBox.fitScreen();
                    }
                }
            }
        });

    }
    /**
     * moves the mousecatchingstage to a new position that is based on the current position and
     * the values of X and Y passed in.
     * @param changeX
     * @param changeY
     */
    private void moveAnnotationWindow(double changeX, double changeY) {
    	double stageXPos = mouseCatchingStage.getX();
    	double stageYPos = mouseCatchingStage.getY();
    	mouseCatchingStage.setX(stageXPos + changeX);
		mouseCatchingStage.setY(stageYPos + changeY);
    }

    /**
     * Does the same thing as resizeAnnotationWindow, but instead of taking in the change in x and the change in y
     * it takes in the new values of x and y.
     */
    public void resizeAnnotationWindow2(double width, double height)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();
        double stageWidth = mouseCatchingStage.getWidth();
        double stageHeight = mouseCatchingStage.getHeight();

        mouseCatchingStage.setWidth( Math.max( Math.min(screenWidth, width), minStageSize[0] ) );
        mouseCatchingStage.setHeight( Math.max( Math.min(screenHeight, height), minStageSize[1] ) );
    }

    /**
     * Resizes the mouseCatching stage so long as the resize would not make it smaller than
     * 100x100 and not larger than the screen size.
     * @param changeX
     * @param changeY
     */
    private void adjustAnnotationWindowSize(double changeX, double changeY) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();
        double stageWidth = mouseCatchingStage.getWidth();
        double stageHeight = mouseCatchingStage.getHeight();

        mouseCatchingStage.setWidth( Math.max( Math.min(screenWidth, stageWidth + changeX), minStageSize[0] ) );
        mouseCatchingStage.setHeight( Math.max( Math.min(screenHeight, stageHeight + changeY), minStageSize[1] ) );
    }

    /**
     * Gets the coordinates of the window associated with the given windowID and ensures the annotationtool window remains snapped to it.
     * If the given window closes, stops the scheduled executor service calling the method.
     * 
     * @param windowID The window to snap to.
     */
    public void resnapToWindow(WindowInfo windowID) {
    	if(windowID != null) {
    		int[] windowInfo = windowID.getDimensions();
    		if(windowInfo[0] != 0 && windowInfo[1] != 0 && windowInfo[2] != 0 && windowInfo[3] != 0) {
    			resizeAnnotationWindow2(windowInfo[0], windowInfo[1]);
    			mouseCatchingStage.setX(windowInfo[2]);
    			mouseCatchingStage.setY(windowInfo[3]);
    			pictureStage.setX(windowInfo[2]);
    			pictureStage.setY(windowInfo[3]);
    		} else {
    			this.windowID = null;
    		}
    	}
    }


    /**
     * Sets the state of the program so that the user can draw arrows.
     */
    public void makeLines() {
        this.resetHandlers();
        this.mouseCatchingScene.setCursor(arrowCursor);
        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, arrowHandler);
    }

    /**
     * clears the main window and then repaints it from scratch.
     */
    public void paintFromUndoStack() {
        root.getChildren().clear();
        root.getChildren().add(borderShape);
        for(ChangeItem changeItem : undoStack) {
            if(!(changeItem instanceof changeItem.EraseShape))          // infinite callse to paintFromUndoStack() if it is an EraseShape
            {
                changeItem.addChangeToStage(this);
            }
        }
    }

    /**
     * changes the state of the program so that the user is erasing.
     */
    public void turnOnErasing() {
        this.resetHandlers();
        this.mouseCatchingScene.setCursor(eraserCursor);
        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, eraseHandler);
    }

    /**
     * Saves a screenshot of the current window as well as anything behind the window
     * and anything that may be drawn on the window.
     */
    public void doSave()
    {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {

                File outFile;
                String fname;
                do
                {
                    fname = String.format("image-%06d.png", saveImageIndex++);
                    System.out.println("Trying " + fname);
                    outFile = new File(fname);
                }
                while (outFile.exists());

                String imageTag = "<img src='" + fname +"'>";
                System.out.println(imageTag);

                if(textOptionStage != null) {
                    textOptionStage.hide();
                }

                try
                {
                    int i = 0;
                    while(!clipboard.hasContent(DataFormat.IMAGE)) {
                        i++;
                        try {
                            if(clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
                            	System.out.println(clipboard.getContent(DataFormat.PLAIN_TEXT));
                            }
                        	Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(i > 50) {
                            break;
                        }
                    }
                    if(clipboard.hasContent(DataFormat.IMAGE)) {
                        clipboard.getImage();
                        BufferedImage screenGrab = SwingFXUtils.fromFXImage(clipboard.getImage(), null);
                        BufferedImage croppedScreenGrab = screenGrab.getSubimage((int)mouseCatchingStage.getX(), (int)mouseCatchingStage.getY(),
                                (int)mouseCatchingStage.getWidth(), (int)mouseCatchingStage.getHeight());
                        ImageIO.write(croppedScreenGrab, "png", outFile);
                        System.out.println("Successful screenshot");
                    } else {
                        System.out.println("No image to edit");
                    }
                }
                catch (HeadlessException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally {
                    controllerBox.show();
                    if(makingTextBox) {
                        textOptionStage.show();
                    }

                }
            }
        });
    }


    /**
     * Resets the stages so that they are in the right order so they do not interfere with
     * each other. The order should be
     * On bottom: picturestage
     * Above that: mousecatchingstage
     * On top: controllerbox
     */
    public void resetStages()
    {
        pictureStage.toFront();
        mouseCatchingStage.toFront();
        controllerBox.toFront();
        pictureStage.setAlwaysOnTop(false);
        pictureStage.setAlwaysOnTop(true);
        mouseCatchingStage.setAlwaysOnTop(false);
        mouseCatchingStage.setAlwaysOnTop(true);
        controllerBox.setAlwaysOnTop(false);
        controllerBox.setAlwaysOnTop(true);
    }

    /**
     * Brings stages that were sent to the back by toBack back to the front
     */
    public void toFront() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mouseCatchingStage.toFront();
                pictureStage.toFront();
                mouseCatchingStage.setIconified(false);
                controllerBox.setAlwaysOnTop(false);
                controllerBox.setAlwaysOnTop(true);
            }
        });
    }

    /**
     * Sends the main stages (not the controllerbox to the back)
     */
    public void toBack() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                mouseCatchingStage.toBack();
//                pictureStage.toBack();
                mouseCatchingStage.setIconified(true);
            }
        });
    }

    public void saveSceneState()
    {

    }

    public void setBorderVisibility(boolean borderVisibility)
    {
        borderShape.setVisible(borderVisibility);
    }

    public StringBuffer getTextBoxText()
    {
        return textBoxText;
    }

    public boolean mouseInWindowBounds(int mouseX, int mouseY)
    {
        if(mouseX > mouseCatchingStage.getX() && mouseX < mouseCatchingStage.getWidth() + mouseCatchingStage.getX() &&
                mouseY > mouseCatchingStage.getY() && mouseY < mouseCatchingStage.getHeight() + mouseCatchingStage.getY()) {
            return true;
        } else {
            return false;
        }
    }

    public InputRecord createWindowLinkedInputRecord(InputRecord record) {
        if(windowID != null && mouseInWindowBounds(record.getxPos(), record.getyPos())) {
            return new WindowLinkedInputRecord(record, mouseCatchingStage.getX(), mouseCatchingStage.getY(), mouseCatchingStage.getWidth(), mouseCatchingStage.getHeight());
        } else {
            return record;
        }
    }

    public void updateRelevantWindows() {
    	if(System.getProperty("os.name").equals("Linux")) {
    		ArrayList<WindowInfo> relevantWindows = new ArrayList<WindowInfo>();
    		if(windowID == null) {
    			X11InfoGatherer x11 = new X11InfoGatherer();
    			ArrayList<WindowInfo> allWindows = x11.getAllWindows();
    			System.out.println("gettin window");
    			for(WindowInfo window : allWindows) {
    				int[] dimensions = window.getDimensions();
    				if(dimensions[0] + dimensions[2] > mouseCatchingStage.getX() &&
    						dimensions[2] < mouseCatchingStage.getX() + mouseCatchingStage.getWidth() &&
    						dimensions[1] + dimensions[3] > mouseCatchingStage.getY() && 
    						dimensions[3] < mouseCatchingStage.getY() + mouseCatchingStage.getHeight()) {
    					relevantWindows.add(window);
    				}
    			}
    			this.relevantWindows = relevantWindows;
    		}
    		else {
    			relevantWindows.add(windowID);
    			this.relevantWindows = relevantWindows;
    		}
    		
    		try {
    			if(mouseCatchingStage != null) {
    				windowWriter.close();
    				windowWriter = new FileWriter(window_fileName);
    				gson.toJson(this.relevantWindows, windowWriter);
    				gson.toJson(new double[] {mouseCatchingStage.getWidth(), mouseCatchingStage.getHeight(), 
    						mouseCatchingStage.getX(), mouseCatchingStage.getY()}, windowWriter);
    			}
			} catch (IOException e) {
				e.printStackTrace();
			} 
    	}
    }


    //================================================================================
    // Getters/Setters
    //================================================================================


    public Stage getPictureStage() {
        return this.pictureStage;
    }
    
    public Stage getControllerBox() {
    	return this.controllerBox;
    }
    
    public Stage getMouseCatchingStage() 
    {
    	return this.mouseCatchingStage;
    }

    public Scene getMouseCatchingScene()
    {
        return mouseCatchingScene;
    }

    public Double getStrokeWidth() {
        return  strokeWidth;
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mouseCatchingStage.setAlwaysOnTop(alwaysOnTop);
            }
        });
    }

    public boolean getMakingTextBox()
    {
        return makingTextBox;
    }

    public boolean getSaveTextBox() {
        return saveTextBox;
    }

    public boolean getSaveEditText()
    {
        return saveEditText;
    }
    public void setSaveEditText(boolean saveEditText)
    {
        this.saveEditText = saveEditText;
    }
    public WindowInfo getWindowID()
    {
        return windowID;
    }

    public void setTextSize(Integer textSize) {
        this.textSize = (double) textSize;
        if(text != null)
            text.setFont(new Font(this.textFont, this.textSize));
        mouseCatchingStage.requestFocus();
    }

    public void setTextSize(Double textSize) {
        this.textSize = textSize;
        if(text != null)
            text.setFont(new Font(this.textFont, this.textSize));
        mouseCatchingStage.requestFocus();
    }

    public void setTextColor(java.awt.Color textColor) {
        this.textColor = new javafx.scene.paint.Color(
                textColor.getRed()/255d,
                textColor.getGreen()/255d,
                textColor.getBlue()/255d,
                textColor.getAlpha()/255d);
        if(text != null)
            text.setFill(this.textColor);
        mouseCatchingStage.requestFocus();
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        if(text != null)
            text.setFill(this.textColor);
        mouseCatchingStage.requestFocus();
    }

    public void setTextFont(String textFont) {
        this.textFont = textFont;
        if(text != null)
            text.setFont(new Font(this.textFont, this.textSize));
        mouseCatchingStage.requestFocus();
    }

    public Stack<ChangeItem> getUndoStack() {
        return undoStack;
    }

    /**
     * Sets the state of the program so that the user is able to move the window around.
     */
    public void setMovingHandler() {
        this.resetHandlers();
        mouseCatchingScene.setCursor(new ImageCursor(new Image("icons/hand.png")));
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, movingHandler);
    }

    /**
     * Sets the paint color based on a java.awt color object.
     * @param paintColor The color to convert to a javafx color object.
     */
    public void setPaint(Color paintColor) {
        this.paint = paintColor;
    }

    public void setMakingText() {
        this.makingTextBox = true;
        this.resetHandlers();
        this.mouseCatchingScene.addEventHandler(MouseEvent.MOUSE_RELEASED, textBoxHandler);
        this.mouseCatchingScene.addEventHandler(KeyEvent.KEY_TYPED, textBoxKeyHandler);
        mouseCatchingScene.setCursor(textCursor);
    }

    /**
     * Sets the state of the program so that you are making rectangles.
     */
    public void setMakingRectangles()
    {
        this.resetHandlers();
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, rectangleHandler);

    }


    /**
     * Sets the state of the program so that you are drawing.
     */
    public void setDrawingText() {
        this.resetHandlers();
        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
        textBoxText.delete(0,textBoxText.length());
    }

    /**
     *  Sets the state of the program so that you are drawing an oval that is outbounded by whatever shape that you may drawn.
     */
    public void setDrawingOutboundedOval()
    {
        this.resetHandlers();
        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, outBoundedOvalHandler);
    }


    public Group getRoot() {
        return this.root;
    }

    public String getTextFont()
    {
        return this.textFont;
    }
    public double getTextSize()
    {
        return textSize;
    }
    public void setSaveTextBox(boolean setSaveTextBox)
    {
        saveTextBox = setSaveTextBox;
    }

    public void setStroke(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * Toggles whether the controllerbox follows the window or not.
     * @return
     */
    public boolean toggleLockedControllerBox() {
        lockedControllerBox = !lockedControllerBox;
        if(lockedControllerBox) {
            controllerBox.fitScreen();
        }
        return lockedControllerBox;
    }

    //================================================================================
    // Inner Classes
    //================================================================================

    /**
     * Sets the state of the program so that the user is editing text
     *
     * @param text The text that is to be edited.
     */
    public void setEditingText(Text text)
    {
        this.resetHandlers();
        editTextToSave = new EditText(text, this);
        commitChange(editTextToSave);
        saveEditText = true;
    }
    
    public void writeJSON(Custom_Shape shape) throws IOException {

        System.out.println(gson.toJson(shape));
        gson.toJson(shape, writer);
        writer.flush();
        updateRelevantWindows();
        windowWriter.flush();
        FilePacker.createZip(temp_jnote_fileName, dataFiles);

        try{
            PrintWriter lastFileWriter = new PrintWriter(last_file_fileName, "UTF-8");
            lastFileWriter.println(temp_jnote_fileName);
            lastFileWriter.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }

        /*ObjectMapper mapper = new ObjectMapper();
        holder.add(shape);

        String json = mapper.writeValueAsString(holder);
        System.out.println(json);
        FileWriter writer = new FileWriter(json_fileName);
        writer.write(json);
        writer.close();*/
    }
    
    public void writeJSON(String fileName) throws IOException {

        updateRelevantWindows();
        windowWriter.flush();
        FilePacker.createZip(fileName, dataFiles);

        try{
            PrintWriter lastFileWriter = new PrintWriter(last_file_fileName, "UTF-8");
            lastFileWriter.println(fileName);
            System.out.println("Last file: " + fileName);
            lastFileWriter.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    /**
     * Small class to provide struct functionality.
     * The event type what type should be used when adding the handler to the
     * mouseCatchingScene.
     */
    private class HandlerGroup {
        EventType eventType;
        EventHandler handler;

        HandlerGroup(EventType eventType, EventHandler handler) {
            this.eventType = eventType;
            this.handler = handler;
        }
    }


    public String getFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        timeStamp += ".json";
        return timeStamp;
    }
    /**
     * Generates a file name based on date and time
     *
     * @return File name as a string.
     */
    public void setSelectAndMoveHandler()
    {
        mouseCatchingStage.toFront();
        pictureStage.toFront();
        controllerBox.toFront();
        AddShape.movingShapes = true;
    }


    /**
     * Puts the user into rectification mode. This lets the user create a polygon based on something the user draws.
     */
    public void setRectifying()
    {
        resetHandlers();
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, rectificationHandler);
    }

    /**
     * This sets the state of the program to making straight lines drawn from one point to another.
     */
    public void setMakingLines()
    {
        resetHandlers();
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, lineHandler);
    }

    //TODO use this
    private void setFollowing(Shape leader, Shape follower)
    {
        setTouchFollowing(leader,follower);
        leader.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            boolean reset = true;
            boolean didReset = false;
            double startX;
            double startY;
            double shapeStartX;
            double shapeStartY;
            double currentX;
            double currentY;
            long delayTime = 10;
            Date startTime;
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED)
                {
                    startTime = new Date();
                    shapeStartX = leader.getLayoutX();
                    shapeStartY = leader.getLayoutY();
                    startX = mouseEvent.getScreenX();
                    startY = mouseEvent.getScreenY();
                    reset = true;
                    //System.out.println(shapeStartX +"," + shapeStartY + ",\n" + startX + "," + startY +'\n'+ leader + '\n' + follower);
                    //oldColor = (Color) follower.getStroke();
                }
                else if(mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED && startTime != null)
                {
                    if (new Date().getTime() - startTime.getTime() > delayTime)
                    {
                        currentX = shapeStartX + mouseEvent.getScreenX() - startX;
                        currentY = shapeStartY + mouseEvent.getScreenY() - startY;
                        leader.setLayoutX(currentX);
                        leader.setLayoutY(currentY);
                        follower.setLayoutX(currentX);
                        follower.setLayoutY(currentY);
                        if(reset)
                        {
                            didReset = true;
                            resetHandlers();
                            //follower.setStroke(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getOpacity()/2));
                        }
                        reset = false;
                    }
                    else
                    {
                        startTime = null;
                    }
                }
                else if(mouseEvent.getEventType() ==MouseEvent.MOUSE_RELEASED)
                {
                    if(didReset)
                    {
                        //TODO make all moveshapes use this constructor
                        commitChange(new MoveShape(follower, shapeStartX,shapeStartY, AnnotationToolApplication.this, leader));
                        setDrawingText();
                        didReset = false;
                        //follower.setFill(oldColor);
                    }
                }
            }
        });
    }


    private void setTouchFollowing(Shape leader, Shape follower)
    {
        leader.addEventHandler(TouchEvent.ANY, new EventHandler<TouchEvent>() {
            boolean reset = true;
            boolean didReset = false;
            double startX;
            double startY;
            double shapeStartX;
            double shapeStartY;
            double currentX;
            double currentY;
            long delayTime = 1000;
            Date startTime;
            @Override
            public void handle(TouchEvent event) {
                if(event.getEventType() == TouchEvent.TOUCH_PRESSED && event.getTouchCount() == 1)
                {
                    System.out.println("onetouch");
                    startTime = new Date();
                    shapeStartX = leader.getLayoutX();
                    shapeStartY = leader.getLayoutY();
                    startX = event.getTouchPoint().getScreenX();
                    startY = event.getTouchPoint().getScreenY();
                    reset = true;
                }
                else if(event.getEventType() == TouchEvent.TOUCH_MOVED && event.getTouchCount() ==1 && startTime !=null)
                {
                    if (new Date().getTime() - startTime.getTime() > delayTime)
                    {
                        currentX = shapeStartX + event.getTouchPoint().getScreenX() - startX;
                        currentY = shapeStartY + event.getTouchPoint().getScreenY() - startY;
                        leader.setLayoutX(currentX);
                        leader.setLayoutY(currentY);
                        follower.setLayoutX(currentX);
                        follower.setLayoutY(currentY);
                        if(reset)
                        {
                            didReset = true;
                            resetHandlers();
                            //follower.setStroke(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getOpacity()/2));
                        }
                        reset = false;
                    }
                    else
                    {
                        startTime = null;
                    }
                }
                else if(event.getEventType() == TouchEvent.TOUCH_RELEASED && didReset)
                {
                    if(didReset)
                    {
                        //TODO make all moveshapes use this constructor
                        commitChange(new MoveShape(follower, shapeStartX,shapeStartY, AnnotationToolApplication.this, leader));
                        setDrawingText();
                        didReset = false;
                        //follower.setFill(oldColor);
                    }
                }
            }
        });
    }

    /**
     * Minimizes both the picture stage and the mouse catching stage.
     */
    public void sendToBack()
    {
        pictureStage.setIconified(true);
        mouseCatchingStage.setIconified(true);
    }

    /**
     * Un-minimizes the picture stage and the mouse catching stage.
     */
    public void bringToFront()
    {
        pictureStage.setIconified(false);
        mouseCatchingStage.setIconified(false);
        controllerBox.toFront();
    }



    public Stack<ChangeItem> getRedoStack()
    {
        return redoStack;
    }

    public Paint getPaint()
    {
        return paint;
    }


    public boolean getClickable()
    {
        return clickable;
    }

    public void setText(Text text)
    {
        this.text = text;
    }
    public Text getText()
    {
        return text;
    }

    /**
     * Allows the follower shape to be moved using a click and hold event
     *
     * @param follower
     */
    public void addLeaderToFollower(Shape follower)
    {
        Shape leader = Shape.union(follower,follower);
        setFollowing(leader, follower);
        leaderRoot.getChildren().add(leader);
        leadersToFollowers.put(leader,follower);
        followersToLeaders.put(follower,leader);
    }

    public Map<Shape, Shape> getFollowersToLeaders()
    {
        return followersToLeaders;
    }

    /**
     * Handler to reset the controllerbox to make sure it is on top
     * in case it is not for whatever reason.
     */
    private class PutControllerBoxOnTopHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            controllerBox.setAlwaysOnTop(false);
            controllerBox.setAlwaysOnTop(true);
        }
    }

    public void fileManagement(String flag) throws IOException {

        String path = getFileName();
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("."));
        //Set extension filter
        chooser.setInitialFileName(path);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Annotation files (*.jnote)", "*.jnote");
        chooser.getExtensionFilters().add(extFilter);

        switch (flag) {
            case "new":  //new project
                writer.flush();
                writer.close();
                File file = chooser.showSaveDialog(primaryStage);
                path = file.getAbsoluteFile().toString();
                jnote_fileName = path;
                root.getChildren().clear(); //clears the stage

                undoStack.clear();
                redoStack.clear();
                prev_shapes.clear();
                remakeFromJSON();
                System.out.println("new Project: " + jnote_fileName);
                break;
            case "open": //open project
                writer.flush();
                writer.close();
                path = chooser.showOpenDialog(null).getAbsoluteFile().toString();

                root.getChildren().clear(); //clears the stage

                undoStack.clear();
                redoStack.clear();
                prev_shapes.clear();
                jnote_fileName = path;
                remakeFromJSON();
                System.out.println("open Project: " + jnote_fileName);
                break;
            case "save":  // save as
                writer.flush();
                writer.close();
                //Show save file dialog
                File file_2 = chooser.showSaveDialog(primaryStage);
                path = file_2.getAbsoluteFile().toString();
                copy(new File(jnote_fileName), new File(path));
                jnote_fileName = path;
                remakeFromJSON();
                System.out.println("save As: " + jnote_fileName);

                break;
            case "sFile": // save file
            	System.out.println("AAAAAAAA, IT'S " + jnote_fileName);
            	writeJSON(jnote_fileName);
            	break;
        }
    }

    // Copy the source file to target file.
    // In case the dst file does not exist, it is created
   private  void copy(File source, File target) throws IOException {

        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
}