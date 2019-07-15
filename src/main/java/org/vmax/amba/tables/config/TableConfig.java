package org.vmax.amba.tables.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.Range;

@Getter
@Setter
@NoArgsConstructor
public class TableConfig extends FirmwareConfig {
    public enum Type {
        UInt32(Integer.BYTES),
        Int32(Integer.BYTES),
        Float32(Float.BYTES),
        UInt16(Short.BYTES),
        Int16(Short.BYTES),
        UByte(java.lang.Byte.BYTES),
        Byte(java.lang.Byte.BYTES);

        @Getter
        private final int byteLen   ;

        Type(int byteLen) {
            this.byteLen = byteLen;
        }
    }

    private String fileName;
    private int tableAddr;
    private int ncol;
    private int nrow;
    private Range range;
    private Type type;

}
