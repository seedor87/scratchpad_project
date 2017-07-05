package changeItem;

import annotation.AnnotationToolApplication;
import javafx.application.Platform;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import sun.security.provider.SHA;

import java.util.ListIterator;
import java.util.Stack;

/**
 * Created by Brennan on 6/13/2017.
 */
public class EraseShape implements ChangeItem
{
    private Path eraseArea;
    private Stack<ChangeItem> shapesPartiallyErased = new Stack<>();
    private Stack<ChangeItem> undidStack;
    public EraseShape(Path eraseArea)
    {
        this.eraseArea = eraseArea;
    }

    public Shape getErasePath()
    {
        return eraseArea;
    }
    public Stack<ChangeItem> getShapesPartiallyErased()
    {
        return shapesPartiallyErased;
    }


    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        ChangeItem oldItem;
        Shape oldShape;
        Shape newShape;
        Stack<ChangeItem> stack = annotationToolApplication.getUndoStack();
        ListIterator<ChangeItem> iterator = stack.listIterator(stack.size());        //list iterator starting from top of stack.
        while(iterator.hasPrevious())
        {
            oldItem = iterator.previous();
            if((oldItem instanceof AddShape))                                       //not instance of eraseshape
            {
                oldShape =((AddShape) oldItem).getShape();

                newShape = Shape.subtract(oldShape, eraseArea);
                newShape.setFill(oldShape.getFill());
                if(oldShape.getFill() == null)
                {
                    newShape.setFill(oldShape.getStroke());
                }
                shapesPartiallyErased.add(oldItem);
                iterator.set(new AddShape(newShape));
            }
            else
            {
                shapesPartiallyErased.add(oldItem);                         //add should probably be push (same for a few lines up)?
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                annotationToolApplication.paintFromUndoStack();
            }
        });
    }

    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        Stack<ChangeItem> undoStack = annotationToolApplication.getUndoStack();
        undidStack = (Stack<ChangeItem>) undoStack.clone();
        undoStack.clear();

        while(!(shapesPartiallyErased.isEmpty()))
        {
            undoStack.push(shapesPartiallyErased.pop());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                annotationToolApplication.paintFromUndoStack();
            }
        });

    }

    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        Stack<ChangeItem> undoStack = annotationToolApplication.getUndoStack();
        shapesPartiallyErased = (Stack<ChangeItem>)undoStack.clone();
        undoStack.clear();
        undoStack.addAll(undidStack);
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                annotationToolApplication.paintFromUndoStack();
            }
        });
    }
}
