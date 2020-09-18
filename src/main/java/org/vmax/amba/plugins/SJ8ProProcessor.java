package org.vmax.amba.plugins;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.bitrate.config.BitrateEditorConfig;
import org.vmax.amba.cfg.*;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;
import org.vmax.amba.tables.config.SingleTableConf;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.config.TableSetConfig;
import org.vmax.amba.yuv.config.YUVConfig;
import org.vmax.amba.yuv.config.YUVTabCfg;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class SJ8ProProcessor implements PreProcessor, PostProcessor {
    FirmwareConfig cfg;
    Map<Integer, SectionInfo> sections;
    @Override
    public SJ8ProProcessor withConfig(FirmwareConfig cfg) {
        this.cfg=cfg;
        return this;
    }

    @Override
    public byte[] postprocess(File out, byte[] fwBytes) throws IOException, NoSuchAlgorithmException {
        File ch = new File(out.getParent(), cfg.getPostProcessor().getMd5fileName()+".mod");
        if(ch.exists()) {
            ch.delete();
        }
        byte[] md5 = Utils.calculateDigest(fwBytes);
        try(FileOutputStream fos = new FileOutputStream(ch)) {
            fos.write(md5);
        }
        return fwBytes;
    }

    @Override
    public byte[] preprocess( File file, byte[] fwBytes) throws Exception {
        //postprocess(fwBytes);
        byte[] digest = Utils.calculateDigest(fwBytes);
        System.out.println("Firmware digest: " + Utils.hex(digest));
        File md5File = new File(file.getParent(), cfg.getPreProcessor().getMd5fileName());

        if(!md5File.exists()) {
            JFileChooser jfc = new JFileChooser(file.getParent());
            if(cfg.getFwFileName()!=null) {
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".ch") || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Firmware MD5 file";
                    }
                };
                jfc.addChoosableFileFilter(filter);
                jfc.setAcceptAllFileFilterUsed(true);
                jfc.setSelectedFile(new File(cfg.getPreProcessor().getMd5fileName()));
            }

            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                if(selectedFile.exists()) {
                    cfg.getPreProcessor().setMd5fileName(selectedFile.getName());
                    cfg.getPostProcessor().setMd5fileName(selectedFile.getName());
                }
            }

        }

        byte[] check = FileUtils.readFileToByteArray(new File(file.getParent(), cfg.getPreProcessor().getMd5fileName()));
        System.out.println(cfg.getPreProcessor().getMd5fileName()+" digest: " + Utils.hex(check));
        if(!Arrays.equals(digest,check)) {
            throw new Exception("File md5 digest mismatch");
        }
        sections = Utils.getSectionInfos(fwBytes, Collections.singletonList(3));
        preprocessConfig(cfg, fwBytes);
        return fwBytes;
    }


    private void preprocessConfig(FirmwareConfig cfg, byte[] fw) throws VerifyException {

        for(Verify v : cfg.getVerify()) {
            if( v.getSection() != null && v.getAddr() == null) {
                        SectionInfo sectionAddr = sections.get(v.getSection());
                        v.setAddr(sectionAddr.addr);
                        CRCverify crCverify = new CRCverify();
                        crCverify.setFromAddr(sectionAddr.addr + SectionAddr.SECTION_HEADER_LEN);
                        crCverify.setLen(sectionAddr.len);
                        v.setCrc(crCverify);
                        if (!v.getFiles().isEmpty()) {
                            for (String file : v.getFiles()) {
                                FileInfo fi = sectionAddr.files.get(file);
                                if (fi == null) {
                                    throw new VerifyException("File: " + file + " not found in section:" + sectionAddr.num);
                                }
                                CRCverify fileCrc = new CRCverify();
                                fileCrc.setFromAddr(sectionAddr.addr+SectionAddr.SECTION_HEADER_LEN+fi.addr);
                                fileCrc.setLen(fi.len);
                                Verify fileVerify = new Verify();
                                fileVerify.setAddr(fi.crcAddr);
                                fileVerify.setCrc(fileCrc);
                                v.getVerifies().add(fileVerify);
                            }
                        }
            }

        }

        if(cfg instanceof GenericTableDataConfig) {
            GenericTableDataConfig<?> config = (GenericTableDataConfig) cfg;
            for (TableDataConfig tc : config.getTableDataConfigs()) {
                Integer addr = tc.getRowsConfig().getFirstRowAddr();
                if(addr == null) {
                    tc.getRowsConfig().setFirstRowAddr(Utils.calcAbsAddr(tc.getRowsConfig().getFirstRowLocation(), sections,fw ));
                }
            }
            for (ParamsConfig tc : config.getParamsTabs()) {
                Integer addr = tc.getBaseAddr();
                if(addr == null) {
                    tc.setBaseAddr(Utils.calcAbsAddr(tc.getBaseLocation(), sections, fw ));
                }
            }
        }
        else if( cfg instanceof BitrateEditorConfig){
            BitrateEditorConfig bitrateEditorConfig = (BitrateEditorConfig) cfg;
            Integer addr = bitrateEditorConfig.getBitratesTableAddress();
            if(addr == null) {
                bitrateEditorConfig.setBitratesTableAddress(Utils.calcAbsAddr(bitrateEditorConfig.getBitratesTableLocation(), sections, fw));
            }
            addr = bitrateEditorConfig.getGopTableAddress();
            if(addr == null) {
                bitrateEditorConfig.setGopTableAddress(Utils.calcAbsAddr(bitrateEditorConfig.getGopTableLocation(), sections, fw ));
            }
        }
        else if(cfg instanceof TableConfig) {
            for(TableSetConfig s : ((TableConfig) cfg).getTableSets()) {
                for(SingleTableConf st : s.getTables()) {
                    Integer addr = st.getAddr();
                    if(addr == null) {
                        st.setAddr(Utils.calcAbsAddr(st.getLocation(), sections, fw ));
                    }

                }
            }
        }
        else if(cfg instanceof YUVConfig) {
            for(YUVTabCfg tc : ((YUVConfig) cfg).getTabs()) {
                for(ShortValueCfg vc : tc.getEditables()) {
                    Integer addr = vc.getAddr();
                    if(addr == null) {
                        vc.setAddr(Utils.calcAbsAddr(vc.getLocation(), sections, fw));
                    }
                }
            }
        }
    }
}
