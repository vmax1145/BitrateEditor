package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.Range;
import org.vmax.amba.cfg.SectionAddr;
import org.vmax.amba.cfg.Type;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor

public class ValueConfig {
    private Integer addrOffset;
    private SectionAddr location;
    private Type type;
    private Range range;
    private String label;
    private boolean hex=false;
    private boolean editable=true;

    private Map<String, String> valuesMapping = new LinkedHashMap<>();

    public ValueConfig(ValueConfig copyFrom) {
        this.addrOffset = copyFrom.addrOffset;
        if(copyFrom.location!=null) {
            this.location = new SectionAddr(copyFrom.location);
        }
        this.type = copyFrom.type;
        this.range = copyFrom.range;
        this.label = copyFrom.label;
        this.hex = copyFrom.hex;
        this.editable = copyFrom.editable;
    }

}
