package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Verify {
    private int addr;
    private String val;
    private Integer int32val;
    private CRCverify crc;
}
