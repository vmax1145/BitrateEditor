package org.vmax.amba.generic;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.cfg.tabledata.ByteBlockConfig;
import org.vmax.amba.cfg.tabledata.FileListConfig;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class FileListTab implements GenericTab {
    private final Frame frame;
    private JTable p ;
    private FileListConfig cfg;
    private byte[] fw;



    public FileListTab(Frame frame, FileListConfig cfg, byte[] fwBytes) {
        this.cfg = cfg;
        this.fw = fwBytes;
        this.frame = frame;
        Object[] colnames = new String[]{
                "File name","length","addr"
        };
        cfg.getFileConfigs().sort( Comparator.comparing(byteBlockConfig -> byteBlockConfig.getLabel().toLowerCase()));


        Object[][] rowData = new Object[cfg.getFileConfigs().size()][4];
        for(int i=0 ; i< cfg.getFileConfigs().size(); i++) {
            rowData[i] = new Object[4];
            ByteBlockConfig bcfg = cfg.getFileConfigs().get(i);
            rowData[i][0] = bcfg.getLabel();
            rowData[i][1] = bcfg.getLen();
            rowData[i][2] = bcfg.getAddr();
        }

        p = new JTable(
                rowData, colnames
        );

    }


    @Override
    public String getTabLabel() {
        return "Files";
    }

    @Override
    public ImportAction getImportAction() {
        return new ImportAction("Import selected files", frame, new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directory to import";
            }
        }, JFileChooser.DIRECTORIES_ONLY
                ) {
            @Override
            protected void importData(File selectedFile) throws IOException {
                ArrayList<ByteBlockConfig> toProcess = getByteBlockConfigsToProcess();
                for(ByteBlockConfig bbc : toProcess) {
                    File file = new File(selectedFile, bbc.getLabel());
                    if(file.length()!=bbc.getLen()) {
                        JOptionPane.showMessageDialog(frame, "Fail to load " + bbc.getLabel() + " File length not match required length");
                    }
                    else {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            int len = bbc.getLen();
                            int nread = 0;
                            do {
                                nread += fis.read(fw, bbc.getAddr() + nread, len);
                                len -= nread;
                            }
                            while (len > 0);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(frame, "Fail to load " + bbc.getLabel() + " See error stream for details");
                        }
                    }
                }

            }
        };
    }

    @Override
    public ExportAction getExportAction() {
        return new ExportAction("Export selected files", frame, new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directory to export";
            }
        }, JFileChooser.DIRECTORIES_ONLY) {
            @Override
            public void exportData(File selectedFile) throws IOException {
                ArrayList<ByteBlockConfig> toProcess = getByteBlockConfigsToProcess();
                for(ByteBlockConfig bbc : toProcess) {
                    try (FileOutputStream fos = new FileOutputStream(new File(selectedFile,bbc.getLabel()))){
                        fos.write(fw,bbc.getAddr(),bbc.getLen());
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(frame,"Fail to save "+bbc.getLabel()+" See error stream for details");
                    }
                }
            }
        };
    }


    public java.util.List<Action> getOtherActions() {
        return Collections.singletonList(new CompareAction("Compare selected files", frame, new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directory with files to compare";
            }
        }, JFileChooser.DIRECTORIES_ONLY) {
            @Override
            public void compareData(File selectedFile) {
                ArrayList<ByteBlockConfig> toProcess = getByteBlockConfigsToProcess();
                for(ByteBlockConfig bbc : toProcess) {
                    try {
                        byte[] fileBytes = FileUtils.readFileToByteArray(new File(selectedFile,bbc.getLabel()));
                        if(fileBytes.length!=bbc.getLen()) {
                            JOptionPane.showMessageDialog(frame,bbc.getLabel()+" different length");
                        }
                        for(int i=0; i<fileBytes.length;i++) {
                            if(fileBytes[i]!=fw[bbc.getAddr()+i]) {
                                JOptionPane.showMessageDialog(frame,bbc.getLabel()+" diff starts at "+i);
                            }
                        }
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(frame,"Fail to read "+bbc.getLabel()+" See error stream for details");
                    }
                }
            }
        });
    }



    public ArrayList<ByteBlockConfig> getByteBlockConfigsToProcess() {
        int[] rows = p.getSelectedRows();
        ArrayList<ByteBlockConfig> toProcess = new ArrayList<>();
        for(int row : rows) {
            String label = (String) p.getModel().getValueAt(row,0);
            ByteBlockConfig bbcfg = cfg.getFileConfigs().stream().filter(
                    byteBlockConfig -> byteBlockConfig.getLabel().equals(label)
            ).findFirst().orElseThrow(()->new RuntimeException("no block config for:"+label));
            toProcess.add(bbcfg);
        }
        return toProcess;
    }

    public Component getComponent() {
        return p;
    }
}
