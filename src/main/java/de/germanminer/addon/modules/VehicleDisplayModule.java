package de.germanminer.addon.modules;

import de.germanminer.addon.GermanMinerAddon;
import net.labymod.addon.AddonLoader;
import net.labymod.addons.labysminimap.LabysMinimap;
import net.labymod.addons.labysminimap.map.MinimapModule;
import net.labymod.api.LabyModAPI;
import net.labymod.api.LabyModAddon;
import net.labymod.ingamegui.Module;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.Material;
import org.apache.logging.log4j.core.util.UuidUtil;

import java.awt.*;
import java.util.UUID;

public class VehicleDisplayModule extends Module {

    private static final String JSON_KEY_SHOW = "show";
    private static final String JSON_KEY_SPEED = "speed";
    private static final String JSON_KEY_LIMITER_SPEED = "limiterSpeed";
    private static final String JSON_KEY_FUEL = "fuelPercent";
    private static final String JSON_KEY_GEAR_POSITION = "gearPosition";
    private static final String JSON_KEY_ENGINE_STATE = "engineState";
    private static final String JSON_KEY_NIGHT_MODE = "nightMode";
    private static final String JSON_KEY_ENGINE_FAILURE = "engineFailure";

    private static final double SPEED_ZERO_ANGLE = Math.toRadians(-112.5f); // Winkel in Grad bei 0 km/h (wird in Radians umgerechnet)
    private static final float TOTAL_ANGLE = 225; // Spannweite des Winkels in Grad bei höchster Speed, die man anzeigen kann
    private static final int MAX_SPEED = 200; // Höchstgeschwindigkeit, die der Tacho anzeigen kann
    private static final double ANGLE_PER_KMH = Math.toRadians(TOTAL_ANGLE / MAX_SPEED);

    // ToDo Einstellungsmenü für Addon: Hotkeys (Hupe, Tempomat, Motor an/aus, ...); Farbe des Tempozeigers; ...?
    // TODo Schön ausführlich kommentieren

    private final GermanMinerAddon addon;
    private final DrawUtils drawUtils;
    private double x;
    private double y;
    private double rightX;
    private double centerX;
    private double centerY;

    private boolean show = false;
    private int speed = -1;
    private int limiterSpeed = -1;
    private int fuelPercent = -1;
    private String gearPosition = null;
    private String engineState = null;
    private boolean nightMode = false;
    private boolean engineFailure = false;

    public VehicleDisplayModule(GermanMinerAddon addon) {
        this.addon = addon;
        this.drawUtils = addon.getApi().getDrawUtils();
        addon.registerMessageConsumer("gmde-vehicle-display", jsonObject -> {
            show = jsonObject.has(JSON_KEY_SHOW) && jsonObject.get(JSON_KEY_SHOW).isJsonPrimitive() && jsonObject.get(JSON_KEY_SHOW).getAsBoolean();
            if (!show)
                return;
            speed = (jsonObject.has(JSON_KEY_SPEED) && jsonObject.get(JSON_KEY_SPEED).isJsonPrimitive()) ? Math.min(jsonObject.get(JSON_KEY_SPEED).getAsInt(), MAX_SPEED) : -1;
            limiterSpeed = (jsonObject.has(JSON_KEY_LIMITER_SPEED) && jsonObject.get(JSON_KEY_LIMITER_SPEED).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_LIMITER_SPEED).getAsInt() : -1;
            fuelPercent = (jsonObject.has(JSON_KEY_FUEL) && jsonObject.get(JSON_KEY_FUEL).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_FUEL).getAsInt() : -1;
            gearPosition = (jsonObject.has(JSON_KEY_GEAR_POSITION) && jsonObject.get(JSON_KEY_GEAR_POSITION).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_GEAR_POSITION).getAsString() : null;
            engineState = (jsonObject.has(JSON_KEY_ENGINE_STATE) && jsonObject.get(JSON_KEY_ENGINE_STATE).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_ENGINE_STATE).getAsString() : null;
            engineFailure = jsonObject.has(JSON_KEY_ENGINE_FAILURE) && jsonObject.get(JSON_KEY_ENGINE_FAILURE).isJsonPrimitive() && jsonObject.get(JSON_KEY_ENGINE_FAILURE).getAsBoolean();
            nightMode = jsonObject.has(JSON_KEY_NIGHT_MODE) && jsonObject.get(JSON_KEY_NIGHT_MODE).isJsonPrimitive() && jsonObject.get(JSON_KEY_NIGHT_MODE).getAsBoolean();
        });
    }

    @Override
    public void draw(double x, double y, double rightX) {
        this.x = x;
        this.y = y;
        this.rightX = rightX;
        this.centerX = x + 64;
        this.centerY = y + 64;

        drawSpeedometer();
        drawEngineAndGearInfo();

    }

    private void drawSpeedometer() {
        drawUtils.bindTexture("germanminer/textures/speedometer.png");
        drawUtils.drawTexture(x, y, 256, 256, 128, 128);

        double speedAngle = (speed * ANGLE_PER_KMH) + SPEED_ZERO_ANGLE;
        double sin = Math.sin(speedAngle);
        double cos = Math.cos(speedAngle) * -1;
        double centerX = x + 64;
        double centerY = y + 64;
        for (float r = 38; r < 62; r += 0.5) {
            int pointX = (int) Math.round((centerX + sin * r));
            int pointY = (int) Math.round((centerY + cos * r));
            drawUtils.drawRect(pointX, pointY, pointX + 1, pointY + 1, Color.ORANGE.getRGB());
        }
        drawUtils.drawCenteredString(String.valueOf(speed), centerX, centerY - 8, 2);
    }

    private void drawEngineAndGearInfo() {
        String info = null;
        if ("STARTING".equals(engineState))
            info = "Motor startet...";
        else if ("OFF".equals(engineState))
            info = "Motor aus";
        else if (gearPosition != null)
            info = "Gang: " + gearPosition.charAt(0);

        if (info != null)
            drawUtils.drawCenteredString(info, centerX, centerY - 16, 0.5d);
    }

    @Override
    public ControlElement.IconData getIconData() {
        return new ControlElement.IconData(Material.COMPASS);
    }

    @Override
    public boolean isShown() {
        return show && addon.getApi().isIngame() && addon.getApi().hasGameFocus(); // ToDo After testing die show-Variable einbauen & nur auf GM anzeigen!
    }

    @Override
    public ModuleCategory getCategory() {
        return GermanMinerAddon.getModuleCategory();
    }

    @Override
    public double getHeight() {
        return getWidth();
    }

    @Override
    public double getWidth() {
        return 128D;
    }

    @Override
    public void loadSettings() {
        // Keine Einstellungen nötig
    }

    @Override
    public String getSettingName() {
        return "gm_vehicle-display-module";
    }

    @Override
    public String getControlName() {
        return "Fahrzeug-Anzeigen";
    }

    @Override
    public String getDescription() {
        return "Zeigt Informationen zu Vehicles an und ermöglicht das Belegen von Hotkeys (z.B. Motor starten, Hupe, ...).";
    }

    @Override
    public int getSortingId() {
        return 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRightX() {
        return rightX;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }
}
