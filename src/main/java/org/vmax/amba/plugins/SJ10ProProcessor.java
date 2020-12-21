package org.vmax.amba.plugins;

public class SJ10ProProcessor extends SJ8ProProcessor {
    @Override
    protected int getFileNameLen() {
        return 0x100;
    }
}
