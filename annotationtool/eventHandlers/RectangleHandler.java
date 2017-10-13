package eventHandlers;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.UUID;

/**
 * This handler adds a rectangle to the stage based on a drag motion. the rectangle must be made
 * starting from the top left part of the rectangle.
 */
public class RectangleHandler implements EventHandler<MouseEvent>
{
    private Rectangle rectangle;
    private UUID uuid;
    private AnnotationToolApplication annotationToolApplication;

    public RectangleHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event)
    {
        if(event.getEventType() == MouseEvent.MOUSE_PRESSED)
        {
            rectangle = new Rectangle(event.getX(), event.getY(), 0,0);
            rectangle.setStrokeWidth(annotationToolApplication.getStrokeWidth());
            rectangle.setStroke(annotationToolApplication.getPaint());
            rectangle.setFill(null);
            annotationToolApplication.commitChange(new AddShape(rectangle));
        }
        else if(event.getEventType() == MouseEvent.MOUSE_DRAGGED)
        {
            rectangle.setWidth(event.getX() - rectangle.getX());
            rectangle.setHeight(event.getY() - rectangle.getY());
        }
        if(event.getEventType() == MouseEvent.MOUSE_RELEASED)
        {
            uuid = UUID.randomUUID();
            Custom_Shape.setUpUUIDMaps(rectangle, uuid);
            Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.RECTANGLE_STRING,
                    new TransferableShapes.Point(String.valueOf(rectangle.getX()), String.valueOf(rectangle.getY())),
                    annotationToolApplication.getPaint(), rectangle.getWidth(), rectangle.getHeight(), annotationToolApplication.getStrokeWidth());
            try {
                annotationToolApplication.writeJSON(custom_shape);
            } catch (IOException e) {
                e.printStackTrace();
            }
            annotationToolApplication.addLeaderToFollower(rectangle);
            rectangle = null;
        }
    }
}
