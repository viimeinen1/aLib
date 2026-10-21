package io.github.viimeinen1.alib.module.region;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Region event
 * @param region region in the event
 * @param current current regions player is in
 * @param player player of the event
 */
public record RegionEvent(String region, List<String> current, Player player) {}
