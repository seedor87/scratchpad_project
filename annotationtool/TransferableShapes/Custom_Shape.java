package TransferableShapes;

import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.paint.Color;

/**
 * Created by Research on 7/11/2017.
 */
public class Custom_Shape {
    private int uuid;
    private String type;
    private Point location = null;
    private double strokeWidth = 0;
    private ArrayList<Point> points = null;
    private double radius = 0;
    private Point start = null;
    private Point end = null;
    private String string = null;
    private Font font = null;
    private String colorString = null;

    public Custom_Shape(int uuid, String type) {
        this.uuid = uuid;
        this.type = type;
    }

    //path
    public Custom_Shape(int uuid, String type, Point location, Color color, double strokeWidth, ArrayList<Point> points) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString  = color.toString();
        this.strokeWidth = strokeWidth;
        this.points = points;
    }

    //circle
    public Custom_Shape(int uuid, String type, Point location, Color color, double strokeWidth, double radius) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString  = color.toString();
        this.strokeWidth = strokeWidth;
        this.radius = radius;
    }

    //Arrow
    public Custom_Shape(int uuid, String type, Color color, double strokeWidth, Point start, Point end) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString  = color.toString();
        this.strokeWidth = strokeWidth;
        this.radius = radius;
    }

    //text
    public Custom_Shape(int uuid, String type, Point location, Color color, String string, Font font) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        colorString = color.toString();
        this.string = string;
        this.font = font;
    }
    public Custom_Shape()
    {

    }


    //earse shape
    public Custom_Shape(int uuid, String type, ArrayList<Point> points) {
        this.uuid = uuid;
        this.type = type;
        this.points = points;
    }

    //edit text
    public Custom_Shape(int uuid, String type, String string, Font font) {
        this.uuid = uuid;
        this.type = type;
        this.string = string;
        this.font = font;
    }

    public Custom_Shape(String type) {
        this.type = type;
    }


    public Custom_Shape readJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("shape.json"), Custom_Shape.class);
    }


    public void writeJSON(Custom_Shape shape) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(shape);
        if (uuid == 0) {

        } else {

            try {
                Files.write(new File("shape.json").toPath(), Arrays.asList(json), StandardOpenOption.APPEND);
            }
            catch (IOException e)
            {
                Files.write(new File("shape.json").toPath(), Arrays.asList(json), StandardOpenOption.CREATE);
            }
        }


        //  mapper.writeValue(new File("shape.json"), shape);
    }


    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
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

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
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

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
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
