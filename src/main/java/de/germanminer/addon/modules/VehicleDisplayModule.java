package de.germanminer.addon.modules;

import com.google.gson.JsonObject;
import de.germanminer.addon.GermanMinerAddon;
import net.labymod.ingamegui.Module;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.Material;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

public class VehicleDisplayModule extends Module {

    public static final String MESSAGE_KEY = "gmde-vehicle-display";
    private static final String JSON_KEY_SHOW = "show";
    private static final String JSON_KEY_IS_DRIVER = "isDriver";
    private static final String JSON_KEY_SPEED = "speed";
    private static final String JSON_KEY_LIMITER_ACTIVE = "limiterActive";
    private static final String JSON_KEY_LIMITER_SPEED = "limiterSpeed";
    private static final String JSON_KEY_FUEL = "fuelPercent";
    private static final String JSON_KEY_GEAR_POSITION = "gearPosition";
    private static final String JSON_KEY_ENGINE_STATE = "engineState";
    private static final String JSON_KEY_NIGHT_MODE = "nightMode";
    private static final String JSON_KEY_DAMAGE_STATE = "damageState";

    private static final String TEXTURE_BASE_PATH = "germanminer/textures/vehicle-display/";
    private static final Color SPEEDOMETER_LINE_COLOR = new Color(228, 201, 74);
    private static final Color SPEEDOMETER_LINE_COLOR_NIGHT = new Color(250, 210, 23);

    private static final Color SPEED_LIMITER_LINE_COLOR_ACTIVE = new Color(194, 75, 75);
    private static final Color SPEED_LIMITER_LINE_COLOR_ACTIVE_NIGHT = new Color(212, 30, 30);
    private static final Color SPEED_LIMITER_LINE_COLOR_INACTIVE = Color.LIGHT_GRAY;

    private static final double SPEED_ZERO_ANGLE = Math.toRadians(-112.5f); // Winkel in Grad bei 0 km/h (wird in Radians umgerechnet)
    private static final float TOTAL_SPEED_ANGLE = 225; // Spannweite des Winkels in Grad bei höchster Speed, die man anzeigen kann
    private static final int MAX_SPEED = 200; // Höchstgeschwindigkeit, die der Tacho anzeigen kann
    private static final double ANGLE_PER_KMH = Math.toRadians(TOTAL_SPEED_ANGLE / MAX_SPEED);

    private static final double FUEL_ZERO_ANGLE = Math.toRadians(-135.0f); // Winkel in Grad bei 0% Tank (wird in Radians umgerechnet)
    private static final float TOTAL_FUEL_ANGLE = 90; // Spannweite des Winkels in Grad bei höchstem Füllstand
    private static final double ANGLE_PER_FUEL_PERCENT = Math.toRadians(TOTAL_FUEL_ANGLE / 100);

    private final GermanMinerAddon addon;
    private final DrawUtils drawUtils;
    private double x;
    private double y;
    private double centerX;
    private double centerY;

    private boolean show = false;
    private boolean isDriver = false;
    private int speed = -1;
    private boolean limiterActive = false;
    private int limiterSpeed = -1;
    private int fuelPercent = -1;
    private String gearPosition = null;
    private String engineState = null;
    private boolean nightMode = false;
    private int damageState = -1;

    private int engineFailureLight; // wird hochgezählt und resettet, damit die Lampe blinkt

    public VehicleDisplayModule(GermanMinerAddon addon) {
        this.addon = addon;
        this.drawUtils = addon.getApi().getDrawUtils();
        addon.registerMessageConsumer(MESSAGE_KEY, jsonObject -> {
            show = jsonObject.has(JSON_KEY_SHOW) && jsonObject.get(JSON_KEY_SHOW).isJsonPrimitive() && jsonObject.get(JSON_KEY_SHOW).getAsBoolean();
            if (!show)
                return;
            isDriver = jsonObject.has(JSON_KEY_IS_DRIVER) && jsonObject.get(JSON_KEY_IS_DRIVER).isJsonPrimitive() && jsonObject.get(JSON_KEY_IS_DRIVER).getAsBoolean();
            speed = (jsonObject.has(JSON_KEY_SPEED) && jsonObject.get(JSON_KEY_SPEED).isJsonPrimitive()) ? Math.min(jsonObject.get(JSON_KEY_SPEED).getAsInt(), MAX_SPEED) : -1;
            limiterActive = jsonObject.has(JSON_KEY_LIMITER_ACTIVE) && jsonObject.get(JSON_KEY_LIMITER_ACTIVE).isJsonPrimitive() && jsonObject.get(JSON_KEY_LIMITER_ACTIVE).getAsBoolean();
            limiterSpeed = (jsonObject.has(JSON_KEY_LIMITER_SPEED) && jsonObject.get(JSON_KEY_LIMITER_SPEED).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_LIMITER_SPEED).getAsInt() : -1;
            fuelPercent = (jsonObject.has(JSON_KEY_FUEL) && jsonObject.get(JSON_KEY_FUEL).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_FUEL).getAsInt() : -1;
            gearPosition = (jsonObject.has(JSON_KEY_GEAR_POSITION) && jsonObject.get(JSON_KEY_GEAR_POSITION).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_GEAR_POSITION).getAsString() : null;
            engineState = (jsonObject.has(JSON_KEY_ENGINE_STATE) && jsonObject.get(JSON_KEY_ENGINE_STATE).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_ENGINE_STATE).getAsString() : null;
            damageState = (jsonObject.has(JSON_KEY_DAMAGE_STATE) && jsonObject.get(JSON_KEY_DAMAGE_STATE).isJsonPrimitive()) ? jsonObject.get(JSON_KEY_DAMAGE_STATE).getAsInt() : -1;
            nightMode = jsonObject.has(JSON_KEY_NIGHT_MODE) && jsonObject.get(JSON_KEY_NIGHT_MODE).isJsonPrimitive() && jsonObject.get(JSON_KEY_NIGHT_MODE).getAsBoolean();
        });
    }

    @Override
    public void draw(double x, double y, double rightX) {
        this.x = x;
        this.y = y;
        this.centerX = x + 64;
        this.centerY = y + 64;

        drawSpeedometer();
        drawSpeedLimiterInfo();
        drawEngineAndGearInfo();
        drawWarningLight();
        drawFuelInfo();

    }

    /**
     * Geschwindigkeits-Zeiger anzeigen
     * @param speed Aktuelle km/h
     * @param color Farbe des Zeigers
     */
    private void drawSpeedLine(int speed, Color color) {
        double speedAngle = (speed * ANGLE_PER_KMH) + SPEED_ZERO_ANGLE;
        double sin = Math.sin(speedAngle);
        double cos = Math.cos(speedAngle) * -1;
        for (float r = 39; r < 62; r += 0.5) {
            int pointX = (int) Math.round((centerX + sin * r));
            int pointY = (int) Math.round((centerY + cos * r));
            drawUtils.drawRect(pointX, pointY, pointX + 1, pointY + 1, color.getRGB());
        }
    }

    /**
     * Tankkfüllstands-Zeiger anzeigen
     * @param fuelPercent Tankfüllstand in Prozent
     * @param color Farbe des Zeigers
     */
    private void drawFuelLine(int fuelPercent, Color color) {
        double fuelAngle = FUEL_ZERO_ANGLE - (fuelPercent * ANGLE_PER_FUEL_PERCENT); // Linie dreht sich andersrum als Tachonadel (also gegen Uhrzeigersinn)
        double sin = Math.sin(fuelAngle);
        double cos = Math.cos(fuelAngle) * -1;
        for (float r = 16; r < 36; r += 0.5) {
            int pointX = (int) Math.round((centerX + sin * r));
            int pointY = (int) Math.round((centerY + cos * r));
            drawUtils.drawRect(pointX, pointY, pointX + 1, pointY + 1, color.getRGB());
        }
    }

    /**
     * Tachohintergrund mit Geschwindigkeits-Zeiger & digitaler Anzeige anzeigen
     */
    private void drawSpeedometer() {
        // Hintergrund des Tachos
        drawUtils.bindTexture(Texture.SPEEDOMETER_BACKGROUND_BLACK.getResourcePath(nightMode));
        drawUtils.drawTexture(x, y, 256, 256, 128, 128);

        // Linie der Geschwindigkeit
        drawSpeedLine(speed, (nightMode ? SPEEDOMETER_LINE_COLOR_NIGHT : SPEEDOMETER_LINE_COLOR));

        // Digitale km/h-Anzeige
        drawUtils.drawCenteredString(String.valueOf(speed), centerX, centerY - 12, 2);
    }

    /**
     * Infos zum Motor & Getriebe anzeigen
     */
    private void drawEngineAndGearInfo() {
        String info = null;
        if ("STARTING".equals(engineState))
            info = "Motor startet...";
        else if ("OFF".equals(engineState))
            info = "Motor aus";
        else if (gearPosition != null)
            info = "Gang: " + gearPosition.charAt(0);

        if (info != null)
            drawUtils.drawCenteredString(info, centerX, centerY - 20, 0.5d);
    }

    /**
     * Infos zum Tempolimiter anzeigen
     */
    private void drawSpeedLimiterInfo() {
        if (limiterSpeed < 10)
            return;

        Texture limiterTexture = limiterActive ? Texture.SPEED_LIMITER_ACTIVE : Texture.SPEED_LIMITER_READY;

        drawUtils.bindTexture(limiterTexture.getResourcePath(nightMode));
        drawUtils.drawTexture(x + 33, y + 100, 256, 256, 12, 12);

        drawSpeedLine(limiterSpeed, limiterActive ? (nightMode ? SPEED_LIMITER_LINE_COLOR_ACTIVE_NIGHT : SPEED_LIMITER_LINE_COLOR_ACTIVE) : SPEED_LIMITER_LINE_COLOR_INACTIVE);
    }

    /**
     * Warnleuchte anzeigen (bei starker Beschädigung)
     */
    private void drawWarningLight() {
        if (damageState < 1)
            return;

        Texture warningLightTexture = damageState == 1 ? Texture.WARNING_YELLOW : Texture.WARNING_RED;

        // Symbol soll blinken, wenn es aktiv ist (2-Sekunden-Takt)
        if (engineFailureLight++ < 40) {
            drawUtils.bindTexture(warningLightTexture.getResourcePath(nightMode));
            drawUtils.drawTexture(x + 57, y + 106, 256, 256, 14, 14);
        }
        if (engineFailureLight == 80)
            engineFailureLight = 0;
    }

    /**
     * Tankanzeigen-Overlay und Zeiger anzeigen
     */
    private void drawFuelInfo() {
        if (fuelPercent == -1)
            return;

        // Hintergrund der Tankanzeige
        drawUtils.bindTexture(Texture.FUEL_OVERLAY.getResourcePath(nightMode));
        drawUtils.drawTexture(x, y, 256, 256, 128, 128);

        // Linie des Füllstandes
        drawFuelLine(fuelPercent, (nightMode ? SPEEDOMETER_LINE_COLOR_NIGHT : SPEEDOMETER_LINE_COLOR));

    }

    @Override
    public ControlElement.IconData getIconData() {
        return new ControlElement.IconData(Material.COMPASS);
    }

    /**
     * Wird beim Laden des Addons / Joinen ausgeführt
     */
    @Override
    public void setEnabled(Set<EnumDisplayType> enabled) {
        super.setEnabled(enabled);
        sendModuleInfo();
    }

    /**
     * Wird ausgeführt, wenn man ein Modul an- / ausschaltet
     */
    @Override
    public void settingUpdated(boolean enabled) {
        super.settingUpdated(enabled);
        sendModuleInfo();
    }

    /**
     * Info an den Server schicken, ob das Modul aktiviert ist
     */
    public void sendModuleInfo() {
        JsonObject moduleInfo = new JsonObject();
        moduleInfo.addProperty("moduleEnabled", isEnabled(EnumDisplayType.INGAME));
        addon.getApi().sendJsonMessageToServer(MESSAGE_KEY, moduleInfo);
    }

    @Override
    public boolean isShown() {
        return GermanMinerAddon.isOnline() && show && addon.getApi().isIngame() && addon.getApi().hasGameFocus();
    }

    @Override
    public ModuleCategory getCategory() {
        return GermanMinerAddon.getModuleCategory();
    }

    @Override
    public double getHeight() {
        return scaleModuleSize(this.getRawSize(), false);
    }

    @Override
    public double getWidth() {
        return getHeight();
    }

    public double getRawSize() {
        return 128D;
    }

    @Override
    public void loadSettings() {
        // Keine Einstellungen nötig
    }

    @Override
    protected boolean supportsRescale() {
        return true;
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

    /**
     * Texturen für den Tacho und darin befindliche Anzeigen
     */
    public enum Texture {
        SPEEDOMETER_BACKGROUND_BLACK("speedometer-background-black.png", "speedometer-background-black-night.png"),
        WARNING_YELLOW("warning-yellow.png", null),
        WARNING_RED("warning-red.png", null),
        SPEED_LIMITER_ACTIVE("speed-limiter-green.png", null),
        SPEED_LIMITER_READY("speed-limiter-yellow.png", null),
        FUEL_OVERLAY("fuel-overlay.png", "fuel-overlay-night.png");

        private final String fileName;
        private final String nightFileName;

        Texture(String fileName, @Nullable String nightFileName) {
            this.fileName = fileName;
            this.nightFileName = nightFileName;
        }

        public String getResourcePath(boolean night) {
            return TEXTURE_BASE_PATH + (night && nightFileName != null ? nightFileName : fileName);
        }
    }

}
