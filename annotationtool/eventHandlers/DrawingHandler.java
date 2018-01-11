package eventHandlers;


import javafx.scene.Cursor;
import transferableShapes.Custom_Shape;
import transferableShapes.TransferableShapePoint;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import com.google.gson.JsonParseException;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Draws lines based on the location of various mouse events.
 * Pressing the mouse starts the line, dragging it extends.
 * Releasing ends the line.
 * should be implemented with MouseEvent.ANY when you add the
 * handler to the mousecatchingscene.
 */
public class DrawingHandler implements EventHandler<MouseEvent> {

    private ArrayList<TransferableShapePoint> pathElements;
    private AnnotationToolApplication annotationToolApplication;
    private Path path;
    private UUID uuid;

    public DrawingHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }


    @Override
    public void handle(MouseEvent event) {
        if(annotationToolApplication.getClickable()) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                path = new Path();
                path.setStrokeWidth(annotationToolApplication.getStrokeWidth());
                path.setSmooth(true);
                MoveTo moveTo = new MoveTo(event.getX(), event.getY());
                LineTo lineTo = new LineTo(event.getX(), event.getY());
                pathElements = new ArrayList<>();
                pathElements.add(new TransferableShapePoint(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
                path.getElements().add(moveTo);
                //root.getChildren().add(path);
                annotationToolApplication.commitChange(new AddShape(path));
                path.setStroke(annotationToolApplication.getPaint());
            } else if (path != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                LineTo moveTo = new LineTo(event.getX(), event.getY());
                pathElements.add(new TransferableShapePoint(String.valueOf(moveTo.getX()), String.valueOf(moveTo.getY())));
                path.getElements().add(moveTo);
            } else if (path != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {

                annotationToolApplication.addLeaderToFollower(path);

                try {
                    uuid = UUID.randomUUID();
                    Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.PATH_STRING, pathElements);
                    shape.setStrokeWidth(String.valueOf(path.getStrokeWidth()));
                    shape.setColorString(path.getStroke().toString());

                    //holder.add(shape);
                    annotationToolApplication.writeJSON(shape);
                    Custom_Shape.setUpUUIDMaps(path, uuid);


                } catch (JsonParseException e) {
                    e.printStackTrace();
                }  catch (IOException e) {
                    e.printStackTrace(); }

                path = null;
                annotationToolApplication.getRedoStack().clear();
            }
            else if (event.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET) {
                annotationToolApplication.getMouseCatchingScene().setCursor(Cursor.OPEN_HAND);
            }
            else if (event.getEventType() == MouseEvent.MOUSE_EXITED_TARGET) {
                annotationToolApplication.getMouseCatchingScene().setCursor(annotationToolApplication.pencilCursor);
            }
        }
    }
}