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
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Handler;

public class AnnotationToolApplication extends Application {

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
    private final Color clickablyClearPaint = new Color(1, 1, 1, 1d / 255d);
    private final Color clearPaint = new Color(0, 0, 0, 0);
    private Stage stage;
    private Scene scene;
    private VBox box;
    private Group root;
    private Path path;
    private javafx.scene.paint.Paint paint = Color.YELLOW;
    private Stroke stroke;
    private boolean mouseTransparent = false;
    private double strokeWidth;
    private boolean clickable = true;
    private Deque<Shape> undoStack = new ArrayDeque<Shape>();
    private Deque<Shape> redoStack = new ArrayDeque<Shape>();
    private DrawingHandler drawingHandler = new DrawingHandler();
    private Text text;
    private Color textColor = Color.BLACK;
    private double textSize = 100d;
    private TextBoxHandler textBoxHandler = new TextBoxHandler();
    private StringBuffer textBoxText = new StringBuffer(64);
    private TextBoxKeyHandler textBoxKeyHandler = new TextBoxKeyHandler();
    private TouchSendToBackHandler touchSendToBackHandler = new TouchSendToBackHandler();

    private List<HandlerGroup> eventHandlers = new LinkedList<HandlerGroup>();


    private boolean makingTextBox = false;
    private int saveImageIndex = 0;

    final ClipboardOwner clipboardOwner = new ClipboardOwner() {
        @Override
        public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
        }
    };

    public void setStroke(double strokeWidth) {
        this.strokeWidth = strokeWidth;        //TODO this
    }

    public void doClear()
    {
        doClear(clickablyClearPaint);
    }

    public void doClear(Color color)
    {
        //TODO this does not work.
        double h = stage.getHeight();
        double w = stage.getWidth();
        Path blockOutShape = new Path();
        blockOutShape.setStrokeWidth(h);
        blockOutShape.getElements().add(new MoveTo(0,h/2));
        blockOutShape.getElements().add(new LineTo(w, h/2));
        blockOutShape.setFill(clearPaint);

        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                commitShape(blockOutShape);
            }
        });
    }
    private void commitShape(Shape shape)
    {
        undoStack.push(shape);
        root.getChildren().add(shape);
    }

    public void redo() {
        if (redoStack.size() > 0) {
            Shape temp = redoStack.pop();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    root.getChildren().add(temp);
                }
            });
            undoStack.push(temp);
        }
    }

    public void undo() {
        if (undoStack.size() > 0) {
            Shape temp = undoStack.pop();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    root.getChildren().remove(temp);
                }
            });
            redoStack.push(temp);
        }
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

    public void toggleClickable()
    {
        clickable = !clickable;
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
                this.resetHandlers();
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
    public void setPaint(java.awt.Color paintColor)
    {
        this.paint = new javafx.scene.paint.Color(
                paintColor.getRed() / 255d,
                paintColor.getGreen() / 255d,
                paintColor.getBlue() / 255d,
                paintColor.getAlpha() / 255d);
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
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setMaximized(true);

        root = new Group();

        scene = new Scene(root);

        //final Scene scene = new Scene(box,300, 250);
        scene.setFill(clickablyClearPaint);

        primaryStage.setScene(scene);

        setupListeners();

        FXControllerBox controllerBox = new FXControllerBox(this);
        controllerBox.setBounds(300, 0, 300, 0);
        controllerBox.pack();
        controllerBox.setVisible(true);

        primaryStage.show();
    }

    /**
     * Sets up all default listeners that the code might need.
     * adds all listeners to the list of handlers so all can be removed at once if needed.
     * Should only ever use handlers that are added to eventHandlers
     */
    private void setupListeners()
    {
        //scene.addEventHandler(MouseEvent.MOUSE_CLICKED, new CircleHandler());
        scene.addEventHandler(MouseEvent.ANY, drawingHandler);
        scene.addEventHandler(ZoomEvent.ANY, touchSendToBackHandler);

        eventHandlers.add(new HandlerGroup(MouseEvent.ANY, drawingHandler));
        eventHandlers.add(new HandlerGroup(KeyEvent.KEY_TYPED,textBoxKeyHandler));
        eventHandlers.add(new HandlerGroup(MouseEvent.MOUSE_CLICKED, textBoxHandler));
        //eventHandlers.add(new HandlerGroup(ZoomEvent.ZOOM_FINISHED,touchSendToBackHandler ));
    }

    /**
     * Adds a circle at the given location of the MouseEvent.
     */
    private class CircleHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Circle circle = new Circle(event.getSceneX(), event.getSceneY(), 30);
            circle.setFill(paint);
            //System.out.println(paint);
            //System.out.println(Color.YELLOW);
            root.getChildren().add(circle);
            System.out.println(event);
        }
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
                //root.getChildren().add(path);
                undoStack.push(path);
                path = null;
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
            text.setFill(textColor);
            undoStack.push(text);
            root.getChildren().add(text);
            textBoxText.delete(0,textBoxText.length());
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
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                File outFile;
                String fname;
                do {
                    fname = String.format("image-%06d.png", saveImageIndex++);
                    System.out.println("Trying " + fname);
                    outFile = new File(fname);
                } while (outFile.exists());

                String imageTag = "<img src='" + fname +"'>";
                Clipboard clip = Clipboard.getSystemClipboard();//this.getToolkit().getSystemClipboard();
                clip.setContent(new HashMap<DataFormat, Object>());
                System.out.println(imageTag);

                try
                {
                    java.awt.Rectangle screenGrabArea = new java.awt.Rectangle((int)stage.getX() /*+ borderThickness*/, (int)stage.getY() /* + borderThickness*/,
                            (int)stage.getWidth()/* - (2 * borderThickness)*/, (int)stage.getHeight()/* - (2 * borderThickness)*/);
                    BufferedImage outImg = new Robot().createScreenCapture(screenGrabArea);
                    ImageIO.write(outImg, "png", outFile);
                }
                catch (HeadlessException | AWTException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        //TODO this
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
