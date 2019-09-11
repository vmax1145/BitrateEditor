package org.vmax.amba.tables.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.Range;
import org.vmax.amba.cfg.Type;

@Getter
@Setter
@NoArgsConstructor
public class TableConfig extends FirmwareConfig {

    private SingleTableConf[] tables;
    private int ncol;
    private int nrow;
    private Range range;
    private Type type;


}
