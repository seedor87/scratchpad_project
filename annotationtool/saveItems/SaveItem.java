package saveItems;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by remem on 7/6/2017.
 */
public abstract class SaveItem implements Serializable
{
    Timestamp timestamp;
    public SaveItem()
    {
        timestamp = new Timestamp(new Date().getTime());
    }
}
