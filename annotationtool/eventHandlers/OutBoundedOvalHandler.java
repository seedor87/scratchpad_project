package eventHandlers;

import transferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import transferableShapes.TransferableShapePoint;

import java.io.IOException;
import java.util.UUID;

/**
 * This handler allows the user to make ovals. It starts by making an outbounded rectangle, and then turns it
 * into an oval that is inbounded to the rectangle.
 */
public class OutBoundedOvalHandler implements EventHandler<MouseEvent>
{
    private double top;
    private double bottom;
    private double left;
    private double right;
    private Path tempPath;
    private AnnotationToolApplication annotationToolApplication;
    private UUID uuid;

    public OutBoundedOvalHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event) {
        if(event.getEventType() == MouseEvent.MOUSE_PRESSED)
        {
            top = event.getY();
            bottom = event.getY();
            left = event.getX();
            right = event.getX();
            tempPath = new Path();
            tempPath.getElements().add(new MoveTo(event.getX(), event.getY()));
            tempPath.setStroke(annotationToolApplication.getPaint());
            tempPath.setStrokeWidth(5);
            annotationToolApplication.getRoot().getChildren().add(tempPath);
        }
        if(event.getEventType() == MouseEvent.MOUSE_DRAGGED)
        {
            if(event.getY() < top)
            {
                top = event.getY();
            }
            if(event.getY() > bottom)
            {
                bottom = event.getY();
            }
            if(event.getX() < left)
            {
                left = event.getX();
            }
            if(event.getX() > right)
            {
                right = event.getX();
            }
            tempPath.getElements().add(new LineTo(event.getX(), event.getY()));
        }
        if(event.getEventType() == MouseEvent.MOUSE_RELEASED)
        {
            Rectangle rectangle = new Rectangle(left, top, right - left, bottom - top);
            rectangle.setFill(null);
            rectangle.setStroke(annotationToolApplication.getPaint());
            rectangle.setStrokeWidth(annotationToolApplication.getStrokeWidth());
                /*
                Oval code
                 */
            rectangle.setArcWidth(right - left);
            rectangle.setArcHeight(bottom - top);

            uuid = UUID.randomUUID();
            Custom_Shape.setUpUUIDMaps(rectangle, uuid);
            Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.OVAL_STRING,
                    new TransferableShapePoint(String.valueOf(rectangle.getX()), String.valueOf(rectangle.getY())),
                    annotationToolApplication.getPaint(), rectangle.getWidth(), rectangle.getHeight(), annotationToolApplication.getStrokeWidth());
            try {
                annotationToolApplication.writeJSON(custom_shape);
            } catch (IOException e) {
                e.printStackTrace();
            }
            annotationToolApplication.commitChange(new AddShape(rectangle));
            annotationToolApplication.addLeaderToFollower(rectangle);
            annotationToolApplication.getRoot().getChildren().remove(tempPath);
            annotationToolApplication.getRedoStack().clear();
        }
    }
}

