package de.germanminer.addon.features.vehicles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.germanminer.addon.GermanMinerAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;

/**
 * Der Client erhält vom Server Infos zu Vehicles & Sitzen in der Nähe, um deren Positionen 100% exakt zeitgleich darzustellen
 * (das klappt von Minecraft aus sonst nicht so reibungslos, da der Server die Positions-Updates in einzelne Netzwerk-Pakete packt und es dadurch verzögert wird)
 */
public final class VehiclePosition {

    public static final String ENTITIES_ARRAY_NAME = "entities";

    private VehiclePosition() {
    }

    public static void initialize() {

        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-vehicle-position", jsonObject -> {
            // Validierung der Daten
            if (!jsonObject.has(ENTITIES_ARRAY_NAME) || !jsonObject.get(ENTITIES_ARRAY_NAME).isJsonArray())
                return;

            JsonArray entitiesData = jsonObject.getAsJsonArray(ENTITIES_ARRAY_NAME);
            for (JsonElement entityData : entitiesData) {
                if (entityData instanceof JsonObject) {
                    int entityId = ((JsonObject) entityData).get("entityId").getAsInt();
                    Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityId);
                    if (entity != null) {
                        double x = ((JsonObject) entityData).get("x").getAsDouble();
                        double y = ((JsonObject) entityData).get("y").getAsDouble();
                        double z = ((JsonObject) entityData).get("z").getAsDouble();
                        float yaw = ((JsonObject) entityData).get("yaw").getAsFloat();
                        float pitch = ((JsonObject) entityData).get("pitch").getAsFloat();
                        EntityTracker.updateServerPosition(entity, x, y, z);
                        entity.setPositionAndRotationDirect(x, y, z, yaw, pitch, 3, false);
                    }
                }
            }
        });
    }


}
