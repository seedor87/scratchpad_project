package TransferableShapes;

import changeItem.AddShape;
import changeItem.ChangeItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.util.UUID;

/**
 * Created by Research on 7/18/2017.
 */
public class Custom_Arrow extends Custom_Shape {
    private Point location = new Point("", "");
    private String strokeWidth = "";
    private String radius = "";
    private String colorString = "";
    private Point start;
    private Point end;

    public Custom_Arrow() {
        setTimestamp();
    }

    public Custom_Arrow(UUID uuid, String type, Color color, double strokeWidth, Point start, Point end) {
        setTimestamp();
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString = color.toString();
        this.strokeWidth = String.valueOf(strokeWidth);
        this.start = start;
        this.end = end;
    }


    @Override
    public ChangeItem toChangeItem() {
        Line line = new Line(Double.valueOf(start.getX()), Double.valueOf(start.getY()), Double.valueOf(end.getX()), Double.valueOf(end.getY()));
        line.setStroke(Color.valueOf(colorString));
        line.setStrokeWidth(Double.valueOf(strokeWidth));
        return new AddShape(addArrowToEndOfLine(line));
    }


    private Shape addArrowToEndOfLine(Line line) {
        final double halfBaseDistance = 2;
        final double heightDistance = 4;
        double slope;
        double xDistance = line.getEndX() - line.getStartX();
        double yDistance = line.getEndY() - line.getStartY();
        if (line.getEndY() == line.getStartY()) {
            slope = Double.POSITIVE_INFINITY;
        } else {
            slope = xDistance - yDistance;
        }
        Polygon triangle = new Polygon();
        double strokeWidth = Double.valueOf(this.strokeWidth);
        if (slope == Double.POSITIVE_INFINITY)//straight upwards line.//TODO check which direction. //DO I really need this check/part?
        {
            //System.out.println("Thing");
            triangle.getPoints().addAll((line.getEndX() - halfBaseDistance * strokeWidth), line.getEndY());
            triangle.getPoints().addAll((line.getEndX() + halfBaseDistance * strokeWidth), line.getEndY());
            triangle.getPoints().addAll(line.getEndX(), (line.getEndY() - heightDistance * strokeWidth));
        } else {
            //point 1
            triangle.getPoints().
                    addAll((line.getEndX() - (halfBaseDistance * strokeWidth * Math.sin(Math.atan2(yDistance, xDistance)))),
                            line.getEndY() + (halfBaseDistance * strokeWidth * Math.cos(Math.atan2(yDistance, xDistance))));
            //point 2
            triangle.getPoints().
                    addAll((line.getEndX() + (halfBaseDistance * strokeWidth * Math.sin(Math.atan2(yDistance, xDistance)))),
                            line.getEndY() - (halfBaseDistance * strokeWidth * Math.cos(Math.atan2(yDistance, xDistance))));
            //triangle.getPoints().addAll( (mouseEvent.getX() + 2*strokeWidth), mouseEvent.getY());
            //point 3
            triangle.getPoints().
                    addAll(line.getEndX() + strokeWidth * heightDistance * Math.cos(Math.atan2(yDistance, xDistance)),
                            line.getEndY() + (strokeWidth * heightDistance * Math.sin(Math.atan2(yDistance, xDistance))));
        }
        Shape newShape = Shape.union(triangle, line);
        newShape.setFill(line.getStroke());
        return newShape;
    }
}
