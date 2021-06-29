package de.germanminer.addon.modules;

import de.germanminer.addon.GermanMinerAddon;
import de.germanminer.addon.features.voice.VoiceClient;
import de.germanminer.addon.features.voice.VoiceSocket;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;

import java.util.LinkedList;
import java.util.List;

public class VoiceModule extends SimpleTextModule {
    private final String[] keys = new String[]{"Funk"};

    @Override
    public String[] getValues() {
        String players = "Nicht verbunden";

        if (VoiceSocket.isConnected()) {
            boolean isSpeaking = VoiceClient.isPttPressed();
            List<String> talkingPlayers = isSpeaking ? new LinkedList<>(VoiceClient.getTalkingPlayers()) : VoiceClient.getTalkingPlayers();
            if (isSpeaking)
                talkingPlayers.add("Du");
            if (!talkingPlayers.isEmpty()) {
                int last = talkingPlayers.size() - 1;
                if (last == 0)
                    players = talkingPlayers.get(0);
                else
                    players = String.join(" und ",
                            String.join(", ", talkingPlayers.subList(0, last)),
                            talkingPlayers.get(last));
            } else {
                players = "Niemand";
            }
        }

        return new String[]{players};
    }

    @Override
    public String[] getDefaultValues() {
        return getValues();
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String[] getDefaultKeys() {
        return getKeys();
    }

    @Override
    public ControlElement.IconData getIconData() {
        return new ControlElement.IconData(Material.NOTE_BLOCK);
    }

    @Override
    public void loadSettings() {
        // no settings
    }

    @Override
    public String getSettingName() {
        return "gm_voicechat";
    }

    @Override
    public String getDescription() {
        return "Zeigt an, wer momentan im Funk spricht.";
    }

    @Override
    public int getSortingId() {
        return 4;
    }

    @Override
    public ModuleCategory getCategory() {
        return GermanMinerAddon.getModuleCategory();
    }

    @Override
    public boolean isShown() {
        return GermanMinerAddon.isOnline();
    }
}
