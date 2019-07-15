package org.vmax.amba.tables.math;


import java.util.Arrays;
import java.util.List;

/**
 * Created by vmax on 7/12/18.
 */

    public class LineInt {

        public ChartPoint start;
        public ChartPoint end;

        private double dx;
        private double dy;
        private double sxey;
        private double exsy;
        private double length;

        public LineInt(ChartPoint start, ChartPoint end) {
            this.start = start;
            this.end = end;
            dx = start.X - end.X;
            dy = start.Y - end.Y;
            sxey = start.X * end.Y;
            exsy = end.X * start.Y;
            length = Math.sqrt(dx*dx + dy*dy);
        }

        @SuppressWarnings("unchecked")
        public List<ChartPoint> asList() {
            return Arrays.asList(start, end);
        }

        double distance(ChartPoint p) {
            return Math.abs(dy * p.X - dx * p.Y + sxey - exsy) / length;
        }

    @Override
    public String toString() {
        return "LineInt{start=" + start +", end=" + end +'}';
    }

    public double getLength() {
        return length;
    }
}


