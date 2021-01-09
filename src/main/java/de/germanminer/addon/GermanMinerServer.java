package de.germanminer.addon;

import net.labymod.api.events.TabListEvent;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.servermanager.Server;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class GermanMinerServer extends Server {

    public GermanMinerServer() {
        super("germanminer_de", "localhost", "127.0.0.1");
    }

    @Override
    public void onJoin(ServerData serverData) {
        GermanMinerAddon.setOnline(true);
    }

    @Override
    public ChatDisplayAction handleChatMessage(String s, String s1) throws Exception {
        return null;
    }

    @Override
    public void handlePluginMessage(String s, PacketBuffer packetBuffer) throws Exception {
        // empty
    }

    @Override
    public void handleTabInfoMessage(TabListEvent.Type type, String s, String s1) throws Exception {
        // empty
    }

    @Override
    public void addModuleLines(List<DisplayLine> lines) {
        // empty
    }

    @Override
    public void loadConfig() {
        // empty
    }

    @Override
    public void fillSubSettings(List<SettingsElement> subSettings) {
        // empty
    }
}
