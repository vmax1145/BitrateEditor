package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.PatchLoaderCfg;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MiPatchLoaderCfg implements PatchLoaderCfg {
    private long patchAddrOffset;
    private List<MiPatchFileConfig> patches = new ArrayList<>();

}
