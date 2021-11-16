package org.vmax.amba.convolution;

import com.sun.prism.image.ViewPort;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
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
    private int[] ZOOMS = {50,100,200,400,800};
    private JLabel imageLabel;
    private JLabel imageLabel2;
    private BufferedImage image;
    private JScrollPane scrollPane1, scrollPane2;
    private int zoom = 1;
    private Kernel kernel;

    public FilterPreviewFrame(String title, String inputImage) throws HeadlessException, IOException {
        super(title);
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

    }


    public void resizeImage() {
        AffineTransform at = new AffineTransform();
        at.scale(ZOOMS[zoom]/100.0, ZOOMS[zoom]/100.0);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage after = scaleOp.filter(image,null);
        imageLabel.setIcon(new ImageIcon(after));
        if(kernel!=null) {
            ConvolveOp convolveOp = new ConvolveOp(kernel);
            BufferedImage filteredImage = convolveOp.filter(image, null);
            BufferedImage after2 = scaleOp.filter(filteredImage, null);
            imageLabel2.setIcon(new ImageIcon(after2));
        }
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

    public void setKernel(Kernel k) {
        this.kernel = k;
        if(k!=null) {
            resizeImage();
        }
    }

    public static void main(String[] args) throws IOException {
        FilterPreviewFrame f = new FilterPreviewFrame("hi","samples/cat.jpeg");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);

        float[] data = new float[] {
                3,  3,  3,  3,  3,  3,  3,
                3,-16,-16,-16,-16,-16,  3,
                3,-16, -8, -8, -8,-16,  3,
                3,-16, -8, 248,-8,-16,  3,
                3,-16, -8, -8, -8,-16,  3,
                3,-16,-16,-16,-16,-16,  3,
                3,  3,  3,  3,  3,  3,  3
        };
        for(int i=0;i<49;i++) {
            data[i]=data[i]/256;
        }
        data[25]+=1.0f;

        Kernel k = new Kernel(7,7,data);
        f.setKernel(k);
    }




}
