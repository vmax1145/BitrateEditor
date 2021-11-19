package org.vmax.amba.convolution;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class FilterPreviewFrame extends JFrame {
    public static final int KERNEL_SIZE = 7;
    public static final float NORMALIZATION =  256.0f;
    private int[] ZOOMS = {50,100,200,400,800};
    private JLabel imageLabel;
    private JLabel imageLabel2;
    private BufferedImage image;
    private JScrollPane scrollPane1, scrollPane2;
    private int zoom = 1;
    private int[] rowData;

    public FilterPreviewFrame(String title, String inputImage) throws HeadlessException, IOException {
        super(title);
        rowData = new int[KERNEL_SIZE*KERNEL_SIZE/2+1];
        Arrays.fill(rowData,0);
        image = ImageIO.read(new File(inputImage));
        Panel p = new Panel();
        p.setLayout(new BorderLayout());
        scrollPane1 = new JScrollPane();
        scrollPane1.setPreferredSize(new Dimension(800,500));
        scrollPane2 = new JScrollPane();
        scrollPane2.setPreferredSize(new Dimension(800,500));

        p.add(scrollPane1, BorderLayout.WEST);
        p.add(scrollPane2, BorderLayout.EAST);

        getContentPane().add(p);

        scrollPane1.getViewport().addChangeListener(this::portChange);
        scrollPane2.getViewport().addChangeListener(this::portChange);


        ImageIcon imageIcon = new ImageIcon(image);
        imageLabel = new JLabel( imageIcon );
        scrollPane1.setViewportView(imageLabel);
        imageLabel.addMouseWheelListener(this::onMouseWheelScroll);

        imageIcon = new ImageIcon();
        imageLabel2 = new JLabel(imageIcon);
        scrollPane2.setViewportView(imageLabel2);
        imageLabel2.addMouseWheelListener(this::onMouseWheelScroll);

        JPanel edit = new JPanel();
        AbstractTableModel tableModel = new FilterTableModel();
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
        tableModel.addTableModelListener(e->{
            resizeImage();
        });
        edit.add(rowTable);
        p.add(edit,BorderLayout.SOUTH);

        JPanel calc = new JPanel();
        calc.setPreferredSize(new Dimension(600,200));
        JTextField kField = new JTextField("255",5);


        JTextField sigmaField = new JTextField("1.0", 5);


        JLabel sigma2label = new JLabel("  Sigma 2:");
        calc.add(sigma2label);
        JTextField sigma2Field = new JTextField("2.0", 5);


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


        calc.add(filterSelect);
        calc.add(new JLabel("  K:"));
        calc.add(kField);
        calc.add(new JLabel("  Sigma 1:"));
        calc.add(sigmaField);
        calc.add(sigma2label);
        calc.add(sigma2Field);



        calc.add(new JButton(new AbstractAction("Calculate") {
            @Override
            public void actionPerformed(ActionEvent event) {
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
                        calulateDoGTable(sigma,sigma2, k);
                    }
                    else {
                        calulateLoGTable(sigma,k);
                    }
                    tableModel.fireTableDataChanged();
                }
                catch (Exception ex){}
            }
        }));
        edit.add(calc);

        calc.add(new JButton(new AbstractAction("Clear") {

            @Override
            public void actionPerformed(ActionEvent e) {
                Arrays.fill(rowData,0);
                tableModel.fireTableDataChanged();
            }
        }));
    }

    private void calulateDoGTable(double sigma1, double sigma2, double k) {
        double twoSigmaSq = 2*sigma1*sigma1;

        int inx=0;
        int sum = 0;
        for(inx=0 ; inx<this.rowData.length-1;inx++) {
            int x = inx%KERNEL_SIZE - KERNEL_SIZE/2;
            int y = inx/KERNEL_SIZE - KERNEL_SIZE/2;
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


    private void calulateLoGTable(double sigma, double k) {
        double twoSigmaSq = 2*sigma*sigma;

        int inx=0;
        int sum = 0;
        for(inx=0 ; inx<this.rowData.length-1;inx++) {
                int x = inx%KERNEL_SIZE - KERNEL_SIZE/2;
                int y = inx/KERNEL_SIZE - KERNEL_SIZE/2;
                double a = (x * x + y * y) / twoSigmaSq;
                double v = 4 * k / Math.PI / twoSigmaSq / twoSigmaSq * (1 - a) * Math.exp(-a);
                int iv = (int) Math.round(v);
                rowData[inx] = iv;
                sum+=iv;
        }
        rowData[rowData.length-1] = -sum * 2;

    }


    public void resizeImage() {
        AffineTransform at = new AffineTransform();
        at.scale(ZOOMS[zoom]/100.0, ZOOMS[zoom]/100.0);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage after = scaleOp.filter(image,null);
        imageLabel.setIcon(new ImageIcon(after));

        Kernel kernel = convertRowToKernel();

        ConvolveOp convolveOp = new ConvolveOp(kernel);
        BufferedImage filteredImage = convolveOp.filter(image, null);
        BufferedImage after2 = scaleOp.filter(filteredImage, null);
        imageLabel2.setIcon(new ImageIcon(after2));
    }

    protected Kernel convertRowToKernel() {
        float[] kernelData = new float[rowData.length*2-1];
        for(int i=0;i<rowData.length;i++) {
            kernelData[i] = rowData[i]/NORMALIZATION;
            kernelData[kernelData.length-1-i] = kernelData[i];
        }
        kernelData[rowData.length-1]+=1.0f;
        return new Kernel(KERNEL_SIZE,KERNEL_SIZE,kernelData);
    }


    public void onMouseWheelScroll(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        int temp = zoom + notches;
        temp = Math.min(temp, ZOOMS.length-1);
        temp = Math.max(temp, 0);
        if (temp != zoom) {
            zoom = temp;
            setTitle(ZOOMS[zoom]+"%");
            resizeImage();
        }
    }

    public void portChange(ChangeEvent e) {
        JScrollPane src, dst;
        if(e.getSource()==scrollPane1.getViewport()) {
            src=scrollPane1;
            dst=scrollPane2;
        }
        else {
            src=scrollPane2;
            dst=scrollPane1;
        }
        dst.getHorizontalScrollBar().setValue(src.getHorizontalScrollBar().getValue());
        dst.getVerticalScrollBar().setValue(src.getVerticalScrollBar().getValue());
    }

    public void setRow(int[] row) {
        System.arraycopy(row,0,this.rowData,0,this.rowData.length);
        resizeImage();
    }

    public int[] getRow() {
        int[] dest = new int[rowData.length];
        System.arraycopy(rowData,0,dest,0,this.rowData.length);
        return dest;
    }


    public static void main(String[] args) throws IOException {
        FilterPreviewFrame f = new FilterPreviewFrame("hi", "samples/cat.jpg");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);

        int[] data = new int[] {
                 0, -3, -9,-13, -9, -3,  0,
                -3,-18,-40,-44,-40,-18, -3,
                -9,-40,  0, 99,  0,-40, -9,
               -13,-44, 99,320
        };
        f.setRow(data);
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
