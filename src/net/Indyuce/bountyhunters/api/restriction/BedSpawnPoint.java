package net.Indyuce.bountyhunters.api.restriction;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BedSpawnPoint implements BountyRestriction {
	private final int radiusSquared;

	public BedSpawnPoint(ConfigurationSection config) {
		radiusSquared = config.getInt("radius") * config.getInt("radius");
	}

	@Override
	public boolean canInteractWith(Player claimer, OfflinePlayer target) {
		Location loc = claimer.getBedSpawnLocation();
		Location loc1 = target.getBedSpawnLocation();
		return loc == null || loc1 == null || loc.distanceSquared(loc1) > radiusSquared;
	}
}
