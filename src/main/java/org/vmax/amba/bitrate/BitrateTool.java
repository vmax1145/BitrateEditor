package org.vmax.amba.bitrate;

import lombok.Getter;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.config.BitrateEditorConfig;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.generic.ExportAction;
import org.vmax.amba.generic.ImportAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


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
    private BitrateEditorConfig cfg;
    private Bitrate[] bitrates;
    private Bitrate[] bitratesFiltered;
    private EditorPanel editorPanel;
    private BitrateCalcDialog bitrateCalcDialog;

    @Override
    public Class<BitrateEditorConfig> getConfigClz() {
        return BitrateEditorConfig.class;
    }

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return startMessage;
    }

    public void init(FirmwareConfig fcfg, byte[] fwBytes) {
        cfg = (BitrateEditorConfig) fcfg;
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

        bitratesFiltered = Arrays.stream(bitrates)
                .filter(Bitrate::isInUse)
                .toArray(Bitrate[]::new);

        editorPanel = new EditorPanel(cfg, bitratesFiltered);

        bitrateCalcDialog = new BitrateCalcDialog(this, editorPanel, cfg, bitrates);



        JScrollPane jsp = new JScrollPane(editorPanel);
        jsp.setPreferredSize(new Dimension(800,500));
        add(jsp, BorderLayout.CENTER);


    }

    @Override
    public JMenuBar buildMenu() {
        JMenuBar bar = new BitrateMenuBuilder(super.buildMenu())
                .with(editorPanel)
                .with(cfg)
                .with(bitrates,bitratesFiltered)
                .with(bitrateCalcDialog)
                .build();
        return bar;
    }


    private Bitrate[] getBitrates(BitrateEditorConfig cfg, byte[] fw)  {
        Bitrate[] bitrates;

        bitrates = new Bitrate[cfg.getVideoModes().length];

        int step=16*cfg.getQualities().length;
        int tableStartAddr = cfg.getBitratesTableAddress(); //tableStartAddr
        for(int i=0;i<cfg.getVideoModes().length;i++) {

            int rowAddr = tableStartAddr + i * step;
            final int type = (int) Utils.readUInt(fw, rowAddr);
            float min = Utils.readFloat(fw,rowAddr+8);
            float max = Utils.readFloat(fw,rowAddr+12);

            final int frowAddr = rowAddr;
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

            Bitrate.Type t = null;
            if(cfg.getBitrateTypeMapping()!=null) {
                t = cfg.getBitrateTypeMapping().entrySet()
                        .stream()
                        .filter(e->e.getValue()==type)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseGet( ()-> {
                                System.out.println("Addr:" + frowAddr + " Bad bitrate type:" + type + " forcing VBR");
                                return Bitrate.Type.VBR;
                            }
                        );
            }
            else  if(type >= Bitrate.Type.values().length || type<0) {
                System.out.println("Addr:"+frowAddr+" Bad bitrate type:"+ type+" forcing VBR");
                t= Bitrate.Type.VBR;
            }
            else {
                t = Bitrate.Type.values()[type];
            }

            bitrates[i] = new Bitrate();
            bitrates[i].setInx(i);
            bitrates[i].setName(cfg.getVideoModes()[i].getName());
            bitrates[i].setMbps(mbps);
            bitrates[i].setType(t);
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


    @Override
    public void updateFW() {
        try {
            for(Bitrate bitrate : bitrates) {
                for(int j=0;j<cfg.getQualities().length; j++) {
                    int rowAddr = cfg.getBitratesTableAddress()+(bitrate.getInx()*cfg.getQualities().length+j)*16;
                    int intType ;
                    if(cfg.getBitrateTypeMapping()!=null) {
                        intType = cfg.getBitrateTypeMapping().get(bitrate.getType());
                    }
                    else {
                        intType = bitrate.getType().ordinal();
                    }
                    Utils.writeUInt(fwBytes, rowAddr, intType);
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




    @Override
    protected List<ImportAction> getImportActions() {
        ImportAction action = new ImportAction("Import settings data", this,new FileNameExtensionFilter("JSON files", "json")) {
            @Override
            protected void importData(File selectedFile) throws IOException {

                    try (FileInputStream fis = new FileInputStream(selectedFile)) {
                        Bitrate[] bitratesLoaded = Utils.getObjectMapper().readerFor(Bitrate[].class).readValue(fis);
                        if(bitrates.length != bitratesLoaded.length) {
                            JOptionPane.showMessageDialog (BitrateTool.this, "File not match configuration","Warning",JOptionPane.ERROR_MESSAGE);
                        }
                        if(cfg.getQualities().length != bitratesLoaded[0].getMbps().length) {
                            JOptionPane.showMessageDialog (BitrateTool.this, "File not match configuration","Warning",JOptionPane.ERROR_MESSAGE);
                        }

                        if(bitratesLoaded.length != bitrates.length) {
                            throw new IOException("Video modes are different");
                        }
                        for (int i = 0; i < bitratesLoaded.length; i++) {
                            if( !bitratesLoaded[i].getName().equals(bitrates[i].getName()) ) {
                                throw new IOException("Video modes are different");
                            }
                        }

                        for (int i = 0; i < bitrates.length; i++) {
                            Bitrate b = bitrates[i];
                            b.fillFrom(bitratesLoaded[i]);
                        }
                        editorPanel.onDataChange();
                    }
            }
        };
        return Collections.singletonList(action);
    }

    @Override
    protected List<ExportAction> getExportActions() {
        ExportAction action = new ExportAction("Export settings data", this, new FileNameExtensionFilter("JSON files", "json")){
            public void exportData(File selectedFile) throws IOException {
                try(FileWriter fw = new FileWriter(selectedFile)) {
                    Utils.getObjectMapper().writer().writeValue(fw,bitrates);
                }
            }
        };
        return Collections.singletonList(action);
    }


}
