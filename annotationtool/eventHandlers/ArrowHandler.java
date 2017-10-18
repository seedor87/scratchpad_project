package eventHandlers;

import transferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import transferableShapes.TransferableShapePoint;

import java.io.IOException;
import java.util.UUID;

/**
 * Creates arrows. should be implemented with MouseEvent.ANY when you add the
 * handler to the mousecatchingscene.
 */
public class ArrowHandler implements EventHandler<MouseEvent>
{
    private AnnotationToolApplication annotationToolApplication;
    private Line line;
    private UUID uuid;
    public ArrowHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }
    @Override
    public void handle(MouseEvent event)
    {
        if(annotationToolApplication.getClickable())
        {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                line = new Line(event.getX(), event.getY(), event.getX(), event.getY());
                line.setStroke(annotationToolApplication.getPaint());
                line.setStrokeWidth(annotationToolApplication.getStrokeWidth());
                annotationToolApplication.commitChange(new AddShape(line));
            }
            else if (line != null && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                line.setEndX(event.getX());
                line.setEndY(event.getY());
            }
            else if (line != null && event.getEventType() == MouseEvent.MOUSE_RELEASED)
            {
                uuid = UUID.randomUUID();
                addArrowToEndOfLine(event, uuid);
                line = null;
                annotationToolApplication.getRedoStack().clear();
            }
        }
    }

    /**
     *  adds a triangle to the most recent straight line drawn to make it an arrow.
     * @param mouseEvent
     */
    private void addArrowToEndOfLine(MouseEvent mouseEvent, UUID uuid)
    {
        double strokeWidth = annotationToolApplication.getStrokeWidth();
        final double halfBaseDistance = 2;
        final double heightDistance = 4;
        double slope;
        double xDistance = line.getEndX() - line.getStartX();
        double yDistance = line.getEndY() - line.getStartY();
        if(line.getEndY() == line.getStartY()) {
            slope = Double.POSITIVE_INFINITY;
        } else {
            slope =  xDistance - yDistance;
        }
        Polygon triangle = new Polygon();
        if(slope == Double.POSITIVE_INFINITY)//straight upwards line.//TODO check which direction. //DO I really need this check/part?
        {
            //System.out.println("Thing");
            triangle.getPoints().addAll( (mouseEvent.getX() - halfBaseDistance*strokeWidth), mouseEvent.getY());
            triangle.getPoints().addAll( (mouseEvent.getX() + halfBaseDistance*strokeWidth), mouseEvent.getY());
            triangle.getPoints().addAll( mouseEvent.getX(), (mouseEvent.getY() - heightDistance*strokeWidth));
        } else {
            //point 1
            triangle.getPoints().
                    addAll( (line.getEndX() - (halfBaseDistance*strokeWidth * Math.sin(Math.atan2(yDistance, xDistance)))),
                            line.getEndY() + (halfBaseDistance*strokeWidth * Math.cos(Math.atan2(yDistance,xDistance))));
            //point 2
            triangle.getPoints().
                    addAll( (line.getEndX() + (halfBaseDistance*strokeWidth * Math.sin(Math.atan2(yDistance, xDistance)))),
                            line.getEndY() - (halfBaseDistance*strokeWidth * Math.cos(Math.atan2(yDistance,xDistance))));
            //triangle.getTransferableShapePoints().addAll( (mouseEvent.getX() + 2*strokeWidth), mouseEvent.getY());
            //point 3
            triangle.getPoints().
                    addAll(line.getEndX() + strokeWidth*heightDistance*Math.cos(Math.atan2(yDistance,xDistance)),
                            line.getEndY() + (strokeWidth*heightDistance*Math.sin(Math.atan2(yDistance, xDistance))));
            //triangle.getTransferableShapePoints().addAll( mouseEvent.getX() + 6), (mouseEvent.getY() + strokeWidth*4*Math.sin(Math.atan2(yDistance,xDistance)));
            //triangle.setRotate(90);
            Shape newShape = Shape.union(triangle, line);
            newShape.setFill(line.getStroke());
            annotationToolApplication.undo();
            annotationToolApplication.commitChange(new AddShape(newShape));
            annotationToolApplication.addLeaderToFollower(newShape);
            uuid = UUID.randomUUID();
            Custom_Shape.setUpUUIDMaps(newShape, uuid);

            //Save arrow to file
            Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.ARROW_STRING, (Color) annotationToolApplication.getPaint(), strokeWidth + "",
                    new TransferableShapePoint(line.getStartX()+"", line.getStartY()+""),
                    new TransferableShapePoint(line.getEndX() + "", line.getEndY()+""));
            try {
                annotationToolApplication.writeJSON(custom_shape);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}