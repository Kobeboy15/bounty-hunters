package net.Indyuce.bountyhunters.comp.social;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import net.Indyuce.bountyhunters.api.restriction.BountyRestriction;

public class PartyAndFriendsSupport implements BountyRestriction {
	private final PAFPlayerManager manager = PAFPlayerManager.getInstance();

	@Override
	public boolean canInteractWith(Player claimer, OfflinePlayer target) {
		return !manager.getPlayer(claimer.getUniqueId()).isAFriendOf(manager.getPlayer(target.getUniqueId()));
	}
}
