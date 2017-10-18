package eventHandlers;


import transferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import transferableShapes.TransferableShapePoint;

import java.io.IOException;
import java.util.UUID;

/**
 * This handler is used for drawing straight lines onto the picture stage.
 */
public class LineHandler implements EventHandler<MouseEvent>
{
    private Line line;
    private UUID uuid;
    private AnnotationToolApplication annotationToolApplication;

    public LineHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event)
    {
        if(event.getEventType() == MouseEvent.MOUSE_PRESSED)
        {
            line = new Line(event.getX(),event.getY(),event.getX(),event.getY());
            line.setStrokeWidth(annotationToolApplication.getStrokeWidth());
            line.setStroke(annotationToolApplication.getPaint());
            annotationToolApplication.commitChange(new AddShape(line));
        }
        else if(event.getEventType() == MouseEvent.MOUSE_DRAGGED)
        {
            line.setEndX(event.getX());
            line.setEndY(event.getY());
        }
        else if(event.getEventType() == MouseEvent.MOUSE_RELEASED)
        {
            uuid = UUID.randomUUID();
            Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.LINE_STRING, (Color) annotationToolApplication.getPaint(),
                    String.valueOf(annotationToolApplication.getStrokeWidth())
                    , new TransferableShapePoint(String.valueOf(line.getStartX()), String.valueOf(line.getStartY())),
                    new TransferableShapePoint(String.valueOf(event.getX()), String.valueOf(event.getY())));
            try {
                annotationToolApplication.writeJSON(custom_shape);
            } catch (IOException e) {
                e.printStackTrace();
            }
            annotationToolApplication.addLeaderToFollower(line);
            Custom_Shape.setUpUUIDMaps(line, uuid);
            annotationToolApplication.getRedoStack().clear();
        }

    }
}