package de.germanminer.addon.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.germanminer.addon.GermanMinerAddon;
import net.labymod.core.ForgeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentString;

public final class VehiclePosition {

    private VehiclePosition() {
    }

    public static void initialize() {

        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-vehicle-position", jsonObject -> {
            // Validierung der Daten
            if (!jsonObject.has("entities") || !jsonObject.get("entities").isJsonArray())
                return;

            JsonArray entities = jsonObject.getAsJsonArray("entities");
            for (JsonElement entityData : entities) {
                if (entityData instanceof JsonObject) {
                    int entityId = ((JsonObject) entityData).get("entityId").getAsInt();
                    Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityId);
                    if (entity != null) {
                        double x = ((JsonObject) entityData).get("x").getAsDouble();
                        double y = ((JsonObject) entityData).get("y").getAsDouble();
                        double z = ((JsonObject) entityData).get("z").getAsDouble();
                        float yaw = ((JsonObject) entityData).get("yaw").getAsFloat();
                        float pitch = ((JsonObject) entityData).get("pitch").getAsFloat();
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("  -> Found Entity " + entityId));
                        entity.setPositionAndRotation(x, y, z, yaw, pitch + 20f); // ToDo Den Test-Pitch seh ich nicht :(
                    }


                }
            }
        });
    }


}
