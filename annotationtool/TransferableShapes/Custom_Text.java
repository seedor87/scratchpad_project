package TransferableShapes;

import changeItem.AddShape;
import changeItem.ChangeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.UUID;

/**
 * Created by Research on 7/18/2017.
 */
public class Custom_Text extends Custom_Shape {
    String font;
    private Point location = new Point("", "");
    private String strokeWidth = "";
    private String radius = "";
    private String colorString = "";
    private String textSize = "";
    private String string;

    public Custom_Text() {
        setTimestamp();
    }

    public Custom_Text(UUID uuid, String type, String string, String font) {
        setTimestamp();
        this.uuid = uuid;
        this.type = type;
        this.strokeWidth = String.valueOf(strokeWidth);
        this.radius = String.valueOf(radius);
        this.string = string;
        this.font = font;


    }


    public Custom_Text(UUID uuid, String type, Point location, Color color, String string, String font) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        this.colorString = color.toString();
        this.strokeWidth = String.valueOf(strokeWidth);
        this.radius = String.valueOf(radius);
        this.string = string;
        this.font = font;


    }

    @Override
    public ChangeItem toChangeItem() {
        Text text = new Text(this.string);
        text.setFont(new Font(font, Double.valueOf(textSize)));
        text.setFill(Color.valueOf(colorString));
        text.setX(Double.valueOf(location.getX()));
        text.setY(Double.valueOf(location.getY()));
        text.setStroke(Color.valueOf(colorString));
        return new AddShape(text);
    }

    public String getTextSize() {
        return textSize;
    }

    public void setTextSize(String textSize) {
        this.textSize = textSize;
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

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }
}