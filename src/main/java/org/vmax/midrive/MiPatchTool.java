package org.vmax.midrive;

import org.vmax.amba.Utils;
import org.vmax.amba.cfg.Patch;
import org.vmax.amba.cfg.PatchEntry;
import org.vmax.amba.generic.PatchTool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class MiPatchTool extends PatchTool<MiPatchToolConfig> {
    @Override
    protected List<Patch> loadPatches(MiPatchToolConfig cfg) throws Exception {
        long addrOffset = cfg.getPatchLoader().getPatchAddrOffset();
        return loadPatches(addrOffset, cfg.getPatchLoader().getPatches());
    }

    private List<Patch> loadPatches(long addrOffset, List<MiPatchFileConfig> patchFiles) {
        return patchFiles.stream()
             .map( pfc -> parseFile(addrOffset, pfc))
             .collect(Collectors.toList());
    }

    private Patch parseFile(long addrOffset, MiPatchFileConfig pfc) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pfc.getPath())))) {
                String line;
                int lnum=0;
                Patch patch = new Patch();
                patch.setLabel(pfc.getLabel());
                patch.setDescription(pfc.getDescription());

                while((line = reader.readLine())!=null) {
                    line = line.trim();
                    if(line.startsWith("himm ")) {
                        line = line.substring("himm ".length()).trim();
                        String[] parts = line.split("\\s");
                        if(parts.length!=2) {
                            throw new Exception("Error parsing line:"+lnum);
                        }
                        long addr = parseLong(parts[0]);
                        long val  = parseLong(parts[1]);
                        addr-=addrOffset;
                        if(addr < 0) {
                            throw new Exception("Error parsing line:"+lnum+" addr<0");
                        }
                        byte[] bytes = new byte[Integer.BYTES];
                        Utils.writeUInt(bytes,0,val);

                        PatchEntry entry = new PatchEntry();
                        entry.setAddr((int) addr);
                        entry.setBytes(bytes);
                        patch.getEntries().add(entry);
                    }
                    lnum++;
                }
                return patch;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Patch file read error:"+pfc.getPath(), e);
        }
    }

    private long parseLong(String part) {
        part=part.trim();
        if(part.startsWith("0x")) {
            return Long.parseLong(part.substring(2),16);
        }
        return Long.parseLong(part);
    }

    @Override
    public Class<MiPatchToolConfig> getConfigClz() {
        return MiPatchToolConfig.class;
    }
}
