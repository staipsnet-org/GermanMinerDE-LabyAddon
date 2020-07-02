package de.germanminer.labyaddon.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;

public final class GuiTextboxPromptManager {

    private GuiTextboxPromptManager() {}

    public static void handlePacket(JsonElement serverMessage) {
        JsonObject guiContentObject = serverMessage.getAsJsonObject();

        String title = guiContentObject.get("title").getAsString();
        String textSubmit = guiContentObject.get("textSubmit").getAsString();
        String textCancel = guiContentObject.get("textCancel").getAsString();
        String placeholder = guiContentObject.get("placeholder").getAsString();

        GuiScreen guiTextboxPrompt = new GuiTextboxPrompt(title, TextFormatting.WHITE + textSubmit, TextFormatting.WHITE + textCancel, placeholder, value -> {
            Minecraft.getMinecraft().player.sendChatMessage(value);
        }, value -> {
            Minecraft.getMinecraft().player.sendChatMessage("exit");
        });
        Minecraft.getMinecraft().displayGuiScreen(guiTextboxPrompt);
    }



}
