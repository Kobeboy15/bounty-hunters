package net.Indyuce.bountyhunters.gui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.api.CustomItem;
import net.Indyuce.bountyhunters.api.language.Language;
import net.Indyuce.bountyhunters.api.player.PlayerData;
import net.Indyuce.bountyhunters.version.VersionMaterial;

public class Leaderboard extends PluginInventory {
	private static final int[] slots = { 13, 21, 22, 23, 29, 30, 31, 32, 33, 37, 38, 39, 40, 41, 42, 43 };

	public Leaderboard(Player player) {
		super(player);
	}

	@Override
	public Inventory getInventory() {

		/*
		 * instead of calculating each player data to check what players has the
		 * most claimed bounties, players will the highest bounties are cached
		 * in a config file (cache/leaderboard.yml) and the 20 best can be
		 * accessed directly using that file.
		 */
		Map<PlayerData, Integer> hunters = new HashMap<>();
		for (String key : BountyHunters.getInstance().getCachedLeaderboard().getKeys(false)) {
			PlayerData data = BountyHunters.getInstance().getPlayerDataManager().get(Bukkit.getOfflinePlayer(UUID.fromString(key)));
			hunters.put(data, data.getClaimedBounties());
		}

		/*
		 * sort players depending on kills
		 */
		hunters = sortByBounties(hunters);

		Inventory inv = Bukkit.createInventory(this, 54, Language.LEADERBOARD_GUI_NAME.format());

		int slot = 0;
		for (Entry<PlayerData, Integer> entry : hunters.entrySet()) {
			if (slot > slots.length)
				break;

			PlayerData data = entry.getKey();
			ItemStack skull = CustomItem.LB_PLAYER_DATA.toItemStack();
			SkullMeta meta = (SkullMeta) skull.getItemMeta();

			final int slot1 = slot;
			Bukkit.getScheduler().runTaskAsynchronously(BountyHunters.getInstance(), () -> {
				BountyHunters.getInstance().getVersionWrapper().setOwner(meta, Bukkit.getOfflinePlayer(data.getUniqueId()));
				inv.getItem(slots[slot1]).setItemMeta(meta);
			});

			meta.setDisplayName(applyPlaceholders(meta.getDisplayName(), data, slot + 1));
			List<String> lore = meta.getLore();
			for (int j = 0; j < lore.size(); j++)
				lore.set(j, applyPlaceholders(lore.get(j), data, slot + 1));
			meta.setLore(lore);
			skull.setItemMeta(meta);

			inv.setItem(slots[slot++], skull);
		}

		ItemStack glass = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(Language.NO_PLAYER.format());
		glass.setItemMeta(glassMeta);

		while (slot < slots.length)
			inv.setItem(slots[slot++], glass);

		return inv;
	}

	private LinkedHashMap<PlayerData, Integer> sortByBounties(Map<PlayerData, Integer> map) {
		LinkedHashMap<PlayerData, Integer> result = new LinkedHashMap<>();
		map.entrySet().stream().sorted((key1, key2) -> key1.getValue() < key2.getValue() ? 1 : key1.getValue() > key2.getValue() ? -1 : 0)
				.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
		return result;
	}

	@Override
	public void whenClicked(ItemStack item, InventoryAction action, int slot) {
	}

	private String applyPlaceholders(String str, PlayerData playerData, int rank) {
		String title = playerData.hasTitle() ? playerData.getTitle().format() : Language.NO_TITLE.format();

		str = str.replace("{level}", "" + playerData.getLevel());
		str = str.replace("{bounties}", "" + playerData.getClaimedBounties());
		str = str.replace("{successful_bounties}", "" + playerData.getSuccessfulBounties());
		str = str.replace("{title}", title);
		str = str.replace("{name}", playerData.getOfflinePlayer().getName());
		str = str.replace("{rank}", "" + rank);

		return str;
	}

	public static void updateCachedLeaderboard(UUID uuid, int bounties) {
		/*
		 * if the leaderboard already contains that player, just add one to the
		 * bounties counter
		 */
		if (BountyHunters.getInstance().getCachedLeaderboard().getKeys(false).contains(uuid.toString())) {
			BountyHunters.getInstance().getCachedLeaderboard().set(uuid.toString(), bounties);
			return;
		}

		/*
		 * if there is still not at least 16 players in the cached leaderboard,
		 * just add it to the keys and that's all
		 */
		if (BountyHunters.getInstance().getCachedLeaderboard().getKeys(false).size() < 16) {
			BountyHunters.getInstance().getCachedLeaderboard().set(uuid.toString(), bounties);
			return;
		}

		/*
		 * if there is more than 16 players in the leaderboard, the plugin will
		 * have to remove the player that has the least bounties and will
		 * replace it by the newer one IF the newer one has more bounties
		 */
		String leastKey = "";
		int leastBounties = Integer.MAX_VALUE;

		for (String key : BountyHunters.getInstance().getCachedLeaderboard().getKeys(false)) {
			int playerBounties = BountyHunters.getInstance().getCachedLeaderboard().getInt(key);
			if (playerBounties < leastBounties) {
				leastBounties = playerBounties;
				leastKey = key;
			}
		}

		if (bounties >= leastBounties) {
			BountyHunters.getInstance().getCachedLeaderboard().set(leastKey, null);
			BountyHunters.getInstance().getCachedLeaderboard().set(uuid.toString(), bounties);
		}
	}
}