package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.GenericTableDataConfig;

@Getter
@Setter
@NoArgsConstructor
public class MiPatchToolConfig extends GenericTableDataConfig<MiLogoImageConfig> {
    private MiPatchLoaderCfg patchLoader;


}
