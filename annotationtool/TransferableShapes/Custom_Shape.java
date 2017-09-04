package TransferableShapes;

import changeItem.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


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
    public static final String RECTIFICATION_STRING = "rectified shape";
    public static final String RECTANGLE_STRING = "rectangle";
    public static final String OVAL_STRING = "oval";
    public static final String LINE_STRING = "line";
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
    private String widthString = "";
    private String heightString = "";


    public static void changeShape(Shape oldShape, Shape newShape)
    {
        UUID uuid = shapesToUUIDMap.get(oldShape);
        setUpUUIDMaps(newShape, uuid);
    }

    /**
     * Constructor that takes in a unique id as well as a String.
     * @param uuid
     * @param type
     */
    public Custom_Shape(UUID uuid, String type) {
        this();
        this.uuid = uuid;
        this.type = type;
    }
    //rectangle, oval

    /**
     * Constructor that should mostly be used for rectangles and ovals.
     * @param uuid The unique id for this custom shape.
     * @param type The type of shape that this this custom shape should represent.
     * @param location The location of this custom shape.
     * @param color The color of this shape.
     * @param width The width of the rectangle or oval that this custom shape represents.
     * @param height The height of the rectangle or oval that this custom shape represents.
     * @param strokeWidth The width of the stroke that this rectangle or oval has.
     */
    public Custom_Shape(UUID uuid, String type, Point location, Paint color, double width, double height, double strokeWidth)
    {
        this(uuid, type);
        this.location = location;
        colorString = color.toString();
        this.widthString = String.valueOf(width);
        this.heightString = String.valueOf(height);
        this.strokeWidth = String .valueOf(strokeWidth);
    }

    //path, rectified shape

    /**
     * A custom shape that should be used to represent a path or a polygon made by a rectified shape.
     * @param uuid The uuid for this custom shape.
     * @param type The string that represents the type of this custom shape.
     * @param location The location of the path or rectified shape.
     * @param color The color of the path or rectified shape.
     * @param strokeWidth The width of the stroke for the path or rectified shape.
     * @param points The points that make up the path or rectified shape represented by this custom shape.
     */
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

    //Arrow, line

    /**
     * A custom shape that should be used to represent an arrow or line.
     * @param uuid The unique id of the shape represented by this custom shape.
     * @param type The string that represents the shape that this custom shape represents.
     * @param color The color of the shape represented by this custom shape.
     * @param strokeWidth The width of the arrow or line that this custom shape represents.
     * @param start the starting point of the arrow or line that this custom shape represents.
     * @param end the ending point of the arrow or line that this custom shape represents.
     */
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

    /**
     * A custom shape that should be used to represent an erased shape.
     * @param uuid The unique id for the shape represented by this custom shape.
     * @param type The string that represents the type of the shape that this custom shape represents.
     * @param points the points of the path area that should get erased by the erase shape that this custom shape represents.
     */
    public Custom_Shape(UUID uuid, String type, ArrayList<Point> points) {
        this(uuid, type);

        this.points = points;
    }

    //edit text

    /**
     * A custom shape that should be used to represent an EditText.
     * @param uuid The unique id attached to the shape that is represented by this custom shape.
     * @param type The string that represents the type of shape that this custom shape represents.
     * @param string The string that is used for the new string value in the EditText represented by this custom shape.
     * @param font The font change that is represented by this EditText.
     */
    public Custom_Shape(UUID uuid, String type, String string, String font) {
        this(uuid, type);

        this.string = string;
        this.font = font;
    }

    /**
     * The generic custom shape constructor.
     * @param type The string that represents the type of this custom shape.
     */
    public Custom_Shape(String type) {
        timestamp = getTimestamp();
        this.type = type;
    }

    //MoveShape

    /**
     * The constructor for a custom shape that represents a MoveShape object.
     * @param type the string that represents the type of the shape that this custom shape represents.
     * @param movedShape The moved shape that this custom shape represents.
     */
    public Custom_Shape(String type, Shape movedShape)
    {
        uuid = shapesToUUIDMap.get(movedShape);
        System.out.println(uuid);
        this.type = type;
        location = new Point(movedShape.getLayoutX() +"", movedShape.getLayoutY() +"");
    }
    //EditText

    /**
     * A custom shape that represents an EditText object.
     * @param editText The EditText object that is to be turned into a custom shape.
     */
    public Custom_Shape(EditText editText)
    {
        Text text = editText.getText();
        uuid = shapesToUUIDMap.get(text);
        this.type = Custom_Shape.EDIT_TEXT_STRING;
        this.string = text.getText();
    }

    /**
     * Gets an uncolored path from the values of the custom shape. The custom shape's type should be one
     * that should contain values that you can obtain a path from.
     * @return the path created.
     */
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

    /**
     * Gets a circle from the values of this custom shape. The custom shape's type should be that of a circle.
     * @return the circle created.
     */
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

    /**
     * Creates a path from the values inside of this custom shape. The custom shape's type should be one
     * that should return a path.
     * @return the path created.
     */
    private Shape toPath() {
        Path path = toUncoloredPath();
        path.setStroke(Color.valueOf(colorString));
        setUpUUIDMaps(path, uuid);
        return path;
    }

    /**
     * Creates an arrow from the values inside of this custom shape. The custom shape's type should be one
     * that should return an arrow.
     * @return the arrow created.
     */
    private Shape toArrow() {
        Line line = new Line(Double.valueOf(start.getX()), Double.valueOf(start.getY()), Double.valueOf(end.getX()), Double.valueOf(end.getY()));
        line.setStroke(Color.valueOf(colorString));
        line.setStrokeWidth(Double.valueOf(strokeWidth));
        Shape arrow = addArrowToEndOfLine(line);
        setUpUUIDMaps(arrow, uuid);
        return arrow;
    }

    /**
     * adds an arrow (triangle) to the end of a line.
     * @param line
     * @return the arrow created.
     */
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

    /**
     * Creates a change item from the values inside of this custom shape. The type of this custom shape should not be undo or redo
     * @return the changeitem created from the values inside of this custom shape.
     */
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
            case RECTIFICATION_STRING:
                return toPolygon();
            case RECTANGLE_STRING:
                return toRectangle();
            case OVAL_STRING:
                return toOval();
            case LINE_STRING:
                return toLine();
            default:
                return null;    //TODO change this to throw a custom exception
        }
    }

    /**
     * Creates a line from the values inside of this custom shape.
     * @return the line that is created. 
     */
    private ChangeItem toLine()
    {
        Line line = new Line(Double.valueOf(start.getX()), Double.valueOf(start.getY()),
                            Double.valueOf(end.getX()), Double.valueOf(end.getY()));
        line.setStroke(Color.valueOf(colorString));
        line.setStrokeWidth(Double.valueOf(strokeWidth));
        setUpUUIDMaps(line,uuid);
        return new AddShape(line);
    }

    private ChangeItem toOval()
    {
        Rectangle rectangle = new Rectangle(Double.valueOf(location.getX()), Double.valueOf(location.getY()),
                Double.valueOf(widthString), Double.valueOf(heightString));
        rectangle.setFill(null);
        rectangle.setStroke(Color.valueOf(colorString));
        rectangle.setStrokeWidth(Double.valueOf(strokeWidth));
        rectangle.setArcHeight(rectangle.getHeight());
        rectangle.setArcWidth(rectangle.getWidth());
        setUpUUIDMaps(rectangle,uuid);
        return new AddShape(rectangle);
    }

    private ChangeItem toRectangle()
    {
        Rectangle rectangle = new Rectangle(Double.valueOf(location.getX()), Double.valueOf(location.getY()),
                                Double.valueOf(widthString), Double.valueOf(heightString));
        rectangle.setFill(null);
        rectangle.setStroke(Color.valueOf(colorString));
        rectangle.setStrokeWidth(Double.valueOf(strokeWidth));
        setUpUUIDMaps(rectangle,uuid);
        return new AddShape(rectangle);
    }

    private ChangeItem toPolygon()
    {
        Polygon polygon = new Polygon();
        for(Point point : points)
        {
            polygon.getPoints().addAll(Double.valueOf(point.getX()), Double.valueOf(point.getY()));
        }
        polygon.setStrokeWidth(Double.valueOf(strokeWidth));
        polygon.setStroke(Color.valueOf(colorString));
        polygon.setFill(null);
        setUpUUIDMaps(polygon, uuid);
        return new AddShape(polygon);
        //TODO test
    }
//    setUpUUIDMaps(newCircle, uuid);
//        return newCircle;

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

    public String getWidthString()
    {
        return widthString;
    }
    public void setWidthString(String widthString)
    {
        this.widthString = widthString;
    }
    public String getHeightString()
    {
        return heightString;
    }
    public void setHeightString(String heightString)
    {
        this.heightString = heightString;
    }

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
