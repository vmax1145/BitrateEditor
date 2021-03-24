package org.vmax.amba.fwsource;

import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.fwsource.ftp.FtpDestination;
import org.vmax.amba.fwsource.ftp.FtpSource;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FwSourceFactory {

    public static FwDestination createDestination(FirmwareConfig cfg) throws Exception {
        FwDestination out = null;
        if(cfg.getFtpConfig()!=null) {
            out = new FtpDestination(cfg);
        }
        else if (!cfg.isShowFileDialog() && cfg.getFwFileName() != null) {
            File fout = new File(cfg.getFwFileName() + ".mod");
            if (fout.exists()) {
                JOptionPane.showMessageDialog(null, "File " + fout.getName() + " already exists");
            }
            else {
                out = new FileFwDestination(cfg, fout);
            }
        } else {
            JFileChooser jfc = new JFileChooser(new File("."));
            if (cfg.getFwFileName() != null) {
                jfc.setSelectedFile(new File(cfg.getFwFileName() + ".mod"));
            }
            if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                out = new FileFwDestination(cfg,jfc.getSelectedFile());
            }
        }
        return out;
    }

    public static FwSource createSource(FirmwareConfig cfg) throws Exception {
        FwSource fwSource = null;
        if(cfg.getFtpConfig()!=null) {
            fwSource = new FtpSource(cfg);
        }
        else if(cfg.getFwFileName()!=null && !cfg.isShowFileDialog()) {
            File f = new File(cfg.getFwFileName());
            if (!f.exists()) {
                JOptionPane.showMessageDialog(null, "File " + cfg.getFwFileName() + " not exists");
            }
            else {
                fwSource = new FileFwSource(cfg, f);
            }
        }
        else {
            JFileChooser jfc = new JFileChooser(new File("."));
            if(cfg.getFwFileName()!=null) {
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".bin") || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Firmware file";
                    }
                };
                jfc.addChoosableFileFilter(filter);
                jfc.setAcceptAllFileFilterUsed(true);
                jfc.setSelectedFile(new File(cfg.getFwFileName()));
            }

            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                if(selectedFile.exists()) {
                    fwSource = new FileFwSource(cfg,selectedFile);
                }
                else {
                    JOptionPane.showMessageDialog(null, "File " + cfg.getFwFileName() + " not exists");
                }
            }
        }
        return fwSource;
    }
}
