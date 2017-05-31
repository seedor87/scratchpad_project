package annotationtool;/**
 * Created by remem on 5/30/2017.
 */

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.MouseEvent;

import java.awt.*;

public class AnnotationToolApplication extends Application
{
    private final Color clickablyClearPaint = new Color(1,1,1,1d/255d);
    private Stage stage;
    private Scene scene;
    private VBox box;
    private Group root;
    private Path path;
    private javafx.scene.paint.Paint paint = Color.YELLOW;
    private Stroke stroke;


    private boolean makingTextBox = false;
    public void setStroke(Stroke stroke)
    {
        this.stroke = stroke;        //TODO this
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
        //TODO this
    }
    public void undo()
    {
        //TODO this
    }
    public void clearHistory()
    {        //TODO this

    }
    public void toggleClickable()
    {
        //TODO this
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

        scene = new Scene(root, 400, 200);

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
        scene.addEventHandler(MouseEvent.MOUSE_CLICKED, new CircleHandler());
    }
    private class CircleHandler implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            Circle circle = new Circle(event.getSceneX(), event.getSceneY(),30);
            circle.setFill(getPaint());
            //System.out.println(paint);
            //System.out.println(Color.YELLOW);
            root.getChildren().add(circle);
            System.out.println(event);
        }
    }
    private Paint getPaint()
    {
        return this.paint;
    }

    public void setTextSize(Integer textSize) {
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

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        //this.alwaysOnTop = alwaysOnTop;
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
