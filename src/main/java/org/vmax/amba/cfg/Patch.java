package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Patch {
    private String label;
    private String description;
    private boolean apply = false;
    private List<PatchEntry> entries = new ArrayList<>();
}
