package rectify;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;

/**
 Created by Bob S on 6/5/2017.
 */
public class Broken {

    private static void print(Object o) {
        System.out.println(o.toString());
    }

    private static class Tuple {
        public final double minx, miny, maxx, maxy;

        public Tuple(double minx, double miny, double maxx, double maxy) {
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;
        }
    }

    /* Number x is roundable to y. */
    private static boolean is_similar(double x, double y, double pixel_tollerance) {
        double delta = Math.abs(x - y);
        if (delta <= pixel_tollerance) {
            return true;
        }
        return false;
    }

    /*
     The list of point is roundable to a rectangle
     Note this algorithm found only the rectangle parallel to the axis.
     */
    static boolean is_a_rectangle(ArrayList list, double pixel_tollerance) {

        if (list.size() != 4) {
            return false;
        } else {
            AnnotatePoint point0 = (AnnotatePoint) list.get(0);
            AnnotatePoint point1 = (AnnotatePoint) list.get(1);
            AnnotatePoint point2 = (AnnotatePoint) list.get(2);
            AnnotatePoint point3 = (AnnotatePoint) list.get(3);
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
        /* Postcondition: it is a rectangle. */
        return true;
    }


    /* Calculate the media of the point pression. */
    private static double calculate_medium_pression(ArrayList<AnnotatePoint> list) {
        int i = 0;
        double total_pressure = 0;
        int length = list.size();

        for (i = 0; i < length; i++) {
            AnnotatePoint cur_point = list.get(i);
            total_pressure = total_pressure + cur_point.getPressure();
        }
        return total_pressure / i;
    }

    /* Take the list and found the minx miny maxx and maxy points. */
    private static Tuple found_min_and_max(ArrayList<AnnotatePoint>  list) {

        int i = 0;
        /* Initialize the min and max to the first point coordinates */
        AnnotatePoint first_point = list.get(i);
        double minx = first_point.getX();
        double miny = first_point.getY();
        double maxx = first_point.getX();
        double maxy = first_point.getY();

        int length = list.size();
        /* Search the min and max coordinates */
        for (i = 1; i < length; i++) {
            AnnotatePoint cur_point = list.get(i);
            minx = Math.min( minx, cur_point.getX());
            miny = Math.min( miny, cur_point.getY());
            maxx = Math.max( maxx, cur_point.getX());
            maxy = Math.max( maxy, cur_point.getY());
        }
        return new Tuple(minx, miny, maxx, maxy);
    }

    /* The path described in list is similar to a regular polygon. */
    private static boolean is_similar_to_a_regular_polygon(ArrayList list, double pixel_tollerance) {
        int i = 0;
        double ideal_distance = -1;
        double total_distance = 0;
        double distance, threshold;

        int length = list.size();
        AnnotatePoint old_point = (AnnotatePoint) list.get(i);

        for (i = 1; i < length; i++) {
            AnnotatePoint point = (AnnotatePoint) list.get(i);
            distance = get_distance(old_point.getX(), old_point.getY(), point.getX(), point.getY());
            total_distance = total_distance + distance;
            old_point = point;
        }

        ideal_distance = total_distance / length;
        i = 0;
        old_point = (AnnotatePoint) list.get(i);

        threshold = ideal_distance / 3 + pixel_tollerance;

        for (i = 1; i < length; i++) {
            AnnotatePoint point = (AnnotatePoint) list.get(i);
            /* I have seen that a good compromise allow around 33% of error. */
            distance = get_distance(point.getX(), point.getY(), old_point.getX(), old_point.getY());

            if (!(is_similar(distance, ideal_distance, threshold))) {
                return false;
            }
            old_point = point;
        }
        return true;
    }


    /* Take a path and return the regular polygon path. */
    private static ArrayList<AnnotatePoint> extract_polygon(ArrayList list) {
        double cx, cy, radius, minx, miny, maxx, maxy, x1, y1, angle_step;
        double angle_off = Math.PI / 2;
        int i, length;
        AnnotatePoint last_point, first_point;

        Tuple quad = found_min_and_max(list);
        minx = quad.minx;
        miny = quad.miny;
        maxx = quad.maxx;
        maxy = quad.maxy;

        cx = (maxx + minx) / 2;
        cy = (maxy + miny) / 2;
        radius = ((maxx - minx) + (maxy - miny)) / 4;
        length = list.size();
        angle_step = 2 * Math.PI / (length - 1);
        angle_off += angle_step / 2;

        for (i = 0; i < length - 1; i++) {
            AnnotatePoint point = (AnnotatePoint) list.get(i);
            x1 = radius * Math.cos(angle_off) + cx;
            y1 = radius * Math.sin(angle_off) + cy;
            point.setX(x1);
            point.setY(y1);
            angle_off += angle_step;
        }

        last_point = (AnnotatePoint) list.get(length - 1);
        first_point = (AnnotatePoint) list.get(0);
        last_point.setX(first_point.getX());
        last_point.setY(first_point.getY());

        return list;
    }

    /* Return the degree of the rectangle between two point respect the axis. */
    private static double calculate_edge_degree(AnnotatePoint point_a, AnnotatePoint point_b) {
        double deltax = Math.abs(point_a.getX() - point_b.getX());
        double deltay = Math.abs(point_a.getY() - point_b.getY());
        double direction_ab = Math.atan2(deltay, deltax) / Math.PI * 180;
        return direction_ab;
    }

    /* mid point formula, used both x and y*/
    private static double find_midpoint(double a, double b) {
        return (a + b) / 2;
    }

    /* find slope of two point line */
    private static double find_slope(AnnotatePoint point_a, AnnotatePoint point_b) {
        return ((point_a.getY()-point_b.getY()) / (point_a.getX()-point_b.getX()));
    }

    /* Straighten the line. */
    private static ArrayList<AnnotatePoint> straighten(ArrayList list) {
        AnnotatePoint inp_point, first_point, last_point, last_out_point;
        double degree_threshold = 15;
        ArrayList<AnnotatePoint> list_out = new ArrayList<AnnotatePoint>();
        int length, i;
        double direction;

        length = list.size();

        /* Copy the first one point; it is a good point. */
        inp_point = (AnnotatePoint) list.get(0);
        first_point = new AnnotatePoint(inp_point.getX(), inp_point.getY(), inp_point.getWidth(), inp_point.getPressure());
        list_out.add(0, first_point);

        for (i = 0; i < length - 2; i++) {
            AnnotatePoint point_a = (AnnotatePoint) list.get(i);
            AnnotatePoint point_b = (AnnotatePoint) list.get(i + 1);
            AnnotatePoint point_c = (AnnotatePoint) list.get(i + 2);
            double direction_ab = calculate_edge_degree(point_a, point_b);
            double direction_bc = calculate_edge_degree(point_b, point_c);
            double delta_degree = Math.abs(direction_ab - direction_bc);

            if (delta_degree > degree_threshold) {
                /* Copy B it's a good point. */
                AnnotatePoint point = new AnnotatePoint(point_b.getX(), point_b.getY(), point_b.getWidth(), point_b.getPressure());
                list_out.add(0, point);
            }
        /* Else: is three the difference degree is minor than the threshold I neglect B. */
        }

        /* Copy the last point; it is a good point. */
        last_point = (AnnotatePoint) list.get(length - 1);
        last_out_point = new AnnotatePoint(last_point.getX(), last_point.getY(), last_point.getWidth(), last_point.getPressure());
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
            for (AnnotatePoint p : list_out) {
                p.setY(y);
            }
        }

        /* It is closed to 90 degree I draw a vertical line. */
        if ((90 - degree_threshold <= direction) && (direction <= 90 + degree_threshold)) {
            /* x is the average */
            double x = (first_point.getX() + last_point.getX()) / 2;
            /* put this x for each element in the list. */
            for (AnnotatePoint p : list_out) {
                p.setX(x);
            }
        }
        return list_out;
    }


    /* Return a new list containing a sub-path of list_inp that contains
     the meaningful points using the standard deviation algorithm.
     */
    public static ArrayList<AnnotatePoint> build_meaningful_point_list(ArrayList<AnnotatePoint> list_inp, boolean rectify, double pixel_tollerance) {
        /* Initialize the list. */
        ArrayList<AnnotatePoint> list_out = new ArrayList<AnnotatePoint>();
        AnnotatePoint first_point, second_point, last_point;

        int length = list_inp.size();
        int i = 0;
        AnnotatePoint point_a = (AnnotatePoint) list_inp.get(i);
        AnnotatePoint point_b = (AnnotatePoint) list_inp.get(i + 1);
        AnnotatePoint point_c = null;
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

        first_point = new AnnotatePoint(a_x, a_y, a_width, pressure);
        /* add a point with the coordinates of point_a. */
        list_out.add(0, first_point);

        if (length == 2) {
            second_point = new AnnotatePoint(b_x, b_y, b_width, pressure);
            /* add a point with the coordinates of point_a. */
            list_out.add(0, second_point);
        } else {
            double area = 0.0;
            double h, x1, y1, x2, y2;
            last_point = (AnnotatePoint) list_inp.get(length - 1);
            AnnotatePoint last_point_copy = null;

            for (i = i + 2; i < length; i++) {
                point_c = (AnnotatePoint) list_inp.get(i);
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
                    AnnotatePoint new_point = new AnnotatePoint(b_x, b_y, b_width, pressure);
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
            last_point_copy = new AnnotatePoint(last_point.getX(), last_point.getY(), last_point.getWidth(), last_point.getPressure());
            list_out.add(0, last_point_copy);
        }

        /* I reverse the list to preserve the initial order. */
        Collections.reverse(list_out);
        return list_out;
    }


    /* Return the out-bounded rectangle outside the path described to list_in. */
    public static  ArrayList<AnnotatePoint> build_outbounded_rectangle(ArrayList list) {
        int length = list.size();
        AnnotatePoint point = (AnnotatePoint) list.get(length / 2);
        ArrayList<AnnotatePoint> ret_list = new ArrayList<AnnotatePoint>();

        double minx, miny, maxx, maxy;

        Tuple quad = found_min_and_max(list);
        minx = quad.minx;
        miny = quad.miny;
        maxx = quad.maxx;
        maxy = quad.maxy;

        AnnotatePoint point3 = new AnnotatePoint(minx, maxy, point.getWidth(), point.getPressure());
        ret_list.add(0, point3);
        AnnotatePoint point2 = new AnnotatePoint(maxx, maxy, point.getWidth(), point.getPressure());
        ret_list.add(0, point2);
        AnnotatePoint point1 = new AnnotatePoint(maxx, miny, point.getWidth(), point.getPressure());
        ret_list.add(0, point1);
        AnnotatePoint point0 = new AnnotatePoint(minx, miny, point.getWidth(), point.getPressure());
        ret_list.add(0, point0);

        return ret_list;
    }


    /* The path in list is similar to an ellipse. */
    public static boolean is_similar_to_an_ellipse(ArrayList list, double pixel_tollerance) {
        int i = 0;
        double minx, miny, maxx, maxy, tollerance;

        /* Semi x-axis */
        double a = 0;

        /* Semi y-axis */
        double b = 0;
        double c = 0.0;

        /* x coordinate of the origin */
        double originx = 0;

        /* y coordinate of the origin */
        double originy = 0;

        /* x coordinate of focus1 */
        double f1x = 0;

        /* y coordinate of focus1 */
        double f1y = 0;

        /* x coordinate of focus2 */
        double f2x = 0;

        /* y coordinate of focus2 */
        double f2y = 0;

        double distance_p1f1 = 0;
        double distance_p1f2 = 0;
        double sump1 = 0;

        double aq = 0;
        double bq = 0;

        int length = list.size();

        Tuple quad = found_min_and_max(list);
        minx = quad.minx;
        miny = quad.miny;
        maxx = quad.maxx;
        maxy = quad.maxy;

        a = (maxx - minx) / 2;
        b = (maxy - miny) / 2;

        aq = Math.pow(a, 2);
        bq = Math.pow(b, 2);

      /*
       If in one point the sum of the distance by focus F1 and F2 differ more than
       the tolerance value the curve line will not be considered an ellipse.
       */
        tollerance = pixel_tollerance + (a + b) / 2;

        originx = minx + a;
        originy = miny + b;

        if (aq > bq) {
            c = Math.sqrt(aq - bq);
            // F1 (x0-c,y0)
            f1x = originx - c;
            f1y = originy;
            // F2 (x0+c,y0)
            f2x = originx + c;
            f2y = originy;
        } else {
            c = Math.sqrt(bq - aq);
            // F1 (x0, y0-c)
            f1x = originx;
            f1y = originy - c;
            // F2 (x0, y0+c)
            f2x = originx;
            f2y = originy + c;
        }

        distance_p1f1 = get_distance(minx, miny, f1x, f1y);
        distance_p1f2 = get_distance(minx, miny, f2x, f2y);
        sump1 = distance_p1f1 + distance_p1f2;

        /* In the ellipse the sum of the distance (p,f1)+distance (p,f2) must be constant. */
        for (i = 0; i < length; i++) {
            AnnotatePoint point = (AnnotatePoint) list.get(i);
            double distancef1 = get_distance(point.getX(), point.getY(), f1x, f1y);
            double distancef2 = get_distance(point.getX(), point.getY(), f2x, f2y);
            double sum = distancef1 + distancef2;
            double difference = Math.abs(sum - sump1);

            if (difference > tollerance) {
              /* The sum is too different from the ideal one;
               I do not approximate the shape to an ellipse.
               */
                return false;
            }
        }
        return true;
    }

    /* Return a list rectified */
    private static ArrayList<AnnotatePoint> build_rectified_list(ArrayList  list_inp, boolean close_path, double pixel_tollerance) {
        ArrayList<AnnotatePoint> ret_list = new ArrayList<AnnotatePoint>();
        if (close_path) {

            int length = list_inp.size();
            int i = 0;

            /* Copy the input list */
            for (i = 0; i < length; i++) {
                AnnotatePoint point = (AnnotatePoint) list_inp.get(i);
                AnnotatePoint point_copy = new AnnotatePoint(point.getX(), point.getY(), point.getWidth(), point.getPressure());
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
            /* Try to make straighten. */
            ret_list = straighten(list_inp);
        }
        return ret_list;
    }


    /* Take a list of point and return magically the new recognized path. */
    public static ArrayList<AnnotatePoint> broken(ArrayList<AnnotatePoint> list_inp, boolean close_path, boolean rectify, double pixel_tollerance) {

        /// pixel tollerance must be at least 2.0 * the thickness of the brush (or up to 4.5x)
        /// Broken is true to apply this mode.
        /// close_path is

        ArrayList<AnnotatePoint> meaningful_point_list = build_meaningful_point_list(list_inp, close_path, pixel_tollerance);

        if (meaningful_point_list.size() > 0) {
            if (rectify) {
                ArrayList rectified_list = build_rectified_list(meaningful_point_list, close_path, pixel_tollerance);
                return rectified_list;
            }
        }
        return meaningful_point_list;
    }

    public static ArrayList<AnnotatePoint> spline (ArrayList<AnnotatePoint> list)
    {
        ArrayList<AnnotatePoint> ret = new ArrayList<AnnotatePoint>();
        int i = 0;
        int length = list.size();
        double[][] mx = new double[length][2];
        double width = 12;
        double pressure = 1;

        /* Pi, Qi are control points for curve (Xi, Xi+1). */
        double[][] mp = new double[length-1][2];
        double[][] mq = new double[length-1][2];

        int s;
        int eq = 0;
        double[][] m = new double[2* (length-1)][2* (length-1)];
        double[] bx = new double[2* (length-1)];
        double[] by = new double[2* (length-1)];
        double[] x = new double[2* length-1];
        double[] perm = new double[2* (length-1)];

        for  (i=0; i<length; i++)
        {
            AnnotatePoint point = (AnnotatePoint) list.get(i);
            mx[i][0] = point.getX();
            mx[i][1] = point.getY();
            if (i==0)
            {
                width = point.getWidth();
                pressure = point.getPressure();
            }
        }

        /*****************************************************************************
         Bezier control points system matrix
         1
         P0 P1 P2 Pn-1 ... Q0 Q1 Q2 Qn-1
         /    1              1           \   /P0  \      /  2*X1\ Pi+1 + Qi = 2*Xi+1
         | 1  2             -2 -1        |   |P1  |      |     0|
         |       1              1        |   |P2  |      |  2*X2|
         |    1  2             -2 -1     | * |Pn-1|   =  |     0| Pi + 2*Pi+1
         |          1              1     |   |Q0  |      |2*Xn-1|    - Qi+1 - 2*Qi = 0
         |       1  2             -2 -1  |   |Q1  |      |     0|
         | 1                             |   |Q2  |      |    X0| P0   = X0
         \                             1 /   \Qn-1/      \    Xn/ Qn-1 = Xn
         A*x = b
         x = inv (A)*b
         Pi, Qi and Xi are (x,y) pairs!
         *****************************************************************************/

        /* Fill-in matrix. */
        for ( i = 0; i < length-2; i++ )
        {
            m[eq][i+1] = 1;             // Pi+1
            m[eq][(length-1)+i] = 1;    // + Qi
            eq++;                       // = 2Xi+1

            m[eq][i] = 1;               // Pi
            m[eq][i+1] = 2;             // + 2*Pi+1
            m[eq][(length-1)+i+1] = -1; // - Qi+1
            m[eq][(length-1)+i] = -2;   // - 2*Qi
            eq++;                       // = 0
        }

        m[eq++][0] = 1;                 // P0   = X0
        m[eq++][2* (length-1)-1] = 1;   // Qn-1 = Xn

        /* Fill-in vectors. */
        for ( i = 0; i < length-2; i++ )
        {
            bx[2*i] = 2*mx[i+1][0];
            by[2*i] = 2*mx[i+1][1];
        }
        bx[2* (length-1)-2] = mx[0][0];
        bx[2* (length-1)-1] = mx[length-1][0];

        by[2* (length-1)-2] = mx[0][1];
        by[2* (length-1)-1] = mx[length-1][1];

        LinearSolve ls = new LinearSolve(m);
        x = ls.solve(perm, bx);

        /* copy solution (@FIXME: should be avoided!) */
        for ( i = 0; i < length-1; i++ )
        {
            mp[i][0] = x[i];
            mq[i][0] = x[i+ (length-1)];
        }

        /* Solve for by. */
        x  = new double[2* (length-1)];
        ls.solve(perm, bx);

        /* copy solution (@FIXME: should be avoided!) */
        for ( i = 0; i < length-1; i++ )
        {
            mp[i][1] = x[i];
            mq[i][1] = x[i+ (length-1)];
        }

        /* Now paint the smoothed line. */
        for ( i = 0; i < length-1; i++ )
        {

            AnnotatePoint first_point =  new AnnotatePoint( mp[i][0], mp[i][1], width, pressure);

            AnnotatePoint second_point =  new AnnotatePoint(mq[i][0], mq[i][1], width, pressure);

            AnnotatePoint third_point =  new AnnotatePoint(mx[i+1][0], mx[i+1][1], width, pressure);

            ret.add(0, first_point);
            ret.add(0, second_point);
            ret.add(0, third_point);
        }

        Collections.reverse(ret);
        return ret;
    }

    public static double get_distance(double x1, double y1, double x2, double y2) {
        double x_delta = Math.abs(x2 - x1);
        double y_delta = Math.abs(y2 - y1);
        double quad_sum = Math.pow(x_delta, 2);
        quad_sum = quad_sum + Math.pow(y_delta, 2);
        return Math.sqrt(quad_sum);
    }

    /* test for curve creation */
    public static void main(String args[]) {

        AnnotatePoint first_point = new AnnotatePoint(4, 20, 1, 1);
        AnnotatePoint second_point = new AnnotatePoint(6, 7, 1, 1);
        AnnotatePoint third_point = new AnnotatePoint(3, 3, 1, 1);

        double midABx = find_midpoint(first_point.getX(), second_point.getX());
        double midABy = find_midpoint(first_point.getY(), second_point.getY());
        double midBCx = find_midpoint(second_point.getX(), third_point.getX());
        double midBCy = find_midpoint(second_point.getY(), third_point.getY());
        print("{" + midABx + ", " + midABy + "} , {" + midBCx + ", " + midBCy + "}");

        double slopeAB = find_slope(first_point, second_point);
        double slopeBC = find_slope(second_point, third_point);
        print(slopeAB + "," + slopeBC);

        double slope_perpAB = -1 * Math.pow(slopeAB, -1);
        double slope_perpBC = -1 * Math.pow(slopeBC, -1);
        print(slope_perpAB + "," + slope_perpBC);

        double X = (((slopeAB * slopeBC * (first_point.getY() - third_point.getY())) + (slopeBC * (first_point.getX() + second_point.getX())) - (slopeAB * (second_point.getX() + third_point.getX()))) / (2 * (slopeBC-slopeAB)));
        double Y = (slope_perpAB * (X - ((first_point.getX() + second_point.getX()) / 2))) + ((first_point.getY() + second_point.getY()) / 2);
        print("Center =  {X : " + X + ", Y: " + Y + "}");

        double r = Math.sqrt((first_point.getX()-X)*(first_point.getX()-X) + (third_point.getY()-Y)*(third_point.getY()-Y));
        double x = X-r;
        double y = Y-r;
        double width = 2*r;
        double height = 2*r;

        double startAngle = (180/Math.PI * Math.atan2(first_point.getY()-Y, first_point.getX()-X));
        double endAngle = (180/Math.PI * Math.atan2(third_point.getY()-Y, third_point.getX()-X));
        print("x, y, width, height, start, end = " + x + " " + y + " " + width + " " + height + " " + startAngle + " " + endAngle);
    }
}