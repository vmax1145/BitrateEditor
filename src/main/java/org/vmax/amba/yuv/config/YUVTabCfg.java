package org.vmax.amba.yuv.config;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.EditableValueCfg;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class YUVTabCfg {

    private String name;

    private List<EditableValueCfg> editables = new ArrayList<>();
}

