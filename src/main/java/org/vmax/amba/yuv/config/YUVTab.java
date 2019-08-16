package org.vmax.amba.yuv.config;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.Slider;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class YUVTab {

    private String name;

    private List<Slider> sliders = new ArrayList<>();
}
