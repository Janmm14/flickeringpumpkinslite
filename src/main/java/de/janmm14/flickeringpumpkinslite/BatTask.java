package de.janmm14.flickeringpumpkinslite;

import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class BatTask extends BukkitRunnable {

	private static final int SOUND_DISTANCE_SQUARED = 10 * 10;
	private final Random random = new Random();
	private final FlickeringPumpkinsLite plugin;
	private final Location pumpkinLocation;
	private List<Bat> bats;
	private int count;

	@Override
	public void run() {
		if (count > 20) {
			if (bats != null) {
				for (Bat bat : bats) {
					bat.setNoDamageTicks(0);
					bat.remove();
				}
			}
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
			if (random.nextInt(4) == 0) { //chance 25% to play sound, 75% of bat spawns, no sound is played
				playScarySound(pumpkinLocation); //only once, not per bat
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
			if (random.nextInt(10) != 0) { //chance 10% to continue - 90% return here
				return;
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

	private void playScarySound(Location loc) {
		playScarySound(loc, FlickeringPumpkinsLiteUpdater.getNearbyPlayers(loc, SOUND_DISTANCE_SQUARED));
	}

	private void playScarySound(Location loc, List<Player> players) {
		if (plugin.isPlaySound()) {
			for (Player plr : players) {
				plr.playSound(loc, Sound.ENDERDRAGON_WINGS, 1 + random.nextFloat() * .5F, 0); //floats: volume - pitch
			}
		}
	}

	private void tryCancel() {
		try {
			cancel();
		} catch (IllegalStateException ex) {
			System.out.println("[FlickeringPumpkinsLite] Could not cancel bat task!");
			ex.printStackTrace();
		}
	}
}
