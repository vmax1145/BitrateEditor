package org.vmax.amba.plugins;

public class SJ10FilesPreprocessor extends SJ8FilesPreprocessor{
    @Override
    protected int getFileNameLen() {
        return 0x100;
    }
}
