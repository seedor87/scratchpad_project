package eventHandlers;

import TransferableShapes.Custom_Shape;
import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import rectify.AnnotatePoint;
import rectify.Broken;
import rectify.Devdata;
import rectify.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This handler is used in order to rectify whatever shape you draw on the stage to some kind of polygon.
 */
public class RectificationHandler implements EventHandler<MouseEvent>
{
    private UUID uuid;
    private int index = 0;
    private Point[] arr = new Point[100000];
    private ArrayList<AnnotatePoint> points = new ArrayList<>();
    private static final int RECTIFY_THICKNESS_FACTOR = 5;
    private static final boolean DEBUG = true;
    private Devdata devdata = new Devdata(DEBUG, new ArrayList<AnnotatePoint>());;
    private Broken broken = new Broken();
    private static final double OUTLINE_STROKE_WIDTH = 5;
    private Path outLinePath;
    private AnnotationToolApplication annotationToolApplication;

    public RectificationHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event)
    {
        if(event.getEventType() == MouseEvent.MOUSE_PRESSED)
        {
            arr[index] = new Point(event.getX(), event.getY());
            points = new ArrayList<AnnotatePoint>();
            points.add(new AnnotatePoint(event.getX(), event.getY(), annotationToolApplication.getStrokeWidth(), 1));
            points.add(new AnnotatePoint(event.getX(), event.getY(), annotationToolApplication.getStrokeWidth(), 1));
            index++;
            outLinePath = new Path();
            annotationToolApplication.getRoot().getChildren().add(outLinePath);
            outLinePath.getElements().add(new MoveTo(event.getX(), event.getY()));
            outLinePath.setStrokeWidth(OUTLINE_STROKE_WIDTH);
            outLinePath.setStroke(annotationToolApplication.getPaint());
        }
        else if(event.getEventType() == MouseEvent.MOUSE_DRAGGED)
        {
            arr[index] = new Point(event.getX(), event.getY());
            points.add(new AnnotatePoint(event.getX(), event.getY(), annotationToolApplication.getStrokeWidth(), 1));
            index++;
            outLinePath.getElements().add(new LineTo(event.getX(), event.getY()));
        }
        else if(event.getEventType() == MouseEvent.MOUSE_RELEASED)
        {
                /*
        Tests conclude that the brush thickness be no more than 5 * pixel width and the annotate thickness be about 5 * brush_thickness
         */
            rectify(points, true);
            drawFromList(devdata.getCoord_list());
            annotationToolApplication.getRoot().getChildren().remove(outLinePath);

            arr = new Point[100000];
            index = 0;
        }


    }
    /* Rectify the line. */
    void rectify(ArrayList<AnnotatePoint> coord_list, boolean closed_path) {

        double tollerance = RECTIFY_THICKNESS_FACTOR * annotationToolApplication.getStrokeWidth();
        ArrayList<AnnotatePoint> broken_list = broken.broken(coord_list, closed_path, true, tollerance);
        devdata.setCoord_list(broken_list);
    }

    /**
     * This method adds a polygon to the picture stage based on a series of points.
     * The first point is the first point of the polygon. Each successive point has a line drawn from the
     * last point. The last point then gets connected to the first point.
     * @param points The list of points used to create the polygon.
     */
    private void drawFromList(ArrayList<AnnotatePoint> points)
    {
        Polygon polygon = new Polygon();
        for(AnnotatePoint point : points)
        {
            polygon.getPoints().addAll(point.getX(), point.getY());
        }
        polygon.setStroke(annotationToolApplication.getPaint());
        polygon.setStrokeWidth(annotationToolApplication.getStrokeWidth());
        polygon.setFill(null);
        annotationToolApplication.commitChange(new AddShape(polygon));
        annotationToolApplication.addLeaderToFollower(polygon);
        uuid = UUID.randomUUID();
        Custom_Shape.setUpUUIDMaps(polygon, uuid);
        ArrayList<TransferableShapes.Point> transferablePoints = new ArrayList<>();
        for(AnnotatePoint annotatePoint : points)
        {
            transferablePoints.add(new TransferableShapes.Point(String .valueOf(annotatePoint.getX()),String.valueOf(annotatePoint.getY())));
        }
        Custom_Shape custom_shape = new Custom_Shape(uuid, Custom_Shape.RECTIFICATION_STRING,
                new TransferableShapes.Point(String.valueOf(polygon.getLayoutX()), String.valueOf(polygon.getLayoutY())),
                (Color) annotationToolApplication.getPaint(), String .valueOf(annotationToolApplication.getStrokeWidth()), transferablePoints);
        try {
            annotationToolApplication.writeJSON(custom_shape);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

