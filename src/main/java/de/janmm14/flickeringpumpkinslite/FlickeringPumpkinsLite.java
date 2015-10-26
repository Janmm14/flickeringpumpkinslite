package de.janmm14.flickeringpumpkinslite;

import de.janmm14.flickeringpumpkinslite.pumpkinconfig.PumpkinConfiguration;
import de.janmm14.flickeringpumpkinslite.pumpkinconfig.YamlPumpkinConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class FlickeringPumpkinsLite extends JavaPlugin {

	private static final String NEWLINE = System.lineSeparator();
	private static final Pattern NO_NUMBER = Pattern.compile("\\D", Pattern.LITERAL);

	private static final int INTERVAL_DEFAULT = 15;
	private static final int PROBABILITY_DEFAULT = 95;
	private static final String INTERVAL_PATH = "interval";
	private static final String PROBABILITY_PATH = "probability";

	@Getter
	private final File flickeringPumpkinsJsonFile = new File(new File(getDataFolder().getParentFile(), "FlickeringPumkins"), "pumpkins.json");

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@Getter
	private final List<UUID> specialPumpkinCreators = new ArrayList<>();
	@Getter
	private PumpkinConfiguration pumpkinConfiguration = new YamlPumpkinConfiguration(new File(getDataFolder(), "pumpkins.yml"));
	@Getter
	private FlickeringPumpkinsLiteUpdater updater = new FlickeringPumpkinsLiteUpdater(this);

	@Getter
	@Setter
	private int interval = INTERVAL_DEFAULT;
	@Getter
	@Setter
	private int probability = PROBABILITY_DEFAULT;

	@Getter
	private ItemStack pumpkinItem;

	@Override
	public void onEnable() {
		setTabExecutor("flickeringpumpkinslite", new FPLiteCommandHandler(this));
		getServer().getPluginManager().registerEvents(new PumpkinPlacementListener(this), this);

		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true).header("FlickeringPumpkinsLite configuration file" + NEWLINE +
			"GNU GPL v3 modified license - Janmm14 - Copyright since 2015" + NEWLINE +
			"See LICENSE file in the jar (use your favorite extractor) or go to my github." +
			NEWLINE +
			NEWLINE +
			"interval: how often the pumpkin should update (measured in ticks, 20 = 1 second)" + NEWLINE +
			"probability: Probability for a pumpkin to change its state at updating (0-100%)");
		cfg.addDefault(INTERVAL_PATH, INTERVAL_DEFAULT);
		cfg.addDefault(PROBABILITY_PATH, PROBABILITY_DEFAULT);
		saveConfig();
		reload(true);
		checkFlickeringPumpkinsPlugin();
		setPumpkinItem();
	}

	private void setPumpkinItem() {
		pumpkinItem = new ItemStack(Material.JACK_O_LANTERN);
		ItemMeta meta = pumpkinItem.getItemMeta();
		meta.setLore(Arrays.asList("§c§4§5§f§7Placing this pumpkin will make it flicker.", "§c§4§5§f§7Proudly presented by FlickeringPumpkinsLite"));
		meta.setDisplayName("§c§4§5§f§6Flickering pumpkin");
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
		specialPumpkinCreators.clear();
	}

	public void saveConfigChanges() {
		FileConfiguration cfg = getConfig();
		cfg.set(INTERVAL_PATH, interval);
		cfg.set(PROBABILITY_PATH, probability);
		saveConfig();
	}

	public void reload(boolean savePumpkins) {
		if (savePumpkins) {
			pumpkinConfiguration.save();
		}
		reloadConfig();
		readInterval();
		readProbability();
		//TODO read pumpkins
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
			getLogger().warning("You set the interval to zero. That means the plugin is disabled.");
		}
		this.interval = interval;
	}

	public void readProbability() {
		int probability = getConfig().getInt(PROBABILITY_PATH, Integer.MIN_VALUE);
		if (probability == Integer.MIN_VALUE) {
			String probStrOrigin = getConfig().getString(PROBABILITY_PATH);
			String probStr = NO_NUMBER.matcher(probStrOrigin.trim()).replaceAll("");
			try {
				probability = Integer.parseInt(probStr);
			} catch (NumberFormatException ex) {
				getLogger().severe("Could not read probability value, it was: " + probStrOrigin);
				getLogger().severe("Setting probability now to " + this.probability);
				return;
			}
		}
		if (probability <= 0) {
			getLogger().severe("Probability value may not be zero or below.");
			getLogger().severe("Setting probability now to " + this.probability);
			return;
		}
		this.probability = probability;
	}

	private <T extends CommandExecutor & TabCompleter> void setTabExecutor(String command, T handler) {
		PluginCommand cmd = getCommand(command);
		cmd.setExecutor(handler);
		cmd.setTabCompleter(handler);
	}
}
