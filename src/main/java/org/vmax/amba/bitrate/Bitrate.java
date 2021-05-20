package org.vmax.amba.bitrate;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Bitrate {


    public void fillFrom(Bitrate bitrate) {
        setInx(bitrate.getInx());
        setName(bitrate.getName());
        setType(bitrate.getType());
        setMin(bitrate.getMin());
        setMax(bitrate.getMax());
        setMbps(bitrate.getMbps());
        setInUse(bitrate.isInUse());
        setGop(bitrate.getGop());

        width = null;
        height = null;
        fps = null;
        interlaced = null;
        hdr = null;

        parseName();
    }

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

    private long[] gop = new long[3];

    private Integer width;
    private Integer height;
    private Integer fps;
    private Boolean interlaced;
    private Boolean hdr;

    public boolean parseName() {

        String[] parts = name.split("\\s+");
        try {
            for (String part : parts) {
                if (part.equalsIgnoreCase("HDR")) {
                    hdr = true;
                }
                if (part.indexOf("x") > 0) {
                    String[] sizes = part.split("x");
                    if (sizes.length != 2) {
                        throw new IllegalArgumentException("Unparseable name:" + name);
                    }

                    width = Integer.valueOf(sizes[0]);
                    height = Integer.valueOf(sizes[1]);
                }
                if (part.endsWith("I") || part.endsWith("P")) {
                    interlaced = part.endsWith("I");
                    fps = Integer.valueOf(part.substring(0, part.length() - 1));
                }
            }
            if(hdr == null) hdr = false;
        } catch (Exception e) {
            width = null;
            height = null;
            fps = null;
            interlaced = null;
            hdr = null;
            return false;
        }
        return width!=null && height!=null && fps!=null && interlaced!=null;
    }

    public double calculateFlow() {
        double flow = 1.0*width*height*fps;
        return interlaced ? flow*0.6 : flow;
    }

}
