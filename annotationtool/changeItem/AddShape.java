package changeItem;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Created by Brennan on 6/13/2017.
 * A subclass of ChangeItem that represents a shape being added to the window.
 */
public class AddShape implements ChangeItem
{
    public static boolean movingShapes = true;
    private final MoveShapeHandler moveShapeHandler = new MoveShapeHandler();
    private Shape shape;
    private AnnotationToolApplication annotationToolApplication;

    public AddShape(Shape shape)
    {
        this.shape = shape;
    }

    public Shape getShape()
    {
        return shape;
    }


    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    annotationToolApplication.getRoot().getChildren().add(shape);
                }
                catch (java.lang.IllegalArgumentException iae)
                {
                    annotationToolApplication.paintFromUndoStack();
                    //Tried to add a duplicate node.
                    //TODO find the reason why I need this try catch.
                    //                commitShape(new AddShape(newCircle)); is one place where it would be thrown.
                }
            }
        });
        shape.addEventHandler(MouseEvent.ANY, moveShapeHandler);
        /*new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                System.out.println("Shp clicked");
                annotationToolApplication.setClickedShape(shape);
            }
        });*/
    }

    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                //annotationToolApplication.getRoot().getChildren().remove(root.getChildren().size() -1);
                annotationToolApplication.getRoot().getChildren().remove(shape);        // above line probably more efficient.
                annotationToolApplication.paintFromUndoStack();                   //inefficient but solves problem.
            }
        });
    }

    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        addChangeToStage(annotationToolApplication);
    }
    private class MoveShapeHandler implements EventHandler<MouseEvent>
    {
        double oldX;
        double oldY;
        double oldClickX;
        double oldClickY;
        double newX;
        double newY;
        @Override
        public void handle(MouseEvent event)
        {
            if(event.getEventType() == MouseEvent.MOUSE_PRESSED && movingShapes)
            {
                //oldX = shape.getTranslateX();
                oldX = shape.layoutXProperty().get();
                oldY = shape.layoutYProperty().get();
                //oldY = shape.getTranslateY();
                oldClickX = event.getScreenX();
                oldClickY = event.getScreenY();
            }
            else if(event.getEventType() == MouseEvent.MOUSE_DRAGGED && movingShapes)
            {
                newX = oldX + event.getScreenX() - oldClickX;
                newY = oldY + event.getScreenY() - oldClickY;
                shape.layoutXProperty().set(newX);
                shape.layoutYProperty().set(newY);
                //shape.setTranslateX(newX);
                //shape.setTranslateY(newY);
            }
            else if(event.getEventType() == MouseEvent.MOUSE_RELEASED && movingShapes)
            {
                annotationToolApplication.commitChange(new MoveShape(shape, oldX, oldY, annotationToolApplication));
                if(shape instanceof Text)
                {
                    annotationToolApplication.setEditingText((Text) shape);
                }
            }
        }
    }
}
