package org.vmax.amba.yuv.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.FirmwareConfig;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class YUVConfig extends FirmwareConfig {

    private List<YUVTab> tabs = new ArrayList<>();

}