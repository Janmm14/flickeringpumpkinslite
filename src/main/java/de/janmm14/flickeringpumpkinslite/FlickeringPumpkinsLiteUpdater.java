package de.janmm14.flickeringpumpkinslite;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.janmm14.flickeringpumpkinslite.darkblade12.particlelibrary.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FlickeringPumpkinsLiteUpdater extends Thread implements Listener { //TODO end gracefully

	private static final int PUMPKIN_DISTANCE_SQUARDED = 100 * 100;

	private final FlickeringPumpkinsLite plugin;
	private final Random random = new Random();
	private final Map<Location, Boolean> states = new HashMap<>();

	public FlickeringPumpkinsLiteUpdater(FlickeringPumpkinsLite plugin) {
		super("FlickeringPumpkinsLiteUpdater");
		this.plugin = plugin;
		notifyUpdate();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void notifyUpdate() {
		synchronized (states) {
			for (Location loc : plugin.getPumpkinConfiguration().getPumpkinLocations()) {
				states.putIfAbsent(loc, false);
			}
		}
	}

	@Override
	public void run() {
		boolean first = true;

		while (plugin.isEnabled()) {

			//check values
			int interval = plugin.getInterval();
			int probability = plugin.getProbability();
			if (interval <= 0 || probability <= 0) {
				trySleep(TimeUnit.SECONDS.toMillis(30));
				continue;
			}

			if (first) {
				first = false;
			} else {
				//noinspection UnnecessaryParentheses
				trySleep((long) (((double) interval) / 20D * 1000D)); //interval ticks -> milliseconds
			}

			if (plugin.getServer().getOnlinePlayers().isEmpty()) {
				continue;
			}
			synchronized (states) {
				for (Map.Entry<Location, Boolean> entry : states.entrySet()) {
					Location loc = entry.getKey();
					List<Player> nearbyPlayers = getNearbyPlayers(loc, PUMPKIN_DISTANCE_SQUARDED);

					if (nearbyPlayers.isEmpty()) {
						continue;
					}
					if (random.nextInt(100) >= probability) {
						continue;
					}

					WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange();
					packet.setLocation(new BlockPosition(loc.toVector()));

					Boolean on = entry.getValue();
					if (on == null) {
						on = false;
					}
					entry.setValue(!on);

					WrappedBlockData data = WrappedBlockData.createData(!on ? Material.JACK_O_LANTERN : Material.PUMPKIN);
					packet.setBlockData(data);

					nearbyPlayers.forEach(packet::sendPacket);

					if (on) {
						for (int i = 10; i > 0; i--) {
							Location pLoc = middleAndRandomizeBlockLocation(loc);
							sendParticle(pLoc, Color.YELLOW, nearbyPlayers);
							sendParticle(pLoc, Color.ORANGE, nearbyPlayers);
						}
						if (random.nextInt(3) == 1) { //chnace of 33.3% to spawn a bat
							spawnBat(loc);
						}
					} else {
						for (int i = 10; i > 0; i--) {
							Location pLoc = middleAndRandomizeBlockLocation(loc);
							sendParticle(pLoc, Color.BLACK, nearbyPlayers);
							sendParticle(pLoc, Color.RED, nearbyPlayers);
						}
					}
				} //end for
			} //end synchronized
			//sleeping / delay at start of while for better usage of continue;
		} //end while
	}

	private Location middleAndRandomizeBlockLocation(Location loc) {
		final double x = .5 + random.nextDouble() - random.nextDouble();
		final double y = .5 + random.nextDouble() - random.nextDouble();
		final double z = .5 + random.nextDouble() - random.nextDouble();
		return loc.clone().add(x, y, z);
	}

	public static void sendParticle(Location loc, Color color) {
		ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue()), loc, getNearbyPlayers(loc, 40 * 40));
	}

	public static void sendParticle(Location loc, Color color, List<Player> sendTo) {
		ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue()), loc, sendTo);
	}

	private void spawnBat(Location loc) {
		new BatTask(plugin, loc).runTaskTimer(plugin, 0, 10);
	}

	private static List<Player> getNearbyPlayers(Location loc, int distanceSquared) {
		Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
		final List<Player> players = new ArrayList<>(onlinePlayers.size() / 2);
		final World locWorld = loc.getWorld();
		for (Player plr : onlinePlayers) {
			Location plrLoc = plr.getLocation();
			plrLoc.setY(loc.getY()); //ignore y difference
			if (plrLoc.getWorld().equals(locWorld) && plrLoc.distanceSquared(loc) < distanceSquared) {
				players.add(plr);
			}
		}
		return players;
	}

	private void trySleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
