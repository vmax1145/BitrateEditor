package org.vmax.amba.plugins;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.bitrate.config.BitrateEditorConfig;
import org.vmax.amba.cfg.*;
import org.vmax.amba.cfg.tabledata.*;
import org.vmax.amba.fwsource.FileFwDestination;
import org.vmax.amba.fwsource.FileFwSource;
import org.vmax.amba.fwsource.FwDestination;
import org.vmax.amba.fwsource.FwSource;
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
import java.util.stream.Collectors;

public class SJ8ProProcessor implements PreProcessor, PostProcessor {
    FirmwareConfig cfg;
    Map<Integer, SectionInfo> sections;
    @Override
    public SJ8ProProcessor withConfig(FirmwareConfig cfg) {
        this.cfg=cfg;
        return this;
    }

    @Override
    public byte[] postprocess(FwDestination out, byte[] fwBytes) throws IOException, NoSuchAlgorithmException {
        if(! (out instanceof FileFwDestination)) {
            throw new IOException("Destination not regular file");
        }
        FileFwDestination ffSource = (FileFwDestination) out;

        File ch = new File(ffSource.getFile().getParent(), cfg.getPostProcessor().getMd5fileName()+".mod");
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
    public byte[] preprocess(FwSource fwSource, byte[] fwBytes) throws Exception {
        //postprocess(fwBytes);
        if(!(fwSource instanceof FileFwSource)) {
            throw new Exception("preprocessor required file firmware source");
        }
        FileFwSource ffs = (FileFwSource) fwSource;
        verifyDigest(ffs.getFile(), fwBytes);
        sections = Utils.getSectionInfos(fwBytes, Collections.singletonList(3), getFileNameLen());
        preprocessConfig(cfg, fwBytes);
        doVerify();
        return fwBytes;
    }

    protected int getFileNameLen() {
        return 0x40;
    }

    protected void verifyDigest(File file, byte[] fwBytes) throws Exception {
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
    }


    protected void preprocessConfig(FirmwareConfig cfg, byte[] fw) throws VerifyException {

        if(cfg instanceof MultiFilesTablesConfig) {
            MultiFilesTablesConfig config = (MultiFilesTablesConfig) cfg;
            for(String fileName : config.getFilenames()) {
                for(int i = 0 ; i<config.getTablesPerFile() ; i++) {
                    TableDataConfig tableDataConfig = new TableDataConfig();
                    NamedRowsConfig rowsConfig = new NamedRowsConfig();
                    tableDataConfig.setLabel(fileName+"/"+i);
                    SectionAddr sa = new SectionAddr();
                    sa.setSectionNum(config.getSectionNum());
                    sa.setFileName(fileName);
                    sa.setFindHex(config.getFindHex());
                    sa.setFindSkip(config.getFindSkip());
                    sa.setRelAddr(config.getRelAddr() + i * config.getTableLen());


                    rowsConfig.setFirstRowLocation(sa);
                    rowsConfig.setRowNames(config.getRowNames());
                    rowsConfig.setRowLenth(config.getRowLength());

                    tableDataConfig.setRowsConfig(rowsConfig);
                    tableDataConfig.setColumnsConfig( config.getColumnsConfig().stream().map(ValueConfig::new).collect(Collectors.toList()) );
                    config.getTableDataConfigs().add(tableDataConfig);
                }
            }
        }

        if(cfg instanceof GenericTableDataConfig) {
            GenericTableDataConfig<?> config = (GenericTableDataConfig<?>) cfg;
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
                for(ValueConfig vc : tc.getParams()) {
                    Integer offset = vc.getAddrOffset();
                    if(offset == null) {
                        vc.setAddrOffset(Utils.calcAbsAddr(vc.getLocation(), sections, fw) - tc.getBaseAddr());
                    }
                }
            }
            for (ByteBlockConfig bc : config.getByteBlockTabs()) {
                Integer addr = bc.getAddr();
                if(addr == null) {
                    bc.setAddr(Utils.calcAbsAddr(bc.getLocation(), sections, fw ));
                }
            }
            for (FileListConfig fileListConfig : config.getFileListTabs()) {
                for(ByteBlockConfig bc : fileListConfig.getFileConfigs()) {
                    bc.setAddr(Utils.calcAbsAddr(bc.getLocation(), sections, fw));
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
                Integer baseAddr = tc.getAddr();
                if(baseAddr == null) {
                    SectionAddr sa = tc.getLocation();
                    if(sa == null) {
                        baseAddr = 0;
                    }
                    else {
                        baseAddr = Utils.calcAbsAddr(sa, sections, fw);
                    }
                }
                for(ShortValueCfg vc : tc.getEditables()) {
                    Integer addr = vc.getAddr();
                    if(addr == null) {
                        vc.setAddr(baseAddr + Utils.calcAbsAddr(vc.getLocation(), sections, fw));
                        System.out.println(vc.getName()+" addr="+ vc.getAddr());
                    }
                }
            }
        }


    }


    void doVerify() throws VerifyException {
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
    }
}
