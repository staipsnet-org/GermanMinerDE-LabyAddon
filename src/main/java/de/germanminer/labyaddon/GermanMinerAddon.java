package de.germanminer.labyaddon;

import com.google.gson.JsonElement;
import de.germanminer.labyaddon.features.GuiTextboxPromptManager;
import de.germanminer.labyaddon.features.NotificationManager;
import de.germanminer.labyaddon.features.WebsiteOpenDialog;
import net.labymod.api.LabyModAPI;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.mojang.inventory.GuiChestCustom;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraftforge.fml.client.config.GuiSelectString;

import java.util.List;

/**
 * Test addon
 */
public class GermanMinerAddon extends LabyModAddon {

    /**
     * Called when the addon gets enabled
     */
    @Override
    public void onEnable() {

        getApi().registerServerSupport(this, new GermanMinerServer());

        getApi().getEventManager().register(new ServerMessageEvent() {

            /**
             * Called when an API-message was sent by the server
             *
             * @param messageKey    the message-key you can identify the message's content by
             * @param serverMessage the message (a json element)
             */
            public void onServerMessage(String messageKey, JsonElement serverMessage) {
                if (!serverMessage.isJsonObject())
                    return;

                if (!(getApi().isCurrentlyPlayingOn("localhost") || getApi().isCurrentlyPlayingOn("germanminer.de") || getApi().isCurrentlyPlayingOn("mc.germanminer.de") || getApi().isCurrentlyPlayingOn("51.89.43.54")))
                    return;

                switch (messageKey) {
                    case "gmde-notification":
                        NotificationManager.handlePacket(serverMessage);
                        break;

                    case "gmde-inputgui":
                        GuiTextboxPromptManager.handlePacket(serverMessage);
                        break;

                    case "gmde-openwebpage":
                        WebsiteOpenDialog.handlePacket(serverMessage);
                        break;

                    default:
                        break;
                }
            }
        });

    }

    /**
     * Called when the addon gets disabled
     */
    @Override
    public void onDisable() {

    }

    /**
     * Called when this addon's config was loaded and is ready to use
     */
    @Override
    public void loadConfig() {

    }

    /**
     * Called when the addon's ingame settings should be filled
     *
     * @param subSettings a list containing the addon's settings' elements
     */
    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {

    }

}