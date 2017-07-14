package TransferableShapes;

import changeItem.AddShape;
import changeItem.ChangeItem;
import changeItem.EraseShape;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javafx.scene.paint.Color;

/**
 * Created by Research on 7/11/2017.
 */
public class Custom_Shape {
    private UUID uuid;
    private String type;
    private Point location = new Point("", "");
    private String strokeWidth = "";
    private ArrayList<Point> points = new ArrayList<Point>();
    private String radius = "";
    private Point start = new Point("", "");
    private Point end = new Point("", "");
    private String string = "";
    private String font = "";
    private String colorString = "";
    public static final String CIRCLE_STRING = "circle";
    public static final String PATH_STRING = "path";
    public static final String ERASE_STRING = "erase";
    public static final String ARROW_STRING = "arrow";
    public static final String TEXT_STRING = "text";

//    public Shape toShape() // TODO throws some custom exception if can not be made into a shape.
//    {
//        if(this.getType().equals("circle"))
//        {
//            return toCircle();
//        }
//        else if (this.getType().equals("path"))
//        {
//            return toPath();
//        }
//        else if(this.getType().equals(Custom_Shape.TEXT_STRING))
//        {
//            //TODO this
//            return null;
//        }
//        else
//        {
//            return null;    //TODO replace with custom exception
//        }
//    }

    private Path toUncoloredPath()
    {
        Path path = new Path();
        path.getElements().add(new MoveTo(Double.valueOf(points.get(0).getX()), Double.valueOf(points.get(0).getY())));
        int size = points.size();
        for(int i = 1;i < size;i++)
        {
            path.getElements().add(new LineTo(Double.valueOf(points.get(i).getX()), Double.valueOf(points.get(i).getY())));
        }
        path.setStrokeWidth(Double.valueOf(strokeWidth));
        return path;
    }
    private Shape toCircle()
    {
        double xLoc = Double.valueOf(location.getX());
        double yLoc = Double.valueOf(location.getY());
        double strokeWidth = Double.valueOf(this.getStrokeWidth());
        Color color = Color.valueOf(this.getColorString());
        double radius = Double.valueOf(this.getRadius());
        Circle circle = new Circle(xLoc,yLoc,radius,color);
        circle.setStroke(color);
        circle.setStrokeWidth(strokeWidth);
        Shape newCircle = Shape.subtract(circle, new Circle(xLoc,yLoc,radius - (strokeWidth/2)));
        newCircle.setFill(color);
        return newCircle;
    }
    private Shape toPath()
    {
        Path path = toUncoloredPath();
        path.setStroke(Color.valueOf(colorString));
        return path;
    }
    private Shape toArrow()
    {
        //TODO this
        return null;
    }

    public ChangeItem toChangeItem()//TODO throw exception if the type is undo or redo.
    {
        switch (type)
        {
            case ERASE_STRING:
                return new EraseShape(toUncoloredPath());
            case ARROW_STRING:
                return new AddShape(toArrow());
            case CIRCLE_STRING:
                return new AddShape(toCircle());
            case TEXT_STRING:
                return new AddShape(toText());
            default:
                return null;    //TODO change this to throw a custom exception
        }
    }

    private Text toText()
    {
        return null;
        //TODO this
    }


    public Custom_Shape(UUID uuid, String type) {
        this.uuid = uuid;
        this.type = type;
    }

    //path
    public Custom_Shape(UUID uuid, String type, Point location, Color color, String strokeWidth, ArrayList<Point> points) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString  = color.toString();
        this.strokeWidth = strokeWidth;
        this.points = points;
    }

    //circle , text
    public Custom_Shape(UUID uuid, String type, Point location, Color color, String stroke_string, String radius_font) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString  = color.toString();

        if (type.equals("circle")) {
            this.strokeWidth = stroke_string;
            this.radius = radius_font;
        } else {
            this.string = stroke_string;
            this.font = radius_font;
        }
    }

    //Arrow
    public Custom_Shape(UUID uuid, String type, Color color, String strokeWidth, Point start, Point end) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString  = color.toString();
        this.strokeWidth = strokeWidth;
        this.radius = radius;
    }


    public Custom_Shape()
    {

    }


    //earse shape
    public Custom_Shape(UUID uuid, String type, ArrayList<Point> points) {
        this.uuid = uuid;
        this.type = type;
        this.points = points;
    }

    //edit text
    public Custom_Shape(UUID uuid, String type, String string, String font) {
        this.uuid = uuid;
        this.type = type;
        this.string = string;
        this.font = font;
    }

    public Custom_Shape(String type) {
        this.type = type;
    }


   /* public Custom_Shape readJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("shape.json"), Custom_Shape.class);
    }


    public void writeJSON(Custom_Shape shape) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(shape);
        //   if (uuid == 0) {

        // } else {

            try {
                Files.write(new File("shape.json").toPath(), Arrays.asList(json), StandardOpenOption.APPEND);
            }
            catch (IOException e)
            {
                Files.write(new File("shape.json").toPath(), Arrays.asList(json), StandardOpenOption.CREATE);
            }
        // }


        //  mapper.writeValue(new File("shape.json"), shape);
    }*/


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

//    public Color getColor() {
//        return Color.valueOf(colorString);
//    }

//    public void setColor(Color color) {
//        colorString = color.toString();
//    }

    public String getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(String strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
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

    public String getColorString()
    {
        return colorString;
    }
    public void setColorString(String colorString)
    {
        this.colorString = colorString;
    }
}
