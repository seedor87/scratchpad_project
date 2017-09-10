package annotation;


import TransferableShapes.Custom_Shape;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.SocketListener;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Resea on 6/14/2017.
 */
public class IconControllerBox extends Stage
{

    private static final int IMAGE_WIDTH = 25;
    private static final int IMAGE_HEIGHT = 25;
    private static final int LEFT_LOCATION = 0;
    private static final int TOP_LOCATION = 1;
    private static final int RIGHT_LOCATION = 2;
    private static final int TOOLTIP_FONT_SIZE = 20;
    private static final Font TOOLTIP_FONT = new Font(TOOLTIP_FONT_SIZE);
    private Thread listenerThread;
    private Pane root;
    private Pane trunk;
    private Scene scene;
    private double smallButtonSize;
    private double medButtonSize;
    private double largeButtonSize;
    private double buttonSize;
    private int location = RIGHT_LOCATION;
    private AnnotationToolApplication at;
    private LinkedList<Button> nodes = new LinkedList<>();
    private LinkedList<Button> shapeSelectingNodes = new LinkedList<>();
    private LinkedList<Button> saveSelectingNodes = new LinkedList<>();
    private Node shapePickerGraphic;
    private Button sendToBackButton;
    private Button bringToFrontButton;
    private Button selectedButton;
    private Background defaultBackground;
    private static final Background SELECTED_BACKGROUND = null;

    public IconControllerBox(AnnotationToolApplication at)
    {
        this.setTitle("Tools");
        this.at = at;
        root = new Pane();
        scene = new Scene(root);
        this.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        this.setScene(scene);
        
        double dotsPerInch = Screen.getPrimary().getDpi();
        if(dotsPerInch == 0)//fixes dual duplicate screen issue.
        {
            dotsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        smallButtonSize = .25 * dotsPerInch;
        medButtonSize = .35 * dotsPerInch;
        largeButtonSize = .6 * dotsPerInch;
        
        final IconControllerBox CONTROLLER_BOX = this;
        listenerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				SocketListener serverSocket = new SocketListener(CONTROLLER_BOX, 26222);
			}
        	
        });
        listenerThread.start();

        Button exitButton = new Button();
        ImageView exitImage = new ImageView("exit.png");
        exitImage.setFitHeight(IMAGE_HEIGHT);
        exitImage.setFitWidth(IMAGE_WIDTH);
        exitButton.setGraphic(exitImage);
        exitButton.setTooltip(getToolTip("Close Application"));
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

                setDialogLocation(alert);

                if(alert.showAndWait().get() == buttonTypeYes)
                {
                    try {
                        at.writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                    	e.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        });
        nodes.add(exitButton);
        
        Button newFileButton = new Button();
        ImageView newFileImage = new ImageView("file.png");
        newFileImage.setFitHeight(IMAGE_HEIGHT);
        newFileImage.setFitWidth(IMAGE_WIDTH);
        newFileButton.setGraphic(newFileImage);
        newFileButton.setTooltip(getToolTip("Create or open a new annotation"));
        newFileButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				newFile("");
			}
        	
        });
        nodes.add(newFileButton);

        Button saveImageButton = new Button();
        ImageView saveImage = new ImageView("camera.png");
        saveImage.setFitHeight(IMAGE_HEIGHT);
        saveImage.setFitWidth(IMAGE_WIDTH);
        saveImageButton.setGraphic(saveImage);
        saveImageButton.setTooltip(getToolTip("Save Image"));
        saveImageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	saveImage(1);
            	saveImage(2);
            }
        });
        nodes.add(saveImageButton);

        Button arrowButton = new Button();
        ImageView arrowImage = new ImageView("arrow.png");
        arrowImage.setFitHeight(IMAGE_HEIGHT);
        arrowImage.setFitWidth(IMAGE_WIDTH);
        arrowButton.setGraphic(arrowImage);
        arrowButton.setTooltip(getToolTip("Draw Arrows"));
        arrowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.makeLines();
            }
        });
        nodes.add(arrowButton);
        shapeSelectingNodes.add(arrowButton);

        Button circleButton = new Button();
        Circle circle = new Circle(IMAGE_HEIGHT/2d - 1, Color.TRANSPARENT);
        circle.setStrokeWidth(2);
        circle.setStroke(Color.BLACK);
        circleButton.setGraphic(circle);
        circleButton.setTooltip(getToolTip("Make Circles"));
        circleButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                at.makeCircles();
            }
        });
        nodes.add(circleButton);
        shapeSelectingNodes.add(circleButton);

        Button rectificationButton = new Button();
        ImageView rectificationImage = new ImageView("star.png");
        rectificationImage.setFitHeight(IMAGE_HEIGHT);
        rectificationImage.setFitWidth(IMAGE_WIDTH);
        rectificationButton.setGraphic(rectificationImage);

        rectificationButton.setTooltip(getToolTip("Make a rectified shape"));
        rectificationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                at.setRectifying();
            }
        });
        nodes.add(rectificationButton);
        shapeSelectingNodes.add(rectificationButton);

        Button lineButton = new Button();
        ImageView lineImage = new ImageView("line.png");
        lineImage.setFitHeight(IMAGE_HEIGHT);
        lineImage.setFitWidth(IMAGE_WIDTH);
        lineButton.setGraphic(lineImage);
        lineButton.setTooltip(getToolTip("Make lines"));
        lineButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                at.setMakingLines();
            }
        });
        nodes.add(lineButton);
        shapeSelectingNodes.add(lineButton);

        Button rectangleButton = new Button();
        ImageView rectangleImage = new ImageView("rectangle.png");
        rectangleImage.setFitHeight(IMAGE_HEIGHT);
        rectangleImage.setFitWidth(IMAGE_WIDTH);
        rectangleButton.setGraphic(rectangleImage);

        rectangleButton.setTooltip(getToolTip("Make a rectangle"));
        rectangleButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                at.setMakingRectangles();
            }
        });
        nodes.add(rectangleButton);
        shapeSelectingNodes.add(rectangleButton);

        Button outBoundedOvalButton = new Button();
        ImageView ovalImage = new ImageView("oval.png");
        ovalImage.setFitHeight(IMAGE_HEIGHT);
        ovalImage.setFitWidth(IMAGE_WIDTH);
        outBoundedOvalButton.setGraphic(ovalImage);
        outBoundedOvalButton.setTooltip(getToolTip("Draw an out-bounded oval"));
        outBoundedOvalButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                at.setDrawingOutboundedOval();
            }
        });
        nodes.add(outBoundedOvalButton);
        shapeSelectingNodes.add(outBoundedOvalButton);

        Button drawButton = new Button();
        ImageView drawImage = new ImageView("pencil-32.png");
        drawImage.setFitHeight(IMAGE_HEIGHT);
        drawImage.setFitWidth(IMAGE_WIDTH);
        drawButton.setGraphic(drawImage);
        drawButton.setTooltip(getToolTip("Draw"));
        drawButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.setDrawingText();     //TODO change to make lines
            }
        });
        nodes.add(drawButton);
        shapeSelectingNodes.add(drawButton);

        Button textButton = new Button();
        ImageView textImage = new ImageView("TextIcon.png");
        textImage.setFitHeight(IMAGE_HEIGHT);
        textImage.setFitWidth(IMAGE_WIDTH);
        textButton.setGraphic(textImage);
        textButton.setTooltip(getToolTip("Add Text"));
        textButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.setMakingText();
            }
        });
        nodes.add(textButton);
        shapeSelectingNodes.add(textButton);

        Button eraseButton = new Button();
        ImageView eraseImage = new ImageView("eraser.png");
        eraseImage.setFitHeight(IMAGE_HEIGHT);
        eraseImage.setFitWidth(IMAGE_WIDTH);
        eraseButton.setGraphic(eraseImage);
        eraseButton.setTooltip(getToolTip("Erase"));
        eraseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.turnOnErasing();
            }
        });
        nodes.add(eraseButton);
        shapeSelectingNodes.add(eraseButton);

        Button shapePickerButton = new Button();
        ImageView shapePickerImage = new ImageView(drawImage.getImage());
        shapePickerGraphic = shapePickerImage;
        shapePickerButton.setGraphic(shapePickerImage);
        shapePickerButton.setTooltip(getToolTip("Pick a shape"));
        shapePickerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
            	
                //shapePickerButton.setGraphic(null);
                //shapePickerButton.graphicProperty().setValue(null);
                Dialog<Double> dialog = new Dialog<>();
                dialog.setTitle("Select Shape Tool");
                dialog.initStyle(StageStyle.UTILITY);
                dialog.initOwner(IconControllerBox.this);


                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                dialog.getDialogPane().setContent(grid);

                int size = shapeSelectingNodes.size();
                Iterator<Button> iterator = shapeSelectingNodes.iterator();
                for(int i = 0; i < size;i++)
                {
                    grid.add(iterator.next(),i,0);
                }
                for(Button b: shapeSelectingNodes)
                {
                    b.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            dialog.close();
                        }
                    });
                }

                dialog.setResizable(true);
                dialog.setWidth(300);

                ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                dialog.getDialogPane().getButtonTypes().addAll(cancelButton);

                setDialogLocation(dialog);
                dialog.showAndWait();
                shapePickerButton.setGraphic(shapePickerGraphic);
                System.out.println("\n\n\n" + shapePickerGraphic);
                at.resetStages();
            }
        });
        nodes.add(shapePickerButton);

        //selectedButton = shapePickerButton;

        /*
        * Sets up the listeners for changing the graphic of the main button that calls the shape
        * selecting dialog.
        * */
        for(Button button : shapeSelectingNodes)
        {
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event)
                {
                    shapePickerGraphic = button.getGraphic();
                    if(shapePickerGraphic instanceof ImageView) {
                        ImageView imageView = (ImageView) shapePickerGraphic;
                        ImageView newImageView = new ImageView(imageView.getImage());
                        newImageView.setFitHeight(imageView.getFitHeight());
                        newImageView.setFitWidth(imageView.getFitWidth());
                        shapePickerGraphic = newImageView;
                    }
                    else if (shapePickerGraphic instanceof Circle)
                    {
                        Circle circle1 = (Circle) shapePickerGraphic;
                        Circle newCircle = new Circle();
                        newCircle.setRadius(circle1.getRadius());
                        newCircle.setStroke(circle1.getStroke());
                        newCircle.setStrokeWidth(circle1.getStrokeWidth());
                        newCircle.setFill(circle1.getFill());
                        shapePickerGraphic = newCircle;
                    }
                }
            });
        }


        Button sizePickerButton = new Button();
        Text numberText = new Text("5");
        sizePickerButton.setGraphic(numberText);
        sizePickerButton.setTooltip(getToolTip("Pick a size"));
        sizePickerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                Dialog<Double> dialog = new Dialog<>();
                dialog.setTitle("Select Brush and Text Size");
                //dialog.initStyle(StageStyle.UTILITY);
                dialog.initOwner(IconControllerBox.this);

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
//                dialog.setX(at.getPictureStage().getX() + at.getPictureStage().getWidth()/2 - dialog.getWidth()/2);
//                dialog.setY((at.getPictureStage().getY()) + (at.getPictureStage().getHeight()/2));
                setDialogLocation(dialog);

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
            	at.hideVirtualKeyboard();
                Color c = colorPicker.getValue();
                at.setPaint(c);
            }
        });
        colorPicker.setMaxWidth(25);
        colorPickerButton.setGraphic(colorPicker);
        colorPickerButton.setTooltip(getToolTip("Pick a color"));
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
        undoButton.setTooltip(getToolTip("Undo"));
        undoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.undo();
                try
                {
                    at.writeJSON(new Custom_Shape(Custom_Shape.UNDO_STRING));
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        });
        nodes.add(undoButton);

        Button redoButton = new Button();
        ImageView redoImage = new ImageView("redoImage.png");
        redoImage.setFitHeight(IMAGE_HEIGHT);
        redoImage.setFitWidth(IMAGE_WIDTH);
        redoButton.setGraphic(redoImage);
        redoButton.setTooltip(getToolTip("Redo"));
        redoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.redo();
                try
                {
                    at.writeJSON(new Custom_Shape(Custom_Shape.REDO_STRING));
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        });
        nodes.add(redoButton);

        Button changeButtonSizeButton = new Button();
        ImageView changeButtonSizeImage = new ImageView("changebuttonsizeimage.png");   //https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Simple_icon_size.svg/1280px-Simple_icon_size.svg.png
        changeButtonSizeImage.setFitHeight(IMAGE_HEIGHT);
        changeButtonSizeImage.setFitWidth(IMAGE_WIDTH);
        changeButtonSizeButton.setGraphic(changeButtonSizeImage);
        changeButtonSizeButton.setTooltip(getToolTip("Change Button Size"));
        changeButtonSizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            double changeSize;
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                changeSize = buttonSize;
                Dialog<Double> dialog = new Dialog<>();
                dialog.setTitle("Select Button Size");
                dialog.initStyle(StageStyle.UTILITY);
                dialog.initOwner(IconControllerBox.this);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                Button
                        button1 = new Button(),
                        button2 = new Button(),
                        button3 = new Button();
                button1.setMaxSize(smallButtonSize,smallButtonSize);
                button1.setMinSize(smallButtonSize,smallButtonSize);
                button1.setTooltip(getToolTip("Small"));
                button1.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = smallButtonSize;
                        setIconSizes(changeSize);
                    }
                });

                button2.setMaxSize(medButtonSize,medButtonSize);
                button2.setMinSize(medButtonSize,medButtonSize);
                button2.setTooltip(getToolTip("Medium"));
                button2.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = medButtonSize;
                        setIconSizes(changeSize);
                    }
                });

                button3.setMaxSize(largeButtonSize,largeButtonSize);
                button3.setMinSize(largeButtonSize,largeButtonSize);
                button3.setTooltip(getToolTip("Large"));
                button3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = largeButtonSize;
                        setIconSizes(changeSize);
                    }
                });
                EventHandler<MouseEvent> dialogHandler = new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        dialog.close();
                    }
                };
                button1.addEventHandler(MouseEvent.MOUSE_CLICKED, dialogHandler);
                button2.addEventHandler(MouseEvent.MOUSE_CLICKED, dialogHandler);
                button3.addEventHandler(MouseEvent.MOUSE_CLICKED, dialogHandler);

                grid.add(button1,0,0);
                grid.add(button2, 1,0);
                grid.add(button3, 2,0);

                dialog.getDialogPane().setContent(grid);

                ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

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

                setDialogLocation(dialog);

                Optional<Double> result = dialog.showAndWait();

                setIconSizes(result.get());
                fitScreen();
                at.resetStages();
            }
        });
        nodes.add(changeButtonSizeButton);


        Button snapToLeftButton = new Button();
        ImageView snapToLeftImage = new ImageView("snapLeftImage.png");
        snapToLeftImage.setFitHeight(IMAGE_HEIGHT);
        snapToLeftImage.setFitWidth(IMAGE_WIDTH);
        snapToLeftButton.setGraphic(snapToLeftImage);
        snapToLeftButton.setTooltip(getToolTip("Snap to left"));
        snapToLeftButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                location = LEFT_LOCATION;
                fitScreen();
            }
        });
        nodes.add(snapToLeftButton);

        Button snapToRightButton = new Button();
        ImageView snapToRightImage = new ImageView("snapRightImage.png");
        snapToRightImage.setFitHeight(IMAGE_HEIGHT);
        snapToRightImage.setFitWidth(IMAGE_WIDTH);
        snapToRightButton.setGraphic(snapToRightImage);
        snapToRightButton.setTooltip(getToolTip("Snap to Right"));
        snapToRightButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                location = RIGHT_LOCATION;
                fitScreen();
            }
        });
        nodes.add(snapToRightButton);

        Button snapToTopButton = new Button();
        ImageView snapToTopImage = new ImageView("snapTopImage.png");
        snapToTopImage.setFitHeight(IMAGE_HEIGHT);
        snapToTopImage.setFitWidth(IMAGE_WIDTH);
        snapToTopButton.setGraphic(snapToTopImage);
        snapToTopButton.setTooltip(getToolTip("Snap to Top"));
        snapToTopButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                location = TOP_LOCATION;
                fitScreen();
            }
        });
        nodes.add(snapToTopButton);

        Button moveButton = new Button();
        ImageView moveImage = new ImageView("hand.png");
        moveImage.setFitHeight(IMAGE_HEIGHT);
        moveImage.setFitWidth(IMAGE_WIDTH);
        moveButton.setGraphic(moveImage);
        moveButton.setTooltip(getToolTip("Move window"));
        moveButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.setMovingHandler();
            }
        });
        nodes.add(moveButton);
        selectedButton = moveButton;

        Button toggleClickableButton = new Button();
        ImageView toggleClickableImage = new ImageView("pointer.png");
        toggleClickableImage.setFitHeight(IMAGE_HEIGHT);
        toggleClickableImage.setFitWidth(IMAGE_WIDTH);
        toggleClickableButton.setGraphic(toggleClickableImage);
        toggleClickableButton.setTooltip(getToolTip("Toggle Clickable"));
        toggleClickableButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.toggleClickable();
                //setAlwaysOnTop(true);
            }
        });
        toggleClickableButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            boolean isSelected = false;
            Background background;
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                isSelected = !isSelected;
                if(isSelected)
                {
                    background = toggleClickableButton.getBackground();
                    toggleClickableButton.setBackground(SELECTED_BACKGROUND);
                }
                else
                {
                    toggleClickableButton.setBackground(background);
                }
            }
        });
        nodes.add(toggleClickableButton);


        /**
         * Image obtained from https://upload.wikimedia.org/wikipedia/commons/thumb/f/f2/Edit-clear.svg/1024px-Edit-clear.svg.png
         */
        Button eraseTransparentButton = new Button();
        ImageView eraseTransparentImage = new ImageView("eraseTransparent.png");
        eraseTransparentImage.setFitHeight(IMAGE_HEIGHT);
        eraseTransparentImage.setFitWidth(IMAGE_WIDTH);
        eraseTransparentButton.setGraphic(eraseTransparentImage);
        eraseTransparentButton.setTooltip(getToolTip("Erase Transparent"));
        eraseTransparentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.doClear();
            }
        });
        nodes.add(eraseTransparentButton);

/*        Button preferencesButton = new Button();
        ImageView preferencesImage = new ImageView("preferences.png");
        preferencesImage.setFitHeight(IMAGE_HEIGHT);
        preferencesImage.setFitWidth(IMAGE_WIDTH);
        preferencesButton.setGraphic(preferencesImage);
        preferencesButton.setTooltip(getToolTip("Change Background."));
        preferencesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose background picture.");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
                File selectedFile = fileChooser.showOpenDialog(at.getPictureStage());
                if(selectedFile != null)
                {
                    Image image = new Image(selectedFile.toURI().toString());
                    ImageView iv = new ImageView(image);
                    at.getRoot().getChildren().add(iv);
                    at.resetStages();
                }
            }
        });
        nodes.add(preferencesButton);*/

        Button clearHistoryButton = new Button();
        ImageView clearHistoryImage = new ImageView("clearHistory.png");
        clearHistoryImage.setFitHeight(IMAGE_HEIGHT);
        clearHistoryImage.setFitWidth(IMAGE_WIDTH);
        clearHistoryButton.setGraphic(clearHistoryImage);
        clearHistoryButton.setTooltip(getToolTip("Clear History"));
        clearHistoryButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.clearHistory();
            }
        });
        nodes.add(clearHistoryButton);

        Button moveShapesButton = new Button();
        ImageView moveShapesImage = new ImageView("selectimage.png");
        moveShapesImage.setFitHeight(IMAGE_HEIGHT);
        moveShapesImage.setFitWidth(IMAGE_WIDTH);
        moveShapesButton.setGraphic(moveShapesImage);
        moveShapesButton.setTooltip(getToolTip("Select and move shapes."));
        moveShapesButton.setGraphic(moveShapesImage);
        moveShapesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.setSelectAndMoveHandler();
            }
        });
        nodes.add(moveShapesButton);
/*

        Button saveStateButton = new Button();
        //Padlock image sourced from http://game-icons.net/lorc/originals/padlock.html by "Lorc".
        ImageView saveStateImage = new ImageView("saveState.png");
        saveStateImage.setFitHeight(IMAGE_HEIGHT);
        saveStateImage.setFitWidth(IMAGE_WIDTH);
        saveStateButton.setGraphic(saveStateImage);
        saveStateButton.setTooltip(getToolTip("Save the state of the Window."));
        saveStateButton.setGraphic(saveStateImage);
        saveStateButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.saveSceneState();
            }
        });
        nodes.add(saveStateButton);


*/





        Button newButton = new Button();
        ImageView newImage = new ImageView("new.png");
        newImage.setFitHeight(IMAGE_HEIGHT);
        newImage.setFitWidth(IMAGE_WIDTH);
        newButton.setGraphic(newImage);
        newButton.setTooltip(getToolTip("New Project"));
        newButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                //
                FXAnnotationToolBuilder builder = new FXAnnotationToolBuilder();
                try {

                    at.fileManagement("new"); //new file

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //nodes.add(newButton);
        saveSelectingNodes.add(newButton);



        Button openButton = new Button();
        ImageView openImage = new ImageView("open.png");
        openImage.setFitHeight(IMAGE_HEIGHT);
        openImage.setFitWidth(IMAGE_WIDTH);
        openButton.setGraphic(openImage);
        openButton.setTooltip(getToolTip("Open Project"));
        openButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                try {
                    at.fileManagement("open");//open project
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //nodes.add(openButton);
        saveSelectingNodes.add(openButton);



        Button saveAsButton = new Button();
        ImageView saveAsImage = new ImageView("saveAs.png");
        saveAsImage.setFitHeight(IMAGE_HEIGHT);
        saveAsImage.setFitWidth(IMAGE_WIDTH);
        saveAsButton.setGraphic(saveAsImage);
        saveAsButton.setTooltip(getToolTip("Save As"));
        saveAsButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                try {

                    at.fileManagement("save"); //save as
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        //nodes.add(newButton);
        saveSelectingNodes.add(saveAsButton);





        Button saveFileButton = new Button();
        ImageView saveFileImage = new ImageView("save-file.png");
        saveFileImage.setFitHeight(IMAGE_HEIGHT);
        saveFileImage.setFitWidth(IMAGE_WIDTH);
        saveFileButton.setGraphic(saveFileImage);
        saveFileButton.setTooltip(getToolTip("Save File"));
        saveFileButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                try {

                    at.fileManagement("sFile"); //save as
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        //nodes.add(newButton);
        saveSelectingNodes.add(saveFileButton);







        Button closeButton = new Button();
        ImageView closeImage = new ImageView("close.png");
        closeImage.setFitHeight(IMAGE_HEIGHT);
        closeImage.setFitWidth(IMAGE_WIDTH);
        closeButton.setGraphic(closeImage);
        closeButton.setTooltip(getToolTip("close Project"));
        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
               System.exit(0);
            }
        });
        //nodes.add(closeButton);
        saveSelectingNodes.add(closeButton);




        Button saveStateButton = new Button();
        //Padlock image sourced from http://game-icons.net/lorc/originals/padlock.html by "Lorc".
        ImageView saveStateImage = new ImageView("saveState.png");
        saveStateImage.setFitHeight(IMAGE_HEIGHT);
        saveStateImage.setFitWidth(IMAGE_WIDTH);
        saveStateButton.setGraphic(saveStateImage);
        saveStateButton.setTooltip(getToolTip("File"));
        saveStateButton.setGraphic(saveStateImage);
        saveStateButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                saveStateButton.setGraphic(null);
                saveStateButton.graphicProperty().setValue(null);
                Dialog<Double> dialog = new Dialog<>();
                dialog.setTitle("File");
                dialog.initStyle(StageStyle.UTILITY);
                dialog.initOwner(IconControllerBox.this);


                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                dialog.getDialogPane().setContent(grid);

                int size = saveSelectingNodes.size();
                Iterator<Button> iterator = saveSelectingNodes.iterator();
                for(int i = 0; i < size;i++)
                {
                    grid.add(iterator.next(),i,0);
                }
                for(Button b: saveSelectingNodes)
                {
                    b.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            dialog.close();
                        }
                    });
                }

                dialog.setResizable(true);
                dialog.setWidth(300);

                ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.YES);

                dialog.getDialogPane().getButtonTypes().addAll(okButton);

                setDialogLocation(dialog);
                dialog.showAndWait();
                saveStateButton.setGraphic(saveStateImage);
                at.resetStages();
            }
        });
        nodes.add(saveStateButton);

        Button lockControllerBoxButton = new Button();
        //Padlock image sourced from http://game-icons.net/lorc/originals/padlock.html by "Lorc".
        ImageView lockControllerBoxImage = new ImageView("padlock.png");
        lockControllerBoxImage.setFitHeight(IMAGE_HEIGHT);
        lockControllerBoxImage.setFitWidth(IMAGE_WIDTH);
        lockControllerBoxButton.setGraphic(lockControllerBoxImage);
        lockControllerBoxButton.setTooltip(getToolTip("Lock the toolbar to the annotation window"));
        lockControllerBoxButton.setGraphic(lockControllerBoxImage);
        lockControllerBoxButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.toggleLockedControllerBox();
            }
        });
        nodes.add(lockControllerBoxButton);


        Button recordInputButton = new Button();
        //Camera image sourced from http://game-icons.net/delapouite/originals/video-camera.html by "Delapouite".
        ImageView recordInputImage = new ImageView("record.png");
        recordInputImage.setFitHeight(IMAGE_HEIGHT);
        recordInputImage.setFitWidth(IMAGE_WIDTH);
        recordInputButton.setGraphic(recordInputImage);
        recordInputButton.setTooltip(getToolTip("Begin recording all input."));
        recordInputButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
            	at.hideVirtualKeyboard();
                at.recordInput();
            }
        });
        nodes.add(recordInputButton);

        /**
         * Image obtained from https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Fast_forward_font_awesome.svg/1024px-Fast_forward_font_awesome.svg.png
         * edited*
         */
        sendToBackButton = new Button();
        ImageView sendToBackImage = new ImageView("sendToBack.png");
        sendToBackImage.setFitHeight(IMAGE_HEIGHT);
        sendToBackImage.setFitWidth(IMAGE_WIDTH);
        sendToBackButton.setGraphic(sendToBackImage);
        sendToBackButton.setTooltip(getToolTip("Send To Back"));
        sendToBackButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
//                at.getMouseCatchingStage().setAlwaysOnTop(false);
//                at.getPictureStage().setAlwaysOnTop(false);
//                at.getPictureStage().toBack();
//                at.toBack();
            	at.hideVirtualKeyboard();
                at.sendToBack();
                nodes.remove(sendToBackButton);
                nodes.add(bringToFrontButton);
                fitScreen();
            }
        });
        nodes.add(sendToBackButton);

        /**
         * Image obtained from https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Fast_forward_font_awesome.svg/1024px-Fast_forward_font_awesome.svg.png
         */
        bringToFrontButton = new Button();
        ImageView bringToFrontImage = new ImageView("bringToFront.png");
        bringToFrontImage.setFitHeight(IMAGE_HEIGHT);
        bringToFrontImage.setFitWidth(IMAGE_WIDTH);
        bringToFrontButton.setGraphic(bringToFrontImage);
        bringToFrontButton.setTooltip(getToolTip("Bring to Front"));
        bringToFrontButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
//                at.toFront();
//                IconControllerBox.this.setAlwaysOnTop(false);
//                IconControllerBox.this.setAlwaysOnTop(true);
//                IconControllerBox.this.toFront();
                at.bringToFront();
                nodes.remove(bringToFrontButton);
                nodes.add(sendToBackButton);
                fitScreen();
            }
        });
        nodes.add(bringToFrontButton);

        setIconSizes(medButtonSize);

//        defaultBackground = selectedButton.getBackground();
//        selectedButton.setBackground(SELECTED_BACKGROUND);

//        for(Button node : nodes)
//        {
//            node.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event)
//                {
//                    selectedButton.setBackground(defaultBackground);
//                    selectedButton = node;
//                    defaultBackground = node.getBackground();
//                    selectedButton.setBackground(SELECTED_BACKGROUND);
//                }
//            });
//        }

        this.show();
        location = TOP_LOCATION;
        nodes.remove(nodes.size()-1);
        this.fitScreen();
        this.setAlwaysOnTop(true);
    }
    
    private void saveImage(int numTimesRun) {
    	at.setBorderVisibility(false);
    	try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	final Clipboard clipboard = Clipboard.getSystemClipboard();
        Image clipImage = null;
        String clipString = null;
        if(clipboard.hasImage()) {
        	clipImage = clipboard.getImage();
        } else if(clipboard.hasString()) {
        	clipString = clipboard.getString();
        }
        clipboard.clear();

        Robot robot;
        try
        {
            robot = new Robot();
        }
        catch (AWTException e)
        {
            throw new RuntimeException(e);          //potentially fixes robot working with ubuntu.
        }

        try {
        	robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
			Thread.sleep(200);
			robot.keyPress(java.awt.event.KeyEvent.VK_PRINTSCREEN);
			Thread.sleep(200);
			robot.keyRelease(java.awt.event.KeyEvent.VK_PRINTSCREEN);
			robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        if(numTimesRun % 2 == 0) {
        	at.doSave();
        }
        at.setBorderVisibility(true);

        ClipboardContent clipContent = new ClipboardContent();
    	if(clipImage != null) {
    		clipContent.putImage(clipImage);
    		clipboard.setContent(clipContent);
    	} else if(clipString != null) {
    		clipContent.putString(clipString);
    		clipboard.setContent(clipContent);
    	}
    }

    private void setIconSizes(double size)
    {
        if(nodes.get(nodes.size()-1) == sendToBackButton)
        {
            nodes.add(bringToFrontButton);
        }
        else
        {
            nodes.add(sendToBackButton);
        }
        for(Button n : this.nodes)
        {
            //n.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
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
        nodes.remove(nodes.size()-1);
        this.buttonSize = size;
        this.fitScreen();
    }
    
    private Tooltip getToolTip(String toolTipString)
    {
        Tooltip tooltip = new Tooltip();
        tooltip.setFont(TOOLTIP_FONT);
        tooltip.setText(toolTipString);
        return tooltip;
    }
    
    public void fitScreen() {
    	int numButtons = nodes.size() - shapeSelectingNodes.size();
    	double menuLength = numButtons * buttonSize;
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int numSplits = 1;
		if(location == TOP_LOCATION) {
			numSplits += (int)(menuLength / screenSize.getWidth());
		} else {
			numSplits += (int)(menuLength / screenSize.getHeight());
		}
		snapBox(numSplits);
    }
    
    private void snapBox(int numSplits) {
    	Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				root.getChildren().remove(trunk);
				Pane[] splits;
				splits = new Pane[numSplits];
				
				if(location == TOP_LOCATION) {
					trunk = new VBox();
					for(int i = 0; i < numSplits; i++) {
						splits[i] = new HBox();
					}
				} else {
					trunk = new HBox();
					for(int i = 0; i < numSplits; i++) {
						splits[i] = new VBox();
					}
				}
				
				for(Pane pane : splits) {
					trunk.getChildren().add(pane);
				}
				
				int i = 0;
				for(Node node : nodes)
				{
				    if (!shapeSelectingNodes.contains(node))
				    {
                        splits[i].getChildren().add(node);
                        i = ++i % numSplits;
                    }
				}
				
				root.getChildren().add(trunk);
				sizeToScene();
				Stage pictureStage = at.getPictureStage();
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				
				switch(location) {
				case TOP_LOCATION:
					if(pictureStage.isFullScreen() || pictureStage.isMaximized())
					{
						setX(at.getPictureStage().getX());
						centerOnScreen();
						setY(0);
					}
					else
					{
						setY( Math.max(0, pictureStage.yProperty().get() - buttonSize) );
						setX( Math.min( Math.max(0, pictureStage.xProperty().get() + pictureStage.getWidth()/2- getWidth()/2), 
								screenSize.getWidth() - ((nodes.size() - shapeSelectingNodes.size()) * buttonSize) / numSplits) );
					}
					break;
				case LEFT_LOCATION:
					if(pictureStage.isFullScreen() || pictureStage.isMaximized())
					{
						centerOnScreen();
						setX(0);
					}
					else
					{
						setY( Math.min( Math.max(0, pictureStage.yProperty().get() + pictureStage.getHeight() / 2 - getHeight() /2), 
								screenSize.getHeight() - ((nodes.size() - shapeSelectingNodes.size()) * buttonSize) / numSplits) );
						setX( Math.max(0, pictureStage.xProperty().get() - (buttonSize * numSplits)) );
					}
					break;
				case RIGHT_LOCATION:
					if(pictureStage.isFullScreen() || pictureStage.isMaximized())
					{
						centerOnScreen();
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						setX(primScreenBounds.getWidth() - getWidth());
					}
					else
					{
						setY( Math.min( Math.max(0, pictureStage.yProperty().get() + pictureStage.getHeight() / 2 - getHeight() /2), 
								screenSize.getHeight() - ((nodes.size() - shapeSelectingNodes.size()) * buttonSize) / numSplits) );
						setX( Math.min(pictureStage.xProperty().get() + pictureStage.getWidth() - getWidth() + (numSplits * buttonSize), 
								Toolkit.getDefaultToolkit().getScreenSize().getWidth() - (numSplits * buttonSize)) );
					}
					break;
				default:
					break;
				}
			}
		});
    }

    private void setDialogLocation(Dialog dialog)
    {
        Stage pictureStage = at.getPictureStage();
        if(pictureStage.getX() < 0)
        {
            dialog.setX(0);
        }
        else
        {
            dialog.setX(pictureStage.getX());
        }
        if(pictureStage.getY() < 0)
        {
            dialog.setY(0);
        }
        else
        {
            dialog.setY(pictureStage.getY());
        }
    }
    
    public void newFile(String path) {
    	System.out.println("Opening new file");
    	Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.initOwner(at.getMouseCatchingStage());
		dialog.setTitle("Creating new annotation");
		dialog.setHeaderText("This annotation will be closed.");
		dialog.setContentText("Would you like to save?");
		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No");
		ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getButtonTypes().setAll(yesBtn, noBtn, cancelBtn);
		Optional<ButtonType> result = dialog.showAndWait();
		
		if(result.get() == yesBtn) {
			try {

                at.fileManagement("sFile"); //save as
            } catch (Exception e) {
                e.printStackTrace();
            }
		} 
		
		if(result.get() != cancelBtn) {
			at.getControllerBox().close();
			at.getMouseCatchingStage().close();
			at.getPictureStage().close();
			FXAnnotationToolBuilder builder = new FXAnnotationToolBuilder();
			try {
				if(!new File(path).exists() || !path.endsWith(".jnote")) {
					builder.start(new Stage());
				} else {
					builder.start(new Stage(), path);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}

