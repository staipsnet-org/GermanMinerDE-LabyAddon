package de.germanminer.labyaddon;

import net.labymod.api.events.TabListEvent;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.servermanager.Server;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class GermanMinerServer extends Server {

    public GermanMinerServer() {
        super( "GermanMinerDE" /* unique server id */, "germanminer.de", "mc.germanminer.de", "51.89.43.54", "localhost" /* <- Server addresses */ );
    }


    @Override
    public void onJoin(net.minecraft.client.multiplayer.ServerData serverData) {
        Minecraft.getMinecraft().player.sendChatMessage("Es funktioniert!");
    }

    @Override
    public ChatDisplayAction handleChatMessage(String clean, String formatted) {
        return ChatDisplayAction.NORMAL;
    }

    @Override
    public void handlePluginMessage(String channelName, PacketBuffer packetBuffer) {

    }

    @Override
    public void handleTabInfoMessage(TabListEvent.Type tabInfoType, String formattedText, String unformattedTex) {

    }

    @Override
    public void fillSubSettings(List<SettingsElement> subSettings) {

    }

}
