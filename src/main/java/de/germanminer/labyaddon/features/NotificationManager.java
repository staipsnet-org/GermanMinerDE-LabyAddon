package de.germanminer.labyaddon.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class NotificationManager {

    private NotificationManager() {}

    private static final HashMap<Integer, Long> notificationDelay = new HashMap<>();

    public static void handlePacket(JsonElement serverMessage) {
        JsonObject notificationObject = serverMessage.getAsJsonObject();

        if (!notificationObject.has("message") && !notificationObject.has("title"))
            return;

        String message = notificationObject.get("message").getAsString();
        String title = notificationObject.get("title").getAsString();

        int hashCode = Objects.hash(message, title);

        if (!notificationDelay.containsKey(hashCode) || notificationDelay.get(hashCode) < System.currentTimeMillis()) {

            // notifyMessageProfile
            if (notificationObject.has("player_uuid")) {
                UUID playerUuid = UUID.fromString(notificationObject.get("player_uuid").getAsString());
                LabyMod.getInstance().notifyMessageProfile(new GameProfile(playerUuid, title),  message);

            // notifyMessageRaw
            } else {
                LabyMod.getInstance().notifyMessageRaw(title, message);
            }

            notificationDelay.put(hashCode, System.currentTimeMillis() + 1500);
        }

    }

}
