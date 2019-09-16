package org.vmax.amba.tables;

import lombok.Getter;
import lombok.Setter;
import org.vmax.amba.tables.config.TableSetConfig;

import java.util.ArrayList;

@Getter
@Setter
public class TableSet {
    @Getter
    private TableSetConfig tableSetConfig;
    @Getter
    private java.util.List<Table2dModel> models = new ArrayList<>();
}
