package org.vmax.amba.bitrate;

public class BitrateGenerator {

    public static void generate(Bitrate[] bitrates) {
        for (int i = 0; i < bitrates.length; i++) {
            Bitrate bitrate = bitrates[i];
            int v = i;
            bitrate.getMbps()[0] = 20 + (v/25)*20;
            v=v%25;
            bitrate.getMbps()[1] = 20 + v/5*20;
            bitrate.getMbps()[2] = 20 + v%5*20;
            bitrate.setType(Bitrate.Type.CBR);
            bitrate.setMax(1.0f);
            bitrate.setMin(1.0f);
        }
    }


}
