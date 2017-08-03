package rectify;

import rectify.AnnotatePoint;

import java.util.ArrayList;

/**
 * Created by Bob S on 6/14/2017.
 */
public class Devdata {

    public boolean debug;
    ArrayList<AnnotatePoint> coord_list;

    public
    Devdata(boolean debug, ArrayList<AnnotatePoint> coord_list) {
        this.debug = debug;
        this.coord_list = coord_list;
    }

    public
    void
    setCoord_list(ArrayList<AnnotatePoint> coord_list) {
        this.coord_list = new ArrayList<AnnotatePoint>(coord_list);
    }

    public
    ArrayList<AnnotatePoint>
    getCoord_list() {
        return this.coord_list;
    }

    public
    String
    toString() {
        return String.format("coord_list: %s", this.coord_list.toString());
    }
}