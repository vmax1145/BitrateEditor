package org.vmax.midrive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MiPatchLoaderCfg {
    private int patchAddrOffset;
    private List<MiPatchFileConfig> patches = new ArrayList<>();

}
