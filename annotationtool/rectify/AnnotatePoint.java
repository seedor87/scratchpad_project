package rectify;
/**
 * Created by Bob S on 6/6/2017.
 */
public class AnnotatePoint {

    private double x;
    private double y;
    private double width;
    private double pressure;

    public AnnotatePoint(double x, double y, double width, double pressure)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.pressure = pressure;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public String toString() {
        return String.format("  { X: %s, Y: %s, Width: %s, Press: %s }  ", this.x, this.y, this.width, this.pressure);
    }
}