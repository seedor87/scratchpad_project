package eventHandlers;

import TransferableShapes.Custom_Shape;
import TransferableShapes.Point;
import annotation.AnnotationToolApplication;
import com.google.gson.JsonParseException;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles erasing a shaded area from the existing shapes on the screen.
 * should be implemented with MouseEvent.ANY when you add the
 * handler to the mousecatchingscene.
 */
public class EraseHandler implements EventHandler<MouseEvent> {

    private ArrayList<Point> pathElements;
    private Color eraserColor = new Color(0,0,0,.1);
    private AnnotationToolApplication annotationToolApplication;
    private Path eraserPath;
    private UUID uuid;

    public EraseHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }
    @Override
    public void handle(MouseEvent event)
    {

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            pathElements = new ArrayList<>();
            eraserPath = new Path();
            eraserPath.setStrokeWidth(annotationToolApplication.getStrokeWidth());
            eraserPath.setSmooth(true);
            MoveTo moveTo = new MoveTo(event.getX(), event.getY());
            pathElements.add(new TransferableShapes.Point(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
            eraserPath.getElements().add(moveTo);
            annotationToolApplication.getRoot().getChildren().add(eraserPath);
            eraserPath.setStroke(eraserColor);
        } else if (eraserPath != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            LineTo moveTo = new LineTo(event.getX(), event.getY());
            pathElements.add(new TransferableShapes.Point(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
            eraserPath.getElements().add(moveTo);
        } else if (eraserPath != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            annotationToolApplication.getRoot().getChildren().remove(eraserPath);
            changeItem.EraseShape eraseShape = new changeItem.EraseShape(eraserPath);
            annotationToolApplication.commitChange(eraseShape);

            try {
                uuid = UUID.randomUUID();
                Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.ERASE_STRING, pathElements);
                shape.setStrokeWidth(String.valueOf(eraserPath.getStrokeWidth()));

                // holder.add(shape);
                annotationToolApplication.writeJSON(shape);

            } catch (JsonParseException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace(); }


            eraseShape = null;
            annotationToolApplication.getRedoStack().clear();
        }
    }
}

