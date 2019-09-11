package org.vmax.amba.tables.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.Colors;

@Getter
@Setter
@NoArgsConstructor
public class SingleTableConf {
    private int addr;
    private Colors color;
}
