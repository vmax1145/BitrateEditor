package org.vmax.amba.tables.ui.datasets;

import org.jfree.data.xy.AbstractXYDataset;
import org.vmax.amba.tables.Table2dModel;
import org.vmax.amba.tables.ui.GraphPanel;

public class TableDataset extends AbstractXYDataset {

    private final Table2dModel model;

    public TableDataset(Table2dModel fileTableModel) {
        this.model = fileTableModel;
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public Comparable getSeriesKey(int i) {
        return GraphPanel.Datasets.RAW.ordinal();
    }

    @Override
    public int getItemCount(int i) {
        return model.getRowCount()*model.getColumnCount();
    }

    @Override
    public Number getX(int series, int x) {
        return x;
    }

    @Override
    public Number getY(int series, int x) {
        return model.getValueAtInx(x);
    }
}
