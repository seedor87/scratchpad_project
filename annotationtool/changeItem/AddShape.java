package changeItem;

import annotation.AnnotationToolApplication;
import javafx.application.Platform;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 * Created by Brennan on 6/13/2017.
 */
public class AddShape implements ChangeItem
{
    private Shape shape;

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
                    //Tried to add a duplicate node.
                    //TODO find the reason why I need this try catch.
                    //                commitShape(new AddShape(newCircle)); is one place where it would be thrown.
                }
            }
        });
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
}
