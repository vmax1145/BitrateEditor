package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.Setter;

public class ProcessorConfig {
    @Getter
    @Setter
    private String className;
    @Getter
    @Setter
    private String fwFileName;

    public String getMd5fileName() {
        if(md5fileName == null) {
            return fwFileName.substring(0,fwFileName.indexOf('_'))+"_CHECK.ch";
        }
        return md5fileName;
    }

    @Setter
    private String md5fileName = null;


}
