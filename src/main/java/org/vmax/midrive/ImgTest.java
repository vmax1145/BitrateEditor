package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.vmax.amba.yuv.ui.SpringUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgTest extends JPanel {
    private final int maxPixels;
    private JTextField nomer;
    private JTextField region;
    private JLabel render;
    private Variant selectedVariant;
    private BufferedImage image;

    public static final String LOAD_FROM_FILE = "Загрузить из файла";
    private final static Map<String,Variant> VARIANTS = new LinkedHashMap<String , Variant>() {
        {
            put("Российский номер",  new Variant("http://line4auto.ru/nomer/nom_{0}_{1}.png", "х777хх 177")

            );
            put("Украинский номер",  new Variant("http://line4auto.ru/nomer/nomua_{0}_{1}_{2}.png", "AA 3223 ЕУ")

            );
            put("Белорусский номер", new Variant("http://line4auto.ru/nomer/nomby_{0}+{1}.png", "0111 EA-1")
            );

            put(LOAD_FROM_FILE, new Variant("", "")
            );
        }
    };


    public ImgTest(int maxPixels) {

        super(new SpringLayout());

        this.maxPixels = maxPixels;
        add(new JLabel());
        JComboBox<String> variant = new JComboBox<>(VARIANTS.keySet().toArray(new String[0]));
        add(variant);

        add(new JLabel("номер", JLabel.TRAILING));
        nomer = new JTextField("х111xx");
        add(nomer);

        add(new JLabel());

        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("Generate"){
            @Override
            public void actionPerformed(ActionEvent e) {
                processImageCreation();
            }
        }));
        add(p);


        add(new JLabel());


        render = new JLabel();
        render.setMinimumSize(new Dimension(300,60));
        render.setPreferredSize(new Dimension(300,60));
        p = new JPanel();
        p.add(render);
        add(p);
        setOpaque(true);
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                4, 2,         //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

        variant.addActionListener(e -> {

            String  key = (String) variant.getSelectedItem();
            ImgTest.this.selectedVariant = VARIANTS.get(key);
            nomer.setText(ImgTest.this.selectedVariant.getExample());
            if(LOAD_FROM_FILE.equals(key)) {
                processImageCreation();
            }
        });
        variant.setSelectedIndex(0);
    }

    private void processImageCreation() {
        if(ImgTest.this.selectedVariant!=null) {
            try {
                BufferedImage bim;
                if(selectedVariant == VARIANTS.get(LOAD_FROM_FILE)) {
                    bim = loadFromExternalFile();
                }
                else {
                    bim = generate(selectedVariant.template, nomer.getText());
                }

                if(bim!=null) {
                    if(bim.getWidth()*bim.getHeight() > maxPixels) {
                        JOptionPane.showMessageDialog(null, "Слишком большая картинка","Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        bim = convertImage(bim);
                        ImageIcon ico = new ImageIcon(bim);
                        render.setMinimumSize(new Dimension(bim.getWidth(), bim.getHeight()));
                        render.setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));
                        render.setIcon(ico);
                        render.getParent().doLayout();
                        ImgTest.this.image = bim;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Oooops! See error stream for details");
            }
        }
    }


    private BufferedImage generate(String genUrl, String nom) throws IOException {
        Pattern pattern = Pattern.compile("\\{\\d\\}");
        Matcher m = pattern.matcher(genUrl);
        int count = 0;
        while (m.find()) {
            count++;
        }
        String[] nomer = nom.split("\\s");
        if(nomer.length!=count) {
            JOptionPane.showMessageDialog(this,"Неправильный формат номера","Ошибка",JOptionPane.ERROR_MESSAGE);
            return null;
        }
        else {
            for (int i = 0; i < nomer.length; i++) {
                nomer[i] = URLEncoder.encode(nomer[i], "cp1251");
            }
            String u = new MessageFormat(genUrl).format(nomer);
            return ImageIO.read(new URL(u));
        }
    }


    private BufferedImage convertImage(BufferedImage bim) {
        BufferedImage converted = new BufferedImage(bim.getWidth(),bim.getHeight(),BufferedImage.TYPE_USHORT_555_RGB);
        Graphics g = converted.getGraphics();
        g.drawImage(bim,0,0,null);
        g.dispose();
        return converted;
    }

    private BufferedImage loadFromExternalFile() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "image files",
                "bmp","jpg","jpeg","gif","tiff","png"
        ));
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            BufferedImage bim = ImageIO.read(f);
            nomer.setText(f.getName());
            return bim;
        }
        return null;
    }


    private static BufferedImage loadBinaryImage(File f) throws IOException {
        byte[] fwBytes = FileUtils.readFileToByteArray(f);
        ByteBuffer bb = ByteBuffer.wrap(fwBytes,0, fwBytes.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int h = bb.getInt();
        int w = bb.getInt();
        bb.getInt(); //skip
        short[] data = new short[w*h];
        for(int i=0;i<data.length;i++) {
            data[i] = bb.getShort();
        }

        BufferedImage bim = new BufferedImage(w,h,BufferedImage.TYPE_USHORT_555_RGB);
        WritableRaster raster = bim.getRaster();
        raster.setDataElements(0,0,w,h,data);
        return bim;
    }

    private static void saveBinaryImage(File f, BufferedImage bim) throws IOException {
        int w = bim.getWidth();
        int h = bim.getHeight();
        byte[] fwBytes = new byte[w*h*Short.BYTES+3*Integer.BYTES];
        short[] data = new short[w*h];
        bim.getRaster().getDataElements(0,0,w,h,data);

        ByteBuffer bb = ByteBuffer.wrap(fwBytes,0, fwBytes.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        bb.putInt(h);
        bb.putInt(w);
        bb.putInt(2);
        for (short pix : data) {
            bb.putShort(pix);
        }
        FileUtils.writeByteArrayToFile(f, bb.array());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class Variant {
        private String template;
        private String example;

        Variant(String template, String example) {
            this.template = template;
            this.example = example;
        }
    }

    public static void main(String[] args) {

        ImgTest p = new ImgTest(9000);

        //Create and set up the window.
        JFrame frame = new JFrame("SpringForm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        frame.getContentPane().add(p);

        JMenuBar bar = new JMenuBar();
        JMenu m = new JMenu("File");
        bar.add(m);
        JMenuItem save = new JMenuItem("Save as binary logo");
        m.add(save);
        save.addActionListener(e-> {
            if(p.image!=null) {
                JFileChooser jfc = new JFileChooser();
                if(jfc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        saveBinaryImage(jfc.getSelectedFile(), p.image);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame,"Fail to save file","Error",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        frame.setJMenuBar(bar);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

}
