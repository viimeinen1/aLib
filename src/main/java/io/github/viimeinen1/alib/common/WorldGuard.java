package io.github.viimeinen1.alib.common;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.viimeinen1.alib.plugin.aLib;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/**
 * WorldGuard hook
 */
public class WorldGuard {

    private static boolean worldguardFound = false;

    /**
     * If WorldGuard exists and is loaded on the server.
     * @return true if WorldGuard is loaded
     */
    public static boolean worldguardFound() { return worldguardFound; }

    /**
     * Check if WorldGuard is loaded.
     */
    public static void checkIfLoaded() {
        worldguardFound = aLib.instance.getServer().getPluginManager().isPluginEnabled("worldguard");
    }

    /**
     * Get regions in a world from WorldGuard. Prefer {@link io.github.viimeinen1.alib.module.region.RegionManager#regionsAt(World)} when possible, as RegionManager keeps its own list of the regions in memory for easier access.
     * @param world world
     * @return regions in a world.
     */
    public static Map<String, ProtectedRegion> getRegions(World world) {
        if (!worldguardFound) return new HashMap<>();
        var regionManager = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) return new HashMap<>();
        return regionManager.getRegions();
    }

}
