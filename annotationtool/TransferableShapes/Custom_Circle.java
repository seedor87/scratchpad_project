package TransferableShapes;

import changeItem.AddShape;
import changeItem.ChangeItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.UUID;

/**
 * Created by Research on 7/18/2017.
 */
public class Custom_Circle extends Custom_Shape {
    private Point location = new Point("", "");
    private String strokeWidth = "";
    private String radius = "";
    private String colorString = "";


    public Custom_Circle() {
        setTimestamp();
    }

    public Custom_Circle(UUID uuid, String type) {
        setTimestamp();
        this.uuid = uuid;
        this.type = type;
    }

    public Custom_Circle(UUID uuid, String type, Color color, double strokeWidth, double radius) {
        setTimestamp();
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString = color.toString();
        this.strokeWidth = String.valueOf(strokeWidth);
        this.radius = String.valueOf(radius);

    }


    @Override
    public ChangeItem toChangeItem() {
        double xLoc = Double.valueOf(location.getX());
        double yLoc = Double.valueOf(location.getY());
        double strokeWidth = Double.valueOf(this.getStrokeWidth());
        Color color = Color.valueOf(this.getColorString());
        double radius = Double.valueOf(this.getRadius());
        Circle circle = new Circle(xLoc, yLoc, radius, color);
        circle.setStroke(color);
        circle.setStrokeWidth(strokeWidth);
        Shape newCircle = Shape.subtract(circle, new Circle(xLoc, yLoc, radius - (strokeWidth / 2)));
        newCircle.setFill(color);
        return new AddShape(newCircle);
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


}
