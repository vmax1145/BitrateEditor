package org.vmax.amba.bitrate;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.bitrate.BitrateEditorConfig;
import org.vmax.amba.cfg.FirmwareConfig;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class BitrateTool extends FirmwareTool<BitrateEditorConfig> {

    @Getter
    public final String startMessage =
                    "  ____  _ _             _       ______    _ _ _             \n" +
                    " |  _ \\(_) |           | |     |  ____|  | (_) |            \n" +
                    " | |_) |_| |_ _ __ __ _| |_ ___| |__   __| |_| |_ ___  _ __ \n" +
                    " |  _ <| | __| '__/ _` | __/ _ \\  __| / _` | | __/ _ \\| '__|\n" +
                    " | |_) | | |_| | | (_| | ||  __/ |___| (_| | | || (_) | |   \n" +
                    " |____/|_|\\__|_|  \\__,_|\\__\\___|______\\__,_|_|\\__\\___/|_|   \n" +
                    "                                                            \n" +
                    "                                                            \n" +
                    "************************************************************\n"+
                    "*  Author do not take any responsibility and isn't liable  *\n"+
                    "*   for any damage or loss caused by using this software.  *\n"+
                    "*                                                          *\n"+
                    "*      !!! All you are doing is at your own risk !!!       *\n"+
                    "*                                                          *\n"+
                    "************************************************************\n";


    private byte[] fwBytes;

    @Override
    public Class<BitrateEditorConfig> getConfigClz() {
        return BitrateEditorConfig.class;
    }

    public void init(FirmwareConfig fcfg, byte[] fwBytes) {
        BitrateEditorConfig cfg = (BitrateEditorConfig) fcfg;
        Bitrate[] bitrates = null;
        this.fwBytes = fwBytes;
        if(cfg.getNote()!=null) {
            setTitle("BitrateEditor : "+cfg.getNote());
        }

        try {
            bitrates = getBitrates(cfg, fwBytes);
        } catch (Exception e) {
            System.out.println("Error reading bitrates");
            e.printStackTrace();
            System.exit(0);
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



    private Bitrate[] getBitrates(BitrateEditorConfig cfg, byte[] fw)  {
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


    public void updateFW(BitrateEditorConfig cfg, Bitrate[] bitrates) {
        try {
            byte[] fwBytes = Arrays.copyOf(this.fwBytes, this.fwBytes.length);

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

            Utils.saveFirmware(cfg, fwBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }

}
