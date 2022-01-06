package org.vmax.amba.convolution;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class FilterEditorPanel extends JPanel {
    public static final int KERNEL_SIZE = 7;
    public static final float NORMALIZATION =  1.6f/256f;
    private Integer[] ZOOMS = {50,100,200,400};
    private JLabel zoomLabel, titleLabel;
    private JComboBox zoomBox;
    private JLabel imageLabel1;
    private JLabel imageLabel2;
    private JScrollPane scrollPane1, scrollPane2;
    private BufferedImage image;
    private int zoom = 100;
    private int[] rowData;
    private FilterTableModel tableModel;

    public FilterEditorPanel(String inputImage) throws HeadlessException, IOException {
        rowData = new int[KERNEL_SIZE*KERNEL_SIZE/2+1];
        Arrays.fill(rowData,0);
        image = ImageIO.read(new File(inputImage));

        setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));

        scrollPane1 = new JScrollPane();
        scrollPane2 = new JScrollPane();

        scrollPane1.setPreferredSize(new Dimension(500,550));
        scrollPane2.setPreferredSize(new Dimension(500,550));
//        scrollPane1.setMinimumSize(new Dimension(100,100));
//        scrollPane2.setMinimumSize(new Dimension(100,100));


        imageLabel1 = new JLabel( new ImageIcon(image) );
        imageLabel2 = new JLabel(new ImageIcon());

        scrollPane1.setViewportView(imageLabel1);
        scrollPane2.setViewportView(imageLabel2);

//        imageLabel1.addMouseWheelListener(this::onMouseWheelScroll);
//        imageLabel2.addMouseWheelListener(this::onMouseWheelScroll);

        scrollPane1.getViewport().addChangeListener(e -> portChange(e,scrollPane1,scrollPane2));
        scrollPane2.getViewport().addChangeListener(e -> portChange(e,scrollPane1,scrollPane2));

        JPanel edit = new JPanel();
        edit.setMinimumSize(new Dimension(300,330));
        edit.setPreferredSize(new Dimension(300,330));
        tableModel = new FilterTableModel();
        JTable rowTable = createPreviewTable();
        edit.add(rowTable);
        JPanel calc = createCalcPanel();
        edit.add(calc);

        add(scrollPane1);
        add(edit);
        add(scrollPane2);





    }


    public JTable createPreviewTable() {
        JTable rowTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                int inx = row*7+col;

                if(inx >= rowData.length) {
                    comp.setEnabled(false);
                    comp.setBackground(Color.LIGHT_GRAY);
                }
                else {
                    comp.setEnabled(true);
                    comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        };
        TableColumnModel cols = rowTable.getColumnModel();
        for(int i=0;i<cols.getColumnCount();i++) {
            cols.getColumn(i).setPreferredWidth(40);
        }


        tableModel.addTableModelListener(e->{
            resizeImage(zoom);
        });
        return rowTable;
    }


    public JPanel createCalcPanel() {
        JPanel calc = new JPanel();
        calc.setLayout(new GridLayout(0,2));
        JTextField kField = new JTextField("255",5);
        JTextField sigmaField = new JTextField("1.0", 5);
        JLabel sigma2label = new JLabel("  Sigma 2:",SwingConstants.RIGHT);
        JTextField sigma2Field = new JTextField("2.0", 5);
        sigma2label.setEnabled(false);
        sigma2Field.setEnabled(false);

        JComboBox filterSelect = new JComboBox(
                new String[]{"Laplacian of Gaussian","Difference of Gaussian"}
        );
        filterSelect.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sigma2Field.setEnabled(filterSelect.getSelectedIndex()==1);
                sigma2label.setEnabled(filterSelect.getSelectedIndex()==1);
            }
        });

        zoomLabel = new JLabel("zoom", SwingConstants.RIGHT );
        zoomBox = new JComboBox<>(ZOOMS);
        zoomBox.setSelectedIndex(1);
        zoomBox.addItemListener(e -> {
            Integer zoom = (Integer) e.getItem();
            resizeImage(zoom);
        });
        JLabel filenameLabel = new JLabel("    ");
        calc.add(filenameLabel);
        calc.add(new JButton(new AbstractAction("Select image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseImage();
            }
        }));

        calc.add(zoomLabel);
        calc.add(zoomBox);
        calc.add(new JLabel("Filter type:",SwingConstants.RIGHT));
        calc.add(filterSelect);
        calc.add(new JLabel("  K:",SwingConstants.RIGHT));
        calc.add(kField);
        calc.add(new JLabel("  Sigma 1:",SwingConstants.RIGHT));
        calc.add(sigmaField);
        calc.add(sigma2label);
        calc.add(sigma2Field);
        calc.add(new JLabel(" Make Square:",SwingConstants.RIGHT));
        JCheckBox square = new JCheckBox();
        calc.add(square);

        calc.add(new JButton(new AbstractAction("Calculate") {
            @Override
            public void actionPerformed(ActionEvent event) {
                calculate(filterSelect, kField, sigmaField, sigma2Field, square);
            }
        }));
        calc.add(new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Arrays.fill(rowData,0);
                tableModel.fireTableDataChanged();
            }
        }));
        return calc;
    }

    public void calculate(JComboBox filterSelect, JTextField kField, JTextField sigmaField, JTextField sigma2Field, JCheckBox square) {
        try {
            int filter = filterSelect.getSelectedIndex();
            double sigma, sigma2;
            double k;
            try {
                k = Double.parseDouble(kField.getText());
                kField.setBackground(Color.WHITE);
            } catch (NumberFormatException e) {
                kField.setBackground(Color.RED);
                throw e;
            }
            try {
                sigma = Double.parseDouble(sigmaField.getText());
                sigmaField.setBackground(Color.WHITE);
            } catch (NumberFormatException e) {
                sigmaField.setBackground(Color.RED);
                throw e;
            }
            if(filter==1) {
                try {
                    sigma2 = Double.parseDouble(sigma2Field.getText());
                    sigma2Field.setBackground(Color.WHITE);
                } catch (NumberFormatException e) {
                    sigma2Field.setBackground(Color.RED);
                    throw e;
                }
                calulateDoGTable(sigma,sigma2, k, square.isSelected());
            }
            else {
                calulateLoGTable(sigma,k, square.isSelected());
            }
            tableModel.fireTableDataChanged();
        }
        catch (Exception ex){}
    }

    private void chooseImage() {
        JFileChooser jfc = new JFileChooser(new File("."));
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if(selectedFile.exists()) {
                try {
                    BufferedImage img = ImageIO.read(selectedFile);
                    setImage(img);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "error reading as image " + selectedFile.getName());
                }
            }
        }
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        resizeImage(zoom);
    }

    private void calulateDoGTable(double sigma1, double sigma2, double k, boolean square) {
        double twoSigmaSq = 2*sigma1*sigma1;

        int inx=0;
        int sum = 0;
        for(inx=0 ; inx<this.rowData.length-1;inx++) {
            int x = inx%KERNEL_SIZE - KERNEL_SIZE/2;
            int y = inx/KERNEL_SIZE - KERNEL_SIZE/2;
            if(square) {
                x=Math.abs(x);
                y=Math.abs(y);
                x=Math.max(x,y);
                y=0;
            }
            double g1 = gaussian(sigma1,x,y);
            double g2 = gaussian(sigma2,x,y);
            double v = k*(g1-g2);
            int iv = (int) Math.round(v);
            rowData[inx] = iv;
            sum+=iv;
        }
        rowData[rowData.length-1] = -sum * 2;
    }

    private double gaussian(double sigma, int x, int y) {
        double twoSigmaSq = 2*sigma*sigma;
        double a = (x * x + y * y) / twoSigmaSq;
        return 1/Math.PI/twoSigmaSq*Math.exp(-a);
    }


    private void calulateLoGTable(double sigma, double k, boolean square) {
        double twoSigmaSq = 2*sigma*sigma;

        int inx=0;
        int sum = 0;
        for(inx=0 ; inx<this.rowData.length-1;inx++) {
                int x = inx%KERNEL_SIZE - KERNEL_SIZE/2;
                int y = inx/KERNEL_SIZE - KERNEL_SIZE/2;
                if(square) {
                    x=Math.abs(x);
                    y=Math.abs(y);
                    x=Math.max(x,y);
                    y=0;
                }
                double a = (x * x + y * y) / twoSigmaSq;
                double v = 4 * k / Math.PI / twoSigmaSq / twoSigmaSq * (1 - a) * Math.exp(-a);
                int iv = (int) Math.round(v);
                rowData[inx] = iv;
                sum+=iv;
        }
        rowData[rowData.length-1] = -sum * 2;

    }


    public void resizeImage(int newZoom) {

        AffineTransform at = new AffineTransform();
        at.scale(newZoom/100.0, newZoom/100.0);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage after = scaleOp.filter(image,null);
        imageLabel1.setIcon(new ImageIcon(after));

        Kernel kernel = convertRowToKernel();

        ConvolveOp convolveOp = new ConvolveOp(kernel);
        BufferedImage filteredImage = convolveOp.filter(image, null);
        BufferedImage after2 = scaleOp.filter(filteredImage, null);
        imageLabel2.setIcon(new ImageIcon(after2));

        zoom = newZoom;

    }

    protected Kernel convertRowToKernel() {
        float[] kernelData = new float[rowData.length*2-1];
        for(int i=0;i<rowData.length;i++) {
            kernelData[i] = rowData[i]*NORMALIZATION;
            kernelData[kernelData.length-1-i] = kernelData[i];
        }
        kernelData[rowData.length-1]+=1.0f;
        return new Kernel(KERNEL_SIZE,KERNEL_SIZE,kernelData);
    }


//    public void onMouseWheelScroll(MouseWheelEvent e) {
//        int notches = e.getWheelRotation();
//        int temp = zoom + notches;
//        temp = Math.min(temp, ZOOMS.length-1);
//        temp = Math.max(temp, 0);
//        if (temp != zoom) {
//            zoom = temp;
//            zoomLabel.setText("zoom="+ZOOMS[zoom]+"%");
//            resizeImage();
//        }
//    }


    public void portChange(ChangeEvent e, JScrollPane scrollPane1, JScrollPane scrollPane2) {
        JScrollPane src, dst;
        if(e.getSource()== scrollPane1.getViewport()) {
            src= scrollPane1;
            dst= scrollPane2;
        }
        else {
            src= scrollPane2;
            dst= scrollPane1;
        }
        System.out.println(src.getViewport().getViewPosition());
        dst.getHorizontalScrollBar().setValue(src.getHorizontalScrollBar().getValue());
        dst.getVerticalScrollBar().setValue(src.getVerticalScrollBar().getValue());
    }

    public void setRow(int[] row) {
        System.arraycopy(row,0,this.rowData,0,this.rowData.length);
        tableModel.fireTableDataChanged();
        resizeImage(zoom);
    }

    public int[] getRow() {
        int[] dest = new int[rowData.length];
        System.arraycopy(rowData,0,dest,0,this.rowData.length);
        return dest;
    }
    private void updateTitle() {
        //todo
    }


    private class   FilterTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return KERNEL_SIZE;
        }

        @Override
        public int getColumnCount() {
            return KERNEL_SIZE;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Integer.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return rowIndex*7+columnIndex <= KERNEL_SIZE*KERNEL_SIZE/2+1;
        }


        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int inx = rowIndex*7+columnIndex;
            if(inx > KERNEL_SIZE*KERNEL_SIZE/2) {
                inx = KERNEL_SIZE*KERNEL_SIZE-inx-1;
            }
            return rowData[inx];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            int inx = rowIndex*7+columnIndex;
            if(inx > KERNEL_SIZE*KERNEL_SIZE/2) {
                inx = KERNEL_SIZE*KERNEL_SIZE-inx-1;
            }
            rowData[inx] = (int) aValue;
            fireTableDataChanged();
        }
    }
}
