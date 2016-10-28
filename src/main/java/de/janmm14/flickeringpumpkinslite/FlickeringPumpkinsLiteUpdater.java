package de.janmm14.flickeringpumpkinslite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import de.janmm14.flickeringpumpkinslite.darkblade12.particlelibrary.ParticleEffect;
import de.janmm14.flickeringpumpkinslite.util.BooleanIntTuple;

public class FlickeringPumpkinsLiteUpdater extends Thread implements Listener { //TODO end gracefully

	private static final int PUMPKIN_DISTANCE_SQUARDED = 100 * 100;
	private static final int PARTICLE_DISTANCE_SQUARED = 40 * 40;

	private final FlickeringPumpkinsLite plugin;
	private final Random random = new Random();
	private final Map<Location, BooleanIntTuple> states = new HashMap<>();
	@Getter
	private final AtomicBoolean pluginDisabled = new AtomicBoolean(false);

	public FlickeringPumpkinsLiteUpdater(FlickeringPumpkinsLite plugin) {
		super("FlickeringPumpkinsLiteUpdater");
		this.plugin = plugin;
		notifyUpdate();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void notifyUpdate() {
		synchronized (states) {
			//TODO profiling over removeAll and addAll
			Set<Location> pumpkinLocations = plugin.getPumpkinConfiguration().getPumpkinLocations();
			//add missing ones
			for (Location loc : pumpkinLocations) {
				if (!states.containsKey(loc)) {
					states.put(loc, new BooleanIntTuple(true, loc.getBlock().getData()));
				}
			}
			//remove removed ones
			Iterator<Map.Entry<Location, BooleanIntTuple>> iterator = states.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Location, BooleanIntTuple> entry = iterator.next();
				if (!pumpkinLocations.contains(entry.getKey())) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			boolean first = true;

			while (plugin.isEnabled() && !pluginDisabled.get()) {

				//check values
				int interval = plugin.getInterval();
				int probabilityOn = plugin.getOnProbability();
				int probabilityOff = plugin.getOffProbability();
				if (interval <= 0) {
					trySleep(TimeUnit.SECONDS.toMillis(10));
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
					for (Map.Entry<Location, BooleanIntTuple> entry : states.entrySet()) {
						Location loc = entry.getKey();
						List<Player> nearbyPlayers = getNearbyPlayers(loc, PUMPKIN_DISTANCE_SQUARDED);

						if (nearbyPlayers.isEmpty()) {
							continue;
						}
					/*if (random.nextInt(probability) == 0) {
						continue;
					}*/

						BooleanIntTuple stateDatavalueTuple = entry.getValue();
						boolean oldState = stateDatavalueTuple.getBool();

						if (oldState) {
							//turn off probability
							if (random.nextInt(100) >= probabilityOff) {
								continue;
							}
						} else {
							//turn on probability
							if (random.nextInt(100) >= probabilityOn) {
								continue;
							}
						}

						WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange();
						packet.setLocation(new BlockPosition(loc.toVector()));

						boolean newState = !oldState;
						stateDatavalueTuple.setBool(newState);

						WrappedBlockData data = WrappedBlockData.createData(oldState ? Material.PUMPKIN : Material.JACK_O_LANTERN, stateDatavalueTuple.getInteger());
						packet.setBlockData(data);

						for (Player plr : nearbyPlayers) {
							packet.sendPacket(plr);
						}

						if (newState) {
							for (int i = 10; i > 0; i--) {
								Location pLoc = middleAndRandomizeBlockLocation(loc);
								sendParticle(pLoc, Color.YELLOW, nearbyPlayers);
								sendParticle(pLoc, Color.ORANGE, nearbyPlayers);
							}
							if (random.nextInt(10) == 6) { //chance of 10% to spawn a bat
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
		} catch (Exception ex) {
			plugin.getLogger().severe("Error in updater thread:");
			ex.printStackTrace();
		}
		plugin.getLogger().info("The pumpkin updater shut down for unknown reasons.");
	}

	private Location middleAndRandomizeBlockLocation(Location loc) {
		final double x = .5 + random.nextDouble() - random.nextDouble();
		final double y = .5 + random.nextDouble() - random.nextDouble();
		final double z = .5 + random.nextDouble() - random.nextDouble();
		return loc.clone().add(x, y, z);
	}

	public static void sendParticle(Location loc, Color color) {
		sendParticle(loc, color, getNearbyPlayers(loc, PARTICLE_DISTANCE_SQUARED));
	}

	public static void sendParticle(Location loc, Color color, List<Player> sendTo) {
		if (!sendTo.isEmpty()) {
			ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue()), loc, sendTo);
		}
	}

	private void spawnBat(Location loc) {
		if (plugin.isBats()) {
			new BatTask(plugin, loc).runTaskTimer(plugin, 0, 10);
		}
	}

	public static List<Player> getNearbyPlayers(Location loc, int distanceSquared) {
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
