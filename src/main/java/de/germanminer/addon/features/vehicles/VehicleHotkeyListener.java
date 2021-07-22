package de.germanminer.addon.features.vehicles;

import com.google.gson.JsonObject;
import de.germanminer.addon.GermanMinerAddon;
import de.germanminer.addon.GermanMinerServer;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.UUID;

public class VehicleHotkeyListener {

    public static final String MESSAGE_KEY = "gmde-vehicle-hotkey";


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (GermanMinerAddon.isOnline() && GermanMinerAddon.getInstance().getApi().hasGameFocus()) {
            long time = System.currentTimeMillis();
            for (Function function : Function.values()) {
                if (function.hotkey != -1 && Keyboard.isKeyDown(function.hotkey) && time - function.lastAction > 1000) {
                    function.lastAction = time;
                    function.execute();
                }
            }
        }
    }

    public enum Function {
        ENGINE_SWITCH(50), // 50 = M
        SIREN_SWITCH(35); // 35 = H
        // ToDo Tempomat +/-

        Function(int defaultHotkey) {
            this.defaultHotkey = defaultHotkey;
            this.hotkey = defaultHotkey;
        }

        private long lastAction;
        private final int defaultHotkey;
        private int hotkey;

        public int getHotkey() {
            return hotkey;
        }

        public void setHotkey(int hotkey) {
            this.hotkey = hotkey;
        }

        public int getDefaultHotkey() {
            return defaultHotkey;
        }

        public void execute() {
            JsonObject moduleInfo = new JsonObject();
            moduleInfo.addProperty("function", this.toString());
            GermanMinerAddon.getInstance().getApi().sendJsonMessageToServer(MESSAGE_KEY, moduleInfo);
        }
    }
}
