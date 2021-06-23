package de.germanminer.addon.modules;

import de.germanminer.addon.GermanMinerAddon;
import de.germanminer.addon.utils.VehicleMapUpdateHandler;
import net.labymod.addons.labysminimap.LabysMinimap;
import net.labymod.addons.labysminimap.MapSettings;
import net.labymod.addons.labysminimap.enums.EnumCachingMethod;
import net.labymod.addons.labysminimap.enums.EnumCardinalType;
import net.labymod.addons.labysminimap.map.MapUpdateHandler;
import net.labymod.addons.labysminimap.map.MinimapModule;
import net.labymod.addons.labysminimap.util.MapRenderUtils;
import net.labymod.core.LabyModCore;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.main.LabyMod;

public class VehicleMapModule extends MinimapModule {

    private final VehicleDisplayModule vehicleDisplayModule;
    private final MapSettings settings = new MapSettings();
    private final VehicleMapUpdateHandler vehicleMapUpdateHandler;

    // ToDo Overwrite Shit & Settings

    public VehicleMapModule(VehicleDisplayModule vehicleDisplayModule, LabysMinimap labysMinimap, VehicleMapUpdateHandler mapUpdateHandler) {
        super(labysMinimap, mapUpdateHandler);
        this.vehicleDisplayModule = vehicleDisplayModule;
        this.vehicleMapUpdateHandler = mapUpdateHandler;
        loadForcedSettings();
    }

    @Override
    public void drawModule(int moduleX, int moduleY, int rightX, int width, int height, int mouseX, int mouseY) {
       super.drawModule((int) vehicleDisplayModule.getX() + 25, (int) vehicleDisplayModule.getY() + 25, (int) vehicleDisplayModule.getRightX(), width, height, mouseX, mouseY);
    }

    @Override
    protected void applyStencil(double x, double y, double radius, boolean circle) {
        MapRenderUtils.applyStencil(x, y, radius, circle);
    }

    @Override
    protected void drawMapBorder(double moduleX, double moduleY) {
        // Keinen Rand einfügen
    }

    @Override
    public MapSettings getSettings() {
        return settings;
    }

    @Override
    public String getSettingName() {
        return "gm_vehicle-map-module";
    }

    private void loadForcedSettings() {
        this.settings.players = false;
        this.settings.enumCardinalType = EnumCardinalType.HIDDEN;
        this.settings.blur = true;
        this.settings.circle = true;
        this.settings.jumpBouncing = false;
        this.settings.undergroundView = false;
    }

    @Override
    public boolean isShown() {
        return super.isShown() && vehicleDisplayModule.isShown();
    }

    @Override
    public String getControlName() {
        return "Navigations-Karte";
    }

    @Override
    public ModuleCategory getCategory() {
        return GermanMinerAddon.getModuleCategory();
    }

    @Override
    public boolean isMovable(int mouseX, int mouseY) {
        return false; // ToDo Try it!
    }

    public VehicleMapUpdateHandler getVehicleMapUpdateHandler() {
        return vehicleMapUpdateHandler;
    }
}
