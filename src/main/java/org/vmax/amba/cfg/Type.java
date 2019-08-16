package org.vmax.amba.cfg;

import lombok.Getter;

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
