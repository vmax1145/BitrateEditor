package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.Dimension;
import org.vmax.amba.cfg.ImageConfig;



@Getter
@Setter
@NoArgsConstructor
public class MiLogoImageConfig extends ImageConfig {
    private Dimension dimension;
    private int addr;
}
