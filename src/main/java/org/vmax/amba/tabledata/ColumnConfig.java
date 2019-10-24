package org.vmax.amba.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.Range;
import org.vmax.amba.cfg.Type;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor

public class ColumnConfig {
    private int addrOffset;
    private Type type;
    private Range range;
    private String columnHeader;
    private Map<String, Integer> valuesMapping = new LinkedHashMap<>();
}
