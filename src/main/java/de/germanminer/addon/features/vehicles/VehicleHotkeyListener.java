package de.germanminer.addon.features.vehicles;

import com.google.gson.JsonObject;
import de.germanminer.addon.GermanMinerAddon;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

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
        SIREN_SWITCH(35), // 35 = H
        SPEEDLIMITER_SWITCH,
        SPEEDLIMITER_SET;

        Function(int defaultHotkey) {
            this.hotkey = defaultHotkey;
        }

        Function() {
            this(-1);
        }

        private long lastAction;
        private int hotkey;

        public int getHotkey() {
            return hotkey;
        }

        public void setHotkey(int hotkey) {
            this.hotkey = hotkey;
        }

        public void execute() {
            JsonObject moduleInfo = new JsonObject();
            moduleInfo.addProperty("function", this.toString());
            GermanMinerAddon.getInstance().getApi().sendJsonMessageToServer(MESSAGE_KEY, moduleInfo);
        }
    }
}
