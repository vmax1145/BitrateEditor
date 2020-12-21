package org.vmax.amba.generic;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public abstract class CompareAction extends AbstractAction {
    private int fileChooserMode = JFileChooser.FILES_ONLY;
    private Frame frame;
    private FileFilter fileNameFilter;

    public CompareAction(String name, Frame frame, FileFilter fileNameFilter) {
        super(name);
        this.frame = frame;
        this.fileNameFilter = fileNameFilter;
    }

    public CompareAction(String name, Frame frame, FileFilter fileNameFilter, int fileChooserFileSelectionMode) {
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
            compareData(selectedFile);
        }

    }

    public abstract void compareData(File selectedFile) ;

}
