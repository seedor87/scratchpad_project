package changeItem;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Created by Brennan on 6/13/2017.
 * An subclass of ChangeItem that is used to represent a path (a shape) that is erased from a stage
 */
public class EraseShape implements ChangeItem
{
    private Path eraseArea;
    private Stack<ChangeItem> shapesPartiallyErased = new Stack<>();
    private Stack<ChangeItem> undidStack;
    private ArrayList<Node> oldLeaders;
    private ArrayList<Node> newLeaders;

    /**
     * @param eraseArea the area that should be subtracted from each existing shape in a window
     */
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

    /**
     * Goes through each shape on the stage and removes any overlapping area between the shapes on the main window
     * and the eraseShape.
     * @param annotationToolApplication The AnnotationToolApplication that is having the change added to.
     */
    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        ChangeItem oldItem;
        Shape oldShape;
        Shape newShape;
        oldLeaders = new ArrayList<>(annotationToolApplication.getLeaderGroup().size());
        oldLeaders.addAll(annotationToolApplication.getLeaderGroup());
        annotationToolApplication.getLeaderGroup().clear();
        Stack<ChangeItem> stack = annotationToolApplication.getUndoStack();
        ListIterator<ChangeItem> iterator = stack.listIterator(stack.size());        //list iterator starting from top of stack.
        while(iterator.hasPrevious())
        {
            oldItem = iterator.previous();
            if((oldItem instanceof AddShape))                                       //not instance of eraseshape
            {
                oldShape =((AddShape) oldItem).getShape();

                if(oldShape instanceof Text)
                {
                    shapesPartiallyErased.add(oldItem);
                }
                else
                {
                    newShape = Shape.subtract(oldShape, eraseArea);
                    newShape.setFill(oldShape.getFill());
                    if (oldShape.getFill() == null)
                    {
                        newShape.setFill(oldShape.getStroke());
                    }
                    shapesPartiallyErased.add(oldItem);
                    Custom_Shape.changeShape(((AddShape) oldItem).getShape(), newShape);
                    annotationToolApplication.addLeaderToFollower(newShape);
                    iterator.set(new AddShape(newShape));
                }
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

    /**
     * Adds the erased area back to the main window.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change
     */
    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        Stack<ChangeItem> undoStack = annotationToolApplication.getUndoStack();
        undidStack = (Stack<ChangeItem>) undoStack.clone();
        undoStack.clear();

        newLeaders = new ArrayList<>(annotationToolApplication.getLeaderGroup().size());
        newLeaders.addAll(annotationToolApplication.getLeaderGroup());
        annotationToolApplication.getLeaderGroup().clear();
        annotationToolApplication.getLeaderGroup().setAll(oldLeaders);

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

    /**
     * Redoes the changes made by undo. This is inherently faster than simply adding the changes to the stage.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change redone to
     */
    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        Stack<ChangeItem> undoStack = annotationToolApplication.getUndoStack();
        shapesPartiallyErased = (Stack<ChangeItem>)undoStack.clone();
        annotationToolApplication.getLeaderGroup().clear();
        annotationToolApplication.getLeaderGroup().setAll(newLeaders);
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
