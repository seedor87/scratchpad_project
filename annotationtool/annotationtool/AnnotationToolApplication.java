package annotationtool;
/**
 * Created by remem on 5/30/2017.
 */

import com.sun.corba.se.spi.ior.Writeable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.w3c.dom.events.*;
import sun.security.provider.SHA;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.chrono.Era;
import java.util.*;
import java.util.List;
import java.util.logging.Handler;

public class AnnotationToolApplication extends Application {

    static
    {
        System.setProperty("java.awt.headless", "false");           //TODO test in linux.
        //https://stackoverflow.com/questions/2552371/setting-java-awt-headless-true-programmatically
        //TODO that link might help with getting images in ubuntu.
    }
    private FXControllerBox controllerBox;

    private class HandlerGroup
    {
        EventType eventType;
        EventHandler handler;
        HandlerGroup(EventType eventType, EventHandler handler)
        {
            this.eventType = eventType;
            this.handler = handler;
        }
    }
    private class EraseShape extends Path
    {
        Path eraseArea;
        Stack<Shape> shapesPartiallyErased = new Stack<>();
        EraseShape(Path eraseArea)
        {
            this.eraseArea = eraseArea;
        }
    }
    private final Color clickablyClearPaint = new Color(1, 1, 1, 1d / 255d);
    private final Color clearPaint = new Color(0, 0, 0, 0);
    private Stage stage;
    private Scene scene;
    private VBox box;
    private Group root;
    private Path path;
    private Line line;
    private Path eraserPath;
    private javafx.scene.paint.Paint paint = Color.BLACK;
    private Stroke stroke;
    private boolean mouseTransparent = false;
    private double strokeWidth;
    private boolean clickable = true;
    private Stack<Shape> undoStack = new Stack<>();
    private Stack<Shape> redoStack = new Stack<>();
    private DrawingHandler drawingHandler = new DrawingHandler();
    private ArrowHandler arrowHandler = new ArrowHandler();
    private Text text;
    private Color textColor = Color.BLACK;
    private double textSize = 100d;
    private TextBoxHandler textBoxHandler = new TextBoxHandler();
    private StringBuffer textBoxText = new StringBuffer(64);
    private TextBoxKeyHandler textBoxKeyHandler = new TextBoxKeyHandler();
    private TouchSendToBackHandler touchSendToBackHandler = new TouchSendToBackHandler();

    private List<HandlerGroup> eventHandlers = new LinkedList<HandlerGroup>();

    private Circle circle;
    private CircleHandler circleHandler = new CircleHandler();

    private EraseHandler eraseHandler = new EraseHandler();

    private int boxWidth = 0;

    private boolean makingTextBox = false;
    private int saveImageIndex = 0;

    final ClipboardOwner clipboardOwner = new ClipboardOwner() {
        @Override
        public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
        }
    };

    public void setStroke(double strokeWidth)
    {
        this.strokeWidth = strokeWidth;
    }

    public void doClear()
    {
        doClear(clickablyClearPaint);
    }

    /**
     * Currently clears the screen transparently.
     * To do for a color, can simply commit the "blockOutShape"
     * If implemented, move the code up to doClear() and then do that.
     * @param color
     */
    public void doClear(Color color)
    {
        double h = stage.getHeight();
        double w = stage.getWidth();
        Path blockOutShape = new Path();
        blockOutShape.setStrokeWidth(h);
        blockOutShape.getElements().add(new MoveTo(0,h/2));
        blockOutShape.getElements().add(new LineTo(w, h/2));
        blockOutShape.setFill(clearPaint);
        EraseShape eraseShape = new EraseShape(blockOutShape);
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                commitShape(eraseShape);
            }
        });
        redoStack.clear();
    }
    private void commitShape(Shape shape)
    {
        if(shape instanceof EraseShape)
        {
            eraseShapes((EraseShape) shape);
        }
        else
        {
            root.getChildren().add(shape);
        }
        undoStack.push(shape);
    }

    public void redo()
    {
        if (redoStack.size() > 0)
        {
            Shape temp = redoStack.pop();
            Platform.runLater(new Runnable() {
                @Override
                public void run()
                {
                    commitShape(temp);
                }
            });
        }
    }

    public void undo()
    {
        if (undoStack.size() > 0)
        {
            Shape temp = undoStack.pop();
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
        }
    }

    private void undoAnEraseShape(EraseShape eraseShape)
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

    }

    public void clearHistory()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                root.getChildren().clear();
            }
        });
        undoStack.clear();
        redoStack.clear();
    }
    public void makeCircles()
    {
        this.resetHandlers();
        this.scene.addEventHandler(MouseEvent.ANY, circleHandler);
    }

    public void toggleClickable()
    {
        clickable = !clickable;
        this.resetHandlers();
        if (clickable)
        {
            scene.setFill(clickablyClearPaint);
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    stage.setAlwaysOnTop(false);
                }
            });
            this.scene.addEventHandler(MouseEvent.ANY, drawingHandler);
        }
        else
            {
            scene.setFill(clearPaint);
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    stage.setAlwaysOnTop(true);
                }
            });
            }
    }


    public void setMakingTextBox(boolean makingTextBox) {
        if (makingTextBox)
        {
            this.resetHandlers();
            this.scene.addEventHandler(MouseEvent.MOUSE_CLICKED, textBoxHandler);
            this.scene.addEventHandler(KeyEvent.KEY_TYPED, textBoxKeyHandler);
        }
        else
        {
            this.resetHandlers();
            this.scene.addEventHandler(MouseEvent.ANY, drawingHandler);
            textBoxText.delete(0,textBoxText.length());
        }
    }

    /**
     * Removes all handlers from the scene.
     * Should be called before adding more in to prevent multiple handlers trying to handle the same event(S).
     */
    private void resetHandlers()
    {
        for (HandlerGroup h : eventHandlers)
        {
            this.scene.removeEventHandler(h.eventType,h.handler);
        }
    }

    /**
     * Sets the paint color based on a java.awt color object.
     * @param paintColor The color to convert to a javafx color object.
     */
    public void setPaint(Color paintColor)
    {
        this.paint = paintColor;
/*        this.paint = new javafx.scene.paint.Color(
                paintColor.getRed() / 255d,
                paintColor.getGreen() / 255d,
                paintColor.getBlue() / 255d,
                paintColor.getAlpha() / 255d);*/
    }


    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * The code starts here.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage)
    {
        this.stage = primaryStage;
        //this.stage.initStyle(StageStyle.UNDECORATED);
        this.stage.setMaximized(true);

        root = new Group();

        scene = new Scene(root);

        //final Scene scene = new Scene(box,300, 250);
        scene.setFill(clickablyClearPaint);

        primaryStage.setScene(scene);

        setupListeners();

        controllerBox = new FXControllerBox(this);
        boxWidth = controllerBox.getWidth();


        primaryStage.show();
    }

    /**
     * Sets up all default listeners that the code might need.
     * adds all listeners to the list of handlers so all can be removed at once if needed.
     * Should only ever use handlers that are added to eventHandlers
     */
    private void setupListeners()
    {
        scene.addEventHandler(MouseEvent.ANY, drawingHandler);
        scene.addEventHandler(ZoomEvent.ANY, touchSendToBackHandler);                       //Doesnt need to be added below cause we always wanna be listening for it
        scene.addEventHandler(MouseEvent.ANY, new BoxHidingHandler());

        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, drawingHandler));
        eventHandlers.add(new HandlerGroup(KeyEvent.KEY_TYPED,textBoxKeyHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.MOUSE_CLICKED, textBoxHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, circleHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, eraseHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, arrowHandler));
    }

    /**
     * Adds a circle at the given location of the MouseEvent.
     */
    private class CircleHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event)
        {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
            {
                //                circle = new Circle(event.getSceneX(), event.getSceneY(),10, paint);      //just this line for full circle.
                circle = new Circle(event.getSceneX(), event.getSceneY(),10, Color.TRANSPARENT);
                circle.setStroke(paint);
                circle.setStrokeWidth(strokeWidth);
                commitShape(circle);

            }
            else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED)
            {
                double xDistance = event.getX() - circle.getCenterX();
                double yDistance = event.getY() - circle.getCenterY();
                circle.setRadius(pythagorize(xDistance,yDistance));
            }
            else if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
            {//TODO fix issue that you get when you draw a circle that is very thick, but very small as well.
                undo();
                circle.setFill(paint);
                Shape newCircle = Shape.subtract(circle, new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius() - (strokeWidth/2)));
                newCircle.setFill(paint);
                commitShape(newCircle);

                redoStack.clear();
                circle = null;
            }
        }
    }
    private double pythagorize(double x, double y)
    {
        double result;
        result = x * x;
        result = result + (y*y);
        return Math.sqrt(result);
    }

    /**
     * Triggers toggleClickable when triggered. Should be implemented with ZoomEvent.ANY
     */
    private class TouchSendToBackHandler implements EventHandler<ZoomEvent>
    {
        @Override
        public void handle(ZoomEvent event)
        {
            if(event.getEventType() == ZoomEvent.ZOOM_STARTED)
            {
                resetHandlers();
            }
            if(event.getEventType() == ZoomEvent.ZOOM_FINISHED)
            {
                if(event.getTotalZoomFactor() < 1)          // if the user triggers a zoom out event.
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            toggleClickable();
                        }
                    });
                }
                scene.addEventHandler(MouseEvent.ANY, drawingHandler);
            }
        }
    }

    /**
     * Draws lines based on the location of various mouse events.
     * Pressing the mouse starts the line, dragging it extends.
     * Releasing ends the line.
     */
    private class DrawingHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event)
        {
            if(clickable)
            {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                path = new Path();
                path.setStrokeWidth(strokeWidth);
                path.setSmooth(true);
                MoveTo moveTo = new MoveTo(event.getX(), event.getY());
                path.getElements().add(moveTo);
                root.getChildren().add(path);
                path.setStroke(paint);
                }
                else if (path != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                LineTo moveTo = new LineTo(event.getX(), event.getY());
                path.getElements().add(moveTo);
                }
                else if (path != null && event.getEventType() == MouseEvent.MOUSE_RELEASED)
                {
                //path.setFillRule(FillRule.EVEN_ODD);
                //path.setFill(paint);
                //root.getChildren().add(path);
                undoStack.push(path);
                path = null;
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
            text.setFont(new Font(textSize));
            text.setFill(paint);
            undoStack.push(text);
            root.getChildren().add(text);
            textBoxText.delete(0,textBoxText.length());
            redoStack.clear();
        }
    }

    /**
     * Edits the current text box based on key inputs. should be implemented with KeyEvent.KEY_TYPED
     */
    private class TextBoxKeyHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            char c = event.getCharacter().charAt(0);
            if((( c > 31)&&(c < 127)))
            {
                textBoxText.append(c);
                text.setText(textBoxText.toString());
            }
            else if(c == 8)
            {
                if(textBoxText.length() > 0)
                {
                    textBoxText.deleteCharAt(textBoxText.length()-1);
                    text.setText(textBoxText.toString());
                }
            }
            else if(c == 13)
            {
                textBoxText.append(System.lineSeparator());
                text.setText(textBoxText.toString());
            }
        }
    }
    private class EraseHandler implements EventHandler<MouseEvent>
    {
        Color eraserColor = new Color(0,0,0,.1);
        @Override
        public void handle(MouseEvent event)
        {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
            {
                eraserPath = new Path();
                eraserPath.setStrokeWidth(strokeWidth);
                eraserPath.setSmooth(true);
                MoveTo moveTo = new MoveTo(event.getX(), event.getY());
                eraserPath.getElements().add(moveTo);
                root.getChildren().add(eraserPath);
                eraserPath.setStroke(eraserColor);
            }
            else if (eraserPath != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                LineTo moveTo = new LineTo(event.getX(), event.getY());
                eraserPath.getElements().add(moveTo);
            }
            else if (eraserPath != null && event.getEventType() == MouseEvent.MOUSE_RELEASED)
            {
                root.getChildren().remove(eraserPath);
                EraseShape eraseShape = new EraseShape(eraserPath);
                commitShape(eraseShape);
                eraseShape = null;
                redoStack.clear();
            }
        }
    }
    /**
     * Hides the box when not being used.
     */
    private class BoxHidingHandler implements EventHandler<MouseEvent>
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
    }
    /**
     * Creates arrows. should be implemented with MouseEvent.ANY.
     */
    private class ArrowHandler implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            if(clickable)
            {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    line = new Line(event.getX(), event.getY(), event.getX(), event.getY());
                    line.setStroke(paint);
                    line.setStrokeWidth(strokeWidth);
                    //line.setStartX(event.getX());
                    //line.setStartY(event.getY());
                    //path.setStrokeWidth(strokeWidth);
                    //path.setSmooth(true);
                    //MoveTo moveTo = new MoveTo(event.getX(), event.getY());
                    //path.getElements().add(moveTo);
                    commitShape(line);
                    //root.getChildren().add(path);
                }
                else if (line != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    line.setEndX(event.getX());
                    line.setEndY(event.getY());
                    //LineTo moveTo = new LineTo(event.getX(), event.getY());
                    //path.getElements().add(moveTo);
                }
                else if (line != null && event.getEventType() == MouseEvent.MOUSE_RELEASED)
                {
                    addArrowToEndOfLine(event);
                    //undoStack.push(path);
                    line = null;
                    redoStack.clear();
                }

            }
        }
    }

    /**
     *
     * @param mouseEvent
     */
    private void addArrowToEndOfLine(MouseEvent mouseEvent)
    {
        final double halfBaseDistance = 2;
        final double heightDistance = 4;
        double slope;
        double xDistance = line.getEndX() - line.getStartX();
        double yDistance = line.getEndY() - line.getStartY();
        if(line.getEndY() == line.getStartY())
        {
            slope = Double.POSITIVE_INFINITY;
        }
        else
        {
            slope =  xDistance - yDistance;
        }
        Polygon triangle = new Polygon();
        if(slope == Double.POSITIVE_INFINITY)//straight upwards line.//TODO check which direction. //DO I really need this check/part?
        {
            //System.out.println("Thing");
            triangle.getPoints().addAll( (mouseEvent.getX() - halfBaseDistance*strokeWidth), mouseEvent.getY());
            triangle.getPoints().addAll( (mouseEvent.getX() + halfBaseDistance*strokeWidth), mouseEvent.getY());
            triangle.getPoints().addAll( mouseEvent.getX(), (mouseEvent.getY() - heightDistance*strokeWidth));
        }
        else
        {
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
            commitShape(newShape);
        }

    }
    public void makeLines()
    {
        this.resetHandlers();
        this.scene.addEventHandler(MouseEvent.ANY, arrowHandler);
    }


    /**
     * Goes through the undo stack and erases parts of shapes contained in a given path.
     */
    private void eraseShapes(EraseShape eraseShape)
    {
        Shape oldShape;
        Shape newShape;
        ListIterator<Shape> iterator = undoStack.listIterator(undoStack.size());        //list iterator starting from top of stack.
        while(iterator.hasPrevious())
        {
            oldShape = iterator.previous();
            if(!(oldShape instanceof EraseShape))                                       //not instance of eraseshape
            {
                newShape = Shape.subtract(oldShape, eraseShape.eraseArea);
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
    }
    private void paintFromUndoStack()
    {
        root.getChildren().clear();
        for(Shape s : undoStack)
        {
            if(!(s instanceof EraseShape))
            {
                root.getChildren().add(s);
            }
        }
    }
    public void turnOnErasing()
    {
        this.resetHandlers();
        this.scene.addEventHandler(MouseEvent.ANY, eraseHandler);
    }

    public void setTextSize(Integer textSize)
    {
        this.textSize = (double) textSize;
    }

    public void setTextColor(java.awt.Color textColor)
    {
        this.textColor = new javafx.scene.paint.Color(
                textColor.getRed()/255d,
                textColor.getGreen()/255d,
                textColor.getBlue()/255d,
                textColor.getAlpha()/255d);
    }

    public void toFront()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.toFront();
            }
        });
    }

    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                stage.setAlwaysOnTop(alwaysOnTop);
            }
        });
    }

    public void doSave()
    {
        try
        {
            Field defaultHeadlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("defaultHeadless");
            defaultHeadlessField.setAccessible(true);
            defaultHeadlessField.set(null,Boolean.TRUE);
            Field headlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
            headlessField.setAccessible(true);
            headlessField.set(null,Boolean.FALSE);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            //see https://stackoverflow.com/questions/2552371/setting-java-awt-headless-true-programmatically for some more stuff on this code. Changed the code, but may work?
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                Robot robot;
                try
                {
                    robot = new Robot();
                }
                catch (AWTException e)
                {
                    throw new RuntimeException(e);          //potentially fixes robot working with ubuntu.
                }
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
                Clipboard clip = Clipboard.getSystemClipboard();//this.getToolkit().getSystemClipboard();
                clip.setContent(new HashMap<DataFormat, Object>());
                System.out.println(imageTag);

                try
                {
                    java.awt.Rectangle screenGrabArea = new java.awt.Rectangle((int)stage.getX() /*+ borderThickness*/, (int)stage.getY() /* + borderThickness*/,
                            (int)stage.getWidth()/* - (2 * borderThickness)*/, (int)stage.getHeight()/* - (2 * borderThickness)*/);
                    BufferedImage outImg = robot.createScreenCapture(screenGrabArea);
                    ImageIO.write(outImg, "png", outFile);
                }
                catch (HeadlessException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void toBack()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.toBack();
            }
        });
    }
}
//TODO list
/*
 * Add in more shapes
 *      also arrows
 * Make controller box dynamically resize as needed             sort of done
 * Color picker
 * Make save image work in linux.
 * Click text to select it and edit it
 * Click nodes to move them.
 * Click nodes to erase them.
 *
 * Resizable stage?
 * moveable stage?
 * Snipping tool?
 *
 * reset the redo stack if things happen.                       done
 */
/*
 *Meeting stuff
 * Implement Snipping tool instead of resizing?
 * Weird issue where undo doesn't undo if node is on top of thing sometimes. Can be fixed with repaint, but like...
 *
 * check into fill property of circles for circling things
 *
 */
/**Priority stuff
 * Make sure it works on linux.
 *      Then do pull request.
 * Make sure save image works on linux.
 *
 * Less Priority
 *
 * Circle thing                                                 done
 * Arrows                                                       done
 * moving/deleting shapes
 *
 * do similar paint fix that I did with circles to lines.
 */
