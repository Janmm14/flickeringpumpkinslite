package de.janmm14.flickeringpumpkinslite;

import lombok.RequiredArgsConstructor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PumpkinBlockListener implements Listener {

	private final FlickeringPumpkinsLite plugin;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPumpinPlace(BlockPlaceEvent event) {
		if (event.isCancelled() || !event.canBuild()) { //TODO find out more about canBuild() method and behaviour
			return;
		}
		final ItemStack pumpkinItem = plugin.getPumpkinItem();
		final ItemStack itemInHand = event.getItemInHand();
		if (pumpkinItem == null) {
			plugin.getLogger().severe("Pumpkin comparision item is null / not initialized.");
			return;
		}
		if (itemInHand != null && pumpkinItem.isSimilar(itemInHand)) {
			setAsPumpkinLocation(event.getBlockPlaced());
		} else if (event.getPlayer().hasPermission("flickeringpumpkinslite.allowtoggle") &&
			xor(plugin.isToggleDefault(), plugin.getSpecialPumpkinCreators().contains(event.getPlayer().getUniqueId()))) {
			Block block = event.getBlockPlaced();
			if (block.getType() != Material.JACK_O_LANTERN) {
				return;
			}
			setAsPumpkinLocation(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPumpkinBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (plugin.getPumpkinConfiguration().getPumpkinLocations().remove(block.getLocation())) {
			plugin.getUpdater().notifyUpdate();
		}
	}

	private void setAsPumpkinLocation(Block block) {
		Location blockPos = block.getLocation();
		plugin.getPumpkinConfiguration().getPumpkinLocations().add(blockPos);
		plugin.getUpdater().notifyUpdate();
	}

	/**
	 * exclusive or, returns like
	 * <p>
	 * {@code first && !second || !first && second}
	 */
	private static boolean xor(boolean first, boolean second) {
		return first ^ second;
	}
}
