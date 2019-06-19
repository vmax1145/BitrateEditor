package org.vmax.bitrate;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.vmax.bitrate.bitrateui.CalcDialog;
import org.vmax.bitrate.bitrateui.EditorPanel;
import org.vmax.bitrate.bitrateui.MenuBuilder;
import org.vmax.bitrate.bitrateui.VerifyException;
import org.vmax.bitrate.cfg.Config;
import org.vmax.bitrate.cfg.Verify;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Service
public class BitrateEditor extends JFrame {


    public BitrateEditor(Config cfg, Bitrate[] bitrates) throws HeadlessException {

        Bitrate[] bitratesFiltered = Arrays.asList(bitrates)
                .stream().filter(b->b.isInUse()).collect(Collectors.toList()).toArray(new Bitrate[0]);

        EditorPanel editorPanel = new EditorPanel(cfg, bitratesFiltered);

        CalcDialog calcDialog = new CalcDialog(this, editorPanel, cfg, bitrates);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JMenuBar bar = new MenuBuilder(this)
                .with(editorPanel)
                .with(cfg)
                .with(bitrates,bitratesFiltered)
                .with(calcDialog)
                .build();


        JScrollPane jsp = new JScrollPane(editorPanel);
        jsp.setPreferredSize(new Dimension(800,500));
        add(jsp, BorderLayout.CENTER);
        setJMenuBar(bar);
        pack();
        setVisible(true);



    }


    public static void main(String args[]) throws Exception {
        System.out.println("  ____  _ _             _       ______    _ _ _             \n" +
                           " |  _ \\(_) |           | |     |  ____|  | (_) |            \n" +
                           " | |_) |_| |_ _ __ __ _| |_ ___| |__   __| |_| |_ ___  _ __ \n" +
                           " |  _ <| | __| '__/ _` | __/ _ \\  __| / _` | | __/ _ \\| '__|\n" +
                           " | |_) | | |_| | | (_| | ||  __/ |___| (_| | | || (_) | |   \n" +
                           " |____/|_|\\__|_|  \\__,_|\\__\\___|______\\__,_|_|\\__\\___/|_|   \n" +
                           "                                                            \n" +
                           "                                                            ");
        System.out.println("************************************************************");
        System.out.println("*  Author do not take any responsibility and isn't liable  *");
        System.out.println("*   for any damage or loss caused by using this software.  *");
        System.out.println("*                                                          *");
        System.out.println("*      !!! All you are doing is at your own risk !!!       *");
        System.out.println("*                                                          *");
        System.out.println("************************************************************");
        System.out.println();
        System.out.print("Are you sure you want to continue (Y/N):");
        char[] in = new char[1];
        new InputStreamReader(System.in).read(in);
        if(!(in[0]=='Y' || in[0]=='y')) {
            return;
        }

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
            verifyFirmware(cfg, raf);
            bitrates =getBitrates(cfg, raf);

        }

        SwingUtilities.invokeLater(() -> new BitrateEditor(cfg, bitrates));

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

        bitrates = new Bitrate[cfg.getVideoModes().length];

        int step=16*cfg.getQualities().length;
        int tableStartAddr = cfg.getBitratesTableAddress(); //tableStartAddr
        for(int i=0;i<cfg.getVideoModes().length;i++) {

            int rowAddr = tableStartAddr + i * step;
            int type = (int) Utils.readUInt(raf, rowAddr);
            float min = Utils.readFloat(raf,rowAddr+8);
            float max = Utils.readFloat(raf,rowAddr+12);

            float[] mbps = new float[cfg.getQualities().length];
            for(int j=0;j<cfg.getQualities().length; j++) {
                if(type != (int) Utils.readUInt(raf,rowAddr )) {
                    System.out.println("Addr:"+rowAddr+" Bitrate type is different for:"+ cfg.getVideoModes()[i]);
                }
                if(min != Utils.readFloat(raf,rowAddr + 8 )) {
                    System.out.println("Addr:"+rowAddr+" Min value is different for:"+ cfg.getVideoModes()[i]);
                }
                if(max != Utils.readFloat(raf,rowAddr + 12 )) {
                    System.out.println("Addr:"+rowAddr+" Max value is different for:"+ cfg.getVideoModes()[i]);
                }

                mbps[j] = Utils.readFloat(raf, rowAddr + 4 );
                rowAddr += 16;
            }

            if(type >= Bitrate.Type.values().length) {
                System.out.println("Addr:"+rowAddr+" Bad bitrate type:"+ type+" forcing VBR");
                type = Bitrate.Type.VBR.ordinal();
            }

            bitrates[i] = new Bitrate();
            bitrates[i].setInx(i);
            bitrates[i].setName(cfg.getVideoModes()[i].getName());
            bitrates[i].setMbps(mbps);
            bitrates[i].setType(Bitrate.Type.values()[type]);
            bitrates[i].setMin(min);
            bitrates[i].setMax(max);
            bitrates[i].setInUse(cfg.getVideoModes()[i].isInUse());

            if(!bitrates[i].parseName()) {
                System.out.print("Addr:"+rowAddr+" unparceable ");
            }
            System.out.println(i+". "+cfg.getVideoModes()[i].getName() +" " +type+" "+mbps[0]+"/"+mbps[1]+"/"+mbps[2]+" "+min+" "+max);


        }

        int addr = cfg.getGopTableAddress();
        if(addr > 0) {
            for (int i = 0; i < bitrates.length; i++) {
                for (int j = 0; j < 3; j++) {
                    bitrates[i].getGop()[j] = (int) Utils.readUInt(raf, addr);
                    addr += 4;
                }
                addr += 4;
                if (bitrates[i].getGop()[1] != bitrates[i].getGop()[2]) {
                    System.out.println(i + " unexpected GOP values: " + bitrates[i].getGop()[0] + "/" + bitrates[i].getGop()[1] + "/" + bitrates[i].getGop()[2]);
                }
            }
        }
        return bitrates;
    }

    private static void verifyFirmware(Config cfg, RandomAccessFile raf) throws IOException, VerifyException, NoSuchAlgorithmException {
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

        if(cfg.getMd5fileName()!=null) {
            byte[] digest = calculateDigest(raf);
            //System.out.println("File digest: " + Utils.hex(digest));
            byte[] check = FileUtils.readFileToByteArray(new File(cfg.getMd5fileName()));
            if(!Arrays.equals(digest,check)) {
                System.out.println("File md5 digest mismatch");
                return;
            }
        }
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


    public void updateFW(Config cfg, Bitrate[] bitrates)  {
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
                for(Bitrate bitrate : bitrates) {
                    for(int j=0;j<cfg.getQualities().length; j++) {
                        int rowAddr = cfg.getBitratesTableAddress()+(bitrate.getInx()*cfg.getQualities().length+j)*16;
                        Utils.writeUInt(raf, rowAddr, bitrate.getType().ordinal());
                        Utils.writeFloat(raf, rowAddr+4, bitrate.getMbps()[j]);
                        Utils.writeFloat(raf, rowAddr+8, bitrate.getMin());
                        Utils.writeFloat(raf, rowAddr+12, bitrate.getMax());
                    }
                }

                if(cfg.getGopTableAddress() > 0) {
                    int addr = cfg.getGopTableAddress();
                    for (int i = 0; i < bitrates.length; i++) {
                        for (int j = 0; j < 3; j++) {
                            Utils.writeUInt(raf, addr, bitrates[i].getGop()[j]);
                            addr += 4;
                        }
                        addr += 4;
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
