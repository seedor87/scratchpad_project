package annotationtool;/**
 * Created by remem on 5/30/2017.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.MouseEvent;
import sun.security.provider.SHA;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

public class AnnotationToolApplication extends Application
{
    private final Color clickablyClearPaint = new Color(1,1,1,1d/255d);
    private final Color clearPaint = new Color(0,0,0,0);
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



    private boolean makingTextBox = false;
    public void setStroke(double strokeWidth)
    {
        this.strokeWidth = strokeWidth;        //TODO this
    }
    public void doClear()
    {
        //TODO this
    }
    public void doClear(java.awt.Color color)
    {
        //TODO this
    }
    public void redo()
    {
        if(redoStack.size() > 0)
        {
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
    public void undo()
    {
        if(undoStack.size() > 0)
        {
        Shape temp = undoStack.pop();
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                root.getChildren().remove(temp);
            }
        });
        redoStack.push(temp);
        }
    }

    public void clearHistory()
    {
        //TODO this
    }
    public void toggleClickable()
    {
        clickable = !clickable;
        if(clickable)
        {
            scene.setFill(clickablyClearPaint);
        }
        else
        {
            scene.setFill(clearPaint);
        }
        root.setMouseTransparent(!clickable);
    }


    public void setMakingTextBox(boolean makingTextBox)
    {
        this.makingTextBox = makingTextBox;
    }

    public void setPaint(java.awt.Color paintColor)
    {
        this.paint = new javafx.scene.paint.Color(
                paintColor.getRed()/255d,
                paintColor.getGreen()/255d,
                paintColor.getBlue()/255d,
                paintColor.getAlpha()/255d);
        System.out.println(this.paint);
    }


    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        this.stage = primaryStage;
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setMaximized(true);

        root = new Group();

        Text text = new Text("Transparent!");
        text.setFont(new Font(40));
        box = new VBox();
        box.getChildren().add(text);

        root.getChildren().add(box);

        scene = new Scene(root);

        //final Scene scene = new Scene(box,300, 250);
        scene.setFill(clickablyClearPaint);

        primaryStage.setScene(scene);

        setupListeners();

        FXControllerBox controllerBox = new FXControllerBox(this);
        controllerBox.setBounds(300, 0, 0, 0);
        controllerBox.pack();
        controllerBox.setVisible(true);

        primaryStage.show();
    }
    private void setupListeners()
    {
        //scene.addEventHandler(MouseEvent.MOUSE_CLICKED, new CircleHandler());
        scene.addEventFilter(MouseEvent.ANY, new DrawingHandler());
    }
    private class CircleHandler implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            Circle circle = new Circle(event.getSceneX(), event.getSceneY(),30);
            circle.setFill(paint);
            //System.out.println(paint);
            //System.out.println(Color.YELLOW);
            root.getChildren().add(circle);
            System.out.println(event);
        }
    }
    private class DrawingHandler implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                path = new Path();
                path.setStrokeWidth(strokeWidth);
                MoveTo moveTo = new MoveTo(event.getX(),event.getY());
                path.getElements().add(moveTo);
                root.getChildren().add(path);
                path.setStroke(paint);
            } else if (path != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                LineTo moveTo = new LineTo(event.getX(),event.getY());
                path.getElements().add(moveTo);
            } else if (path != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                //root.getChildren().add(path);
                undoStack.push(path);
                path = null;

            }

        }
    }
    private Paint getPaint()
    {
        return this.paint;
    }

    public void setTextSize(Integer textSize)
    {
        //this.textSize = textSize;
        //TODO this


    }

    public void setTextColor(java.awt.Color textColor) {
        //TODO this
        //this.textColor = textColor;
    }

    public void toFront()
    {
        //TODO this
    }

    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
//TODO this
    }

    public void doSave()
    {
        //TODO this
    }

    public void toBack()
    {
        //TODO this
    }
}
