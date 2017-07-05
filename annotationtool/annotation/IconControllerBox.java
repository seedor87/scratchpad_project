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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.peer.ButtonPeer;
import java.io.File;
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

    private double smallButtonSize;
    private double medButtonSize;
    private double largeButtonSize;
    private double buttonSize;
    private static final int LEFT_LOCATION = 0;
    private static final int TOP_LOCATION = 1;
    private static final int RIGHT_LOCATION = 2;
    private int location = RIGHT_LOCATION;
    private static final int TOOLTIP_FONT_SIZE = 20;
    private static final Font TOOLTIP_FONT = new Font(TOOLTIP_FONT_SIZE);

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
        
        double dotsPerInch = Screen.getPrimary().getDpi();
        smallButtonSize = .25 * dotsPerInch;
        medButtonSize = .35 * dotsPerInch;
        largeButtonSize = .6 * dotsPerInch;

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
        saveImageButton.setTooltip(getToolTip("Save Image"));
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
        arrowButton.setTooltip(getToolTip("Draw Arrows"));
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
        circleButton.setTooltip(getToolTip("Make Circles"));
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
        drawButton.setTooltip(getToolTip("Draw"));
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
        textButton.setTooltip(getToolTip("Add Text"));
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
        eraseButton.setTooltip(getToolTip("Erase"));
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
        sizePickerButton.setTooltip(getToolTip("Pick a size"));
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
                at.resetStages();

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
                at.undo();
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
                at.redo();
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
                button1.setMaxSize(smallButtonSize,smallButtonSize);
                button1.setMinSize(smallButtonSize,smallButtonSize);
                button1.setTooltip(getToolTip("Small"));
                button1.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = smallButtonSize;
                    }
                });

                button2.setMaxSize(medButtonSize,medButtonSize);
                button2.setMinSize(medButtonSize,medButtonSize);
                button2.setTooltip(getToolTip("Medium"));
                button2.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = medButtonSize;
                    }
                });

                button3.setMaxSize(largeButtonSize,largeButtonSize);
                button3.setMinSize(largeButtonSize,largeButtonSize);
                button3.setTooltip(getToolTip("Large"));
                button3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        changeSize = largeButtonSize;
                    }
                });

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
                at.setMovingHandler();
            }
        });
        nodes.add(moveButton);

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
        sendToBackButton.setTooltip(getToolTip("Send To Back"));
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
        bringToFrontButton.setTooltip(getToolTip("Bring to Front"));
        bringToFrontButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                at.toFront();
                IconControllerBox.this.setAlwaysOnTop(false);
                IconControllerBox.this.setAlwaysOnTop(true);
                IconControllerBox.this.toFront();

            }
        });
        nodes.add(bringToFrontButton);

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
                at.setSelectAndMoveHandler();
            }
        });
        nodes.add(moveShapesButton);
        
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
                at.toggleLockedControllerBox();
            }
        });
        nodes.add(lockControllerBoxButton);
        
        setIconSizes(medButtonSize);

        this.show();
        location = TOP_LOCATION;
        this.fitScreen();
        this.setAlwaysOnTop(true);
    }

    private void setIconSizes(double size)
    {
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
    	int numButtons = nodes.size();
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
            splits[i].getChildren().add(node);
            i = ++i % numSplits;
        }
    	
    	root.getChildren().add(trunk);
    	this.sizeToScene();
        Stage pictureStage = at.getPictureStage();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
    	switch(location) {
    		case TOP_LOCATION:
    			if(pictureStage.isFullScreen() || pictureStage.isMaximized())
    	        {
    	            //TODO handle all if statements
    	            this.setX(at.getPictureStage().getX());
    	            centerOnScreen();
    	            this.setY(0);
    	        }
    	        else
    	        {
    	            this.setY( Math.max(0, pictureStage.yProperty().get() - buttonSize) );
    	            this.setX( Math.min( Math.max(0, pictureStage.xProperty().get() + pictureStage.getWidth()/2- this.getWidth()/2), 
    	            		   screenSize.getWidth() - (nodes.size() * buttonSize) / numSplits) );
    	        }
    			break;
    		case LEFT_LOCATION:
    			if(pictureStage.isFullScreen() || pictureStage.isMaximized())
    	        {
    	            centerOnScreen();
    	            this.setX(0);
    	        }
    	        else
    	        {
    	            this.setY( Math.min( Math.max(0, pictureStage.yProperty().get() + pictureStage.getHeight() / 2 - this.getHeight() /2), 
    	            		   screenSize.getHeight() - (nodes.size() * buttonSize) / numSplits) );
    	            this.setX( Math.max(0, pictureStage.xProperty().get() - (buttonSize * numSplits)) );
    	        }
    			break;
    		case RIGHT_LOCATION:
    			if(pictureStage.isFullScreen() || pictureStage.isMaximized())
    	        {
    	            centerOnScreen();
    	            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
    	            this.setX(primScreenBounds.getWidth() - this.getWidth());
    	        }
    	        else
    	        {
    	        	this.setY( Math.min( Math.max(0, pictureStage.yProperty().get() + pictureStage.getHeight() / 2 - this.getHeight() /2), 
 	            		   	   screenSize.getHeight() - (nodes.size() * buttonSize) / numSplits) );
    	            this.setX( Math.min(pictureStage.xProperty().get() + pictureStage.getWidth() - this.getWidth() + (numSplits * buttonSize), 
    	            		   Toolkit.getDefaultToolkit().getScreenSize().getWidth() - (numSplits * buttonSize)) );
    	        }
    			break;
    		default:
    			break;
    	}
    }
    
}

