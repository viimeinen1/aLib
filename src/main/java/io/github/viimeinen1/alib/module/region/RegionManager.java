package io.github.viimeinen1.alib.module.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.viimeinen1.alib.common.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WorldGuard region manager.<br>
 * <br>
 * Keeps its own region list, which is kept in memory to allow for faster accessing. All actions should be thread safe. Region list can be reloaded from WorldGuard with {@link RegionManager#reload()}.<br>
 * <br>
 * In case WorldGuard is not installed, manager will always return empty region lists.
 */
public class RegionManager {

    private record CurrentRegions(World world, List<String> regions) { }

    private static final ConcurrentHashMap<World, Map<String, ProtectedRegion>> regions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, CurrentRegions> current = new ConcurrentHashMap<>();

    /**
     * Get region from world with name.
     * @param world world
     * @param name name
     * @return ProtectedRegion
     */
    public static @Nullable ProtectedRegion getRegion(@NotNull World world, @NotNull String name) {
        var r = regions.get(world);
        return r == null ? null : r.get(name);
    }

    /**
     * Get all regions in world.
     * @param world world
     * @return Map with region name as key, ProtectedRegion as value.
     */
    public static @NotNull Map<String, ProtectedRegion> regionsAt(@NotNull World world) {
        return regions.containsKey(world) ? regions.get(world) : new HashMap<>();
    }

    /**
     * Returns all regions that contain this location. Prefer {@link RegionManager#regionsAt(Player)} and {@link RegionManager#regionsAt(UUID)} when possible, as regions a player is currently in are always kept in memory.
     * @param loc location
     * @return list of region names at this location.
     */
    public static @NotNull List<String> regionsAt(@NotNull Location loc) {
        var world = loc.getWorld();
        if (!regions.containsKey(world)) return new ArrayList<>();
        var r = regions.get(world);
        return r.entrySet().stream().filter(e -> e.getValue().contains(BukkitAdapter.asBlockVector(loc))).map(Map.Entry::getKey).toList();
    }

    /**
     * Return all regions player is currently.
     * @param player player
     * @return regions player is currently in.
     */
    public static @NotNull List<String> regionsAt(@NotNull Player player) {
        return regionsAt(player.getUniqueId());
    }

    /**
     * Return all regions player is currently.
     * @param player player
     * @return regions player is currently in.
     */
    public static @NotNull List<String> regionsAt(@NotNull UUID player) {
        if (!current.containsKey(player)) return new ArrayList<>();
        return current.get(player).regions();
    }

    private static final CopyOnWriteArrayList<RegionListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Register region listener.
     * @param listener listener.
     */
    public static void registerListener(RegionListener listener) { listeners.add(listener); }

    /**
     * Unregister region listener.
     * @param listener listener.
     */
    public static void unregisterListener(RegionListener listener) { listeners.remove(listener); }

    private static final rListener listener = new rListener();
    private static boolean waitingSync = false;
    private static final BukkitRunnable regionTask = new BukkitRunnable() {
        @Override
        public void run() {
            if (waitingSync) return; // wait until reload is done to avoid unnecessary event calls
            var players = Bukkit.getOnlinePlayers();
            for (var p : players) {
                if (!current.containsKey(p.getUniqueId())) current.put(p.getUniqueId(), new CurrentRegions(p.getWorld(), new ArrayList<>()));
                var prev = current.get(p.getUniqueId());
                var next = regionsAt(p.getLocation()); // use location version, as player versions only refer to the value we are setting this to.
                current.put(p.getUniqueId(), new CurrentRegions(p.getWorld(), next));

                // world changed, all regions are new
                if (!prev.world().equals(p.getWorld())) {
                    listeners.forEach(l -> l.onRegionChange(p, prev.regions(), next));
                    prev.regions().forEach(r -> listeners.forEach(l -> l.onRegionExit(new RegionEvent(r, prev.regions(), p))));
                    next.forEach(r -> listeners.forEach(l -> l.onRegionEnter(new RegionEvent(r, next, p))));
                    return;
                }
                else {
                    var unchanged = prev.regions().stream().filter(next::contains).toList();
                    var enter = next.stream().filter(r -> !unchanged.contains(r)).toList();
                    var leave = prev.regions().stream().filter(r -> !unchanged.contains(r)).toList();

                    if (!enter.isEmpty() || !leave.isEmpty()) listeners.forEach(l -> l.onRegionChange(p, prev.regions(), next));
                    if (!enter.isEmpty()) enter.forEach(r -> listeners.forEach(l -> l.onRegionEnter(new RegionEvent(r, next, p))));
                    if (!leave.isEmpty()) leave.forEach(r -> listeners.forEach(l -> l.onRegionExit(new RegionEvent(r, next, p))));
                }
            }
        }
    };

    /**
     * Enable RegionManager and region listeners.
     * @param plugin plugin
     */
    @ApiStatus.Internal
    public static void enable(JavaPlugin plugin) {
        reload();
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        try { regionTask.runTaskTimer(plugin, 1L, 1L); } catch (Exception _) {}
    }

    /**
     * Reload regions.
     */
    public static void reload() {
        WorldGuard.checkIfLoaded();
        waitingSync = true;
        HashMap<World, Map<String, ProtectedRegion>> r = new HashMap<>();
        for (World w : Bukkit.getWorlds()) {
            r.put(w, WorldGuard.getRegions(w));
        }
        regions.clear();
        regions.putAll(r);
        waitingSync = false;
    }

    /**
     * Disable RegionManager. Will stop all region events.
     */
    @ApiStatus.Internal
    public static void disable() {
        HandlerList.unregisterAll(listener);
        try { regionTask.cancel(); } catch (Exception _) {}
    }

    private static class rListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            var regions = current.remove(e.getPlayer().getUniqueId());
            if (regions == null) return;
            listeners.forEach(l -> l.onRegionChange(e.getPlayer(), regions.regions(), new ArrayList<>()));
            regions.regions().forEach(r -> listeners.forEach(l -> l.onRegionExit(new RegionEvent(r, new ArrayList<>(), e.getPlayer()))));
        }
    }

}
