package io.github.viimeinen1.alib.module.region;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * WorldGuard region listener.<br>
 * Register events by using {@link RegionManager#registerListener(RegionListener)}, and unregister by using {@link RegionManager#unregisterListener(RegionListener)}<br>
 * <br>
 * As all changes in regions will trigger events (including joins, leaves and world changes), using listener to track current regions should never desync from actual regions.
 * This means there should be no need to sync regions manually, and you can fully rely on the listener.
 */
public interface RegionListener {

    /**
     * Triggers every time players enters a region.<br>
     * Triggers also on join and world change, and is triggered once for each unique region that was entered.<br>
     * <br>
     * Regions are checked once every tick.
     * @param regionEvent event. Region in the event is the region that was entered to.
     */
    default void onRegionEnter(RegionEvent regionEvent) {}

    /**
     * Triggers every time players exits a region.<br>
     * Triggers also on quit and world change, and is triggered once for each unique region that was exited.<br>
     * <br>
     * Regions are checked once every tick.
     * @param regionEvent event. Region in the event is the region that was exited from.
     */
    default void onRegionExit(RegionEvent regionEvent) {}

    /**
     * Triggers every time player moves so that the regions they are in change.<br>
     * Triggers only once on region change, so if player changes 2 regions in 1 tick, this event will only trigger once.<br>
     * Will run only once on joins, leaves and world changes as well.<br>
     * <br>
     * Regions are checked once every tick, so this can only trigger once on every tick.
     * @param player player
     * @param prev previous regions the player was in
     * @param next current regions the player is in
     */
    default void onRegionChange(Player player, List<String> prev, List<String> next) {}
}
