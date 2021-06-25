package de.germanminer.addon.features.voice;

import de.germanminer.addon.GermanMinerAddon;
import net.labymod.opus.OpusCodec;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceClient {
    protected static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 1, 2, 48000, false);

    private static String playerKey = "";

    private static boolean enabled = true;

    private static TargetDataLine microphone;
    private static SourceDataLine speaker;

    private static OpusCodec codec;

    private static boolean pttPressed;

    private static String customMicrophone;
    private static int pttHotkey = -1;
    private static float inputVolume = 1;
    private static float outputVolume = 1;

    public static void initialize() {
        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-voice-connect", jsonObject -> {
            playerKey = jsonObject.get("key").getAsString();

            VoiceSocket.disconnect();
            connect();
        });
        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-voice-disconnect", jsonObject -> VoiceSocket.disconnect());
    }

    private static void connect() {
        new Thread(() -> {
            // -- WebSocket verbinden --
            VoiceSocket.connect();

            // -- Opus-Codec laden --
            try {
                OpusCodec.setupWithTemporaryFolder();
            } catch (IOException e) {
                e.printStackTrace();
            }
            codec = OpusCodec.createDefault();

            // -- Mikrofon und Kopfhörer öffnen --
            try {
                if (customMicrophone != null) {
                    Map<String, Mixer.Info> devices = VoiceUtils.getMicrophones();
                    if (devices.containsKey(customMicrophone))
                        microphone = AudioSystem.getTargetDataLine(audioFormat, devices.get(customMicrophone));
                }
                if (microphone == null) {
                    microphone = AudioSystem.getTargetDataLine(audioFormat);
                }
                microphone.open(microphone.getFormat());

                speaker = AudioSystem.getSourceDataLine(audioFormat);
                speaker.open(microphone.getFormat());
                microphone.start();
                speaker.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!pttPressed)
                        return;
                    int bufferSize = codec.getChannels() * codec.getFrameSize() * 2;
                    if (microphone.available() < bufferSize)
                        return;
                    byte[] data = new byte[bufferSize];
                    microphone.read(data, 0, data.length);
                    //Encoding PCM data chunk
                    byte[] encoded = codec.encodeFrame(data);
                    VoiceSocket.sendBytes(encoded);
                }
            }, 5, 5);
        }).start();
    }

    protected static void disconnected() {
        microphone.stop();
        speaker.stop();

        microphone.close();
        speaker.close();
    }

    protected static void handleAudioBuffer(byte[] buffer) {
        byte[] decoded = codec.decodeFrame(buffer);
        if (outputVolume > 1f)
            decoded = VoiceUtils.adjustVolume(decoded, outputVolume);
        speaker.write(decoded, 0, decoded.length);
    }

    public static void setEnabled(boolean enabled) {
        VoiceClient.enabled = enabled;

        if (!enabled && VoiceSocket.isConnected())
            VoiceSocket.disconnect();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setCustomMicrophone(String customMicrophone) {
        VoiceClient.customMicrophone = customMicrophone;

        if (microphone != null && microphone.isOpen()) {
            microphone.close();
            try {
                microphone = AudioSystem.getTargetDataLine(audioFormat, VoiceUtils.getMicrophones().get(customMicrophone));
                microphone.open(microphone.getFormat());
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCustomMicrophone() {
        return customMicrophone;
    }

    public static void setPttHotkey(int pttHotkey) {
        VoiceClient.pttHotkey = pttHotkey;
    }

    public static int getPttHotkey() {
        return pttHotkey;
    }

    public static void setPttPressed(boolean pttPressed) {
        VoiceClient.pttPressed = pttPressed;
    }

    public static void setInputVolume(float inputVolume) {
        VoiceClient.inputVolume = inputVolume;
    }

    public static float getInputVolume() {
        return inputVolume;
    }

    public static void setOutputVolume(float outputVolume) {
        VoiceClient.outputVolume = outputVolume;
    }

    public static float getOutputVolume() {
        return outputVolume;
    }

    protected static String getPlayerKey() {
        return playerKey;
    }

}
