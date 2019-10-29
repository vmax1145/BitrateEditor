package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.PatchToolConfig;

@Getter
@Setter
@NoArgsConstructor
public class MiPatchToolConfig extends PatchToolConfig {
    private MiPatchLoaderCfg patchLoader;
}
