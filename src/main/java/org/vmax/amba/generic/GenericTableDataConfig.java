package org.vmax.amba.generic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.tabledata.ImageConfig;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class GenericTableDataConfig extends FirmwareConfig {

    private List<TableDataConfig> tableDataConfigs = new ArrayList<>();
    private List<ParamsConfig> paramsTabs = new ArrayList<>();
    private List<ImageConfig> imageTabs = new ArrayList<>();
}
