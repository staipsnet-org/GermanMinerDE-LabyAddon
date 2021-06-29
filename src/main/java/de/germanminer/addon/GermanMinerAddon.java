package de.germanminer.addon;

import com.google.gson.JsonObject;
import de.germanminer.addon.features.GuiTextboxPrompt;
import de.germanminer.addon.features.NotificationManager;
import de.germanminer.addon.features.voice.VoiceClient;
import de.germanminer.addon.features.voice.VoiceListener;
import de.germanminer.addon.features.voice.VoiceSocket;
import de.germanminer.addon.features.voice.VoiceUtils;
import de.germanminer.addon.modules.BankSystemModule;
import de.germanminer.addon.modules.PlayerLevelModule;
import de.germanminer.addon.modules.VoiceModule;
import net.labymod.api.EventManager;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

        if (!online) {
            VoiceClient.setPlayerKey(null);
            VoiceSocket.disconnect();
        }
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

        eventManager.registerOnQuit(serverData -> setOnline(false));

        server = new GermanMinerServer();
        getApi().registerServerSupport(this, server);

        moduleCategory = new ModuleCategory("GermanMinerDE", true, new ControlElement.IconData("germanminer/textures/icon.png"));
        ModuleCategoryRegistry.loadCategory(moduleCategory);

        System.out.println("[GermanMinerDE] Registering Modules...");
        getApi().registerModule(new BankSystemModule(this));
        getApi().registerModule(new PlayerLevelModule(this));
        getApi().registerModule(new VoiceModule());

        System.out.println("[GermanMinerDE] Registering Features...");
        GuiTextboxPrompt.initialize();
        NotificationManager.initialize();

        System.out.println("[GermanMinerDE] Voice-Initialising...");
        VoiceClient.initialize();
        getApi().registerForgeListener(new VoiceListener());

        System.out.println("[GermanMinerDE] Finished initialization.");
    }

    @Override
    public void loadConfig() {
        if (getConfig().has("voiceEnabled"))
            VoiceClient.setEnabled(getConfig().get("voiceEnabled").getAsBoolean());
        if (getConfig().has("radioSoundsEnabled"))
            VoiceClient.setRadioSoundsEnabled(getConfig().get("radioSoundsEnabled").getAsBoolean());
        if (getConfig().has("inputVolume"))
            VoiceClient.setInputVolume(getConfig().get("inputVolume").getAsFloat());
        if (getConfig().has("outputVolume"))
            VoiceClient.setOutputVolume(getConfig().get("outputVolume").getAsFloat());
        if (getConfig().has("pttHotkey"))
            VoiceClient.setPttHotkey(getConfig().get("pttHotkey").getAsInt());
        if (getConfig().has("customMicrophone"))
            VoiceClient.setCustomMicrophone(getConfig().get("customMicrophone").getAsString());
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        /*
         * Voice-Client
         */
        // ** Grundeinstellungen **
        list.add(new HeaderElement("§cGrundeinstellungen"));
        list.add(new BooleanElement("Aktiviert", new ControlElement.IconData(Material.LEVER), enabled -> {
            VoiceClient.setEnabled(enabled);

            if (!enabled)
                getConfig().addProperty("voiceEnabled", false);
            else
                getConfig().remove("voiceEnabled");
            saveConfig();
        }, VoiceClient.isEnabled()));
        list.add(new KeyElement("Push-To-Talk", new ControlElement.IconData(Material.TORCH), VoiceClient.getPttHotkey(), key -> {
            if (key < -1)
                key = -1;

            if (key != -1)
                getConfig().addProperty("pttHotkey", key);
            else
                getConfig().remove("pttHotkey");
            saveConfig();

            VoiceClient.setPttHotkey(key);
        }));

        // ** Mikrofon-Einstellungen **
        list.add(new HeaderElement("§cMikrofon-Einstellungen"));
        Set<String> microphones = new LinkedHashSet<>();
        microphones.add("Standard");
        microphones.addAll(VoiceUtils.getMicrophones().keySet());
        DropDownMenu<String> microphoneDropDown = new DropDownMenu<String>("Auswahl", 0, 0, 0, 0).fill(microphones.toArray(new String[0]));
        if (VoiceClient.getCustomMicrophone() == null || microphones.contains(VoiceClient.getCustomMicrophone()))
            microphoneDropDown.setSelected(VoiceClient.getCustomMicrophone() == null ? "Standard" : VoiceClient.getCustomMicrophone());
        list.add(new DropDownElement<String>("Auswahl", microphoneDropDown).setCallback(option -> {
            String microphone = option.equals("Standard") ? null : option;

            VoiceClient.setCustomMicrophone(microphone);

            if (microphone != null)
                getConfig().addProperty("customMicrophone", microphone);
            else
                getConfig().remove("customMicrophone");
            saveConfig();
        }));
        list.add(new SliderElement("Verstärkung", new ControlElement.IconData(Material.NOTE_BLOCK), VoiceClient.getOutputVolume() == 1 ? 0 : (int) (VoiceClient.getOutputVolume() * 100 / 10f))
                .setRange(0, 100)
                .setSteps(5)
                .addCallback(value -> {
                    float finalValue = value != 0 ? (value * 15f / 100) : 1f;

                    VoiceClient.setOutputVolume(finalValue);

                    if (finalValue != 1f)
                        getConfig().addProperty("outputVolume", finalValue);
                    else
                        getConfig().remove("outputVolume");
                    saveConfig();
                }));

        // ** Ausgabe-Einstellungen **
        list.add(new HeaderElement("§cAusgabe-Einstellungen"));
        list.add(new BooleanElement("Funk-Sounds", new ControlElement.IconData(Material.NOTE_BLOCK), enabled -> {
            VoiceClient.setRadioSoundsEnabled(enabled);

            if (!enabled)
                getConfig().addProperty("radioSoundsEnabled", false);
            else
                getConfig().remove("radioSoundsEnabled");
            saveConfig();
        }, VoiceClient.areRadioSoundsEnabled()));
        list.add(new SliderElement("Verstärkung", new ControlElement.IconData(Material.NOTE_BLOCK), VoiceClient.getInputVolume() == 1 ? 0 : (int) (VoiceClient.getInputVolume() * 100 / 4f))
                .setRange(0, 100)
                .setSteps(5)
                .addCallback(value -> {
                    float finalValue = value != 0 ? (value * 6f / 100) : 1f;

                    VoiceClient.setOutputVolume(finalValue);

                    if (finalValue != 1f)
                        getConfig().addProperty("inputVolume", finalValue);
                    else
                        getConfig().remove("inputVolume");
                    saveConfig();
                }));
    }
}
