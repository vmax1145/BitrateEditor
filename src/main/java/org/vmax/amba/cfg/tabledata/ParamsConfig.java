package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.SectionAddr;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ParamsConfig {
    private String label;
    private Integer baseAddr;
    private SectionAddr baseLocation;
    private List<ValueConfig> params = new ArrayList<>();
}
