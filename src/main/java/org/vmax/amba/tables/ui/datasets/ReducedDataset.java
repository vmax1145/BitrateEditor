package org.vmax.amba.tables.ui.datasets;

import org.jfree.data.xy.AbstractXYDataset;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.math.ChartPoint;
import org.vmax.amba.tables.math.LineInt;
import org.vmax.amba.tables.math.PointWithRange;
import org.vmax.amba.tables.ui.GraphPanel;

import java.util.List;

public class ReducedDataset extends AbstractXYDataset {
    private TableConfig cfg;
    private List<ChartPoint> reducedPoints;

    public ReducedDataset(TableConfig cfg, List<ChartPoint> reducedPoints) {
        this.cfg = cfg;
        this.reducedPoints = reducedPoints;
    }

    @Override
    public int getItemCount(int series) {
        return reducedPoints.size();
    }

    @Override
    public Number getX(int series, int x) {
        return reducedPoints.get(x).X;
    }

    @Override
    public Number getY(int series, int x) {
        return reducedPoints.get(x).Y;
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public Comparable getSeriesKey(int series) {
        return GraphPanel.Datasets.REQUCED.ordinal();
    }

    public PointWithRange findReducedPoint(int x, float y, int delta) {
        ChartPoint p = getChartPointAtXY(x, y, delta);
        if (p != null) {
            int maxX = cfg.getNcol()*cfg.getNrow();
            ChartPoint start = new ChartPoint(0, cfg.getRange().getMin());
            ChartPoint end = new ChartPoint(maxX, cfg.getRange().getMax());
            LineInt range = new LineInt(start, end);
            int inx = reducedPoints.indexOf(p);
            if (inx == 0) {
                start.X = 0;
                end.X = 1;
            } else if (inx == reducedPoints.size() - 1) {
                end.X = cfg.getNcol()*cfg.getNrow();
                start.X = end.X-1;
            } else {
                start.X = reducedPoints.get(inx - 1).X + 1;
                end.X = reducedPoints.get(inx + 1).X;
            }
            start.Y = 0;
            end.Y = 1024;
            return new PointWithRange(p, range);
        }
        return null;
    }

    private ChartPoint getChartPointAtXY(int x, float y, int delta) {
        return reducedPoints.stream().sorted(
                    (p1, p2) -> {
                        int dx = (p1.X - x);
                        int dx2 = (p2.X - x);
                        return Double.compare(Math.abs(dx), Math.abs(dx2));
                    }
            ).filter(pp-> new LineInt(pp,new ChartPoint(x,y)).getLength()<=delta).findFirst().orElse(null);
    }

    public void deletePoint(int x, float y, int delta) {
        ChartPoint p = getChartPointAtXY(x, y, delta);
        int inx = reducedPoints.indexOf(p);
        if(inx>0 && inx<reducedPoints.size()-1) {
            reducedPoints.remove(inx);
        }
    }

    public void insertPoint(int x, float y) {
        for(int i=0;i<reducedPoints.size()-1;i++) {
            if(reducedPoints.get(i).X < x && reducedPoints.get(i+1).X>x) {
                reducedPoints.add(i+1,  new ChartPoint(x,y));
            }
        }
    }


    public void updateReduced(List<ChartPoint> reduced) {
        this.reducedPoints.clear();
        this.reducedPoints.addAll(reduced);
    }
}
