package TransferableShapes;

import changeItem.AddShape;
import changeItem.ChangeItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Research on 7/18/2017.
 */
public class Custom_EraserPath extends Custom_Shape {
    private Point location;
    private String strokeWidth = "";
    private String radius = "";
    private String colorString = "";
    private ArrayList<Point> points;


    public Custom_EraserPath() {
        setTimestamp();
    }

    public Custom_EraserPath(UUID uuid, String type, ArrayList<Point> points) {
        setTimestamp();
        this.uuid = uuid;
        this.type = type;
        this.strokeWidth = String.valueOf(strokeWidth);
        this.points = points;
    }

    @Override
    public ChangeItem toChangeItem() {
        Path path = toUncoloredPath();
        path.setStroke(Color.valueOf(colorString));
        return new AddShape(path);
    }

    private Path toUncoloredPath() {
        Path path = new Path();
        path.getElements().add(new MoveTo(Double.valueOf(points.get(0).getX()), Double.valueOf(points.get(0).getY())));
        int size = points.size();
        for (int i = 1; i < size; i++) {
            path.getElements().add(new LineTo(Double.valueOf(points.get(i).getX()), Double.valueOf(points.get(i).getY())));
        }
        path.setStrokeWidth(Double.valueOf(strokeWidth));
        return path;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(String strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }
}
