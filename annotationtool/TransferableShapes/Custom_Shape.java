package TransferableShapes;

import changeItem.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.scene.paint.Color;

/**
 * Created by Research on 7/11/2017.
 */
public class Custom_Shape {
    public static final String CIRCLE_STRING = "circle";
    public static final String PATH_STRING = "path";
    public static final String ERASE_STRING = "erase";
    public static final String ARROW_STRING = "arrow";
    public static final String TEXT_STRING = "text";
    public static final String REDO_STRING = "redo";
    public static final String UNDO_STRING = "undo";
    public static final String MOVE_SHAPE_STRING = "move shape";
    public static final String EDIT_TEXT_STRING = "edit text";
    private static HashMap<UUID, Shape> addedShapes = new HashMap<>();
    private static Map<Shape, UUID> shapesToUUIDMap = new HashMap<>();
    private String timestamp = "";
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
    private String textSize = "";
    private String colorString = "";


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

    public static void changeShape(Shape oldShape, Shape newShape)
    {
        UUID uuid = shapesToUUIDMap.get(oldShape);
        setUpUUIDMaps(newShape, uuid);
    }
    public Custom_Shape(UUID uuid, String type) {
        this();
        this.uuid = uuid;
        this.type = type;
    }

    //path
    public Custom_Shape(UUID uuid, String type, Point location, Color color, String strokeWidth, ArrayList<Point> points) {
        this(uuid, type);
        this.location = location;
        colorString = color.toString();
        this.strokeWidth = strokeWidth;
        this.points = points;
    }

    //circle , text
    public Custom_Shape(UUID uuid, String type, Point location, Color color, String stroke_string, String radius_font) {
        this(uuid, type);
        this.location = location;
        colorString = color.toString();

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
        this(uuid, type);

        this.location = location;
        colorString = color.toString();
        this.strokeWidth = strokeWidth;
        this.radius = radius;
        this.start = start;
        this.end = end;
    }

    public Custom_Shape()
    {
        timestamp = getTimestamp();
    }

    //earse shape
    public Custom_Shape(UUID uuid, String type, ArrayList<Point> points) {
        this(uuid, type);

        this.points = points;
    }

    //edit text
    public Custom_Shape(UUID uuid, String type, String string, String font) {
        this(uuid, type);

        this.string = string;
        this.font = font;
    }


    public Custom_Shape(String type) {
        timestamp = getTimestamp();
        this.type = type;
    }

    //MoveShape
    public Custom_Shape(String type, Shape movedShape)
    {
        uuid = shapesToUUIDMap.get(movedShape);
        System.out.println(uuid);
        this.type = type;
        location = new Point(movedShape.getLayoutX() +"", movedShape.getLayoutY() +"");
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

    private Shape toCircle() {
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
        setUpUUIDMaps(newCircle, uuid);
        return newCircle;
    }

    private Shape toPath() {
        Path path = toUncoloredPath();
        path.setStroke(Color.valueOf(colorString));
        setUpUUIDMaps(path, uuid);
        return path;
    }

    private Shape toArrow() {
        Line line = new Line(Double.valueOf(start.getX()), Double.valueOf(start.getY()), Double.valueOf(end.getX()), Double.valueOf(end.getY()));
        line.setStroke(Color.valueOf(colorString));
        line.setStrokeWidth(Double.valueOf(strokeWidth));
        Shape arrow = addArrowToEndOfLine(line);
        setUpUUIDMaps(arrow, uuid);
        return arrow;
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

    public ChangeItem toChangeItem()//TODO throw exception if the type is undo or redo.
    {
        switch (type)
        {
            case PATH_STRING:
                return new AddShape(toPath());
            case ERASE_STRING:
                return new EraseShape(toUncoloredPath());
            case ARROW_STRING:
                return new AddShape(toArrow());
            case CIRCLE_STRING:
                return new AddShape(toCircle());
            case TEXT_STRING:
                return new AddShape(toText());
            case MOVE_SHAPE_STRING:
                return toMoveShape();
            case EDIT_TEXT_STRING:
                return toEditText();
            default:
                return null;    //TODO change this to throw a custom exception
        }
    }
    private MoveShape toMoveShape()
    {
        Shape movedShape = addedShapes.get(uuid);
        double newX = Double.valueOf(location.getX());
        double newY = Double.valueOf(location.getY());
        return new MoveShape(newX, newY, movedShape);
    }//TODO this does the input for the above and below method, but now the output needs to be done.
    private EditText toEditText()
    {
        Text editedText = (Text) addedShapes.get(uuid);
        return new EditText(editedText, string);
    }
    public static void setUpUUIDMaps(Shape shape, UUID uuid)
    {
        addedShapes.put(uuid, shape);
        shapesToUUIDMap.put(shape, uuid);
    }
//    public boolean isAddShape()
//    {
//        if(type.equals(PATH_STRING) || type.equals(CIRCLE_STRING) || type.equals(ARROW_STRING) || type.equals(TEXT_STRING))
//        {
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//    }

    private Text toText()
    {
        Text text = new Text(this.string);
        text.setFont(new Font(font, Double.valueOf(textSize)));
        text.setFill(Color.valueOf(colorString));
        text.setX(Double.valueOf(location.getX()));
        text.setY(Double.valueOf(location.getY()));
        text.setStroke(Color.valueOf(colorString));
        setUpUUIDMaps(text, uuid);
        return text;
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
    public String toString()
    {
        return type;
    }


    public String getTimestamp() {
        Long temp = System.currentTimeMillis() / 1000L;
        String timeStamp = temp.toString();
        return timeStamp; 
    }


    public String getTextSize() {
        return textSize;
    }

    public void setTextSize(String textSize) {
        this.textSize = textSize;
    }

}
