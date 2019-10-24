package org.vmax.amba.generic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;


@Getter
@Setter
@NoArgsConstructor
public class GenericTableDataConfig extends FirmwareConfig {

    private TableDataConfig tableDataConfig;
}
