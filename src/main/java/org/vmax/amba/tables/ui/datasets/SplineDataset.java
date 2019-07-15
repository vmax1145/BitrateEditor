package org.vmax.amba.tables.ui.datasets;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.data.xy.AbstractXYDataset;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.ui.GraphPanel;

public class SplineDataset extends AbstractXYDataset {
    private TableConfig cfg;
    private ReducedDataset reducedDataset;
    private PolynomialSplineFunction function;

    public SplineDataset(TableConfig cfg, ReducedDataset reducedDataset) {
        this.cfg = cfg;
        this.reducedDataset = reducedDataset;
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public Comparable getSeriesKey(int i) {
        return GraphPanel.Datasets.SPLINE.ordinal();
    }

    @Override
    public int getItemCount(int i) {
        return cfg.getNrow()*cfg.getNcol();
    }

    @Override
    public Number getX(int series, int x) {
        return x;
    }

    @Override
    public Number getY(int series, int x) {
        return (Math.max(cfg.getRange().getMin(), Math.min(cfg.getRange().getMax(), function.value(x))));
    }


    public void updateSpline() {
        int count = reducedDataset.getItemCount(0);
        double[] ax = new double[count];
        double[] ay = new double[count];

        for(int inx=0; inx<count; inx++) {
            ax[inx]= reducedDataset.getX(0,inx).doubleValue();
            ay[inx]= reducedDataset.getY(0,inx).doubleValue();
        }
        if(count>2) {
            function = new SplineInterpolator().interpolate(ax, ay);
        }
        else if(count==2){
            function = new LinearInterpolator().interpolate(ax,ay);
        }
    }
}
