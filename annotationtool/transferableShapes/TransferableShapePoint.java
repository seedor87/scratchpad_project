package transferableShapes;

/**
 * Created by Research on 7/11/2017.
 */
public class TransferableShapePoint {
    private String x;
    private String y;

    public TransferableShapePoint(String x, String y) {
        this.x = x;

        this.y = y;
    }
    public TransferableShapePoint()
    {

    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
