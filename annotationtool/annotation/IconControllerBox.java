package annotation;

import com.sun.javafx.font.freetype.HBGlyphLayout;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * Created by Resea on 6/14/2017.
 */
public class IconControllerBox extends Stage
{
    private HBox root;
    private Scene scene;
    private static final int IMAGE_WIDTH = 25;
    private static final int IMAGE_HEIGHT = 25;
    private static final int X_OFFSET = 30;
    public IconControllerBox(AnnotationToolApplication at)
    {
        this.setTitle("Tools");
        root = new HBox();
        scene = new Scene(root);
        this.setScene(scene);

        Button arrowButton = new Button();
        ImageView arrowImage = new ImageView("arrow.png");
        arrowImage.setFitHeight(IMAGE_HEIGHT);
        arrowImage.setFitWidth(IMAGE_WIDTH);
        arrowButton.setGraphic(arrowImage);
        arrowButton.setTooltip(new Tooltip("Draw Arrows"));
        arrowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.makeLines();
            }
        });
        root.getChildren().add(arrowButton);

        Button circleButton = new Button();
        Circle circle = new Circle(IMAGE_HEIGHT/2d - 1, Color.TRANSPARENT);
        circle.setStrokeWidth(2);
        circle.setStroke(Color.BLACK);
        circleButton.setGraphic(circle);
        circleButton.setTooltip(new Tooltip("Make Circles"));
        circleButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                at.makeCircles();
            }
        });
        root.getChildren().add(circleButton);

        Button drawButton = new Button();
        ImageView drawImage = new ImageView("pencil-32.png");
        drawImage.setFitHeight(IMAGE_HEIGHT);
        drawImage.setFitWidth(IMAGE_WIDTH);
        drawButton.setGraphic(drawImage);
        drawButton.setTooltip(new Tooltip("Draw"));
        drawButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.setMakingTextBox(false);     //TODO change to make lines
            }
        });
        root.getChildren().add(drawButton);

        Button textButton = new Button();
        ImageView textImage = new ImageView("TextIcon.png");
        textImage.setFitHeight(IMAGE_HEIGHT);
        textImage.setFitWidth(IMAGE_WIDTH);
        textButton.setGraphic(textImage);
        textButton.setTooltip(new Tooltip("Add Text"));
        textButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.setMakingTextBox(true);
            }
        });
        root.getChildren().add(textButton);




        this.show();
        this.setAlwaysOnTop(true);
    }

}
