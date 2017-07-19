package changeItem;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Brennan on 6/13/2017.
 * A subclass of ChangeItem that represents a shape being moved from one location on the screen to another.
 */
public class MoveShape implements ChangeItem
{
    Shape shape;
    double oldX;
    double oldY;
    double newX;
    double newY;

    public MoveShape(Shape shape, double oldX, double oldY, AnnotationToolApplication annotationToolApplication)
    {
        this.shape = shape;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = shape.getLayoutX();
        this.newY = shape.getLayoutY();
        Custom_Shape custom_shape = new Custom_Shape(Custom_Shape.MOVE_SHAPE_STRING, shape);
        try
        {
            annotationToolApplication.writeJSON(custom_shape, false);
        }
        catch (IOException ioe)
        {

        }
    }
    public MoveShape(double newX, double newY, Shape shape)
    {
        this.shape = shape;
        this.newY = newY;
        this.newX = newX;
        this.oldY = shape.getLayoutY();
        this.oldX = shape.getLayoutX();
        shape.setLayoutX(newX);
        shape.setLayoutY(newY);
    }

    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        shape.layoutXProperty().set(newX);
        shape.layoutYProperty().set(newY);
    }

    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        shape.layoutYProperty().set(oldY);
        shape.layoutXProperty().set(oldX);
    }

    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        addChangeToStage(annotationToolApplication);
    }
}
