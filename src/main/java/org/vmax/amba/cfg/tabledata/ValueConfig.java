package org.vmax.amba.cfg.tabledata;

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

public class ValueConfig {
    private int addrOffset;
    private Type type;
    private Range range;
    private String label;
    private boolean hex;
    private Map<String, Integer> valuesMapping = new LinkedHashMap<>();
}
