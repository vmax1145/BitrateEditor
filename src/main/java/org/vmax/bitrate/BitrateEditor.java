package org.vmax.bitrate;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.vmax.bitrate.bitrateui.BitratesTableModel;
import org.vmax.bitrate.bitrateui.EditorPanel;
import org.vmax.bitrate.bitrateui.VerifyException;
import org.vmax.bitrate.cfg.Config;
import org.vmax.bitrate.cfg.Verify;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;

@Service
public class BitrateEditor extends JFrame {


    public BitrateEditor(Config cfg, Bitrate[] bitrates) throws HeadlessException {

        EditorPanel editorPanl = new EditorPanel(cfg, bitrates);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

        fileMenu.add(new AbstractAction("Save") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 updateFW(cfg,bitrates);
             }
        });

        fileMenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });


        JMenu advancedMenu = new JMenu("Advanced");
        advancedMenu.add(new AbstractAction("Generate test bitrates") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DetectGenerator.generate(bitrates);
                ((BitratesTableModel)(editorPanl.getModel())).fireTableDataChanged();
            }
        });
        bar.add(advancedMenu);




        JScrollPane jsp = new JScrollPane(editorPanl);
        add(jsp, BorderLayout.CENTER);
        setJMenuBar(bar);
        pack();
        setVisible(true);
    }


    public static void main(String args[]) throws Exception {
        System.out.println("BitrateEditor by v_max");
        if(args.length<1) {
            System.out.println("config name required");
            return;
        }
        Config cfg = Config.readConfig(args[0]);
        File f = new File(cfg.getFwFileName());
        if(!f.exists()) {
            System.out.println("FW file "+cfg.getFwFileName()+" not found");
            return;
        }
        Bitrate[] bitrates;
        try (RandomAccessFile raf = new RandomAccessFile(f,"r")) {
            bitrates =getBitrates(cfg, raf);

            if(cfg.getMd5fileName()!=null) {
                byte[] digest = calculateDigest(raf);
                System.out.print("File digest: " + Utils.hex(digest));
                byte[] check = FileUtils.readFileToByteArray(new File(cfg.getMd5fileName()));
                if(!Arrays.equals(digest,check)) {
                    System.out.println("File md5 digest mismatch");
                    return;
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            new BitrateEditor(cfg, bitrates);
        });

    }

    private static byte[] calculateDigest(RandomAccessFile raf) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        raf.seek(0);
        byte[] buf = new byte[4096];
        int nread;
        while((nread = raf.read(buf))>=0) {
            md.update(buf,0,nread);
        }
        byte[] digest = md.digest();
        byte[] digestReversed = new byte[digest.length];
        for (int i = 0; i < digest.length; i++) {
            digestReversed[i] = digest[i / 4 * 4 + 3 - (i % 4)];

        }
        return digestReversed;
    }

    private static Bitrate[] getBitrates(Config cfg, RandomAccessFile raf) throws Exception {
        Bitrate[] bitrates;
        if (!verifyCheckSum(cfg, raf)) {
            System.out.println("Error verify fw file:");
            throw new VerifyException("Verify fail: checksum");
        }


        for(Verify verify : cfg.getVerify()) {
            byte[] bytes = verify.getVal().getBytes("ASCII");
            raf.seek(verify.getAddr());
            for(byte b : bytes) {
                if(raf.read() != (b & 0xff)) {
                    throw new VerifyException("Verify fail:"+verify.getVal());
                }
            }
        }

        bitrates = new Bitrate[cfg.getVideoModes().length];


        int step=16*cfg.getQualities().length;
        int tableStartAddr = cfg.getBitratesTableAddress(); //tableStartAddr
        for(int i=0;i<cfg.getVideoModes().length;i++) {

            int type = (int) Utils.readUInt(raf,tableStartAddr+i*step);
            float min = Utils.readFloat(raf,tableStartAddr+i*step+8);
            float max = Utils.readFloat(raf,tableStartAddr+i*step+12);

            float[] mbps = new float[cfg.getQualities().length];
            for(int j=0;j<cfg.getQualities().length; j++) {
                if(type != (int) Utils.readUInt(raf,tableStartAddr+i*step+ j*16)) {
                    System.out.println("Bitrate type is different for:"+ cfg.getVideoModes()[i]);
                }
                if(min != Utils.readFloat(raf,tableStartAddr+i*step+8 + j*16)) {
                    System.out.println("Min value is different for:"+ cfg.getVideoModes()[i]);
                }
                if(max != Utils.readFloat(raf,tableStartAddr+i*step+12 + j*16)) {
                    System.out.println("Max value is different for:"+ cfg.getVideoModes()[i]);
                }

                mbps[j] = Utils.readFloat(raf, tableStartAddr + i * step + 4 + j*16);
            }

            bitrates[i] = new Bitrate();
            bitrates[i].setName(cfg.getVideoModes()[i].getName());
            bitrates[i].setMbps(mbps);
            bitrates[i].setType(Bitrate.Type.values()[type]);
            bitrates[i].setMin(min);
            bitrates[i].setMax(max);
            bitrates[i].setInUse(cfg.getVideoModes()[i].isInUse());

            System.out.println(i+". "+cfg.getVideoModes()[i].getName() +" " +type+" "+mbps[0]+"/"+mbps[1]+"/"+mbps[2]+" "+min+" "+max);
        }
        return bitrates;
    }

    private static boolean verifyCheckSum(Config cfg, RandomAccessFile raf) throws IOException {

        CRC32 sectionCrc = calculateSectionCrc32(cfg, raf);
        return sectionCrc.getValue() == Utils.readUInt(raf,cfg.getSectionCrcAddr());
    }

    private static CRC32 calculateSectionCrc32(Config cfg, RandomAccessFile raf) throws IOException {
        byte[] section = new byte[cfg.getSectionLen()-0x100];
        raf.seek(cfg.getSectionStartAddr()+0x100);
        raf.read(section);
        CRC32 sectionCrc = new CRC32();
        sectionCrc.update(section);
        return sectionCrc;
    }


    private void updateFW(Config cfg, Bitrate[] bitrates)  {
        try {
            File out = new File(cfg.getFwFileName() + ".mod");
            if(out.exists()) {
                JOptionPane.showMessageDialog(this,"File "+out.getName()+" already exists");
                return;
            }
            File ch = new File(cfg.getMd5fileName()+".mod");
            if(cfg.getMd5fileName()!=null && ch.exists()) {
                JOptionPane.showMessageDialog(this,"File "+ch.getName()+" already exists");
                return;
            }

            FileUtils.copyFile(new File(cfg.getFwFileName()), out);
            try(RandomAccessFile raf = new RandomAccessFile(out,"rw")) {
                for(int i=0;i<cfg.getVideoModes().length;i++) {
                    for(int j=0;j<cfg.getQualities().length; j++) {
                        int rowAddr = cfg.getBitratesTableAddress()+(i*cfg.getQualities().length+j)*16;
                        Utils.writeUInt(raf, rowAddr, bitrates[i].getType().ordinal());
                        Utils.writeFloat(raf, rowAddr+4, bitrates[i].getMbps()[j]);
                        Utils.writeFloat(raf, rowAddr+8, bitrates[i].getMin());
                        Utils.writeFloat(raf, rowAddr+12, bitrates[i].getMax());
                    }
                }
                CRC32 crc = calculateSectionCrc32(cfg,raf);
                Utils.writeUInt(raf,cfg.getSectionCrcAddr(), (int) crc.getValue());

                if(cfg.getMd5fileName()!=null) {
                    byte[] md5 = calculateDigest(raf);
                    try(FileOutputStream fos = new FileOutputStream(ch)) {
                        fos.write(md5);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }


}
