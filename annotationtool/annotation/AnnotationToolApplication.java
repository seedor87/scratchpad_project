package annotation;
/**
 * Created by remem on 5/30/2017.
 */
import changeItem.*;
import TransferableShapes.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;
import util.ProcessRunner;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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
    //record keeping
    UUID uuid;
    private Gson gson = new Gson();
    private String JSON_FILE_NAME = getJSON_FILE_NAME();
    private ArrayList prev_shapes = new ArrayList<Custom_Shape>();

    // Screen Setup and Layout
    private IconControllerBox controllerBox;
    private Stage mouseCatchingStage;
    private Stage pictureStage;
    private Stage textOptionStage;
    private Scene mouseCatchingScene;
    private Scene drawingScene;
    private Group root;
    private Group notRoot;
    private VBox box;
    private Color textColor = Color.BLACK;
    private Color borderColor = Color.BLUE;
    private javafx.scene.paint.Paint paint = Color.BLACK;
    // Handlers
    private List<HandlerGroup> eventHandlers = new LinkedList<HandlerGroup>();
    private MovingHandler movingHandler = new MovingHandler();
    private DrawingHandler drawingHandler = new DrawingHandler();
    private PutControllerBoxOnTopHandler putControllerBoxOnTopHandler = new PutControllerBoxOnTopHandler();
    private ArrowHandler arrowHandler = new ArrowHandler();
    private TwoTouchHandler twoTouchHandler = new TwoTouchHandler();
    private ShortcutHandler shortcutHandler = new ShortcutHandler();
    private TextBoxHandler textBoxHandler = new TextBoxHandler();
    private TextBoxKeyHandler textBoxKeyHandler = new TextBoxKeyHandler();
    private TouchSendToBackHandler touchSendToBackHandler = new TouchSendToBackHandler();
    private CircleHandler circleHandler = new CircleHandler();
    private EraseHandler eraseHandler = new EraseHandler();
    private TwoTouchChangeSizeAndMoveHandler twoTouchChangeSizeAndMoveHandler = new TwoTouchChangeSizeAndMoveHandler();
    // Annotation Objects
    private Path path;
    private Line line;
    private Path eraserPath;
    private Stroke stroke;
    private Text text;
    private Circle circle;
    private Rectangle borderShape;
    private StringBuffer textBoxText = new StringBuffer(64);
    private Stack<ChangeItem> undoStack = new Stack<>();
    private Stack<ChangeItem> redoStack = new Stack<>();
    // Cursors
    private Cursor pencilCursor = new ImageCursor(new Image("pencil-cursor.png"));
    private Cursor eraserCursor = new ImageCursor(new Image("eraser-cursor.png"));
    private Cursor textCursor = new ImageCursor(new Image("TextIcon.png"));
    private Cursor arrowCursor = new ImageCursor(new Image("arrow-cursor.png"));
    // Settings
    private String windowID = "";
    private String textFont = "Times New Roman";
    private double strokeWidth = 5d;
    private double textSize = 24d;
    private int borderWidth = 5;
    private int boxWidth = 0;
    private int saveImageIndex = 0;
    private boolean mouseTransparent = false;
    private boolean clickable = true;
    private boolean makingTextBox = false;
    private boolean saveTextBox = false;
    private boolean saveEditText = false;
    private EditText editTextToSave;

    //================================================================================
    // Constructors/Starts
    //================================================================================
    private boolean lockedControllerBox = true;

    public AnnotationToolApplication(Stage primaryStage, Stage secondaryStage, double x, double y, boolean sizedWindow) throws IOException {
        start(primaryStage, secondaryStage, x, y, sizedWindow);
    }

    public AnnotationToolApplication(Stage primaryStage, Stage secondaryStage, double x, double y, boolean sizedWindow, String windowID) throws IOException {
        this.windowID = windowID;
        start(primaryStage, secondaryStage, x, y, sizedWindow);
    }

    public void start(Stage primaryStage) throws IOException {
        start(primaryStage, new Stage(), 0, 0, false);
    }

    //================================================================================
    // Mutators
    //================================================================================

    /**
     * The code starts here.
     * @param primaryStage
     */
    public void start(Stage primaryStage, Stage secondaryStage, double x, double y, boolean sizedWindow) throws IOException {
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
        } else {
            this.mouseCatchingStage.setX(x);
            this.mouseCatchingStage.setY(y);
        }
        this.mouseCatchingStage.setOpacity(0.004);
        this.mouseCatchingStage.initStyle(StageStyle.UNDECORATED);

        root = new Group();

        notRoot = new Group();
        mouseCatchingScene = new Scene(notRoot);         // the scene that catches all the mouse events
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
        } else {
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

        if(windowID != "") {
            ScheduledExecutorService windowChecker = Executors.newScheduledThreadPool(1);
            Runnable task = () -> resnapToWindow(windowID, windowChecker);

            int initialDelay = 0;
            int period = 10;

            windowChecker.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
        }

        mouseCatchingStage.show();
        pictureStage.show();

        resetStages();

        remakeFromJSON();
        try {
            writer = new FileWriter(JSON_FILE_NAME);
        } catch (FileNotFoundException fileNotFound) {
            System.out.println("ERROR: While Creating or Opening the File " + JSON_FILE_NAME);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (Object prev_shape : prev_shapes) {
            writeJSON((Custom_Shape) prev_shape);
        }





    }

    private void remakeFromJSON() {


        try {
            InputStream is = new FileInputStream(new File(JSON_FILE_NAME));
            Reader r = new InputStreamReader(is, "UTF-8");
            Gson gson = new GsonBuilder().create();
            JsonStreamParser p = new JsonStreamParser(r);
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
        } catch (Exception exc) {
            exc.printStackTrace();
        }



    }

    private void saveTextBox()
    {
        uuid = UUID.randomUUID();
        Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.TEXT_STRING, text.getText(), textFont);
        custom_shape.setTextSize(text.getFont().getSize() + "");
        custom_shape.setColorString(paint.toString());
        custom_shape.setLocation(new Point(String.valueOf(text.getX()), String.valueOf(text.getY())));
        try{
            writeJSON(custom_shape);
            Custom_Shape.setUpUUIDMaps(text, uuid);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    private class SaveTextBoxHandler implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            if(saveTextBox)
            {
                saveTextBox();
                saveTextBox = false;
            }
        }
    }
    private void saveEditText()
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
    private class SaveEditTextHandler implements EventHandler<MouseEvent>
    {
        //TODO, add this to the stages that it needs to be added to
        @Override
        public void handle(MouseEvent event)
        {
            if(saveEditText)
            {
                saveEditText();
                saveEditText = false;
            }

        }
    }

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
        ArrayList<Point> pathElements = new ArrayList<>();
        pathElements.add(new Point(0 +"",h/2 +""));
        pathElements.add(new Point(w + "", h/2 + ""));
        try
        {
            uuid = UUID.randomUUID();
            Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.ERASE_STRING, pathElements);
            shape.setStrokeWidth(String.valueOf(h));

            writeJSON(shape);

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

 /*       if(shape instanceof EraseShape)
        {
            eraseShapes((EraseShape) shape);
        }
        else
        {
            root.getChildren().add(shape);
        }
        undoStack.push(shape);*/
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
        /*
        if (redoStack.size() > 0)
        {
            Shp temp = redoStack.pop();
            Platform.runLater(new Runnable() {
                @Override
                public void run()
                {
                    commitShape(temp);
                }
            });
        }*/
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
/*        if (undoStack.size() > 0)
        {
            Shp temp = undoStack.pop();
            if(temp instanceof EraseShape)
            {
                undoAnEraseShape((EraseShape) temp);
            }
            else
                {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //root.getChildren().remove(root.getChildren().size() -1);
                        root.getChildren().remove(temp);        // above line probably more efficient.
                        paintFromUndoStack();                   //inefficient but solves problem.
                        //System.out.println("removing shape");
                    }
                });
            }
            redoStack.push(temp);
        }*/
    }

/*    private void undoAnEraseShape(EraseShape eraseShape)
    {
        undoStack.clear();
        while(!(eraseShape.shapesPartiallyErased.isEmpty()))
        {
            undoStack.push(eraseShape.shapesPartiallyErased.pop());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                paintFromUndoStack();
            }
        });

    }*/

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
        try
        {
            Files.write(new File(JSON_FILE_NAME).toPath(), Arrays.asList(""), StandardOpenOption.TRUNCATE_EXISTING);
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
     */
    public void toggleClickable() {
        clickable = !clickable;
        this.resetHandlers();
        if (clickable) {
            mouseCatchingScene.setFill(clickablyClearPaint);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pictureStage.setAlwaysOnTop(false);
                    mouseCatchingStage.setAlwaysOnTop(false);
                    mouseCatchingStage.setOpacity(0.004);
                }
            });
            this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
        } else {
            mouseCatchingScene.setFill(clearPaint);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pictureStage.setAlwaysOnTop(true);
                    mouseCatchingStage.setAlwaysOnTop(true);
                    mouseCatchingStage.setOpacity(0.0);
                    controllerBox.setAlwaysOnTop(false);//resets the controllerbox so that it stays on top.
                    controllerBox.setAlwaysOnTop(true);

                }
            });
        }
    }

    /**
     * Removes all handlers from the scene.
     * Should be called before adding more in to prevent multiple handlers trying to handle the same event(S).
     */
    private void resetHandlers() {
        for (HandlerGroup h : eventHandlers) {
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
     * Sets up all default listeners that the code might need.
     * adds all listeners to the list of handlers so all can be removed at once if needed.
     * Should only ever use handlers that are added to eventHandlers
     */
    private void setupListeners() {
        drawingScene.addEventHandler(MouseEvent.MOUSE_PRESSED, putControllerBoxOnTopHandler);
        mouseCatchingScene.addEventHandler(MouseEvent.MOUSE_PRESSED,putControllerBoxOnTopHandler);
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
        //mouseCatchingScene.addEventHandler(ZoomEvent.ANY, touchSendToBackHandler);                       //Doesnt need to be added below cause we always wanna be listening for it
        //mouseCatchingScene.addEventHandler(TouchEvent.ANY, twoTouchHandler);
        mouseCatchingScene.addEventHandler(TouchEvent.ANY, twoTouchChangeSizeAndMoveHandler);
        mouseCatchingScene.addEventHandler(KeyEvent.KEY_PRESSED, shortcutHandler);
        //mouseCatchingScene.addEventHandler(MouseEvent.ANY, new moveShapeHandler());
        mouseCatchingScene.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveTextBoxHandler());
        pictureStage.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveTextBoxHandler());
        mouseCatchingStage.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveEditTextHandler());
        pictureStage.addEventHandler(MouseEvent.MOUSE_PRESSED, new SaveEditTextHandler());


        //mouseCatchingStage.addEventHandler(TouchEvent.ANY, new TwoTouchChangeSize());
/*
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, new BoxHidingHandler());
*/

        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, drawingHandler));
        eventHandlers.add(new HandlerGroup(KeyEvent.KEY_TYPED,textBoxKeyHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.MOUSE_RELEASED, textBoxHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, circleHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, eraseHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, arrowHandler));
        //eventHandlers.add(new HandlerGroup(TouchEvent.ANY, twoTouchHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, movingHandler));
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
                pictureStage.setX(mouseCatchingStage.getX());
                if(lockedControllerBox) {
                    controllerBox.fitScreen();
                }
            }
        });

        mouseCatchingStage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pictureStage.setY(mouseCatchingStage.getY());
                if(lockedControllerBox) {
                    controllerBox.fitScreen();
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
    private void resizeAnnotationWindow2(double width, double height) {
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
    private void resizeAnnotationWindow(double changeX, double changeY) {
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
     * @param executor The Scheduled Executor Service calling this method.
     */
    private void resnapToWindow(String windowID, ScheduledExecutorService executor) {
        Process proc = null;
        Double[] windowInfo = ProcessRunner.getWindowInfoByID(windowID, proc);
        if(windowInfo[0] != -1 && windowInfo[1] != -1 && windowInfo[2] != -1 && windowInfo[3] != -1) {
            resizeAnnotationWindow2(windowInfo[0], windowInfo[1]);
            mouseCatchingStage.setX(windowInfo[2]);
            mouseCatchingStage.setY(windowInfo[3]);
        } else {
            executor.shutdownNow();
        }
    }


    /**
     * x^2 + y^2 = z^2
     * @param x
     * @param y
     * @return z
     */
    private double pythagorize(double x, double y) {
        double result;
        result = x * x;
        result = result + (y*y);
        return Math.sqrt(result);
    }


    /**
     * Hides the box when not being used.
     */
/*    private class BoxHidingHandler implements EventHandler<MouseEvent>
    {

        @Override
        public void handle(MouseEvent event)
        {
            if(event.getEventType() == MouseEvent.MOUSE_ENTERED)
            {
                controllerBox.setBounds(controllerBox.getX(), controllerBox.getY(), boxWidth,0);
            }
            else if(event.getEventType() == MouseEvent.MOUSE_EXITED)
            {
                controllerBox.pack();
            }
        }
    }*/


    /**
     *  adds a triangle to the most recent straight line drawn to make it an arrow.
     * @param mouseEvent
     */
    private void addArrowToEndOfLine(MouseEvent mouseEvent, UUID uuid) {
        final double halfBaseDistance = 2;
        final double heightDistance = 4;
        double slope;
        double xDistance = line.getEndX() - line.getStartX();
        double yDistance = line.getEndY() - line.getStartY();
        if(line.getEndY() == line.getStartY()) {
            slope = Double.POSITIVE_INFINITY;
        } else {
            slope =  xDistance - yDistance;
        }
        Polygon triangle = new Polygon();
        if(slope == Double.POSITIVE_INFINITY)//straight upwards line.//TODO check which direction. //DO I really need this check/part?
        {
            //System.out.println("Thing");
            triangle.getPoints().addAll( (mouseEvent.getX() - halfBaseDistance*strokeWidth), mouseEvent.getY());
            triangle.getPoints().addAll( (mouseEvent.getX() + halfBaseDistance*strokeWidth), mouseEvent.getY());
            triangle.getPoints().addAll( mouseEvent.getX(), (mouseEvent.getY() - heightDistance*strokeWidth));
        } else {
            //point 1
            triangle.getPoints().
                    addAll( (line.getEndX() - (halfBaseDistance*strokeWidth * Math.sin(Math.atan2(yDistance, xDistance)))),
                            line.getEndY() + (halfBaseDistance*strokeWidth * Math.cos(Math.atan2(yDistance,xDistance))));
            //point 2
            triangle.getPoints().
                    addAll( (line.getEndX() + (halfBaseDistance*strokeWidth * Math.sin(Math.atan2(yDistance, xDistance)))),
                            line.getEndY() - (halfBaseDistance*strokeWidth * Math.cos(Math.atan2(yDistance,xDistance))));
            //triangle.getPoints().addAll( (mouseEvent.getX() + 2*strokeWidth), mouseEvent.getY());
            //point 3
            triangle.getPoints().
                    addAll(line.getEndX() + strokeWidth*heightDistance*Math.cos(Math.atan2(yDistance,xDistance)),
                            line.getEndY() + (strokeWidth*heightDistance*Math.sin(Math.atan2(yDistance, xDistance))));
            //triangle.getPoints().addAll( mouseEvent.getX() + 6), (mouseEvent.getY() + strokeWidth*4*Math.sin(Math.atan2(yDistance,xDistance)));
            //triangle.setRotate(90);
            Shape newShape = Shape.union(triangle, line);
            newShape.setFill(line.getStroke());
            undo();
            commitChange(new AddShape(newShape));
            Custom_Shape.setUpUUIDMaps(newShape, uuid);
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
     * Goes through the undo stack and erases parts of shapes contained in a given path.
     */
/*    private void eraseShapes(EraseShape eraseShape)
    {
        Shp oldShape;
        Shp newShape;
        ListIterator<ChangeItem> iterator = undoStack.listIterator(undoStack.size());        //list iterator starting from top of stack.
        while(iterator.hasPrevious())
        {
            oldShape = iterator.previous();
            if(!(oldShape instanceof EraseShape))                                       //not instance of eraseshape
            {
                newShape = Shp.subtract(oldShape, eraseShape.eraseArea);
                newShape.setFill(oldShape.getFill());
                if(oldShape.getFill() == null)
                {
                    newShape.setFill(oldShape.getStroke());
                }
                eraseShape.shapesPartiallyErased.add(oldShape);
                iterator.set(newShape);
            }
            else
            {
                eraseShape.shapesPartiallyErased.add(oldShape);                         //add should probably be push (same for a few lines up)?
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                paintFromUndoStack();
            }
        });
    }*/

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
    public void doSave() {

        try {
            Field defaultHeadlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("defaultHeadless");
            defaultHeadlessField.setAccessible(true);
            defaultHeadlessField.set(null,Boolean.TRUE);
            Field headlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
            headlessField.setAccessible(true);
            headlessField.set(null,Boolean.FALSE);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            //see https://stackoverflow.com/questions/2552371/setting-java-awt-headless-true-programmatically for some more stuff on this code. Changed the code, but may work?
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Robot robot;
                try {
                    robot = new Robot();
                } catch (AWTException e) {
                    throw new RuntimeException(e);          //potentially fixes robot working with ubuntu.
                }
                File outFile;
                String fname;
                do {
                    fname = String.format("image-%06d.png", saveImageIndex++);
                    System.out.println("Trying " + fname);
                    outFile = new File(fname);
                }
                while (outFile.exists());

                String imageTag = "<img src='" + fname +"'>";
                Clipboard clip = Clipboard.getSystemClipboard();//this.getToolkit().getSystemClipboard();
                clip.setContent(new HashMap<DataFormat, Object>());
                System.out.println(imageTag);

                try {
                    textOptionStage.hide();
                    controllerBox.hide();
                    java.awt.Rectangle screenGrabArea = new java.awt.Rectangle((int)mouseCatchingStage.getX() /*+ borderThickness*/, (int)mouseCatchingStage.getY() /* + borderThickness*/,
                            (int)mouseCatchingStage.getWidth()/* - (2 * borderThickness)*/, (int)mouseCatchingStage.getHeight()/* - (2 * borderThickness)*/);
                    BufferedImage outImg = robot.createScreenCapture(screenGrabArea);
                    ImageIO.write(outImg, "png", outFile);
                } catch (HeadlessException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    controllerBox.show();
                    if(makingTextBox)
                        textOptionStage.show();
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
    public void resetStages() {
        pictureStage.toFront();
        mouseCatchingStage.toFront();
        controllerBox.toFront();
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
    
    /*private CirclePopupMenu initializeShapeMenu() {
        StackPane popupPane = new StackPane();
    	popupPane.setMinSize(mouseCatchingStage.getWidth(), mouseCatchingStage.getHeight());
    	notRoot.getChildren().add(popupPane);ƒ
    	CirclePopupMenu shapeMenu = new CirclePopupMenu(popupPane, MouseButton.SECONDARY);
    	MenuItem testItem = new MenuItem("This is a test", new ImageView(new Image("pencil-cursor.png")));
    	shapeMenu.getItems().add(testItem);
    	MenuItem testItem2 = new MenuItem("This is a test", new ImageView(new Image("pencil-cursor.png")));
    	shapeMenu.getItems().add(testItem2);
    	MenuItem testItem3 = new MenuItem("This is a test", new ImageView(new Image("pencil-cursor.png")));
    	shapeMenu.getItems().add(testItem3);
    	
    	return new CirclePopupMenu(popupPane, MouseButton.SECONDARY);
    }*/

    //================================================================================
    // Getters/Setters
    //================================================================================


    public Stage getPictureStage() {
        return this.pictureStage;
    }

    public Stage getMouseCatchingStage() {
        return this.mouseCatchingStage;
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
        mouseCatchingScene.setCursor(new ImageCursor(new Image("hand.png")));
        mouseCatchingScene.addEventHandler(MouseEvent.ANY, movingHandler);

    }

    /**
     * Sets the paint color based on a java.awt color object.
     * @param paintColor The color to convert to a javafx color object.
     */
    public void setPaint(Color paintColor) {
        this.paint = paintColor;
    /*	        this.paint = new javafx.scene.paint.Color(
                paintColor.getRed() / 255d,
                paintColor.getGreen() / 255d,
                paintColor.getBlue() / 255d,
                paintColor.getAlpha() / 255d);*/
    }

    public void setMakingText() {
        this.makingTextBox = true;
        this.resetHandlers();
        this.mouseCatchingScene.addEventHandler(MouseEvent.MOUSE_RELEASED, textBoxHandler);
        this.mouseCatchingScene.addEventHandler(KeyEvent.KEY_TYPED, textBoxKeyHandler);
        mouseCatchingScene.setCursor(textCursor);
    }


    /**
     * Sets the state of the program so that you are drawing.
     */
    public void setDrawingText() {
        this.resetHandlers();
        this.mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
        textBoxText.delete(0,textBoxText.length());
    }


    public Group getRoot() {
        return this.root;
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
    
    /*    private class EraseShape extends Path
    {
        Path eraseArea;
        Stack<Shp> shapesPartiallyErased = new Stack<>();
        EraseShape(Path eraseArea)
        {
            this.eraseArea = eraseArea;
        }
    }*/

    /**
     * Sets the state of the program so that the user can move shapes that are on the screen.
     */
    public void setSelectAndMoveHandler() {
        mouseCatchingStage.toFront();
        pictureStage.toFront();
        controllerBox.toFront();
        AddShape.movingShapes = true;
    }

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

    public ArrayList<Custom_Shape> readJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Custom_Shape> readShapes = mapper.reader().withType(new TypeReference<ArrayList<Custom_Shape>>() {
        }).readValue(new File(JSON_FILE_NAME));
        System.out.println(readShapes);
        return readShapes;
    }

    public void writeJSON(Custom_Shape shape) throws IOException {


        System.out.println(gson.toJson(shape));
        gson.toJson(shape, writer);



        /*ObjectMapper mapper = new ObjectMapper();
        holder.add(shape);

        String json = mapper.writeValueAsString(holder);
        System.out.println(json);
        FileWriter writer = new FileWriter(JSON_FILE_NAME);
        writer.write(json);
        writer.close();*/
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

    /**
     * Generates a file name based on date and time
     *
     * @return File name as a string.
     */
    public String getJSON_FILE_NAME() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        timeStamp += ".json";
        //return timeStamp;
        return "shape.json";
    }

    /**
     * Handler for moving the stage. should be implemented with MouseEvent.ANY when you add the
     * handler to the mousecatchingscene.
     */
    private class MovingHandler implements EventHandler<MouseEvent> {
        double originalX = -1;
        double originalY;
        double originalStageX;
        double originalStageY;

        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                originalX = event.getScreenX();
                originalY = event.getScreenY();
                originalStageX = mouseCatchingStage.getX();
                originalStageY = mouseCatchingStage.getY();
                mouseCatchingScene.setCursor(new ImageCursor(new Image("grab.png")));
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                double changeX = event.getScreenX() - originalX;
                double changeY = event.getScreenY() - originalY;
                mouseCatchingStage.setX(originalStageX + changeX);
                mouseCatchingStage.setY(originalStageY + changeY);
            } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                originalX = -1;
                mouseCatchingScene.setCursor(new ImageCursor(new Image("hand.png")));
            }
        }
    }

    /**
     * Creates arrows. should be implemented with MouseEvent.ANY when you add the
     * handler to the mousecatchingscene.
     */
    private class ArrowHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if(clickable)
            {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
                {
                    line = new Line(event.getX(), event.getY(), event.getX(), event.getY());
                    line.setStroke(paint);
                    line.setStrokeWidth(strokeWidth);
                    commitChange(new AddShape(line));
                }
                else if (line != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED)
                {
                    line.setEndX(event.getX());
                    line.setEndY(event.getY());
                }
                else if (line != null && event.getEventType() == MouseEvent.MOUSE_RELEASED)
                {
                    uuid = UUID.randomUUID();
                    addArrowToEndOfLine(event, uuid);

                    try {
                        Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.ARROW_STRING, (Color)paint, String.valueOf(strokeWidth), new Point(String.valueOf(line.getStartX()), String.valueOf(line.getStartY())), new Point(String.valueOf(line.getEndX()), String.valueOf(line.getEndY())));

                        // holder.add(shape);
                        writeJSON(shape);

                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }


                    line = null;
                    redoStack.clear();
                }

            }
        }
    }

    /**
     * Creates a text box at the given location of click. Should be implemented with MouseEvent.MOUSE_CLICKED
     * TextBoxKeyHandler changes the text in the box if needed.
     */
    private class TextBoxHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            String defaultText = "Text";
            text = new Text(event.getX(), event.getY(), defaultText);
            text.setFont(new Font(textFont, textSize));
            text.setFill(textColor);
            //undoStack.push(new AddShape(text));
            commitChange(new AddShape(text));
            root.getChildren().add(text);
            textBoxText.delete(0, textBoxText.length());
            saveTextBox = true;

            redoStack.clear();
        }
    }

    /**
     * Edits the current text box based on key inputs. should be implemented with KeyEvent.KEY_TYPED
     */
    private class TextBoxKeyHandler implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            char c = event.getCharacter().charAt(0);
            System.out.println(c);
            if (((c > 31) && (c < 127))) {
                textBoxText.append(c);
                text.setText(textBoxText.toString());
            } else if (c == 8) {
                if (textBoxText.length() > 0) {
                    textBoxText.deleteCharAt(textBoxText.length() - 1);
                    text.setText(textBoxText.toString());
                }
            } else if (c == 13) {
                textBoxText.append(System.lineSeparator());
                text.setText(textBoxText.toString());
            }
        }
    }

    /**
     * Draws lines based on the location of various mouse events.
     * Pressing the mouse starts the line, dragging it extends.
     * Releasing ends the line.
     * should be implemented with MouseEvent.ANY when you add the
     * handler to the mousecatchingscene.
     */
    private class DrawingHandler implements EventHandler<MouseEvent> {
        //TODO arraylist or linked?
        ArrayList<Point> pathElements;

        @Override
        public void handle(MouseEvent event) {
            if(clickable) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    path = new Path();
                    path.setStrokeWidth(strokeWidth);
                    path.setSmooth(true);
                    MoveTo moveTo = new MoveTo(event.getX(), event.getY());
                    LineTo lineTo = new LineTo(event.getX(), event.getY());
                    pathElements = new ArrayList<>();
                    pathElements.add(new Point(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
                    path.getElements().add(moveTo);
                    //root.getChildren().add(path);
                    commitChange(new AddShape(path));
                    path.setStroke(paint);
                } else if (path != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    LineTo moveTo = new LineTo(event.getX(), event.getY());
                    pathElements.add(new Point(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
                    path.getElements().add(moveTo);
                } else if (path != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {

                    try {
                        uuid = UUID.randomUUID();
                        Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.PATH_STRING, pathElements);
                        shape.setStrokeWidth(String.valueOf(path.getStrokeWidth()));
                        shape.setColorString(path.getStroke().toString());

                        //holder.add(shape);
                        writeJSON(shape);
                        Custom_Shape.setUpUUIDMaps(path, uuid);


                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace(); }
                    //path.setFillRule(FillRule.EVEN_ODD);
                    //path.setFill(paint);
                    //root.getChildren().add(path);
                    //undoStack.push(new AddShape(path));
                    path = null;
                    redoStack.clear();
                }

            }
        }
    }

    /**
     * Handles erasing a shaded area from the existing shapes on the screen.
     * should be implemented with MouseEvent.ANY when you add the
     * handler to the mousecatchingscene.
     */
    private class EraseHandler implements EventHandler<MouseEvent> {
        //TODO probably linkedlist is better.
        ArrayList<Point> pathElements;
        Color eraserColor = new Color(0,0,0,.1);

        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                pathElements = new ArrayList<>();
                eraserPath = new Path();
                eraserPath.setStrokeWidth(strokeWidth);
                eraserPath.setSmooth(true);
                MoveTo moveTo = new MoveTo(event.getX(), event.getY());
                pathElements.add(new Point(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
                eraserPath.getElements().add(moveTo);
                root.getChildren().add(eraserPath);
                eraserPath.setStroke(eraserColor);
            } else if (eraserPath != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                LineTo moveTo = new LineTo(event.getX(), event.getY());
                pathElements.add(new Point(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
                eraserPath.getElements().add(moveTo);
            } else if (eraserPath != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                root.getChildren().remove(eraserPath);
                changeItem.EraseShape eraseShape = new changeItem.EraseShape(eraserPath);
                commitChange(eraseShape);

                try {
                    uuid = UUID.randomUUID();
                    Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.ERASE_STRING, pathElements);
                    shape.setStrokeWidth(String.valueOf(eraserPath.getStrokeWidth()));

                    // holder.add(shape);
                    writeJSON(shape);

                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace(); }


                eraseShape = null;
                redoStack.clear();
            }
        }
    }

    /**
     * Triggers toggleClickable when triggered. Should be implemented with ZoomEvent.ANY
     * Not being used in the most current version.
     */
    private class TouchSendToBackHandler implements EventHandler<ZoomEvent> {
        @Override
        public void handle(ZoomEvent event) {
            if(event.getEventType() == ZoomEvent.ZOOM_STARTED) {
                resetHandlers();
            }
            if(event.getEventType() == ZoomEvent.ZOOM_FINISHED) {
                if(event.getTotalZoomFactor() < 1)          // if the user triggers a zoom out event.
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            toggleClickable();
                        }
                    });
                }
                mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
            }
        }
    }

    /**
     * Handles touch events that involve two touch points.
     * can be used to move the window with two fingers, and/or to
     * resize it with two fingers
     * should be implemented with TouchEvent.ANY when you add the
     * handler to the mousecatchingscene.
     */
    private class TwoTouchChangeSizeAndMoveHandler implements EventHandler<TouchEvent> {
        boolean using = false;
        int topPointIndex;
        int bottomPointIndex;
        int rightPointIndex;
        int leftPointIndex;
        TouchPoint topPoint;
        TouchPoint bottomPoint;
        TouchPoint leftPoint;
        TouchPoint rightPoint;
        double originalScreenX;
        double originalScreenY;
        double originalScreenWidth;
        double originalScreenHeight;

        double rightXChange = 0;
        double topYChange = 0;
        double leftXChange = 0;
        double bottomYChange = 0;

        @Override
        public void handle(TouchEvent event) {
            if(event.getTouchCount() == 2) {
                using = true;
                /*
                Sets up some variables to keep track of which first point was which.
                 */
                if(event.getEventType() == TouchEvent.TOUCH_PRESSED) {
                    resetHandlers();
                    originalScreenX = mouseCatchingStage.getX();
                    originalScreenY = mouseCatchingStage.getY();
                    originalScreenWidth = mouseCatchingStage.getWidth();
                    originalScreenHeight = mouseCatchingStage.getHeight();
                    double rightXChange = 0;
                    double topYChange = 0;
                    double leftXChange = 0;
                    double bottomYChange = 0;

                    TouchPoint point1 = event.getTouchPoints().get(0);
                    TouchPoint point2 = event.getTouchPoints().get(1);
                    if(point1.getY() < point2.getY()) {
                        topPointIndex = 0;
                        bottomPointIndex = 1;
                        topPoint = point1;
                        bottomPoint = point2;
                    } else {
                        bottomPointIndex = 0;
                        topPointIndex = 1;
                        bottomPoint = point1;
                        topPoint = point2;
                    }
                    if(point1.getScreenX() > point2.getScreenX()) {
                        rightPointIndex = 0;
                        leftPointIndex = 1;
                        rightPoint = point1;
                        leftPoint = point2;
                    } else {
                        rightPointIndex = 1;
                        leftPointIndex = 0;
                        rightPoint = point2;
                        leftPoint = point1;
                    }
                } else if(event.getEventType() == TouchEvent.TOUCH_MOVED && event.getTouchCount() == 2) {
                    for(TouchPoint touchPoint : event.getTouchPoints()) {
                        if(touchPoint.getState() != TouchPoint.State.STATIONARY) {
                            int index = event.getTouchPoints().indexOf(touchPoint);
                            if(index == rightPointIndex) {
                                rightXChange = touchPoint.getScreenX() - rightPoint.getScreenX();
                            } else if(index == leftPointIndex) {
                                leftXChange = leftPoint.getScreenX() - touchPoint.getScreenX();
                            }
                            if(index == topPointIndex) {
                                topYChange = topPoint.getScreenY() - touchPoint.getScreenY();
                            } else if(index == bottomPointIndex) {
                                bottomYChange = touchPoint.getScreenY() - bottomPoint.getScreenY();
                            }
                            //mouseCatchingStage.setHeight(topYChange + originalScreenHeight + bottomYChange);
                            //mouseCatchingStage.setWidth(rightXChange + leftXChange);
                            resizeAnnotationWindow2(rightXChange  +  originalScreenWidth + leftXChange
                                    ,topYChange + originalScreenHeight + bottomYChange);
                            mouseCatchingStage.setX(originalScreenX - leftXChange);
                            mouseCatchingStage.setY(originalScreenY - topYChange);
                        }
                    }
                }
            } else if(event.getEventType() == TouchEvent.TOUCH_RELEASED && using) {
                using = false;
                mouseCatchingScene.addEventHandler(MouseEvent.ANY, drawingHandler);
            }

        }
    }

    /**
     * not being used in current version. Original purpose was similar to TwoTouchChangeSizeAndMoveHandler
     */
    private class TwoTouchHandler implements EventHandler<TouchEvent> {
        TouchPoint firstPoint;
        TouchPoint secondPoint;
        private double[] primaryTouchCoords = {-1d, -1d};
        private double[] secondaryTouchCoords = {-1d, -1d};
        private double[] touchDist = {0, 0};
        private double resizeTolerance = 6;

        @Override
        public void handle(TouchEvent event) {
            if(event.getEventType() == TouchEvent.TOUCH_PRESSED && event.getTouchCount() == 2) {
                firstPoint = event.getTouchPoints().get(0);
                secondPoint = event.getTouchPoints().get(1);
            }
            if(event.getTouchCount() == 2) {
                TouchPoint primaryTouch = event.getTouchPoints().get(0);
                TouchPoint secondaryTouch = event.getTouchPoints().get(1);

                if(event.getEventType() == TouchEvent.TOUCH_PRESSED) {
                    setPoints(primaryTouch, secondaryTouch);
                }

                if(event.getEventType() == TouchEvent.TOUCH_MOVED) {
                    if(primaryTouchCoords[0] == -1 || primaryTouchCoords[1] == -1 || secondaryTouchCoords[0] == -1 || secondaryTouchCoords[1] == -1) {
                        setPoints(primaryTouch, secondaryTouch);
                    } else {
                        double[] newPrimaryCoords = {primaryTouch.getScreenX(), primaryTouch.getScreenY()};
                        double[] newSecondaryCoords = {secondaryTouch.getScreenX(), secondaryTouch.getScreenY()};
                        double[] newTouchDist = {Math.abs(newPrimaryCoords[0] - newSecondaryCoords[0]), Math.abs(newPrimaryCoords[1] - newSecondaryCoords[1])};
                        if(pythagorize(newTouchDist[0], newTouchDist[1]) > resizeTolerance) {
                            resizeAnnotationWindow(newTouchDist[0] - touchDist[0], newTouchDist[1] - touchDist[1]);
                        } else {
                            moveAnnotationWindow(newPrimaryCoords[0] - primaryTouchCoords[0], newPrimaryCoords[1] - primaryTouchCoords[1]);
                        }
                        primaryTouchCoords = newPrimaryCoords;
                        secondaryTouchCoords = newSecondaryCoords;
                        touchDist = newTouchDist;
                    }
                }
            }

            if(event.getEventType() == TouchEvent.TOUCH_RELEASED) {
                clickable = true;
                primaryTouchCoords[0] = -1;
                primaryTouchCoords[1] = -1;
                secondaryTouchCoords[0] = -1;
                secondaryTouchCoords[1] = -1;
                touchDist[0] = 0;
                touchDist[1] = 0;
            }
        }

        private void setPoints(TouchPoint primaryTouch, TouchPoint secondaryTouch) {
            clickable = false;
            primaryTouchCoords[0] = primaryTouch.getScreenX();
            primaryTouchCoords[1] = primaryTouch.getScreenY();
            secondaryTouchCoords[0] = secondaryTouch.getScreenX();
            secondaryTouchCoords[1] = secondaryTouch.getScreenY();
            touchDist[0] = Math.abs(primaryTouchCoords[0] - secondaryTouchCoords[0]);
            touchDist[1] = Math.abs(primaryTouchCoords[1] - secondaryTouchCoords[1]);
        }

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

    /**
     * Handles if the user presses the escape button.
     * If the user is in a text box, it closes the text box. It returns you to
     * drawing mode if you are in a text box.
     * <p>
     * If you are not in a text box, the program is closed.
     */
    private class ShortcutHandler implements EventHandler<KeyEvent> {
        public void handle(KeyEvent event) {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (makingTextBox) {
                    setDrawingText();
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.exit(0);
                        }
                    });
                }
            }
        }
    }

    /**
     * Adds a circle at the given location of the MouseEvent. should be
     * implemented with MouseEvent.ANY when you add the
     * handler to the mousecatchingscene.
     */
    private class CircleHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                //                circle = new Circle(event.getSceneX(), event.getSceneY(),10, paint);      //just this line for full circle.
                circle = new Circle(event.getSceneX(), event.getSceneY() ,10, Color.TRANSPARENT);
                circle.setStroke(paint);
                circle.setStrokeWidth(strokeWidth);
                commitChange(new AddShape(circle));


            } else if (circle != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                double xDistance = event.getX() - circle.getCenterX();
                double yDistance = event.getY() - circle.getCenterY();
                circle.setRadius(pythagorize(xDistance,yDistance ));

            } else if (circle != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                undo();
                circle.setFill(paint);
                Shape newCircle = Shape.subtract(circle, new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius() - (strokeWidth/2)));
                newCircle.setFill(paint);
                AddShape addShape = new AddShape(newCircle);
                commitChange(addShape);

                try {
                    uuid = UUID.randomUUID();
                    Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.CIRCLE_STRING);
                    shape.setLocation(new Point(String.valueOf(circle.getCenterX()), String.valueOf(circle.getCenterY())));
                    shape.setColorString((paint.toString()));
                    shape.setStrokeWidth(String.valueOf(strokeWidth));
                    shape.setRadius(String.valueOf(circle.getRadius()));

                    //holder.add(shape);
                    writeJSON(shape);
                    Custom_Shape.setUpUUIDMaps(newCircle, uuid);


                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace(); }


                redoStack.clear();
                circle = null;
            }


        }


    }

}





