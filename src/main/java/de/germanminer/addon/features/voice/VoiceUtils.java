package de.germanminer.addon.features.voice;

import javax.sound.sampled.*;
import java.util.*;

public class VoiceUtils {

    public static Map<String, Mixer.Info> getMicrophones() {
        Map<String, Mixer.Info> lines = new HashMap<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            if (lineInfos.length != 0)
                if (lineInfos[0].getLineClass().equals(TargetDataLine.class))
                    lines.put(info.getName(), info);
        }
        return lines;
    }

    public static byte[] adjustVolume(byte[] audioSamples, float volume) {
        byte[] array = new byte[audioSamples.length];
        for (int i = 0; i < array.length; i += 2) {
            short buf1 = audioSamples[i + 1];
            short buf2 = audioSamples[i];
            buf1 = (short) ((buf1 & 0xFF) << 8);
            buf2 = (short) (buf2 & 0xFF);
            short res = (short) (buf1 | buf2);
            res = (short) (int) (res * volume);
            array[i] = (byte) res;
            array[i + 1] = (byte) (res >> 8);
        }
        return array;
    }

}
