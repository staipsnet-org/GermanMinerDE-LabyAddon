package de.germanminer.addon;

import com.google.gson.JsonObject;
import net.labymod.api.events.TabListEvent;
import net.labymod.main.LabyMod;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.servermanager.Server;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class GermanMinerServer extends Server {

    public GermanMinerServer() {
        super("germanminer_de", "mc.germanminer.de", "germanminer.de", "testserver.germanminer.de", "bauserver.germanminer.de", "51.77.73.236:25565", "51.89.46.236:26510", "51.89.46.236:26511", "51.89.46.236:26512");
    }

    @Override
    public void onJoin(ServerData serverData) {
        GermanMinerAddon.setOnline(true);

        // -- Version an den Server senden --
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", GermanMinerAddon.getVersion());
        LabyMod.getInstance().getLabyModAPI().sendJsonMessageToServer("gmde-addon-info", jsonObject);

        // -- Info zum VehicleDisplayModule an den Server senden --
        GermanMinerAddon.getVehicleDisplayModule().sendModuleInfo();
    }

    @Override
    public ChatDisplayAction handleChatMessage(String s, String s1) throws Exception {
        return ChatDisplayAction.NORMAL;
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
