package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.generic.GenericImageTab;
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

public class MiLogoTab extends JPanel implements GenericImageTab {
    private final byte[] fwBytes;
    private MiLogoImageConfig cfg;
    private JTextField nomer;
    private JLabel render;
    private Variant selectedVariant;
    private BufferedImage image;

    public static final String LOAD_FROM_FILE = "Загрузить из файла";

    public static Dimension[] DIMENSIONS = {
            new Dimension(150, 60),
            new Dimension(230, 90),
            new Dimension(300, 124),
            new Dimension(316, 130)
    };


    private final static Map<String, Variant> VARIANTS = new LinkedHashMap<String, Variant>() {
        {
            put("Российский номер", new Variant("http://line4auto.ru/nomer/nom_{0}_{1}.png", "х777хх 177")

            );
            put("Украинский номер", new Variant("http://line4auto.ru/nomer/nomua_{0}_{1}_{2}.png", "AA 3223 ЕУ")

            );
            put("Белорусский номер", new Variant("http://line4auto.ru/nomer/nomby_{0}+{1}.png", "0111 EA-1")
            );

            put(LOAD_FROM_FILE, new Variant("", "")
            );
        }
    };


    public MiLogoTab(MiLogoImageConfig cfg, byte[] fwBytes) {
        super(new SpringLayout());
        this.fwBytes = fwBytes;
        //System.out.println(findAddr(cfg, fwBytes));

        this.cfg = cfg;
        add(new JLabel());
        JComboBox<String> variant = new JComboBox<>(VARIANTS.keySet().toArray(new String[0]));
        add(variant);

        add(new JLabel("номер", JLabel.TRAILING));
        nomer = new JTextField("х111xx");
        add(nomer);

        add(new JLabel());

        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("Generate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                processImageCreation();
            }
        }));
        add(p);


        add(new JLabel());

        image = loadFromFirmware();
        render = new JLabel(new ImageIcon(image));
        render.setMinimumSize(new Dimension(cfg.getDimension().getWidth() * 3 / 2, cfg.getDimension().getHeight() * 3 / 2));
        render.setPreferredSize(new Dimension(cfg.getDimension().getWidth() * 3 / 2, cfg.getDimension().getHeight() * 3 / 2));
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

            String key = (String) variant.getSelectedItem();
            MiLogoTab.this.selectedVariant = VARIANTS.get(key);
            nomer.setText(MiLogoTab.this.selectedVariant.getExample());
            if (LOAD_FROM_FILE.equals(key)) {
                processImageCreation();
            }
        });
        variant.setSelectedIndex(0);
    }

//    private List<String> findAddr(MiLogoImageConfig cfg, byte[] fwBytes) {
//        byte[] find = new byte[6 * Integer.BYTES];
//        ByteBuffer bb = ByteBuffer.wrap(find);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//        bb.putInt(cfg.getDimension().getHeight());
//        bb.putInt(cfg.getDimension().getWidth());
//        bb.putInt(2);
//        bb.putInt(0xFD80FD80);
//        bb.putInt(0xFD80FD80);
//        bb.putInt(0xFD80FD80);
//        find = bb.array();
//        return Utils.findArray(fwBytes, find).stream().map(Utils::hex).collect(Collectors.toList());
//    }

    private void processImageCreation() {
        if (MiLogoTab.this.selectedVariant != null) {
            try {
                BufferedImage bim;
                if (selectedVariant == VARIANTS.get(LOAD_FROM_FILE)) {
                    bim = loadFromExternalFile();
                } else {
                    bim = generate(selectedVariant.template, nomer.getText());
                }

                if (bim != null) {

                    bim = convertImage(bim);
                    if (bim.getWidth() * bim.getHeight() > cfg.getDimension().getWidth() * cfg.getDimension().getHeight()) {
                        JOptionPane.showMessageDialog(null, "Слишком большая картинка", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    } else {
                        ImageIcon ico = new ImageIcon(bim);
                        render.setMinimumSize(new Dimension(bim.getWidth(), bim.getHeight()));
                        render.setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));
                        render.setIcon(ico);
                        render.getParent().doLayout();
                        MiLogoTab.this.image = bim;
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
        if (nomer.length != count) {
            JOptionPane.showMessageDialog(this, "Неправильный формат номера", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return null;
        } else {
            for (int i = 0; i < nomer.length; i++) {
                nomer[i] = URLEncoder.encode(nomer[i], "cp1251");
            }
            String u = new MessageFormat(genUrl).format(nomer);
            return ImageIO.read(new URL(u));
        }
    }


    private BufferedImage convertImage(BufferedImage bim) {
        int scaledW, scaledH;
        if (1.0 * bim.getWidth() / cfg.getDimension().getWidth() > 1.0 * bim.getHeight() / cfg.getDimension().getHeight()) {
            scaledW = cfg.getDimension().getWidth();
            scaledH = bim.getHeight() * cfg.getDimension().getWidth() / bim.getWidth();
        }
        else {
            scaledW = bim.getWidth() * cfg.getDimension().getHeight() / bim.getHeight();
            scaledH = cfg.getDimension().getHeight();
        }
        Image im = bim.getScaledInstance(scaledW,scaledH,Image.SCALE_SMOOTH);

        BufferedImage converted = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_USHORT_555_RGB);
        Graphics g = converted.getGraphics();
        g.drawImage(im, 0, 0, null);
        g.dispose();
        return converted;
    }

    private BufferedImage loadFromExternalFile() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "image files",
                "bmp", "jpg", "jpeg", "gif", "tiff", "png"
        ));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            BufferedImage bim = ImageIO.read(f);
            nomer.setText(f.getName());

            return bim;
        }
        return null;
    }

    private BufferedImage loadFromFirmware() {
        ByteBuffer bb = ByteBuffer.wrap(fwBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.position(cfg.getAddr());
        int h = bb.getInt();
        int w = bb.getInt();
        bb.getInt(); //skip
        short[] data = new short[w * h];
        for (int i = 0; i < data.length; i++) {
            data[i] = bb.getShort();
        }

        BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_555_RGB);
        WritableRaster raster = bim.getRaster();
        raster.setDataElements(0, 0, w, h, data);
        System.out.println(cfg.getAddr()+":"+w+"*"+h);
        return bim;
    }

    @Override
    public void updateFW() {
        if(image!=null) {
            int w = image.getWidth();
            int h = image.getHeight();

            short[] data = new short[w * h];
            image.getRaster().getDataElements(0, 0, w, h, data);

            ByteBuffer bb = ByteBuffer.wrap(fwBytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.position(cfg.getAddr());
            bb.putInt(h);
            bb.putInt(w);
            bb.putInt(2);
            for (short pix : data) {
                bb.putShort((short) (pix&0xFFFE));
            }
            System.out.println(cfg.getAddr()+":"+w+"*"+h);
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTabLabel() {
        return cfg.getLabel()+" ("+cfg.getDimension().getWidth()+"x"+cfg.getDimension().getHeight()+")";
    }


//    private static void saveBinaryImage(File f, BufferedImage bim) throws IOException {
//        int w = bim.getWidth();
//        int h = bim.getHeight();
//        byte[] fwBytes = new byte[w*h*Short.BYTES+3*Integer.BYTES];
//        short[] data = new short[w*h];
//        bim.getRaster().getDataElements(0,0,w,h,data);
//
//        ByteBuffer bb = ByteBuffer.wrap(fwBytes,0, fwBytes.length);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//
//        bb.putInt(h);
//        bb.putInt(w);
//        bb.putInt(2);
//        for (short pix : data) {
//            bb.putShort(pix);
//        }
//        FileUtils.writeByteArrayToFile(f, bb.array());
//    }

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

}
