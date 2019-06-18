package org.vmax.bitrate;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Bitrate {


    public enum Type {
        CBR, VBR
    }
    private int inx;
    private String name;
    private Type type;
    private float[] mbps;
    private float min=0.75f;
    private float max=1.25f;
    private boolean inUse = false;

    private Integer width;
    private Integer height;
    private Integer fps;
    private Boolean interlaced;
    private Boolean hdr;
    private Integer ratioX;
    private Integer ratioY;

//    public boolean parseName() {
//
//        String[] parts = name.split("\\s+");
//        try {
//            for (String part : parts) {
//                if (part.equalsIgnoreCase("HDR")) {
//                    hdr = true;
//                }
//                if (part.indexOf("x") > 0) {
//                    String[] sizes = StringUtils.split(part, "x");
//                    if (sizes.length != 2) {
//                        throw new IllegalArgumentException("Unparseable name:" + name);
//                    }
//
//                    width = Integer.valueOf(sizes[0]);
//                    height = Integer.valueOf(sizes[1]);
//                }
//                if (part.endsWith("I") || part.endsWith("P")) {
//                    interlaced = part.endsWith("I");
//                    fps = Integer.valueOf(part.substring(0, part.length() - 1));
//                }
//                if(part.indexOf(":")>0) {
//                    String[] sizes = StringUtils.split(part, ":");
//                    if (sizes.length != 2) {
//                        throw new IllegalArgumentException("Unparseable name:" + name);
//                    }
//                    ratioX = Integer.valueOf(sizes[0]);
//                    ratioY = Integer.valueOf(sizes[1]);
//                }
//            }
//            if(hdr == null) hdr = false;
//        } catch (Exception e) {
//            System.out.println("Unparseable name:" + name);
//            width = null;
//            height = null;
//            fps = null;
//            interlaced = null;
//            hdr = null;
//            ratioX = null;
//            ratioY = null;
//            return false;
//        }
//        return width!=null && height!=null && fps!=null && interlaced!=null && ratioX!=null && ratioY!=null;
//    }

}
