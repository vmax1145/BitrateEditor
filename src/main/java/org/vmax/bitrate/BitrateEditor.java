package org.vmax.bitrate;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.vmax.bitrate.bitrateui.CalcDialog;
import org.vmax.bitrate.bitrateui.EditorPanel;
import org.vmax.bitrate.bitrateui.MenuBuilder;
import org.vmax.bitrate.bitrateui.VerifyException;
import org.vmax.bitrate.cfg.Config;
import org.vmax.bitrate.cfg.Verify;
import org.vmax.bitrate.plugins.PostProcessor;
import org.vmax.bitrate.plugins.PreProcessor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Service
public class BitrateEditor extends JFrame {

    private byte[] fwBytes;

    public BitrateEditor(Config cfg, Bitrate[] bitrates, byte[] fwBytes)  {
        this.fwBytes = fwBytes;
        if(cfg.getNote()!=null) {
            setTitle("BitrateEditor : "+cfg.getNote());
        }
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
        byte[] fwBytes = FileUtils.readFileToByteArray(f);

        if(cfg.getPreProcessor()!=null) {
            PreProcessor preprocessor = (PreProcessor) Class.forName(cfg.getPreProcessor().getClassName()).newInstance();
            preprocessor.withConfig(cfg);
            preprocessor.preprocess(fwBytes);
        }
FileUtils.writeByteArrayToFile(new File("preprocessed"),fwBytes);

        verifyFirmware(cfg, fwBytes);

        Bitrate[] bitrates = getBitrates(cfg,fwBytes);


        SwingUtilities.invokeLater(() -> new BitrateEditor(cfg, bitrates, fwBytes));

    }

    private static Bitrate[] getBitrates(Config cfg, byte[] fw) throws Exception {
        Bitrate[] bitrates;

        bitrates = new Bitrate[cfg.getVideoModes().length];

        int step=16*cfg.getQualities().length;
        int tableStartAddr = cfg.getBitratesTableAddress(); //tableStartAddr
        for(int i=0;i<cfg.getVideoModes().length;i++) {

            int rowAddr = tableStartAddr + i * step;
            int type = (int) Utils.readUInt(fw, rowAddr);
            float min = Utils.readFloat(fw,rowAddr+8);
            float max = Utils.readFloat(fw,rowAddr+12);

            float[] mbps = new float[cfg.getQualities().length];
            for(int j=0;j<cfg.getQualities().length; j++) {
                if(type != (int) Utils.readUInt(fw,rowAddr )) {
                    System.out.println("Addr:"+rowAddr+" Bitrate type is different for:"+ cfg.getVideoModes()[i].getName());
                }
                if(min != Utils.readFloat(fw,rowAddr + 8 )) {
                    System.out.println("Addr:"+rowAddr+" Min value is different for:"+ cfg.getVideoModes()[i].getName());
                }
                if(max != Utils.readFloat(fw,rowAddr + 12 )) {
                    System.out.println("Addr:"+rowAddr+" Max value is different for:"+ cfg.getVideoModes()[i].getName());
                }

                mbps[j] = Utils.readFloat(fw, rowAddr + 4 );
                rowAddr += 16;
            }

            if(type >= Bitrate.Type.values().length || type<0) {
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
                    bitrates[i].getGop()[j] = (int) Utils.readUInt(fw, addr);
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

    private static void verifyFirmware(Config cfg, byte[] fwBytes) throws IOException, VerifyException, NoSuchAlgorithmException {


        for(Verify verify : cfg.getVerify()) {
            if(verify.getVal()!=null) {
                byte[] bytes = verify.getVal().getBytes("ASCII");

                for (int i = 0, addr = verify.getAddr(); i < bytes.length; i++, addr++) {
                    byte b = bytes[i];
                    if (fwBytes[addr] != b) {
                        throw new VerifyException("Verify fail:" + verify.getVal());
                    }
                }
            }
            else if(verify.getCrc()!=null) {
                // expected 1783079994
                long expectedCrc = Utils.readUInt(fwBytes,verify.getAddr());
                CRC32 crc = new CRC32();
                crc.update(fwBytes, verify.getCrc().getFromAddr(), verify.getCrc().getLen());
                long crcActual = crc.getValue();
                if(crcActual!=expectedCrc) {
                    throw new VerifyException("Verify CRC fail:" + verify.getAddr()+" expected:"+expectedCrc+" actual:"+crcActual);
                }
            }
        }

        //verify section crc
        Utils.crcCheck(fwBytes, cfg.getSectionStartAddr()+0x100, cfg.getSectionLen()-0x100, cfg.getSectionCrcAddr());

        if(cfg.getMd5fileName()!=null) {
            byte[] digest = Utils.calculateDigest(fwBytes);
            //System.out.println("File digest: " + Utils.hex(digest));
            byte[] check = FileUtils.readFileToByteArray(new File(cfg.getMd5fileName()));
            if(!Arrays.equals(digest,check)) {
                System.out.println("File md5 digest mismatch");
                return;
            }
        }
    }

    public void updateFW(Config cfg, Bitrate[] bitrates) {
        try {
            byte[] fwBytes = Arrays.copyOf(this.fwBytes, this.fwBytes.length);
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

            for(Bitrate bitrate : bitrates) {
                for(int j=0;j<cfg.getQualities().length; j++) {
                    int rowAddr = cfg.getBitratesTableAddress()+(bitrate.getInx()*cfg.getQualities().length+j)*16;
                    Utils.writeUInt(fwBytes, rowAddr, bitrate.getType().ordinal());
                    Utils.writeFloat(fwBytes, rowAddr+4, bitrate.getMbps()[j]);
                    Utils.writeFloat(fwBytes, rowAddr+8, bitrate.getMin());
                    Utils.writeFloat(fwBytes, rowAddr+12, bitrate.getMax());
                }
            }

            if(cfg.getGopTableAddress() > 0) {
                int addr = cfg.getGopTableAddress();
                for (int i = 0; i < bitrates.length; i++) {
                    for (int j = 0; j < 3; j++) {
                        Utils.writeUInt(fwBytes, addr, bitrates[i].getGop()[j]);
                        addr += 4;
                    }
                    addr += 4;
                }
            }


            Utils.crcSet(fwBytes, cfg.getSectionStartAddr()+0x100, cfg.getSectionLen()-0x100, cfg.getSectionCrcAddr());

            if(cfg.getPostProcessor()!=null) {
                PostProcessor postprocessor = (PostProcessor) Class.forName(cfg.getPostProcessor().getClassName()).newInstance();
                postprocessor.withConfig(cfg);
                postprocessor.postprocess(fwBytes);
            }

            FileUtils.writeByteArrayToFile(out, fwBytes);
            if(cfg.getMd5fileName()!=null) {
                byte[] md5 = Utils.calculateDigest(fwBytes);
                try(FileOutputStream fos = new FileOutputStream(ch)) {
                    fos.write(md5);
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }



}
