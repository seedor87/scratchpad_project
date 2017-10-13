package eventHandlers;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import com.google.gson.JsonParseException;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds a circle at the given location of the MouseEvent. should be
 * implemented with MouseEvent.ANY when you add the
 * handler to the mousecatchingscene.
 */
public class CircleHandler implements EventHandler<MouseEvent>
{
    private AnnotationToolApplication annotationToolApplication;
    private Circle circle;
    private UUID uuid;
    public CircleHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }
    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            //                circle = new Circle(event.getSceneX(), event.getSceneY(),10, paint);      //just this line for full circle.
            circle = new Circle(event.getSceneX(), event.getSceneY() ,10, Color.TRANSPARENT);
            circle.setStroke(annotationToolApplication.getPaint());
            circle.setStrokeWidth(annotationToolApplication.getStrokeWidth());
            annotationToolApplication.commitChange(new AddShape(circle));

        } else if (circle != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            double xDistance = event.getX() - circle.getCenterX();
            double yDistance = event.getY() - circle.getCenterY();
            circle.setRadius(Math.hypot(xDistance,yDistance ));

        } else if (circle != null && event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            annotationToolApplication.undo();
            circle.setFill(annotationToolApplication.getPaint());
            Shape newCircle = Shape.subtract(circle, new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius() - (annotationToolApplication.getStrokeWidth()/2)));
            newCircle.setFill(annotationToolApplication.getPaint());
            AddShape addShape = new AddShape(newCircle);
            annotationToolApplication.commitChange(addShape);

            try {
                uuid = UUID.randomUUID();
                Custom_Shape shape = new Custom_Shape(uuid, Custom_Shape.CIRCLE_STRING);
                shape.setLocation(new TransferableShapes.Point(String.valueOf(circle.getCenterX()), String.valueOf(circle.getCenterY())));
                shape.setColorString((annotationToolApplication.getPaint().toString()));
                shape.setStrokeWidth(String.valueOf(annotationToolApplication.getStrokeWidth()));
                shape.setRadius(String.valueOf(circle.getRadius()));

                //holder.add(shape);
                annotationToolApplication.writeJSON(shape);
                Custom_Shape.setUpUUIDMaps(newCircle, uuid);


            } catch (JsonParseException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace(); }

            annotationToolApplication.addLeaderToFollower(newCircle);

            annotationToolApplication.getRedoStack().clear();
            circle = null;
        }
    }
}
