package changeItem;

import annotation.AnnotationToolApplication;
import javafx.scene.shape.*;

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

    public MoveShape(Shape shape, double oldX, double oldY)
    {
        this.shape = shape;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = shape.getLayoutX();
        this.newY = shape.getLayoutY();
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
