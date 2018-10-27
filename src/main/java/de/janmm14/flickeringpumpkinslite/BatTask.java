package de.janmm14.flickeringpumpkinslite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import lombok.SneakyThrows;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BatTask extends BukkitRunnable {

	private static final int SOUND_DISTANCE_SQUARED = 10 * 10;
	private static final List<BatTask> BAT_TASKS = new ArrayList<>();

	private static boolean exb1;
	private static boolean exb2;
	private static boolean exb3;

	private final Random random = new Random();
	private final FlickeringPumpkinsLite plugin;
	private final Location pumpkinLocation;
	private List<Bat> bats;
	private int count;

	public static List<BatTask> getBatTasks() {
		return BAT_TASKS;
	}

	public BatTask(FlickeringPumpkinsLite plugin, Location pumpkinLocation) {
		this.plugin = plugin;
		this.pumpkinLocation = pumpkinLocation;
		BAT_TASKS.add(this);
	}

	@Override
	public void run() {
		if (count > 20) {
			/*if (bats != null) {
				for (Bat bat : bats) {
					bat.setNoDamageTicks(0);
					bat.remove();
				}
			}*/
			tryCancel();
			return;
		}
		if (bats == null) {
			bats = new ArrayList<>();
			for (int i = 3; i > 0; i--) {
				Bat bat = pumpkinLocation.getWorld().spawn(pumpkinLocation.clone().add(.5, 1.5, .5), Bat.class);
				bat.setMaxHealth(2048.0);
				bat.setHealth(2048.0);
				bat.setNoDamageTicks(Integer.MAX_VALUE);
				bat.setRemoveWhenFarAway(true);
				bat.setAwake(true);
				bats.add(bat);
			}
			if (random.nextInt(4) < 2) { //chance 50% to play sound
				playScarySoundWings(pumpkinLocation); //only once, not per bat
			}
			return;
		}
		if (bats.isEmpty()) {
			tryCancel();
			return;
		}
		Iterator<Bat> iterator = bats.iterator();
		while (iterator.hasNext()) {
			Bat bat = iterator.next();
			if (bat.isDead() || !bat.isValid()) {
				iterator.remove();
			}
		}

		count++;

		for (Bat bat : bats) {
			if (random.nextInt(10) != 0) { //chance 10% to continue - 90% breaks here
				break;
			}

			for (int i = 10; i > 0; i--) {
				final Location pLoc = bat.getLocation().clone()
					.add(random.nextDouble() - random.nextDouble(),
						random.nextDouble() - random.nextDouble(),
						random.nextDouble() - random.nextDouble());

				FlickeringPumpkinsLiteUpdater.sendParticle(pLoc, Color.BLACK);
				FlickeringPumpkinsLiteUpdater.sendParticle(pLoc, Color.RED);
			}
		}
	}

	private void playScarySoundWings(Location loc) {
		playScarySoundWings(loc, FlickeringPumpkinsLiteUpdater.getNearbyPlayers(loc, SOUND_DISTANCE_SQUARED));
	}

	private void playScarySoundWings(Location loc, List<Player> players) {
		if (plugin.isPlaySound()) {
			for (Player plr : players) {
				final Sound sound = getScarySoundWings();
				if (sound != null) {
					plr.playSound(loc, sound, 1, 0); //floats: volume - pitch
				}
			}
		}
	}

	public void tryCancel() {
		if (bats != null) {
			for (Bat bat : bats) {
				bat.setNoDamageTicks(0);
				bat.remove();
			}
			bats.clear();
		}
		try {
			cancel();
			BAT_TASKS.remove(this);
		} catch (IllegalStateException ex) {
			System.out.println("[FlickeringPumpkinsLite] Could not cancel bat task!");
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Nullable
	private Sound getScarySoundWings() {
		try {
			final String sound = plugin.getSound();
			return Sound.valueOf(sound);
		} catch (NullPointerException | IllegalArgumentException ex) {
			if (!exb1) {
				exb1 = true;
				new IllegalArgumentException("Configured sound " + plugin.getSound() + " could not be found, trying defaults now", ex).printStackTrace();
			}
			try {
				return Sound.ENDERDRAGON_WINGS;
			} catch (NoSuchFieldError ex2) {
				if (!exb2) {
					exb2 = true;
					new Exception("Couldn't find sound ENDERDRAGON_WINGS, trying ENTITY_ENDERDRAGON_FLAP", ex2).printStackTrace();
				}
				try {
					return Sound.valueOf("ENTITY_ENDERDRAGON_FLAP");
				} catch (IllegalArgumentException ex3) {
					if (!exb3) {
						exb3 = true;
						new Exception("Couldn't find sound ENTITY_ENDERDRAGON_FLAP, giving up...", ex3).printStackTrace();
					}
					return null;
				}
			}
		}
	}
}
