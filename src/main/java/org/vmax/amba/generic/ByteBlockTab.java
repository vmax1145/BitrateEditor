package org.vmax.amba.generic;

import org.vmax.amba.cfg.tabledata.ByteBlockConfig;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteBlockTab implements GenericTab {
    private final Frame frame;
    private Panel p ;
    private String label ;
    private ByteBlockConfig bcfg;
    private byte[] fw;


    public ByteBlockTab(Frame frame, ByteBlockConfig bcfg, byte[] fwBytes) {
        this.bcfg = bcfg;
        this.fw = fwBytes;
        this.frame = frame;
        p = new Panel();
        p.setLayout(new GridLayout(5,2));
        p.add(new Label("Addr"));
        p.add(new Label(""+bcfg.getAddr()));
        p.add(new Label("Section"));
        p.add(new Label(bcfg.getLocation().getSectionNum()+""));
        p.add(new Label("File"));
        p.add(new Label(bcfg.getLocation().getFileName()+""));
        p.add(new Label("Offset"));
        p.add(new Label(bcfg.getLocation().getRelAddr()+""));
        p.add(new Label("Length"));
        p.add(new Label(bcfg.getLen()+""));
    }


    @Override
    public String getTabLabel() {
        return bcfg.getLabel();
    }

    @Override
    public ImportAction getImportAction() {
        return new ImportAction("Import "+bcfg.getLabel(), frame, new FileNameExtensionFilter("Binary bytes data", "bytes")) {
            @Override
            protected void importData(File selectedFile) throws IOException {
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    int len = bcfg.getLen();
                    int nread = 0;
                    do {
                        nread += fis.read(fw,bcfg.getAddr()+nread,len);
                        len -= nread;
                    }
                    while (len>0);
                }
            }
        };
    }

    @Override
    public ExportAction getExportAction() {
        return new ExportAction("Export "+bcfg.getLabel(), this.frame, new FileNameExtensionFilter("Binary bytes data", "bytes")) {
            @Override
            public void exportData(File selectedFile) throws IOException {
                try (FileOutputStream fos = new FileOutputStream(selectedFile)){
                    fos.write(fw,bcfg.getAddr(),bcfg.getLen());
                }
            }
        };
    }

    public Component getComponent() {
        return p;
    }
}
