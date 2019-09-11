package org.vmax.amba.tables.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataset;
import org.vmax.amba.tables.Table2dModel;
import org.vmax.amba.tables.config.SingleTableConf;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.math.ChartPoint;
import org.vmax.amba.tables.math.DouglassPeucker;
import org.vmax.amba.tables.math.PointWithRange;
import org.vmax.amba.tables.ui.datasets.ReducedDataset;
import org.vmax.amba.tables.ui.datasets.SplineDataset;
import org.vmax.amba.tables.ui.datasets.TableDataset;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends ChartPanel implements TableModelListener {


    public enum Datasets {
        RAW,
        REQUCED,
        SPLINE
    }
    private final static float ERR = 1.0f;
    private final static int DELTA =5;
    private ChartPoint anchor;
    private List<PointWithRange> selectedPoints = new ArrayList<>();
    private XYPlot plot;
    private List<Table2dModel> fileTableModels;
    private boolean[] enabledGraphs;

    public static GraphPanel create(TableConfig cfg, List<Table2dModel> fileTableModels) {
        JFreeChart chart = createChart(cfg,fileTableModels);
        GraphPanel graphTable = new GraphPanel( chart, fileTableModels);
        graphTable.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseMoved(MouseEvent e) {
                graphTable.onMouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                graphTable.onMouseDragged(e);
            }
        });
        graphTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                graphTable.onMousePressed(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                graphTable.onMouseReleased(e);
            }
        });
        graphTable.getPopupMenu().addSeparator();
        graphTable.getPopupMenu().add(
                new AbstractAction("Insert point") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graphTable.insertPointPressed();
                    }
                }
        );
        graphTable.getPopupMenu().add(
                new AbstractAction("Delete point") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graphTable.deletePointPressed();
                    }
                }
        );
        graphTable.getPopupMenu().addSeparator();
        SingleTableConf[] tables = cfg.getTables();
        for (int i = 0; i < tables.length; i++) {
            SingleTableConf stcfg = tables[i];
            int inx = i;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(stcfg.getColor().name(), true);
            graphTable.getPopupMenu().add(item);
            item.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    graphTable.graphEnabled(inx,item.getState());
                }
            });
        }
        return graphTable;
    }

    private void graphEnabled(int inx, boolean state) {
        enabledGraphs[inx] = state;
    }


    public GraphPanel( JFreeChart chart, List<Table2dModel> fileTableModels) {
        super(chart);
        this.enabledGraphs = new boolean[fileTableModels.size()];
        this.plot = chart.getXYPlot();
        this.fileTableModels = fileTableModels;
        setDomainZoomable(false);
        setRangeZoomable(false);
        fileTableModels.forEach(m->m.addTableModelListener(this));
    }


    private static JFreeChart createChart(TableConfig cfg, List<Table2dModel> fileTableModels)
    {
        final JFreeChart chart = ChartFactory.createXYLineChart(
                null,
                "offset",                      // x axis label
                "value",                     // y axis label
                null,                        // data
                PlotOrientation.VERTICAL,
                false,                        // include legend
                true,                       // tooltips
                false                        // urls
        );

        chart.setBackgroundPaint(Color.white);

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(232, 232, 232));

        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint (Color.gray);

        // Определение отступа меток делений
        plot.setAxisOffset(new RectangleInsets(-2.0, -2.0, 2.0, 2.0));

        // Скрытие осевых линий и меток делений
        ValueAxis axis = plot.getDomainAxis();
        axis.setAxisLineVisible (false);    // осевая линия
        axis.setAutoRange(false);
        axis.setRange(0,cfg.getNcol()*cfg.getNrow());

        // Настройка NumberAxis
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAxisLineVisible (false);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(cfg.getRange().getMin(),cfg.getRange().getMax());

        SingleTableConf[] tables = cfg.getTables();
        for (int i = 0; i < tables.length; i++) {
            SingleTableConf stcfg = tables[i];
            Color color = stcfg.getColor().getColor();
            XYItemRenderer rawDataRenderer = createLinesRenderer(color);
            XYItemRenderer reducedDataRenderer = createShapedRenderer(color);
            XYItemRenderer splineDataRenderer = createDottedRenderer(color);


            // Наборы данных
            XYDataset rawDataset = new TableDataset(fileTableModels.get(i));
            List<ChartPoint> reduced = createReducedPoints(fileTableModels.get(i));
            ReducedDataset reducedDataset = new ReducedDataset(cfg, reduced);
            SplineDataset splineDataset = new SplineDataset(cfg, reducedDataset);
            splineDataset.updateSpline();

            int inx = i*Datasets.values().length;
            plot.setDataset(Datasets.RAW.ordinal()+inx, rawDataset);
            plot.setDataset(Datasets.REQUCED.ordinal()+inx, reducedDataset);
            plot.setDataset(Datasets.SPLINE.ordinal()+inx, splineDataset);

            // Подключение Spline Renderer к наборам данных
            plot.setRenderer(Datasets.RAW.ordinal()+inx, rawDataRenderer);
            plot.setRenderer(Datasets.REQUCED.ordinal()+inx, reducedDataRenderer);
            plot.setRenderer(Datasets.SPLINE.ordinal()+inx, splineDataRenderer);
        }

        return chart;
    }

    private static List<ChartPoint> createReducedPoints(Table2dModel fileTableModel) {
        List<ChartPoint> points = new ArrayList<>();
        int x=0;
        for(int i=0; i < fileTableModel.getColumnCount();i++) {
            for(int j=0; j < fileTableModel.getRowCount();j++) {
                points.add(new ChartPoint(x,fileTableModel.getValueAtInx(x).floatValue()));
                x++;
            }
        }
        return DouglassPeucker.reduce(points,ERR);
    }

    private static XYItemRenderer createLinesRenderer(Color col) {
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        r.setSeriesPaint         (0, col);
        r.setSeriesShapesVisible(0,false);
        return r;
    }

    private static XYItemRenderer createDottedRenderer(Color col) {
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        r.setSeriesPaint(0, col);
        float[] dashSequence = new float[] {
                4.0f, 4.0f
        };
        r.setSeriesStroke(0,new BasicStroke(1,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                1.0f,
                dashSequence,
                0.0f));
        r.setSeriesShapesVisible(0,false);
        r.setDrawSeriesLineAsPath(true);
        return r;
    }

    private static XYLineAndShapeRenderer createShapedRenderer (Color col) {
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        r.setSeriesPaint         (0, col);
        r.setSeriesLinesVisible  (0,false);
        return r;
    }




    ChartPoint convertMouseEventCoordinates(MouseEvent event) {

        Rectangle2D dataArea = getScreenDataArea();
        Insets insets = getInsets();
        int x = (event.getX() - insets.left);
        int y = (event.getY() - insets.top);
        XYPlot plot = getChart().getXYPlot();
        if (dataArea.contains(x, y)) {
            // set the anchor value for the horizontal axis...
            ValueAxis xaxis = plot.getDomainAxis();
            ValueAxis yaxis = plot.getRangeAxis();
            if (xaxis == null || yaxis == null) {
                return null;
            }
            return new ChartPoint(
                    (int)Math.round(xaxis.java2DToValue(x, dataArea, plot.getDomainAxisEdge())),
                    (float) yaxis.java2DToValue(y, dataArea, plot.getRangeAxisEdge())
            );

        }
        return null;
    }

    void onMouseMoved(MouseEvent event) {
        ChartPoint p = convertMouseEventCoordinates(event);
        if(p == null) {
            return;
        }
        showCoords(p);
    }

    void onMouseDragged(MouseEvent event) {
        ChartPoint p = convertMouseEventCoordinates(event);
        if(p == null) {
            return;
        }
        if(anchor!=null) {
            if(SwingUtilities.isLeftMouseButton(event)) {
                moveSelectedPoints(p);
            }
        }
        showCoords(p);
    }


    void onMousePressed(MouseEvent event) {
        anchor = convertMouseEventCoordinates(event);
        if(anchor != null) {
            if(SwingUtilities.isLeftMouseButton(event)) {
                this.selectedPoints.clear();
                int inx=0;
                for(int i=0; i<fileTableModels.size();i++) {
                    if(enabledGraphs[i]) {
                        ReducedDataset reducedDataset = (ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal() + inx);
                        PointWithRange nearestPoint = reducedDataset.findReducedPoint(anchor.X, anchor.Y, DELTA);
                        if (nearestPoint != null) {
                            this.selectedPoints.add(nearestPoint);
                        }
                    }
                    inx+=Datasets.values().length;
                }
            }
        }
        showCoords(anchor);
    }
    void onMouseReleased(MouseEvent event) {
        ChartPoint p = convertMouseEventCoordinates(event);
        if(p == null) {
            return;
        }
        if(anchor!=null) {
            if(SwingUtilities.isLeftMouseButton(event)) {
                moveSelectedPoints(p);
            }
        }
        showCoords(p);

    }

    private void moveSelectedPoints(ChartPoint to) {
        for(PointWithRange selectedPoint : selectedPoints) {
            int x = to.X;
            x = Math.max(x, selectedPoint.getRange().start.X);
            x = Math.min(x, selectedPoint.getRange().end.X - 1);
            float y = to.Y;
            y = Math.max(y, selectedPoint.getRange().start.Y);
            y = Math.min(y, selectedPoint.getRange().end.Y - 1);
            selectedPoint.getP().X = x;
            selectedPoint.getP().Y = y;
        }
        int inx=0;
        for(int i=0;i<fileTableModels.size();i++) {
            if(enabledGraphs[i]) {
                ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal() + inx)).updateSpline();
            }
            inx+=Datasets.values().length;
        }
    }

    private void deletePointPressed() {
        if(anchor != null) {
            int inx=0;
            for(int i=0 ; i<fileTableModels.size(); i++) {
                if(enabledGraphs[i]) {
                    ((ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal() + inx)).deletePoint(anchor.X, anchor.Y, DELTA);
                    ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal() + inx)).updateSpline();
                }
                inx+=Datasets.values().length;
            }
            getChart().fireChartChanged();
        }
    }

    private void insertPointPressed() {
        if(anchor != null) {
            int inx=0;
            for(int i=0 ; i<fileTableModels.size(); i++) {
                if(enabledGraphs[i]) {
                    ((ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal() + inx)).insertPoint(anchor.X, anchor.Y);
                    ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal() + inx)).updateSpline();
                }
                inx+=Datasets.values().length;
            }
            getChart().fireChartChanged();
        }
    }


    private void showCoords(ChartPoint p) {
        if(p!=null) {
            getChart().getXYPlot().getDomainAxis().setLabel("X:" + p.X + " Y:"+ p.Y);
        }
    }


    @Override
    public void tableChanged(TableModelEvent e) {
        getChart().fireChartChanged();
        int inx = 0;
        selectedPoints.clear();
        for(Table2dModel fileTableModel : fileTableModels) {
            List<ChartPoint> reduced = createReducedPoints(fileTableModel);

            ((ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal()+inx)).updateReduced(reduced);
            ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal()+inx)).updateSpline();
            inx+= Datasets.values().length;
        }
    }

    public void updateTable() {
        int inx=0;
        for(Table2dModel fileTableModel : fileTableModels) {
            for (int i = 0; i < fileTableModel.getColumnCount() * fileTableModel.getRowCount(); i++) {
                Number v = plot.getDataset(Datasets.SPLINE.ordinal()+inx).getY(0, i);
                fileTableModel.setValueAtInx(i, v);
            }
            fileTableModel.fireTableDataChanged();
            inx+= Datasets.values().length;
        }
    }


}

