package de.janmm14.flickeringpumpkinslite;

import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

@RequiredArgsConstructor
public class BatTask extends BukkitRunnable {

	private final Random random = new Random();
	private final FlickeringPumpkinsLite plugin;
	private final Location pumpkinLocation;
	private Bat bat;
	private int count;

	@Override
	public void run() {
		if (count > 20) {
			if (bat != null) {
				bat.setNoDamageTicks(0);
				bat.remove();
			}
			tryCancel();
			return;
		}
		if (bat == null) {
			bat = pumpkinLocation.getWorld().spawn(pumpkinLocation.clone().add(.5, 1.5, .5), Bat.class);
			bat.setMaxHealth(2048.0);
			bat.setHealth(2048.0);
			bat.setNoDamageTicks(Integer.MAX_VALUE);
			bat.setRemoveWhenFarAway(true);
			return;
		}
		if (bat.isDead() || !bat.isValid()) {
			tryCancel();
			return;
		}

		count++;

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

	private void tryCancel() {
		try {
			cancel();
		} catch (IllegalStateException ex) {
			System.out.println("[FlickeringPumpkinsLite] Could not cancel bat task!");
			ex.printStackTrace();
		}
	}
}
