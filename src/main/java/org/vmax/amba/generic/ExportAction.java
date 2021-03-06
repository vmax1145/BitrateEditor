package org.vmax.amba.generic;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public abstract class ExportAction extends AbstractAction {
    private int fileChooserMode = JFileChooser.FILES_ONLY;
    private Frame frame;
    private FileFilter fileNameFilter;

    public ExportAction(String name, Frame frame, FileFilter fileNameFilter) {
        super(name);
        this.frame = frame;
        this.fileNameFilter = fileNameFilter;
    }

    public ExportAction(String name, Frame frame, FileFilter fileNameFilter, int fileChooserFileSelectionMode) {
        super(name);
        this.frame = frame;
        this.fileNameFilter = fileNameFilter;
        this.fileChooserMode = fileChooserFileSelectionMode;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser(new File("."));
        jfc.setFileSelectionMode(fileChooserMode);
        jfc.addChoosableFileFilter(fileNameFilter);
        int returnValue = jfc.showSaveDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.exists() && !selectedFile.isDirectory()) {
                int dialogResult = JOptionPane.showConfirmDialog(jfc, "Overwrite existing file?", "Warning", JOptionPane.YES_NO_OPTION);
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try {
                exportData(selectedFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error exporting data","Error",JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public abstract void exportData(File selectedFile) throws IOException;

}
