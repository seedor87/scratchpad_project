package changeItem;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import javafx.scene.shape.*;

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

    /**
     *
     * @param shape the shape that this moveShape represents. The new x and y locations are taken from the shape.
     * @param oldX the old x location of the shape
     * @param oldY the old y location of the shape.
     * @param annotationToolApplication
     */
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
            annotationToolApplication.writeJSON(custom_shape);
        }
        catch (IOException ioe)
        {

        }
    }

    /**
     *
     * @param newX The new x location of the shape.
     * @param newY The new y location of the shape.
     * @param shape The shape represented by this MoveShape. The old x and y locations are taken
     *              from this shape.
     */
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

    /**
     * Moves the shape represented by this MoveShape object to the new x and y location set in the constructor.
     * @param annotationToolApplication The AnnotationToolApplication that is having the change added to.
     */
    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        shape.layoutXProperty().set(newX);
        shape.layoutYProperty().set(newY);
    }

    /**
     * Moves the shape represented by this MoveShape back to the old x and y location that were set in the constructor.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change
     */
    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        shape.layoutYProperty().set(oldY);
        shape.layoutXProperty().set(oldX);
    }

    /**
     * Moves the shape represented by this MoveShape back to the new X and Y location.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change redone to
     */
    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        addChangeToStage(annotationToolApplication);
    }
}
