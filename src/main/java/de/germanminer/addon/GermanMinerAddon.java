package de.germanminer.addon;

import com.google.gson.JsonObject;
import de.germanminer.addon.features.GuiTextboxPrompt;
import de.germanminer.addon.features.NotificationManager;
import de.germanminer.addon.modules.BankSystemModule;
import de.germanminer.addon.modules.PlayerLevelModule;
import net.labymod.api.EventManager;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class GermanMinerAddon extends LabyModAddon {

    private static ModuleCategory moduleCategory;
    private static GermanMinerServer server;
    private static GermanMinerAddon instance;
    private static boolean online = false;

    private final HashMap<String, Consumer<JsonObject>> serverMessageConsumers = new HashMap<>();

    public static ModuleCategory getModuleCategory() {
        return moduleCategory;
    }

    public static GermanMinerServer getServer() {
        return server;
    }

    public static GermanMinerAddon getInstance() {
        return instance;
    }

    public static boolean isOnline() {
        return online;
    }

    public static void setOnline(boolean online) {
        GermanMinerAddon.online = online;
    }

    public void registerMessageConsumer(String messageKey, Consumer<JsonObject> consumer) {
        System.out.println("[GermanMinerDE] Registering Consumer: " + messageKey);
        serverMessageConsumers.put(messageKey, consumer);
    }

    @Override
    public void onEnable() {
        System.out.println("[GermanMinerDE] Initialising...");
        instance = this;

        EventManager eventManager = getApi().getEventManager();
        eventManager.register((ServerMessageEvent) (messageKey, serverMessage) -> {
            Consumer<JsonObject> consumer = serverMessageConsumers.get(messageKey);
            if (!online || consumer == null || !serverMessage.isJsonObject())
                return;

            consumer.accept(serverMessage.getAsJsonObject());
        });

        eventManager.registerOnQuit(serverData -> {
            setOnline(false);
        });

        server = new GermanMinerServer();
        getApi().registerServerSupport(this, server);

        moduleCategory = new ModuleCategory("GermanMinerDE", true, new ControlElement.IconData("germanminer/textures/icon.png"));
        ModuleCategoryRegistry.loadCategory(moduleCategory);

        System.out.println("[GermanMinerDE] Registering Modules...");
        getApi().registerModule(new BankSystemModule(this));
        getApi().registerModule(new PlayerLevelModule(this));

        System.out.println("[GermanMinerDE] Registering Features...");
        GuiTextboxPrompt.initialize();
        NotificationManager.initialize();

        System.out.println("[GermanMinerDE] Finished initialization.");
    }

    @Override
    public void loadConfig() {
        // no config
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        // no settings
    }
}
