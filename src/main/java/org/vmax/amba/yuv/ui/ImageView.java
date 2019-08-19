package org.vmax.amba.yuv.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

public class ImageView extends JPanel {

    private BufferedImage image;
    private int Y,U,V,Yoffset,Uoffset,Voffset;
    private RGBImageFilter imageFilter;

    public ImageView(String imagePath, int Y,int U, int V, int Yoffset, int Uoffset, int Voffset) {
        try {
            image = ImageIO.read(new File(imagePath));
            this.Y = Y;
            this.U = U;
            this.V = V;
            this.Yoffset = Yoffset;
            this.Uoffset = Uoffset;
            this.Voffset = Voffset;

            this.imageFilter = new RGBImageFilter(){
                @Override
                public int filterRGB(int x, int y, int rgb) {
                    return rgb;
                }
            };

        } catch (IOException ex) {
            System.out.println("Sample image file not exists:"+imagePath);
            System.exit(0);
        }

    }

    public void valuesChanged(int inY,int inU, int inV, int inYoffset, int inUoffset, int inVoffset) {
        
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        final ImageProducer ip = new FilteredImageSource(image.getSource(), imageFilter);
        Image im = Toolkit.getDefaultToolkit().createImage(ip);
        g.drawImage(im, 0, 0, this); // see javadoc for more info on the parameters
    }



}
