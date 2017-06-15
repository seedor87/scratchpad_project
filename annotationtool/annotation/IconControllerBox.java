package annotation;

import com.sun.javafx.font.freetype.HBGlyphLayout;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.awt.peer.ButtonPeer;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Created by Resea on 6/14/2017.
 */
public class IconControllerBox extends Stage
{
    private Pane root;
    private Pane trunk;
    private Scene scene;
    private static final int IMAGE_WIDTH = 25;
    private static final int IMAGE_HEIGHT = 25;

    private static final double SMALL_BUTTON_SIZE = 25;
    private static final double MEDIUM_BUTTON_SIZE = 35;
    private static final double LARGE_BUTTON_SIZE = 45;
    private double buttonSize = MEDIUM_BUTTON_SIZE;
    private static final int LEFT_LOCATION = 0;
    private static final int TOP_LOCATION = 1;
    private static final int RIGHT_LOCATION = 2;
    private int location = RIGHT_LOCATION;


    private AnnotationToolApplication at;
    private LinkedList<Button> nodes = new LinkedList<>();
    public IconControllerBox(AnnotationToolApplication at)
    {
        this.setTitle("Tools");
        this.at = at;
        root = new Pane();
        scene = new Scene(root);
        this.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        this.setScene(scene);

        Button exitButton = new Button();
        ImageView exitImage = new ImageView("exit.png");
        exitImage.setFitHeight(IMAGE_HEIGHT);
        exitImage.setFitWidth(IMAGE_WIDTH);
        exitButton.setGraphic(exitImage);
        exitButton.setTooltip(new Tooltip("Close Application"));
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle("Confirm Quit?");
                alert.setContentText("Confirm Quit?");
                alert.setHeaderText("");

                ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType buttonTypeNo = new ButtonType("Cancel", ButtonBar.ButtonData.NO);

                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                alert.initOwner(IconControllerBox.this);        // this is what causes it to show off screen if it is on the left.

                if(alert.showAndWait().get() == buttonTypeYes)
                {
                    System.exit(0);
                }
            }
        });
        nodes.add(exitButton);

        Button saveImageButton = new Button();
        ImageView saveImage = new ImageView("camera.png");
        saveImage.setFitHeight(IMAGE_HEIGHT);
        saveImage.setFitWidth(IMAGE_WIDTH);
        saveImageButton.setGraphic(saveImage);
        saveImageButton.setTooltip(new Tooltip("Save Image"));
        saveImageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.doSave();
            }
        });
        nodes.add(saveImageButton);

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
        nodes.add(arrowButton);

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
        nodes.add(circleButton);

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
        nodes.add(drawButton);

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
        nodes.add(textButton);

        Button eraseButton = new Button();
        ImageView eraseImage = new ImageView("eraser.png");
        eraseImage.setFitHeight(IMAGE_HEIGHT);
        eraseImage.setFitWidth(IMAGE_WIDTH);
        eraseButton.setGraphic(eraseImage);
        eraseButton.setTooltip(new Tooltip("Erase"));
        eraseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.turnOnErasing();
            }
        });
        nodes.add(eraseButton);

        Button sizePickerButton = new Button();
        Text numberText = new Text("5");
        sizePickerButton.setGraphic(numberText);
        sizePickerButton.setTooltip(new Tooltip("Pick a size"));
        sizePickerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                Dialog<Double> dialog = new Dialog<>();
                dialog.setTitle("Select Brush and Text Size");
                dialog.initStyle(StageStyle.UTILITY);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                Slider slider = new Slider(2,70,at.getStrokeWidth());
                slider.setShowTickMarks(true);
                slider.setShowTickLabels(true);
                slider.setMajorTickUnit(4f);
                slider.setBlockIncrement(0.1f);

                grid.add(slider, 0,0);

                Text text;
                if(at.getStrokeWidth().toString().length() > 4)
                {
                    text = new Text(at.getStrokeWidth().toString().substring(0,5));
                }
                else
                {
                    text = new Text(at.getStrokeWidth().toString() + "00");
                }

                grid.add(text, 1,0);

                slider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                    {
                        if(newValue.toString().length() > 4)
                        {
                            text.setText(newValue.toString().substring(0,4));
                        }
                        else
                        {
                            text.setText(newValue.toString());
                        }
                    }
                });

                dialog.getDialogPane().setContent(grid);

                dialog.setResizable(true);
                dialog.setWidth(300);
                slider.setPrefWidth(250);

                ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

                dialog.setResultConverter( buttonType ->
                {
                    if(buttonType == okButton)
                    {
                        return slider.getValue();
                    }
                    else
                    {
                        return at.getStrokeWidth();
                    }
                });

                Optional<Double> result = dialog.showAndWait();

                at.setStroke(result.get());
                at.setTextSize(result.get().intValue());
                numberText.setText(new Integer(result.get().intValue()).toString());

            }
        });
        nodes.add(sizePickerButton);

        Button colorPickerButton = new Button();
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                Color c = colorPicker.getValue();
                at.setPaint(c);
            }
        });
        colorPicker.setMaxWidth(25);
        colorPickerButton.setGraphic(colorPicker);
        colorPickerButton.setTooltip(new Tooltip("Pick a color"));
        colorPickerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                colorPicker.show();
            }
        });
        nodes.add(colorPickerButton);

        Button undoButton = new Button();
        ImageView undoImage = new ImageView("undoImage.png");
        undoImage.setFitHeight(IMAGE_HEIGHT);
        undoImage.setFitWidth(IMAGE_WIDTH);
        undoButton.setGraphic(undoImage);
        undoButton.setTooltip(new Tooltip("Undo"));
        undoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.undo();
            }
        });
        nodes.add(undoButton);

        Button redoButton = new Button();
        ImageView redoImage = new ImageView("redoImage.png");
        redoImage.setFitHeight(IMAGE_HEIGHT);
        redoImage.setFitWidth(IMAGE_WIDTH);
        redoButton.setGraphic(redoImage);
        redoButton.setTooltip(new Tooltip("Redo"));
        redoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.redo();
            }
        });
        nodes.add(redoButton);

        Button changeButtonSizeButton = new Button();
        ImageView changeButtonSizeImage = new ImageView("changeButtonSizeImage.png");   //https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Simple_icon_size.svg/1280px-Simple_icon_size.svg.png
        changeButtonSizeImage.setFitHeight(IMAGE_HEIGHT);
        changeButtonSizeImage.setFitWidth(IMAGE_WIDTH);
        changeButtonSizeButton.setGraphic(changeButtonSizeImage);
        changeButtonSizeButton.setTooltip(new Tooltip("Change Button Size"));
        changeButtonSizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            double changeSize;
            @Override
            public void handle(MouseEvent event)
            {
                changeSize = buttonSize;
                Dialog<Double> dialog = new Dialog<>();
                dialog.setTitle("Select Button Size");
                dialog.initStyle(StageStyle.UTILITY);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                Button
                        button1 = new Button(),
                        button2 = new Button(),
                        button3 = new Button();
                button1.setMaxSize(SMALL_BUTTON_SIZE,SMALL_BUTTON_SIZE);
                button1.setMinSize(SMALL_BUTTON_SIZE,SMALL_BUTTON_SIZE);
                button1.setText("Small");
                button1.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = SMALL_BUTTON_SIZE;
                    }
                });

                button2.setMaxSize(MEDIUM_BUTTON_SIZE,MEDIUM_BUTTON_SIZE);
                button2.setMinSize(MEDIUM_BUTTON_SIZE,MEDIUM_BUTTON_SIZE);
                button2.setText("Medium");
                button2.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = MEDIUM_BUTTON_SIZE;
                    }
                });

                button3.setMaxSize(LARGE_BUTTON_SIZE,LARGE_BUTTON_SIZE);
                button3.setMinSize(LARGE_BUTTON_SIZE,LARGE_BUTTON_SIZE);
                button3.setText("Large");
                button3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = LARGE_BUTTON_SIZE;
                    }
                });

                grid.add(button1,0,0);
                grid.add(button2, 1,0);
                grid.add(button3, 2,0);

                dialog.getDialogPane().setContent(grid);

                ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

                dialog.showAndWait();

                dialog.setResultConverter( buttonType ->
                {
                    if(buttonType == okButton)
                    {
                        return changeSize;
                    }
                    else
                    {
                        return buttonSize;
                    }
                });

                Optional<Double> result = dialog.showAndWait();

                setIconSizes(result.get());
                resetLocation();

                //TODO Handle this
            }
        });
        nodes.add(changeButtonSizeButton);


        Button snapToLeftButton = new Button();
        ImageView snapToLeftImage = new ImageView("snapLeftImage.png");
        snapToLeftImage.setFitHeight(IMAGE_HEIGHT);
        snapToLeftImage.setFitWidth(IMAGE_WIDTH);
        snapToLeftButton.setGraphic(snapToLeftImage);
        snapToLeftButton.setTooltip(new Tooltip("Snap to left"));
        snapToLeftButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                snapToLeft();
            }
        });
        nodes.add(snapToLeftButton);

        Button snapToRightButton = new Button();
        ImageView snapToRightImage = new ImageView("snapRightImage.png");
        snapToRightImage.setFitHeight(IMAGE_HEIGHT);
        snapToRightImage.setFitWidth(IMAGE_WIDTH);
        snapToRightButton.setGraphic(snapToRightImage);
        snapToRightButton.setTooltip(new Tooltip("Snap to Right"));
        snapToRightButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                snapToRight();
            }
        });
        nodes.add(snapToRightButton);

        Button snapToTopButton = new Button();
        ImageView snapToTopImage = new ImageView("snapTopImage.png");
        snapToTopImage.setFitHeight(IMAGE_HEIGHT);
        snapToTopImage.setFitWidth(IMAGE_WIDTH);
        snapToTopButton.setGraphic(snapToTopImage);
        snapToTopButton.setTooltip(new Tooltip("Snap to Top"));
        snapToTopButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                snapBoxToTop();
            }
        });
        nodes.add(snapToTopButton);

        Button toggleClickableButton = new Button();
        ImageView toggleClickableImage = new ImageView("hand.png");
        toggleClickableImage.setFitHeight(IMAGE_HEIGHT);
        toggleClickableImage.setFitWidth(IMAGE_WIDTH);
        toggleClickableButton.setGraphic(toggleClickableImage);
        toggleClickableButton.setTooltip(new Tooltip("Toggle Clickable"));
        toggleClickableButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.toggleClickable();
                setAlwaysOnTop(true);
            }
        });
        nodes.add(toggleClickableButton);

        /**
         * Image obtained from https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Fast_forward_font_awesome.svg/1024px-Fast_forward_font_awesome.svg.png
         * edited*
         */
        Button sendToBackButton = new Button();
        ImageView sendToBackImage = new ImageView("sendToBack.png");
        sendToBackImage.setFitHeight(IMAGE_HEIGHT);
        sendToBackImage.setFitWidth(IMAGE_WIDTH);
        sendToBackButton.setGraphic(sendToBackImage);
        sendToBackButton.setTooltip(new Tooltip("Send To Back"));
        sendToBackButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.toBack();
            }
        });
        nodes.add(sendToBackButton);

        /**
         * Image obtained from https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Fast_forward_font_awesome.svg/1024px-Fast_forward_font_awesome.svg.png
         */
        Button bringToFrontButton = new Button();
        ImageView bringToFrontImage = new ImageView("bringToFront.png");
        bringToFrontImage.setFitHeight(IMAGE_HEIGHT);
        bringToFrontImage.setFitWidth(IMAGE_WIDTH);
        bringToFrontButton.setGraphic(bringToFrontImage);
        bringToFrontButton.setTooltip(new Tooltip("Bring to Front"));
        bringToFrontButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.toFront();
            }
        });
        nodes.add(bringToFrontButton);

        /**
         * Image obtained from https://upload.wikimedia.org/wikipedia/commons/thumb/f/f2/Edit-clear.svg/1024px-Edit-clear.svg.png
         */
        Button eraseTransparentButton = new Button();
        ImageView eraseTransparentImage = new ImageView("EraseTransparent.png");
        eraseTransparentImage.setFitHeight(IMAGE_HEIGHT);
        eraseTransparentImage.setFitWidth(IMAGE_WIDTH);
        eraseTransparentButton.setGraphic(eraseTransparentImage);
        eraseTransparentButton.setTooltip(new Tooltip("Erase Transparent"));
        eraseTransparentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.doClear();
            }
        });
        nodes.add(eraseTransparentButton);

        Button clearHistoryButton = new Button();
        ImageView clearHistoryImage = new ImageView("clearHistory.png");
        clearHistoryImage.setFitHeight(IMAGE_HEIGHT);
        clearHistoryImage.setFitWidth(IMAGE_WIDTH);
        clearHistoryButton.setGraphic(clearHistoryImage);
        clearHistoryButton.setTooltip(new Tooltip("Clear History"));
        clearHistoryButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.clearHistory();
            }
        });
        nodes.add(clearHistoryButton);

        setIconSizes(MEDIUM_BUTTON_SIZE);

        this.show();
        this.snapToRight();
        this.setAlwaysOnTop(true);
    }
    private void resetLocation()
    {
        if(location == LEFT_LOCATION)
        {
            snapToLeft();
        }
        else if(location == TOP_LOCATION)
        {
            snapBoxToTop();
        }
        else if(location == RIGHT_LOCATION)
        {
            snapToRight();
        }
    }

    private void setIconSizes(double size)
    {
        for(Button n : this.nodes)
        {
            n.setMinSize(size, size);
            n.setMaxSize(size, size);

            Node graphicsContext = n.getGraphic();
            if(graphicsContext instanceof ImageView)
            {
                ((ImageView) graphicsContext).setFitWidth(size-5);
                ((ImageView) graphicsContext).setFitHeight(size-5);
            }
            else if(graphicsContext instanceof Text)
            {
                ((Text)graphicsContext).setFont(new Font(size-15));
            }
            else if(graphicsContext instanceof Circle)
            {
                ((Circle)graphicsContext).setRadius((size-10)/2);
            }
        }
    }

    /**
     * Must be called after show was called on this stage.
     */
    private void snapBoxToTop()
    {
        location = TOP_LOCATION;

        root.getChildren().remove(trunk);

        trunk = new HBox();
        root.getChildren().add(trunk);

        //this.setScene(scene);


        for(Node node : nodes)
        {
            trunk.getChildren().add(node);
        }
        this.sizeToScene();

        Stage drawingStage = at.getDrawingStage();
        if(drawingStage.isFullScreen() || drawingStage.isMaximized())
        {
            this.setX(at.getDrawingStage().getX());
            centerOnScreen();
            this.setY(0);
        }
        else
        {
            this.setY(drawingStage.yProperty().get());
            this.setX(drawingStage.xProperty().get() + drawingStage.getWidth()/2- this.getWidth()/2);
        }
    }
    private void snapToLeft()
    {
        location = LEFT_LOCATION;
        root.getChildren().remove(trunk);

        trunk = new VBox();
        root.getChildren().add(trunk);

        for(Node b : nodes)
        {
            trunk.getChildren().add(b);
        }
        this.sizeToScene();

        Stage drawingStage = at.getDrawingStage();
        if(drawingStage.isFullScreen() || drawingStage.isMaximized())
        {
            centerOnScreen();
            this.setX(0);
        }
        else
        {
            this.setY(drawingStage.yProperty().get() + drawingStage.getHeight() / 2 - this.getHeight() /2);
            this.setX(drawingStage.xProperty().get());
        }

    }
    private void snapToRight()
    {
        location = RIGHT_LOCATION;
        root.getChildren().remove(trunk);

        trunk = new VBox();
        root.getChildren().add(trunk);

        for(Node node : nodes)
        {
            trunk.getChildren().add(node);
        }

        this.sizeToScene();

        Stage drawingStage = at.getDrawingStage();

        if(drawingStage.isFullScreen() || drawingStage.isMaximized())
        {
            centerOnScreen();
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            this.setX(primScreenBounds.getWidth() - this.getWidth());
        }
        else
        {
            this.setY(drawingStage.yProperty().get() + drawingStage.getHeight() / 2 - this.getHeight() /2);
            this.setX(drawingStage.xProperty().get() + drawingStage.getWidth() - this.getWidth());
        }

    }
}

