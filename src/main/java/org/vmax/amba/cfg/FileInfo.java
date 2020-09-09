package org.vmax.amba.cfg;

public class FileInfo {
    public String name;
    public int addr;
    public int len;
    public int crcAddr;

    public boolean isAddrInFile(int addr) {
        return this.addr<=addr && this.addr+len > addr;
    }
}
