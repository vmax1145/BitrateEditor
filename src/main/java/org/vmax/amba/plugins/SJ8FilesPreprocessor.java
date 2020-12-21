package org.vmax.amba.plugins;

import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FileInfo;
import org.vmax.amba.cfg.GenericTableDataConfig;
import org.vmax.amba.cfg.SectionAddr;
import org.vmax.amba.cfg.SectionInfo;
import org.vmax.amba.cfg.tabledata.ByteBlockConfig;
import org.vmax.amba.cfg.tabledata.FileListConfig;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class SJ8FilesPreprocessor extends SJ8ProProcessor {
    @Override
    public byte[] preprocess( File file, byte[] fwBytes) throws Exception {
        //postprocess(fwBytes);
        verifyDigest(file, fwBytes);
        sections = Utils.getSectionInfos(fwBytes, Collections.singletonList(3),getFileNameLen());
        addFilesToConfig(sections.get(3));
        preprocessConfig(cfg, fwBytes);
        doVerify();
        return fwBytes;
    }

    private void addFilesToConfig(SectionInfo section) {
        cfg.getVerify().stream().filter(v->v.getSection()==3).findFirst().get().getFiles().addAll(section.files.keySet());

        FileListConfig fileListConfig = new FileListConfig();
        for(Map.Entry<String, FileInfo> fi : section.files.entrySet()) {

            String fn = fi.getKey();
            SectionAddr sa  = new SectionAddr();
            sa.setSectionNum(section.num);
            sa.setRelAddr(0);
            sa.setFileName(fn);
            ByteBlockConfig bcfg = new ByteBlockConfig();
            bcfg.setLabel(fn);
            bcfg.setLocation(sa);
            bcfg.setLen(fi.getValue().len);
            fileListConfig.getFileConfigs().add(bcfg);
        }
        ((GenericTableDataConfig)cfg).getFileListTabs().add(fileListConfig);
    }

}
