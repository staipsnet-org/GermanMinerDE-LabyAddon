package de.germanminer.addon.utils;

import de.germanminer.addon.GermanMinerAddon;
import de.germanminer.addon.modules.VehicleMapModule;
import net.labymod.addons.labysminimap.LabysMinimap;
import net.labymod.addons.labysminimap.enums.EnumCachingMethod;
import net.labymod.addons.labysminimap.map.MapUpdateHandler;
import net.labymod.core.LabyModCore;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Decompiled and modified copy from LabysMinimap
 */
public class VehicleMapUpdateHandler extends MapUpdateHandler {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final LabysMinimap labysMinimap;
    private double animatedHighestBlockY = 0.0D;

    public VehicleMapUpdateHandler(LabysMinimap labysMinimap) {
        super(labysMinimap);
        this.labysMinimap = labysMinimap;
    }

    @Override
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (getVehicleMapModule().isShown() && getVehicleMapModule().isEnabled(EnumDisplayType.INGAME)) {
                if (!GermanMinerAddon.getInstance().getApi().isIngame()) {
                    if (getMapProvider().getBuffer() != null) {
                        getMapProvider().clear();
                    }

                } else {
                    EntityPlayerSP player = LabyModCore.getMinecraft().getPlayer();
                    int blockZoom = getVehicleMapModule().getSettings().zoom;
                    int x1 = (int)(player.posX - (double)blockZoom);
                    int z1 = (int)(player.posZ - (double)blockZoom);
                    int x2 = (int)(player.posX + (double)blockZoom);
                    int z2 = (int)(player.posZ + (double)blockZoom);
                    EnumCachingMethod method = getVehicleMapModule().getSettings().enumCachingMethod;
                    if (getMapProvider().canUpdate(x1, z1, x2, z2, (int)player.posY, method)) {
                        this.updateMap(0);
                    }

                    int roundUp = 10;
                    double highestBlockY = (double)(getMapProvider().getHighestBlockY() / roundUp * roundUp);
                    if (this.animatedHighestBlockY < highestBlockY) {
                        this.animatedHighestBlockY += 0.5D;
                    }

                    if (this.animatedHighestBlockY > highestBlockY) {
                        this.animatedHighestBlockY -= 0.5D;
                    }

                }
            }
        }
    }

    @Override
    public void updateMap(final int delay) {
        if (!labysMinimap.isOutOfMemory()) {
            this.executorService.execute(new Runnable() {
                public void run() {
                    if (delay != 0) {
                        try {
                            Thread.sleep((long)delay);
                        } catch (InterruptedException var11) {
                            var11.printStackTrace();
                        }
                    }

                    EntityPlayerSP player = LabyModCore.getMinecraft().getPlayer();
                    if (player != null) {
                        int blockZoom = getVehicleMapModule().getSettings().zoom;
                        int x1 = (int)(player.posX - (double)blockZoom);
                        int z1 = (int)(player.posZ - (double)blockZoom);
                        int x2 = (int)(player.posX + (double)blockZoom);
                        int z2 = (int)(player.posZ + (double)blockZoom);
                        EnumCachingMethod method = getVehicleMapModule().getSettings().enumCachingMethod;
                        boolean underground = getVehicleMapModule().getSettings().undergroundView && (!labysMinimap.isFairPlay());
                        if (!labysMinimap.isOutOfMemory()) {
                            try {
                                getMapProvider().captureArea(underground, x1, z1, x2, z2, method, (int)player.posX, (int)player.posY, (int)player.posZ, getVehicleMapModule().getSettings().blur);
                            } catch (OutOfMemoryError var10) {
                                labysMinimap.setOutOfMemory(true);
                                var10.printStackTrace();
                            }
                        }

                    }
                }
            });
        }
    }

    @Override
    public double getAnimatedHighestBlockY() {
        return animatedHighestBlockY;
    }

    public VehicleMapModule getVehicleMapModule() {
        return GermanMinerAddon.getInstance().getVehicleMapModule();
    }

}
