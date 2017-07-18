package TransferableShapes;
import changeItem.ChangeItem;
import java.util.UUID;

/**
 * Created by Research on 7/11/2017.
 */
public abstract class Custom_Shape {
    public static final String CIRCLE_STRING = "circle";
    public static final String PATH_STRING = "path";
    public static final String ERASE_STRING = "erase";
    public static final String ARROW_STRING = "arrow";
    public static final String TEXT_STRING = "text";
    protected String timestamp;
    protected UUID uuid;
    protected String type;



    public Custom_Shape(UUID uuid, String type) {
        setTimestamp();
        this.uuid = uuid;
        this.type = type;
    }


    public Custom_Shape()
    {
        setTimestamp();

    }


    public Custom_Shape(String type) {
        setTimestamp();
        this.type = type;
    }


    public abstract ChangeItem toChangeItem(); //TODO throw exception if the type is undo or redo.


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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp() {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000L);
    }
}
