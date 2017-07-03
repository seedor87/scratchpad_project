package changeItem;

import annotation.AnnotationToolApplication;
import javafx.scene.shape.*;
import javafx.stage.Stage;

/**
 * Created by Brennan on 6/13/2017.
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

    //TODO this class.

    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        shape.setLayoutX(newX);
        shape.setLayoutY(newY);
    }

    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        shape.setLayoutX(oldX);
        shape.setLayoutY(oldY);
    }

    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        addChangeToStage(annotationToolApplication);
    }
}
