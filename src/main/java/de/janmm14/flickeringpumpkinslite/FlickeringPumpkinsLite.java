package de.janmm14.flickeringpumpkinslite;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.janmm14.flickeringpumpkinslite.pumpkinconfig.PumpkinConfiguration;
import de.janmm14.flickeringpumpkinslite.pumpkinconfig.YamlPumpkinConfiguration;

public class FlickeringPumpkinsLite extends JavaPlugin {

	private static final String NEWLINE = System.lineSeparator();
	private static final Pattern NO_NUMBER = Pattern.compile("\\D", Pattern.LITERAL);

	private static final int INTERVAL_DEFAULT = 5;
	private static final int ON_PROBABILITY_DEFAULT = 5;
	private static final int OFF_PROBABILITY_DEFAULT = 50;
	private static final int SOUND_PROBABILITY_DEFAULT = 35;
	private static final boolean BATS_DEFAULT = true;
	private static final boolean TOGGLE_DEFAULT_DEFAULT = false;
	private static final boolean PLAY_SOUND_DEFAULT = true;
	private static final boolean SCARY_DEFAULT = false;
	private static final String SOUND_DEFAULT = "ENDERDRAGON_WINGS";

	private static final String INTERVAL_PATH = "interval";
	private static final String ON_PROBABILITY_PATH = "probability-on";
	private static final String OFF_PROBABILITY_PATH = "probability-off";
	private static final String SOUND_PROBABILITY_PATH = "sound-probability";
	private static final String BATS_PATH = "spawn-bats";
	private static final String TOGGLE_DEFAULT_PATH = "toggle-default";
	private static final String PLAY_SOUND_PATH = "play-sound";
	private static final String SCARY_PATH = "scary";
	private static final String SOUND_PATH = "sound";

	@Getter
	private final File flickeringPumpkinsJsonFile = new File(new File(getDataFolder().getParentFile(), "FlickeringPumkins"), "pumpkins.json");

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@Getter
	private final List<UUID> specialPumpkinCreators = new ArrayList<>();
	@Getter
	private PumpkinConfiguration pumpkinConfiguration = new YamlPumpkinConfiguration(new File(getDataFolder(), "pumpkins.yml"));
	@Getter
	private FlickeringPumpkinsLiteUpdater updater;

	@Getter
	@Setter
	private int interval = INTERVAL_DEFAULT;
	@Getter
	@Setter
	private int onProbability = ON_PROBABILITY_DEFAULT;
	@Getter
	@Setter
	private int offProbability = OFF_PROBABILITY_DEFAULT;
	@Getter
	@Setter
	private int soundProbability = SOUND_PROBABILITY_DEFAULT;
	@Getter
	@Setter
	private boolean bats = BATS_DEFAULT;
	@Getter
	@Setter
	private boolean toggleDefault = TOGGLE_DEFAULT_DEFAULT;
	@Getter
	@Setter
	private boolean playSound = PLAY_SOUND_DEFAULT;
	@Getter
	@Setter
	private boolean scary = SCARY_DEFAULT;
	@Getter
	@Setter
	private String sound = SOUND_DEFAULT;

	@Getter
	private ItemStack pumpkinItem;

	@Override
	public void onEnable() {
		//Load classes to not error on disable in case of other errors
		BatTask.class.getName();
		FlickeringPumpkinsLiteUpdater.class.getName();
		PumpkinConfiguration.class.getName();

		setPumpkinItem();

		setTabExecutor("flickeringpumpkinslite", new FPLiteCommandHandler(this));
		getServer().getPluginManager().registerEvents(new PumpkinBlockListener(this), this);
		updater = new FlickeringPumpkinsLiteUpdater(this);

		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true).header("FlickeringPumpkinsLite configuration file" + NEWLINE +
			"GNU GPL v3 modified license - Janmm14 - Copyright 2015 - 2018 (and further)" + NEWLINE +
			"See LICENSE file in the jar (use your favorite zip extractor) or go to my github." +
			NEWLINE +
			NEWLINE +
			"interval: how often the pumpkin should be checked for update (measured in ticks, 20 = 1 second)" + NEWLINE +
			"probability-on: Probability for a pumpkin to turn on at updating (1-100)%" + NEWLINE +
			"probability-off: Probability for a pumpkin to turn off at updating (1-100)%" + NEWLINE +
			"spawn-bats: Whether to spawn bats with turning a pumpkin on" + NEWLINE +
			"toggle-default: The default state of the toggle pumpkins placed turn into flickering pumpkins" + NEWLINE +
			"play-sound: Whether to play a sound on bat spawn" + NEWLINE +
//			"scary: Whether to enable scary mode" + NEWLINE +
			"sound: What sound should be played");
		cfg.addDefault(INTERVAL_PATH, INTERVAL_DEFAULT);
		cfg.addDefault(ON_PROBABILITY_PATH, ON_PROBABILITY_DEFAULT);
		cfg.addDefault(OFF_PROBABILITY_PATH, OFF_PROBABILITY_DEFAULT);
		cfg.addDefault(SOUND_PROBABILITY_PATH, SOUND_PROBABILITY_DEFAULT);
		cfg.addDefault(BATS_PATH, BATS_DEFAULT);
		cfg.addDefault(TOGGLE_DEFAULT_PATH, TOGGLE_DEFAULT_DEFAULT);
		cfg.addDefault(PLAY_SOUND_PATH, PLAY_SOUND_DEFAULT);
//		cfg.addDefault(SCARY_PATH, SCARY_DEFAULT);
		cfg.addDefault(SOUND_PATH, SOUND_DEFAULT);
		saveConfig();
		reload(true);
		checkFlickeringPumpkinsPlugin();
		updater.start();
		saveConfigChanges();
	}

	private void setPumpkinItem() {
		pumpkinItem = new ItemStack(Material.JACK_O_LANTERN);
		ItemMeta meta = pumpkinItem.getItemMeta();
		meta.setDisplayName("§c§4§5§f§6Flickering pumpkin");
		meta.setLore(Arrays.asList("§c§4§5§f§7Placing this pumpkin will make it flicker.", "§c§4§5§f§7Proudly presented by FlickeringPumpkinsLite"));
		pumpkinItem.setItemMeta(meta);
	}

	private void checkFlickeringPumpkinsPlugin() {
		Plugin plugin = getServer().getPluginManager().getPlugin("FlickeringPumkins");
		if (plugin != null) {
			getLogger().warning("The plugin FlickeringPumkins is still loaded. That plugin is built inefficienty.");
			if (flickeringPumpkinsJsonFile.exists()) {
				getLogger().warning("You can import the data from the plugin with the command /fpl import");
				File pluginFile = getPluginFile((JavaPlugin) plugin);
				if (pluginFile == null) {
					getLogger().warning("You can safely remove the plugin FlickeringPumpkins out of your plugins folder.");
				} else {
					getLogger().warning("You can safely remove the file " + pluginFile.getName() + " out of the plugins folder.");
				}
				getLogger().warning("However to be able to import your pumpkin locations, you should leave the file plugins/FlickeringPumpkins/pumpkins.json");
			} else {
				getLogger().warning("It seems like you did not used the plugin yet, so you can just remove it.");
			}
		} else if (flickeringPumpkinsJsonFile.exists()) {
			getLogger().info("Found pumpkin position file by the plugin FlickeringPumpkins.");
			getLogger().info("You can import the data by using the command /fpl import");
		}
	}

	private static File getPluginFile(JavaPlugin plugin) {
		try {
			Field fileField = JavaPlugin.class.getDeclaredField("file");

			boolean accessible = fileField.isAccessible();
			fileField.setAccessible(true);

			File file = (File) fileField.get(plugin);

			fileField.setAccessible(accessible);

			return file;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onDisable() {
		updater.getPluginDisabled().set(true);
		List<BatTask> batTasks = BatTask.getBatTasks();
		final ArrayList<BatTask> batTasksCopy = new ArrayList<>(batTasks);
		for (BatTask batTask : batTasksCopy) {
			batTask.tryCancel();
		}
		batTasks.clear();
		try {
			updater.stop();
		} catch (Exception ex) {
			getLogger().warning("Could not stop updater thread, it will cancel itself hopefully soon. Error:");
			ex.printStackTrace();
		}
		updater = null;
		specialPumpkinCreators.clear();
		pumpkinConfiguration.save();
		pumpkinConfiguration = null;
	}

	public void saveConfigChanges() {
		FileConfiguration cfg = getConfig();
		cfg.set(INTERVAL_PATH, interval);
		cfg.set(ON_PROBABILITY_PATH, onProbability);
		cfg.set(OFF_PROBABILITY_PATH, offProbability);
		cfg.set(SOUND_PROBABILITY_PATH, soundProbability);
		cfg.set(BATS_PATH, bats);
		cfg.set(TOGGLE_DEFAULT_PATH, toggleDefault);
		cfg.set(PLAY_SOUND_PATH, playSound);
//		cfg.set(SCARY_PATH, scary);
		cfg.set(SOUND_PATH, sound);
		saveConfig();
	}

	public void reload(boolean savePumpkins) {
		if (savePumpkins) {
			pumpkinConfiguration.save();
		}
		reloadConfig();
		readInterval();
		readProbability();
		readBats();
		readToggleDefault();
		readPlaySound();
//		readScary();
		readSound();
		updater.notifyUpdate();
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		pumpkinConfiguration.reload();
	}

	public void readInterval() {
		int interval = getConfig().getInt(INTERVAL_PATH, Integer.MIN_VALUE);
		if (interval == Integer.MIN_VALUE) {
			getLogger().severe("Could not read interval value, it was: " + getConfig().getString(INTERVAL_PATH));
			getLogger().severe("Setting interval now to " + this.interval);
			return;
		}
		if (interval < 0) {
			getLogger().severe("Interval value may not be below zero.");
			getLogger().severe("Setting interval now to " + this.interval);
			return;
		}
		if (interval == 0) {
			getLogger().warning("You set the interval to zero. That means the plugin is essentially disabled.");
		}
		this.interval = interval;
	}

	public void readProbability() {
		readProbabilityOn();
		readProbabilityOff();
		readSoundProbability();
	}

	private void readProbabilityOn() {
		int probability = getConfig().getInt(ON_PROBABILITY_PATH, Integer.MIN_VALUE);
		if (probability == Integer.MIN_VALUE) {
			String probStrOrigin = getConfig().getString(ON_PROBABILITY_PATH);
			if (probStrOrigin == null || probStrOrigin.isEmpty()) {
				return;
			}
			String probStr = NO_NUMBER.matcher(probStrOrigin.trim()).replaceAll("");
			try {
				probability = Integer.parseInt(probStr);
			} catch (NumberFormatException ex) {
				getLogger().severe("Could not read probability-on value, it was: " + probStrOrigin);
				getLogger().severe("Setting probability-on now to " + this.onProbability);
				return;
			}
		}
		if (probability <= 0) {
			getLogger().severe("Probability-on value may not be zero or below.");
			getLogger().severe("Setting probability-on now to " + this.onProbability);
			return;
		}
		this.onProbability = probability;
	}

	private void readProbabilityOff() {
		int probability = getConfig().getInt(OFF_PROBABILITY_PATH, Integer.MIN_VALUE);
		if (probability == Integer.MIN_VALUE) {
			String probStrOrigin = getConfig().getString(OFF_PROBABILITY_PATH);
			if (probStrOrigin == null || probStrOrigin.isEmpty()) {
				return;
			}
			String probStr = NO_NUMBER.matcher(probStrOrigin.trim()).replaceAll("");
			try {
				probability = Integer.parseInt(probStr);
			} catch (NumberFormatException ex) {
				getLogger().severe("Could not read probability-on value, it was: " + probStrOrigin);
				getLogger().severe("Setting probability-on now to " + this.offProbability);
				return;
			}
		}
		if (probability <= 0) {
			getLogger().severe("Probability-on value may not be zero or below.");
			getLogger().severe("Setting probability-on now to " + this.offProbability);
			return;
		}
		this.offProbability = probability;
	}

	private void readSoundProbability() {
		int probability = getConfig().getInt(SOUND_PROBABILITY_PATH, Integer.MIN_VALUE);
		if (probability == Integer.MIN_VALUE) {
			String probStrOrigin = getConfig().getString(SOUND_PROBABILITY_PATH);
			if (probStrOrigin == null || probStrOrigin.isEmpty()) {
				return;
			}
			String probStr = NO_NUMBER.matcher(probStrOrigin.trim()).replaceAll("");
			try {
				probability = Integer.parseInt(probStr);
			} catch (NumberFormatException ex) {
				getLogger().severe("Could not read sound-probability value, it was: " + probStrOrigin);
				getLogger().severe("Setting sound-probability now to " + this.offProbability);
				return;
			}
		}
		if (probability < 0) {
			getLogger().severe("Sound-probability value may not be negative.");
			getLogger().severe("Setting sound-probability now to " + this.offProbability);
			return;
		}
		this.soundProbability = probability;
	}

	public void readBats() {
		bats = getConfig().getBoolean(BATS_PATH);
	}

	public void readToggleDefault() {
		toggleDefault = getConfig().getBoolean(TOGGLE_DEFAULT_PATH);
	}

	public void readPlaySound() {
		playSound = getConfig().getBoolean(PLAY_SOUND_PATH);
	}

	private void readScary() {
//		scary = getConfig().getBoolean(SCARY_PATH);
	}

	private void readSound() {
		String sound = getConfig().getString(SOUND_PATH);
		if (sound == null) {
			getLogger().severe("No sound configured!");
			getLogger().severe("Setting sound now to " + this.sound);
			return;
		}
		sound = sound.toUpperCase(Locale.ENGLISH);
		try {
			Sound.valueOf(sound);
			this.sound = sound;
		} catch (IllegalArgumentException ex) {
			getLogger().severe("Could not find configured sound " + sound);
			getLogger().severe("Setting sound now to " + this.sound);
		}
	}

	@SuppressWarnings("SameParameterValue")
	private <T extends CommandExecutor & TabCompleter> void setTabExecutor(String command, T handler) {
		PluginCommand cmd = getCommand(command);
		cmd.setExecutor(handler);
		cmd.setTabCompleter(handler);
	}
}
