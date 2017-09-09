package saveItems;

import changeItem.ChangeItem;

/**
 * Created by remem on 7/6/2017.
 */
public class SavedChange extends SaveItem
{
    private ChangeItem changeItem;
    public SavedChange(ChangeItem changeItem)
    {
        super();
        this.changeItem = changeItem;
    }
    public ChangeItem getChangeItem()
    {
        return this.changeItem;
    }
}
