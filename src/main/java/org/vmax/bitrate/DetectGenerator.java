package org.vmax.bitrate;

public class DetectGenerator {

    public static void generate(Bitrate[] bitrates) {
        for (int i = 0; i < bitrates.length; i++) {
            Bitrate bitrate = bitrates[i];
            bitrate.getMbps()[bitrate.getMbps().length-1] = (i%10)*10+5;
            bitrate.getMbps()[bitrate.getMbps().length-2] = (i/10)*10+5;
            bitrate.setType(Bitrate.Type.CBR);
            bitrate.setMax(1.0f);
            bitrate.setMin(1.0f);
        }
    }


}
