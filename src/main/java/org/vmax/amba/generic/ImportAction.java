package org.vmax.amba.generic;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public abstract class ImportAction extends AbstractAction {
        private Frame frame;
        private FileFilter fileNameFilter;

    public ImportAction(String name, Frame frame, FileFilter fileNameFilter) {
            super(name);
            this.frame = frame;
            this.fileNameFilter = fileNameFilter;
        }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser(new File("."));
        jfc.addChoosableFileFilter(fileNameFilter);
        int returnValue = jfc.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (!selectedFile.exists()) {
                JOptionPane.showMessageDialog(jfc, "File not exists", "Warning", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                importData(selectedFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error importing data","Error",JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    protected abstract void importData(File selectedFile) throws IOException;

}
