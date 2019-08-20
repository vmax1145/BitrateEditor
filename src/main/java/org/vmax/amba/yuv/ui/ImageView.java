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
import java.util.List;

public class ImageView extends JPanel implements SliderValuesListener {

    private BufferedImage image;
    private int Y,U,V,Yoffset,Uoffset,Voffset;
    private int inY,inU,inV,inYoffset,inUoffset,inVoffset;
    private RGBImageFilter imageFilter;

    public ImageView(String imagePath, List<Integer> originalVals) {
        try {
            image = ImageIO.read(new File(imagePath));
            this.Y = originalVals.get(0);
            this.U = originalVals.get(1);;
            this.V = originalVals.get(2);
            this.Yoffset = originalVals.get(3);
            this.Uoffset = originalVals.get(4);
            this.Voffset = originalVals.get(5);

            this.inY=Y;
            this.inU=U;
            this.inV=V;
            this.inYoffset=Yoffset;
            this.inUoffset=Uoffset;
            this.inVoffset=Voffset;

            this.imageFilter = new RGBImageFilter(){
                @Override
                public int filterRGB(int x, int y, int rgb) {
                    return ImageView.this.filterRGB(x,y,rgb);
                }
            };

        } catch (IOException ex) {
            System.out.println("Sample image file not exists:"+imagePath);
            System.exit(0);
        }

    }

    private int filterRGB(int x, int y, int rgb) {
        float[] yuv = rgbToYCbCr(rgb);
        yuv[0] = (yuv[0]-16-Yoffset)*inY/Y + inYoffset+16;
        yuv[1] = (yuv[1]-128f-Uoffset)*inU/U + inUoffset + 128.f;
        yuv[2] = (yuv[2]-128f-Voffset)*inV/V + inVoffset + 128.f;
        return ycbcrToRgb(yuv);
    }


    @Override
    protected void paintComponent(Graphics g) {
        final ImageProducer ip = new FilteredImageSource(image.getSource(), imageFilter);
        Image im = Toolkit.getDefaultToolkit().createImage(ip);
        super.paintComponent(g);
        g.drawImage(im, 0, 0, this); // see javadoc for more info on the parameters
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    @Override
    public void valuesChanged(List<Integer> values) {

        this.inY=values.get(0);
        this.inU=values.get(1);
        this.inV=values.get(2);
        this.inYoffset=values.get(3);
        this.inUoffset=values.get(4);
        this.inVoffset=values.get(5);
        repaint();
    }


    private static float[] rgbToYCbCr( int rgb) {
        // multiply coefficients in book by 1024, which is 2^10
        int r = (rgb>>16)&0xff;
        int g = (rgb>>8)&0xff;
        int b = (rgb)&0xff;
        float[] yuv = new float[3];
        yuv[0] = (( 187*r + 629*g + 63*b ) >> 10) + 16;
        yuv[1] = ((-103*r - 346*g + 450*b) >> 10) + 128;
        yuv[2] = (( 450*r - 409*g - 41*b ) >> 10) + 128;
        return yuv;
    }

    private static int ycbcrToRgb( float[] yuv ) {
        // multiply coefficients in book by 1024, which is 2^10
        float y=yuv[0];
        y = 1191*(y - 16);
        if( y < 0 ) y = 0;
        float cb = yuv[1]-128;
        float cr = yuv[2]-128;

        int r = ((int)(y + 1836*cr)) >> 10;
        int g = ((int)(y - 547*cr - 218*cb)) >> 10;
        int b = ((int)(y + 2165*cb)) >> 10;

        if( r < 0 ) r = 0;
        else if( r > 255 ) r = 255;
        if( g < 0 ) g = 0;
        else if( g > 255 ) g = 255;
        if( b < 0 ) b = 0;
        else if( b > 255 ) b = 255;

        return (r<<16) | (g<<8) | b | 0xff000000;
    }

}
