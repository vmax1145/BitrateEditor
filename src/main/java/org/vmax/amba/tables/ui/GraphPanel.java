package org.vmax.amba.tables.ui;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Align;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataset;
import org.vmax.amba.tables.Table2dModel;
import org.vmax.amba.tables.config.SingleTableConf;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.config.TableSetConfig;
import org.vmax.amba.tables.math.ChartPoint;
import org.vmax.amba.tables.math.DouglassPeucker;
import org.vmax.amba.tables.math.PointWithRange;
import org.vmax.amba.tables.ui.datasets.ReducedDataset;
import org.vmax.amba.tables.ui.datasets.SplineDataset;
import org.vmax.amba.tables.ui.datasets.TableDataset;

import javax.imageio.ImageIO;
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
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
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
    private FilteredImageSource previewImageSource = null;


    public static GraphPanel create(TableConfig cfg, TableSetConfig tsCfg, List<Table2dModel> fileTableModels) {
        JFreeChart chart = createChart(cfg, tsCfg , fileTableModels);

        GraphPanel graphTable = new GraphPanel( cfg, chart, fileTableModels);

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
        SingleTableConf[] tables = tsCfg.getTables();
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
        graphTable.getPopupMenu().addSeparator();
        graphTable.getPopupMenu().add(
                new AbstractAction("Linear spline") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graphTable.linearSpline();
                    }
                }
        );
        graphTable.getPopupMenu().add(
                new AbstractAction("Zero spline") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graphTable.zeroSpline();
                    }
                }
        );

        return graphTable;
    }


    private void graphEnabled(int inx, boolean state) {
        enabledGraphs[inx] = state;
    }


    public GraphPanel(TableConfig cfg, JFreeChart chart, List<Table2dModel> fileTableModels) {
        super(chart);
        this.enabledGraphs = new boolean[fileTableModels.size()];
        Arrays.fill(enabledGraphs,true);
        this.plot = chart.getXYPlot();
        this.fileTableModels = fileTableModels;
        setDomainZoomable(false);
        setRangeZoomable(false);

        if(cfg.getImageSample()!=null) {
            try {
                if(fileTableModels.size()==3 && fileTableModels.get(0).getRowCount()*fileTableModels.get(0).getRowCount()==256) {
                    Image im  = ImageIO.read(new File(cfg.getImageSample()));
                    previewImageSource = new FilteredImageSource(im.getSource(), new PreviewImageFilter(plot, cfg.getRange().getMax().intValue()));
                }
                else {
                    log.error("Preview requires 3 tables with 256 values each");
                }
            }
            catch (IOException e) {
                log.error("Error loading sample image:"+e.getMessage());
            }
            updateBackgroundImageAndFireChartChanged();
        }


        fileTableModels.forEach(m->m.addTableModelListener(this));
    }


    private static JFreeChart createChart(TableConfig cfg, TableSetConfig tableSetConfig, List<Table2dModel> fileTableModels)
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

        SingleTableConf[] tables = tableSetConfig.getTables();
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
        updateBackgroundImageAndFireChartChanged();
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
            updateBackgroundImageAndFireChartChanged();
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
            updateBackgroundImageAndFireChartChanged();
        }
    }


    private void showCoords(ChartPoint p) {
        if(p!=null) {
            getChart().getXYPlot().getDomainAxis().setLabel("X:" + p.X + " Y:"+ p.Y);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        updateBackgroundImageAndFireChartChanged();
    }


    public void updateSplineFromTable() {

        int inx = 0;
        selectedPoints.clear();
        for(Table2dModel fileTableModel : fileTableModels) {
            List<ChartPoint> reduced = createReducedPoints(fileTableModel);
            ((ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal()+inx)).updateReduced(reduced);
            ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal()+inx)).updateSpline();
            inx+= Datasets.values().length;
        }
        updateBackgroundImageAndFireChartChanged();
    }

    public void updateTableFromSpline() {
        int inx=0;

        for(Table2dModel fileTableModel : fileTableModels) {
            for (int i = 0; i < fileTableModel.getColumnCount() * fileTableModel.getRowCount(); i++) {
                Number v = plot.getDataset(Datasets.SPLINE.ordinal()+inx).getY(0, i);
                fileTableModel.setValueAtInx(i, v);
            }
            inx+= Datasets.values().length;
            fileTableModel.fireTableDataChanged();
        }
    }

    private void linearSpline() {
        int inx=0;
        for(int i=0 ; i<fileTableModels.size(); i++) {
            if(enabledGraphs[i]) {
                ((ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal() + inx)).linear();
                ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal() + inx)).updateSpline();
            }
            inx+=Datasets.values().length;
        }
        updateBackgroundImageAndFireChartChanged();
    }

    private void zeroSpline() {
        int inx=0;
        for(int i=0 ; i<fileTableModels.size(); i++) {
            if(enabledGraphs[i]) {
                ((ReducedDataset) plot.getDataset(Datasets.REQUCED.ordinal() + inx)).zero();
                ((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal() + inx)).updateSpline();
            }
            inx+=Datasets.values().length;
        }
        updateBackgroundImageAndFireChartChanged();
    }


    private void updateBackgroundImageAndFireChartChanged() {
        if ( previewImageSource != null) {
            Image bgImage = createImage(previewImageSource);
            plot.setBackgroundImageAlignment(Align.TOP_LEFT);
            plot.setBackgroundImageAlpha(1);
            plot.setBackgroundImage(bgImage);
        }
        getChart().fireChartChanged();
    }


    private static class PreviewImageFilter extends RGBImageFilter {
        private SplineDataset[] splines = new SplineDataset[3];
        private int max;

        PreviewImageFilter(XYPlot plot, int max) {
            this.max=max;
            int inx=0;
            for(int i=0 ; i<3; i++) {
                splines[i]=((SplineDataset) plot.getDataset(Datasets.SPLINE.ordinal() + inx));
                inx+=Datasets.values().length;
            }
        }

        @Override
        public int filterRGB(int x, int y, int rgb) {
            int r = (rgb>>16)&0xff;
            int g = (rgb>>8)&0xff;
            int b = (rgb)&0xff;

            r = splines[0].getY(0,r).intValue()*256/max;
            g = splines[1].getY(0,g).intValue()*256/max;
            b = splines[2].getY(0,b).intValue()*256/max;

            if( r < 0 ) r = 0;
            else if( r > 255 ) r = 255;
            if( g < 0 ) g = 0;
            else if( g > 255 ) g = 255;
            if( b < 0 ) b = 0;
            else if( b > 255 ) b = 255;
            return (r<<16) | (g<<8) | b | 0xff000000;
        }
    }
}

