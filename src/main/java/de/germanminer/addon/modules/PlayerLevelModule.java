package de.germanminer.addon.modules;

import de.germanminer.addon.GermanMinerAddon;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;

public class PlayerLevelModule extends SimpleTextModule {

    private final String[] keys = new String[]{"Level", "Levelpunke"};
    private final String[] defaultValues = new String[]{"?", "?"};

    private int currentLevel = -1;
    private int levelPoints = -1;
    private int requiredLevelPoints = -1;

    public PlayerLevelModule(GermanMinerAddon addon) {
        addon.registerMessageConsumer("gmde-level", jsonObject -> {
            currentLevel = (jsonObject.has("current_level") && jsonObject.get("current_level").isJsonPrimitive()) ? jsonObject.get("current_level").getAsInt() : -1;
            levelPoints = (jsonObject.has("level_points") && jsonObject.get("level_points").isJsonPrimitive()) ? jsonObject.get("level_points").getAsInt() : -1;
            requiredLevelPoints = (jsonObject.has("required_level_points") && jsonObject.get("required_level_points").isJsonPrimitive()) ? jsonObject.get("required_level_points").getAsInt() : -1;
        });
    }

    @Override
    public String[] getValues() {
        return new String[]{String.valueOf(currentLevel), levelPoints + ModColor.GRAY.toString() + " / " + requiredLevelPoints};

    }

    @Override
    public String[] getDefaultValues() {
        return defaultValues;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String[] getDefaultKeys() {
        return getKeys();
    }

    @Override
    public ControlElement.IconData getIconData() {
        return new ControlElement.IconData(Material.EXP_BOTTLE);
    }

    @Override
    public void loadSettings() {
        // no settings
    }

    @Override
    public String getSettingName() {
        return "gm_playerlevelmodule";
    }

    @Override
    public String getControlName() {
        return "Spieler-Level";
    }

    @Override
    public String getDescription() {
        return "Zeigt dein aktuelles Spielerlevel an.";
    }

    @Override
    public int getSortingId() {
        return 2;
    }

    @Override
    public ModuleCategory getCategory() {
        return GermanMinerAddon.getModuleCategory();
    }

    @Override
    public boolean isShown() {
        return GermanMinerAddon.isOnline();
    }
}