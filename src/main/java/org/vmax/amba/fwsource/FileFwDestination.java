package org.vmax.amba.fwsource;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;

public class FileFwDestination extends FwDestination {
    @Getter
    private File file;

    public FileFwDestination(FirmwareConfig cfg, File file) throws Exception {
        super(cfg);
        this.file = file;
    }

    @Override
    public void save(byte[] fwBytes) throws Exception {
        FileUtils.writeByteArrayToFile(file, fwBytes);
    }


}
