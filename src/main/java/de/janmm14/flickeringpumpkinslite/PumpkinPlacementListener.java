package de.janmm14.flickeringpumpkinslite;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

@RequiredArgsConstructor
public class PumpkinPlacementListener implements Listener {

	private final FlickeringPumpkinsLite plugin;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPumpinPlace(BlockPlaceEvent event) {
		if (event.isCancelled() || !event.canBuild()) {
			return;
		}
		if (plugin.getSpecialPumpkinCreators().contains(event.getPlayer().getUniqueId())) {
			Block block = event.getBlockPlaced();
			if (block.getType() != Material.PUMPKIN && block.getType() != Material.JACK_O_LANTERN) {
				return;
			}
			Location blockPos = block.getLocation();
			plugin.getPumpkinConfiguration().getPumpkinLocations().add(blockPos);
			plugin.getUpdater().notifyUpdate();
		}
	}
}
