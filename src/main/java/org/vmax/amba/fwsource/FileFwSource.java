package org.vmax.amba.fwsource;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;

public class FileFwSource extends FwSource {
    @Getter
    private final File file;

    public FileFwSource(FirmwareConfig cfg, File f) {
        super(cfg);
        this.file = f;
    }

    @Override
    public byte[] load() throws Exception {
        try {
            return FileUtils.readFileToByteArray(file);
        }
        catch (Exception e) {
            throw new Exception("Failed to load file: "+ file.getAbsolutePath(),e);
        }
    }
}
