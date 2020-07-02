package de.germanminer.labyaddon.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.labymod.main.LabyMod;

public final class WebsiteOpenDialog {

    private WebsiteOpenDialog() {}

    public static void handlePacket(JsonElement serverMessage) {
        JsonObject websiteOpenPacket = serverMessage.getAsJsonObject();

        if (!websiteOpenPacket.has("url"))
            return;

        LabyMod.getInstance().openWebpage(websiteOpenPacket.get("url").getAsString(), true);
    }


}
