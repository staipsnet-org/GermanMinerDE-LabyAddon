package de.germanminer.addon.modules;

import de.germanminer.addon.GermanMinerAddon;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.settings.elements.ControlElement;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class BankSystemModule extends SimpleTextModule {

    private final DecimalFormat NUMBER_FORMAT;
    private final String[] keys = new String[]{"Bargeld", "Kontostand"};
    private final String[] defaultValues = new String[]{"?", "?"};

    private double cashBalance = -1;
    private double bankBalance = -1;


    public BankSystemModule(GermanMinerAddon addon) {
        NUMBER_FORMAT = ((DecimalFormat) NumberFormat.getNumberInstance(Locale.GERMANY));
        NUMBER_FORMAT.applyPattern("#,##0.00");

        addon.registerMessageConsumer("gmde-balance", jsonObject -> {
            cashBalance = (jsonObject.has("cash") && jsonObject.get("cash").isJsonPrimitive()) ? jsonObject.get("cash").getAsDouble() : -1;
            bankBalance = (jsonObject.has("bank") && jsonObject.get("bank").isJsonPrimitive()) ? jsonObject.get("bank").getAsDouble() : -1;
        });
    }

    @Override
    public String[] getValues() {
        return new String[]{NUMBER_FORMAT.format(cashBalance) + " Euro", NUMBER_FORMAT.format(bankBalance) + " Euro"};
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
        return new ControlElement.IconData("germanminer/textures/bankcard.png");
    }

    @Override
    public void loadSettings() {
        // no settings
    }

    @Override
    public String getSettingName() {
        return "gm_bankmodule";
    }

    @Override
    public String getControlName() {
        return "Banksystem";
    }

    @Override
    public String getDescription() {
        return "Zeigt deine aktuelles Bargeld und Kontostand an.";
    }

    @Override
    public int getSortingId() {
        return 0;
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
