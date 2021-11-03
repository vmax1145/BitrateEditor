package org.vmax.amba.yuv.config;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.SectionAddr;
import org.vmax.amba.cfg.ShortValueCfg;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class YUVTabCfg {

    private String name;
    private String imageSample;
    private SectionAddr location;
    private Integer addr;
    private List<ShortValueCfg> editables = new ArrayList<>();

}

