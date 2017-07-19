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
        //TODO write out to the file here with the moveshape.
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
    //TODO set it so that I can move more than once and retain undo/redo info for those moves
    // is there any way to get the
    //        shape.layoutXProperty().set(); and get();
    // should prob fix it.

    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        System.out.println(newX + "," + newY);
        shape.layoutXProperty().set(newX);
        shape.layoutYProperty().set(newY);
        //shape.setLayoutX(newX);
        //shape.setLayoutY(newY);
    }

    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        System.out.println(oldX + ", " + oldY + '\n' + newX + ", " + newY);
        shape.layoutYProperty().set(oldY);
        shape.layoutXProperty().set(oldX);
        //shape.setLayoutX(oldX);
        //shape.setLayoutY(oldY);
    }

    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        addChangeToStage(annotationToolApplication);
    }
}
