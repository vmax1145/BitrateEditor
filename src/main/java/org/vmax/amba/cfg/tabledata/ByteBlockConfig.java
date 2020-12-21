package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.SectionAddr;

@Getter
@Setter
@NoArgsConstructor
public class ByteBlockConfig {
    private String label;
    private Integer addr;
    private SectionAddr location;
    private int len;
}
