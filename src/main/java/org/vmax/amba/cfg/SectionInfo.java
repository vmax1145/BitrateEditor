package org.vmax.amba.cfg;

import org.vmax.amba.Utils;

import java.util.HashMap;
import java.util.Map;

public class SectionInfo {
    public int num;
    public int addr;
    public int len;
    public long crc;
    public Map<String,FileInfo> files = new HashMap<>();

    public boolean isInSection(int addr) {
        if (addr < this.addr) return false;
        if (addr >= this.addr + len) return false;
        return true;
    }

    public String toJson() {
        return "    {\n" +
                "      //section crc\n" +
                "      \"section\":" + num + "," +
                "      \"addr\":" + addr + ", //" + Utils.hex(addr) + "\n" +
                "      \"crc\": {\n" +
                "        \"fromAddr\":" + (addr + 256) + ", //" + Utils.hex(addr + 256) + "\n" +
                "        \"len\":" + len + " //" + Utils.hex(len) + "\n" +
                "      }\n" +
                "    }";
    }
}
