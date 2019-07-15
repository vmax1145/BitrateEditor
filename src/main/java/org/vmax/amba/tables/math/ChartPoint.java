package org.vmax.amba.tables.math;


import lombok.NoArgsConstructor;

/**
 * Created by vmax on 7/12/18.
 */

@NoArgsConstructor
public class ChartPoint {
    public int X;
    public float Y;

    public ChartPoint(int x, float y) {
        this.X=x;
        this.Y=y;
    }

}
