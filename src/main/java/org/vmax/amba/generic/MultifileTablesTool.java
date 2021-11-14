package org.vmax.amba.generic;

import org.vmax.amba.cfg.GenericTableDataConfig;
import org.vmax.amba.cfg.MultiFilesTablesConfig;

public class MultifileTablesTool extends GenericTool {
    @Override
    public Class<? extends GenericTableDataConfig> getConfigClz() {
        return MultiFilesTablesConfig.class;
    }
}
