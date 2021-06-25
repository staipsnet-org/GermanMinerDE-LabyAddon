package de.germanminer.addon.features;

import de.germanminer.addon.GermanMinerAddon;
import com.mojang.authlib.GameProfile;
import net.labymod.main.LabyMod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class NotificationManager {

    private static final HashMap<Integer, Long> notificationDelay = new HashMap<>();

    private NotificationManager() {
    }

    public static void initialize() {
        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-notification", jsonObject -> {
            String title = jsonObject.has("title") && jsonObject.get("title").isJsonPrimitive() ? jsonObject.get("title").getAsString() : null;
            String description = jsonObject.has("message") && jsonObject.get("message").isJsonPrimitive() ? jsonObject.get("message").getAsString() : null;
            String uuid = jsonObject.has("uuid") && jsonObject.get("uuid").isJsonPrimitive() ? jsonObject.get("uuid").getAsString() : null;
            String imageURL = jsonObject.has("url") && jsonObject.get("url").isJsonPrimitive() ? jsonObject.get("url").getAsString() : null;

            if (title != null && description != null) {
                sendNotification(title, description, uuid, imageURL);
            }
        });
    }

    public static void sendNotification(String title, String description, @Nullable String uuid, @Nullable String imageURL) {
        int hashCode = Objects.hash(title, description, uuid);

        if (!notificationDelay.containsKey(hashCode) || notificationDelay.get(hashCode) < System.currentTimeMillis()) {
            if (imageURL != null) {
                LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(imageURL, title, description);
            } else if (uuid != null) {
                LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(new GameProfile(UUID.fromString(uuid), title), title, description);
            } else {
                LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(title, description);
            }

            notificationDelay.put(hashCode, System.currentTimeMillis() + 1500);
        }
    }


}
