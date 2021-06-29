package de.germanminer.addon.features.voice;

import de.germanminer.addon.GermanMinerAddon;
import net.labymod.opus.OpusCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.*;

public class VoiceClient {
    protected static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 1, 2, 48000, false);

    private static String playerKey = "";

    private static boolean enabled = true;

    private static TargetDataLine microphone;
    private static SourceDataLine speaker;

    private static OpusCodec codec;

    private static final List<String> talkingPlayers = new ArrayList<>();
    private static long lastAudioPacket = System.currentTimeMillis();
    private static boolean flushNextTime;
    private static boolean pttPressed;

    private static String customMicrophone;
    private static int pttHotkey = -1;
    private static float inputVolume = 1;
    private static float outputVolume = 1;
    private static boolean radioSoundsEnabled = true;

    public static void initialize() {
        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-voice-connect", jsonObject -> {
            playerKey = jsonObject.get("key").getAsString();

            VoiceSocket.disconnect();
            connect();
        });
        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-voice-disconnect", jsonObject -> {
            playerKey = null;

            VoiceSocket.disconnect();
        });
    }

    public static void connect() {
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
                microphone.start();

                speaker = AudioSystem.getSourceDataLine(audioFormat);
                speaker.open(microphone.getFormat());
                speaker.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (flushNextTime) {
                        flushNextTime = false;
                        microphone.flush();
                    }
                    int bufferSize = codec.getChannels() * codec.getFrameSize() * 2;
                    if (microphone.available() < bufferSize)
                        return;
                    byte[] data = new byte[bufferSize];
                    microphone.read(data, 0, data.length);
                    if (!pttPressed)
                        return;
                    byte[] encoded = codec.encodeFrame(data);
                    VoiceSocket.sendBytes(encoded);
                }
            }, 0, 5);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!enabled || speaker == null || !speaker.isOpen() || lastAudioPacket == -1)
                        return;
                    if ((System.currentTimeMillis() - lastAudioPacket) > 100) {
                        lastAudioPacket = -1;
                        speaker.flush();
                    }
                }
            }, 20, 20);
        }).start();
    }

    protected static void disconnected() {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
        if (speaker != null) {
            speaker.stop();
            speaker.close();
        }

        talkingPlayers.clear();
    }

    protected static void handleAudioBuffer(byte[] buffer) {
        byte[] decoded = codec.decodeFrame(buffer);
        if (outputVolume > 1f)
            decoded = VoiceUtils.adjustVolume(decoded, outputVolume);
        lastAudioPacket = System.currentTimeMillis();
        speaker.write(decoded, 0, decoded.length);
    }

    public static void setEnabled(boolean enabled) {
        VoiceClient.enabled = enabled;

        VoiceSocket.cancelReconnectTimer();

        if (!enabled && VoiceSocket.isConnected())
            VoiceSocket.disconnect();
        if (enabled && !VoiceSocket.isConnected() && playerKey == null)
            connect();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean areRadioSoundsEnabled() {
        return radioSoundsEnabled;
    }

    public static void setRadioSoundsEnabled(boolean radioSoundsEnabled) {
        VoiceClient.radioSoundsEnabled = radioSoundsEnabled;
    }

    public static void setCustomMicrophone(String customMicrophone) {
        VoiceClient.customMicrophone = customMicrophone;

        if (microphone != null && microphone.isOpen()) {
            microphone.close();
            try {
                microphone = AudioSystem.getTargetDataLine(audioFormat, VoiceUtils.getMicrophones().get(customMicrophone));
                microphone.open(microphone.getFormat());
                microphone.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCustomMicrophone() {
        return customMicrophone;
    }

    public static void setPttPressed(boolean pttPressed) {
        if (enabled && pttPressed != VoiceClient.pttPressed && VoiceSocket.isConnected()) {
            if (pttPressed && areRadioSoundsEnabled())
                Minecraft.getMinecraft().player.playSound(new SoundEvent(new ResourceLocation("funk_ende")), 1f, 1f);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "talking");
            jsonObject.put("state", pttPressed);
            VoiceSocket.sendJson(jsonObject);

            if (pttPressed)
                flushNextTime = true;
        }

        VoiceClient.pttPressed = pttPressed;
    }

    public static void setPttHotkey(int pttHotkey) {
        VoiceClient.pttHotkey = pttHotkey;
    }

    public static int getPttHotkey() {
        return pttHotkey;
    }

    public static boolean isPttPressed() {
        return pttPressed;
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

    public static List<String> getTalkingPlayers() {
        return talkingPlayers;
    }

    protected static String getPlayerKey() {
        return playerKey;
    }

    public static void setPlayerKey(String playerKey) {
        VoiceClient.playerKey = playerKey;
    }
}
