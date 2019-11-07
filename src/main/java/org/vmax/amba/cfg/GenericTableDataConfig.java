package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class GenericTableDataConfig<I> extends FirmwareConfig {

    private List<TableDataConfig> tableDataConfigs = new ArrayList<>();
    private List<ParamsConfig> paramsTabs = new ArrayList<>();
    private List<I> imageTabs = new ArrayList<>();
    private PatchLoaderCfg patchLoader;
}
