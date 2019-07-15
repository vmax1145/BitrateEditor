package org.vmax.amba.tables.math;

/**
 * Created by vmax on 7/20/18.
 */
public class PointWithRange {
    ChartPoint p;
    LineInt range;

    public PointWithRange(ChartPoint p, LineInt range) {
        this.p = p;
        this.range = range;
    }

    public ChartPoint getP() {
        return p;
    }

    public void setP(ChartPoint p) {
        this.p = p;
    }

    public LineInt getRange() {
        return range;
    }

    public void setRange(LineInt range) {
        this.range = range;
    }

    @Override
    public String toString() {
        return "PointWithRange{" +
                "p=" + p +
                ", range=" + range +
                '}';
    }
}
