package de.germanminer.addon.features.voice;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class VoiceListener {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (VoiceClient.isEnabled() && VoiceClient.getPttHotkey() != -1)
            VoiceClient.setPttPressed(Keyboard.isKeyDown(VoiceClient.getPttHotkey()));
    }

}
