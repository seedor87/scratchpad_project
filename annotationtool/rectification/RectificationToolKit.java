package rectification;

import annotation.AnnotationPoint;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;

/**
 Created by Bob S on 6/5/2017.

 This code has been adopted and modified from the source of the Ardesia program.
 https://github.com/gfreeau/ardesia/blob/master/src/broken.c

 The functions contained within are used in the re-representation of shapes as more well defined polygons, circles and ovals.
 Used during rectification mode in the Annotation Tool, these tools are applied to warp the listed polygon points to make them more 'regular'
 */
public class RectificationToolKit {

    private static class PointTuple {
        public final double min_x, min_y, max_x, max_y;

        public PointTuple(double minx_x, double min_y, double max_x, double max_y) {
            this.min_x = minx_x;
            this.min_y = min_y;
            this.max_x = max_x;
            this.max_y = max_y;
        }
    }

    /**
     *  Number x is round-able to y.
     */
    private static boolean is_similar(double x, double y, double pixel_tollerance) {
        double delta = Math.abs(x - y);
        if (delta <= pixel_tollerance) {
            return true;
        }
        return false;
    }

    /**
     The list of point is round-able to a rectangle
     Note this algorithm found only the rectangle parallel to the axis.
     */
    static boolean is_a_rectangle(ArrayList<AnnotationPoint> list, double pixel_tollerance) {

        if (list.size() != 4) {
            return false;
        } else {
            AnnotationPoint point0 = list.get(0);
            AnnotationPoint point1 = list.get(1);
            AnnotationPoint point2 = list.get(2);
            AnnotationPoint point3 = list.get(3);
            if (!(is_similar(point0.getX(), point1.getX(), pixel_tollerance))) {
                return false;
            }
            if (!(is_similar(point1.getY(), point2.getY(), pixel_tollerance))) {
                return false;
            }
            if (!(is_similar(point2.getX(), point3.getX(), pixel_tollerance))) {
                return false;
            }
            if (!(is_similar(point3.getY(), point0.getY(), pixel_tollerance))) {
                return false;
            }
        }
        return true;
    }


    /**
     *  Calculate the media of the point pression.
     */
    private static double calculate_medium_pression(ArrayList<AnnotationPoint> list) {
        int i = 0;
        double total_pressure = 0;
        int length = list.size();

        for (i = 0; i < length; i++) {
            AnnotationPoint cur_point = list.get(i);
            total_pressure = total_pressure + cur_point.getPressure();
        }
        return total_pressure / i;
    }

    /**
     *  Take the list and find the min_x min_y max_x and max_y points.
     */
    private static PointTuple found_min_and_max(ArrayList<AnnotationPoint>  list) {

        int i = 0;
        /* Initialize the min and max to the first point coordinates */
        AnnotationPoint first_point = list.get(i);
        double min_x = first_point.getX();
        double min_y = first_point.getY();
        double max_x = first_point.getX();
        double max_y = first_point.getY();

        int length = list.size();
        /* Search the min and max coordinates */
        for (i = 1; i < length; i++) {
            AnnotationPoint cur_point = list.get(i);
            min_x = Math.min( min_x, cur_point.getX());
            min_y = Math.min( min_y, cur_point.getY());
            max_x = Math.max( max_x, cur_point.getX());
            max_y = Math.max( max_y, cur_point.getY());
        }
        return new PointTuple(min_x, min_y, max_x, max_y);
    }

    /**
     *  The path described in list is similar to a regular polygon.
     */
    private static boolean is_similar_to_a_regular_polygon(ArrayList<AnnotationPoint> list, double pixel_tollerance) {
        int i = 0;
        double ideal_distance = -1;
        double total_distance = 0;
        double distance, threshold;

        int length = list.size();
        AnnotationPoint old_point =   list.get(i);

        for (i = 1; i < length; i++) {
            AnnotationPoint point =   list.get(i);
            distance = get_distance(old_point.getX(), old_point.getY(), point.getX(), point.getY());
            total_distance = total_distance + distance;
            old_point = point;
        }

        ideal_distance = total_distance / length;
        i = 0;
        old_point =   list.get(i);

        threshold = ideal_distance / 3 + pixel_tollerance;

        for (i = 1; i < length; i++) {
            AnnotationPoint point =   list.get(i);
            /* I have seen that a good compromise allow around 33% of error. */
            distance = get_distance(point.getX(), point.getY(), old_point.getX(), old_point.getY());

            if (!(is_similar(distance, ideal_distance, threshold))) {
                return false;
            }
            old_point = point;
        }
        return true;
    }


    /**
     *  Take a path and return the regular polygon path. 
     */
    private static ArrayList<AnnotationPoint> extract_polygon(ArrayList<AnnotationPoint> list) {
        double cx, cy, radius, min_x, min_y, max_x, max_y, x1, y1, angle_step;
        double angle_off = Math.PI / 2;
        int i, length;
        AnnotationPoint last_point, first_point;

        PointTuple quad = found_min_and_max(list);
        min_x = quad.min_x;
        min_y = quad.min_y;
        max_x = quad.max_x;
        max_y = quad.max_y;

        cx = (max_x + min_x) / 2;
        cy = (max_y + min_y) / 2;
        radius = ((max_x - min_x) + (max_y - min_y)) / 4;
        length = list.size();
        angle_step = 2 * Math.PI / (length - 1);
        angle_off += angle_step / 2;

        for (i = 0; i < length - 1; i++) {
            AnnotationPoint point = list.get(i);
            x1 = radius * Math.cos(angle_off) + cx;
            y1 = radius * Math.sin(angle_off) + cy;
            point.setX(x1);
            point.setY(y1);
            angle_off += angle_step;
        }

        last_point = list.get(length - 1);
        first_point = list.get(0);
        last_point.setX(first_point.getX());
        last_point.setY(first_point.getY());

        return list;
    }

    /**
     *  Return the degree of the rectangle between two point respect the axis. 
     */
    private static double calculate_edge_degree(AnnotationPoint point_a, AnnotationPoint point_b) {
        double deltax = Math.abs(point_a.getX() - point_b.getX());
        double deltay = Math.abs(point_a.getY() - point_b.getY());
        double direction_ab = Math.atan2(deltay, deltax) / Math.PI * 180;
        return direction_ab;
    }

    /**
     *  Straighten the line. 
     */
    private static ArrayList<AnnotationPoint> straighten(ArrayList<AnnotationPoint> list) {
        AnnotationPoint inp_point, first_point, last_point, last_out_point;
        double degree_threshold = 15;
        ArrayList<AnnotationPoint> list_out = new ArrayList<AnnotationPoint>();
        int length, i;
        double direction;

        length = list.size();

        /* Copy the first one point; it is a good point. */
        inp_point =   list.get(0);
        first_point = new AnnotationPoint(inp_point.getX(), inp_point.getY(), inp_point.getWidth(), inp_point.getPressure());
        list_out.add(0, first_point);

        for (i = 0; i < length - 2; i++) {
            AnnotationPoint point_a =   list.get(i);
            AnnotationPoint point_b =   list.get(i + 1);
            AnnotationPoint point_c =   list.get(i + 2);
            double direction_ab = calculate_edge_degree(point_a, point_b);
            double direction_bc = calculate_edge_degree(point_b, point_c);
            double delta_degree = Math.abs(direction_ab - direction_bc);

            if (delta_degree > degree_threshold) {
                /* Copy B it's a good point. */
                AnnotationPoint point = new AnnotationPoint(point_b.getX(), point_b.getY(), point_b.getWidth(), point_b.getPressure());
                list_out.add(0, point);
            }
        /* Else: is three the difference degree is minor than the threshold I neglect B. */
        }

        /* Copy the last point; it is a good point. */
        last_point =   list.get(length - 1);
        last_out_point = new AnnotationPoint(last_point.getX(), last_point.getY(), last_point.getWidth(), last_point.getPressure());
        list_out.add(0, last_out_point);

        /* I reverse the list to preserve the initial order. */
        Collections.reverse(list_out);

        length = list_out.size();

        if (length != 2) {
            return list_out;
        }

        /* It is a segment! */
        direction = calculate_edge_degree(first_point, last_point);

        /* is it is closed to 0 degree I draw an horizontal line. */
        if ((0 - degree_threshold <= direction) && (direction <= 0 + degree_threshold)) {
            /* y is the average */
            double y = (first_point.getY() + last_point.getY()) / 2;
            /* Put this y for each element in the list. */
            for (AnnotationPoint p : list_out) {
                p.setY(y);
            }
        }

        /* It is closed to 90 degree I draw a vertical line. */
        if ((90 - degree_threshold <= direction) && (direction <= 90 + degree_threshold)) {
            /* x is the average */
            double x = (first_point.getX() + last_point.getX()) / 2;
            /* put this x for each element in the list. */
            for (AnnotationPoint p : list_out) {
                p.setX(x);
            }
        }
        return list_out;
    }


    /** Return a new list containing a sub-path of list_inp that contains
     the meaningful points using the standard deviation algorithm.
     */
    public static ArrayList<AnnotationPoint> build_meaningful_point_list(ArrayList<AnnotationPoint> list_inp, boolean rectify, double pixel_tollerance) {
        /* Initialize the list. */
        ArrayList<AnnotationPoint> list_out = new ArrayList<AnnotationPoint>();
        AnnotationPoint first_point, second_point, last_point;

        int length = list_inp.size();
        int i = 0;
        AnnotationPoint point_a =   list_inp.get(i);
        AnnotationPoint point_b =   list_inp.get(i + 1);
        AnnotationPoint point_c = null;
        double pressure = calculate_medium_pression(list_inp);

        double a_x = point_a.getX();
        double a_y = point_a.getY();
        double a_width = point_a.getWidth();

        double b_x = point_b.getX();
        double b_y = point_b.getY();
        double b_width = point_b.getWidth();

        double c_x = point_b.getX();
        double c_y = point_b.getY();
        double c_width = point_b.getWidth();

        first_point = new AnnotationPoint(a_x, a_y, a_width, pressure);
        /* add a point with the coordinates of point_a. */
        list_out.add(0, first_point);

        if (length == 2) {
            second_point = new AnnotationPoint(b_x, b_y, b_width, pressure);
            /* add a point with the coordinates of point_a. */
            list_out.add(0, second_point);
        } else {
            double area = 0.0;
            double h, x1, y1, x2, y2;
            last_point =   list_inp.get(length - 1);
            AnnotationPoint last_point_copy = null;

            for (i = i + 2; i < length; i++) {
                point_c =   list_inp.get(i);
                c_x = point_c.getX();
                c_y = point_c.getY();
                c_width = point_c.getWidth();

                x1 = b_x - a_x;
                y1 = b_y - a_y;
                x2 = c_x - a_x;
                y2 = c_y - a_y;

                area += (x1 * y2 - x2 * y1);

                h = (2 * area) / Math.sqrt(x2 * x2 + y2 * y2);

                if (Math.abs(h) >= (pixel_tollerance)) {
	                /* Add  a point with the B coordinates. */
                    AnnotationPoint new_point = new AnnotationPoint(b_x, b_y, b_width, pressure);
                    list_out.add(0, new_point);
                    area = 0.0;
                    a_x = b_x;
                    a_y = b_y;
                    a_width = b_width;
                }
	            /* Put to B the C coordinates. */
                b_x = c_x;
                b_y = c_y;
                b_width = c_width;
            }

            /* Add the last point with the coordinates. */
            last_point_copy = new AnnotationPoint(last_point.getX(), last_point.getY(), last_point.getWidth(), last_point.getPressure());
            list_out.add(0, last_point_copy);
        }

        /* I reverse the list to preserve the initial order. */
        Collections.reverse(list_out);
        return list_out;
    }


    /**
     *  Return the out-bounded rectangle outside the path described to list_in.
     */
    public static  ArrayList<AnnotationPoint> build_outbounded_rectangle(ArrayList<AnnotationPoint> list) {
        int length = list.size();
        AnnotationPoint point = list.get(length / 2);
        ArrayList<AnnotationPoint> ret_list = new ArrayList<AnnotationPoint>();

        double min_x, min_y, max_x, max_y;

        PointTuple quad = found_min_and_max(list);
        min_x = quad.min_x;
        min_y = quad.min_y;
        max_x = quad.max_x;
        max_y = quad.max_y;

        AnnotationPoint point3 = new AnnotationPoint(min_x, max_y, point.getWidth(), point.getPressure());
        ret_list.add(0, point3);
        AnnotationPoint point2 = new AnnotationPoint(max_x, max_y, point.getWidth(), point.getPressure());
        ret_list.add(0, point2);
        AnnotationPoint point1 = new AnnotationPoint(max_x, min_y, point.getWidth(), point.getPressure());
        ret_list.add(0, point1);
        AnnotationPoint point0 = new AnnotationPoint(min_x, min_y, point.getWidth(), point.getPressure());
        ret_list.add(0, point0);

        return ret_list;
    }

    /**
     *  Return a list rectified
     */
    private static ArrayList<AnnotationPoint> build_rectified_list(ArrayList<AnnotationPoint>  list_inp, boolean close_path, double pixel_tollerance) {
        ArrayList<AnnotationPoint> ret_list = new ArrayList<AnnotationPoint>();
        if (close_path) {

            int length = list_inp.size();
            int i = 0;

            /* Copy the input list */
            for (i = 0; i < length; i++) {
                AnnotationPoint point =   list_inp.get(i);
                AnnotationPoint point_copy = new AnnotationPoint(point.getX(), point.getY(), point.getWidth(), point.getPressure());
                ret_list.add(0, point_copy);
            }

            /* I reverse the list to preserve the initial order. */
            Collections.reverse(ret_list);

            /* jump the algorithm and return the list as is */
            if (ret_list.size() <= 3) {
                return ret_list;
            }

            /* It is similar to regular a polygon. */
            if (is_similar_to_a_regular_polygon(ret_list, pixel_tollerance)) {
                ret_list = extract_polygon(ret_list);
            } else {

                if (is_a_rectangle(ret_list, pixel_tollerance)) {
                    /* It is a rectangle. */
                    ArrayList rect_list = build_outbounded_rectangle(ret_list);
                    ret_list = rect_list;
                }
            }
        } else {
            /* Try to make straight */
            ret_list = straighten(list_inp);
        }
        return ret_list;
    }


    /**
     *  Take a list of point and return magically the new recognized path.
     */
    public static ArrayList<AnnotationPoint> broken(ArrayList<AnnotationPoint> list_inp, boolean close_path, boolean rectify, double pixel_tollerance) {

        // pixel tolerance must be at least 2.0 * the thickness of the brush (or up to 4.5x)
        // RectificationToolKit is true to apply this mode.
        ArrayList<AnnotationPoint> meaningful_point_list = build_meaningful_point_list(list_inp, close_path, pixel_tollerance);

        if (meaningful_point_list.size() > 0) {
            if (rectify) {
                ArrayList rectified_list = build_rectified_list(meaningful_point_list, close_path, pixel_tollerance);
                return rectified_list;
            }
        }
        return meaningful_point_list;
    }

    /**
     * Return net distance between two points (abs value of displacment)
     */
    public static double get_distance(double x1, double y1, double x2, double y2) {
        double x_delta = Math.abs(x2 - x1);
        double y_delta = Math.abs(y2 - y1);
        double quad_sum = Math.pow(x_delta, 2);
        quad_sum = quad_sum + Math.pow(y_delta, 2);
        return Math.sqrt(quad_sum);
    }
}