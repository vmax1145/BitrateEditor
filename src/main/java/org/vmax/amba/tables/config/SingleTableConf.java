package org.vmax.amba.tables.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.Colors;
import org.vmax.amba.cfg.SectionAddr;

@Getter
@Setter
@NoArgsConstructor
public class SingleTableConf {
    private Integer addr;
    private SectionAddr location;
    private Colors color;
    private String label;
}
