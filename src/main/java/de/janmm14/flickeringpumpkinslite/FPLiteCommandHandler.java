package de.janmm14.flickeringpumpkinslite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import de.janmm14.flickeringpumpkinslite.pumpkinconfig.JsonPumpkinConfiguration;
import de.janmm14.flickeringpumpkinslite.pumpkinconfig.PumpkinConfiguration;

@RequiredArgsConstructor
public class FPLiteCommandHandler implements TabExecutor {

	private static final List<String> SUBCOMMAND_LIST = ImmutableList.of("get", "toggle", "reload", "options", "import", "list-sounds");
	private static final List<String> OPTIONS_OPTIONS = ImmutableList.of("interval", "probability-on", "probability-off", "sound-probability", "spawn-bats", "toggle-default", "play-sound"/*, "scary"*/, "sound");
	private static final List<String> TRUE_AND_FALSE = ImmutableList.of("true", "false");
	private static final List<String> SOUNDS;
	private static final Joiner COMMA_JOINER = Joiner.on("§7, §e");
	private static final String PERMISSION_CMD_PREFIX = "flickeringpumpkinslite.command";

	static {
		final ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (Sound value : Sound.values()) {
			builder.add(value.name().toUpperCase(Locale.ENGLISH));
		}
		SOUNDS = builder.build();
	}

	private final FlickeringPumpkinsLite plugin;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (!sender.hasPermission(PERMISSION_CMD_PREFIX + ".use")) {
			sender.sendMessage(cmd.getPermissionMessage());
			return true;
		}
		if (args.length == 0) {
			sendHelp(sender, alias);
			return true;
		}
		switch (args[0].toLowerCase(Locale.ENGLISH)) {
			case "get": {
				if (!(sender instanceof Player)) {
					sender.sendMessage("§cThis subcommand is only available as a player.");
					return true;
				}
				Player plr = (Player) sender;
				if (!plr.hasPermission(PERMISSION_CMD_PREFIX + ".get")) {
					plr.sendMessage("§cYou do not have permission to use this sub-command!");
					return true;
				}
				plr.getInventory().addItem(plugin.getPumpkinItem());
				break;
			}
			case "toggle": {
				if (!(sender instanceof Player)) {
					sender.sendMessage("§cThis subcommand is only available as a player.");
					return true;
				}
				Player plr = (Player) sender;
				if (!plr.hasPermission(PERMISSION_CMD_PREFIX + ".toggle")) {
					plr.sendMessage("§cYou do not have permission to use this sub-command!");
					return true;
				}
				List<UUID> list = plugin.getSpecialPumpkinCreators();
				UUID uuid = plr.getUniqueId();
				if (list.remove(uuid)) {
					if (plugin.isToggleDefault()) {
						sender.sendMessage("§6You are now building flickering pumpkins by placing a jack o'lantern.");
					} else {
						sender.sendMessage("§6You are no longer building flickering pumpkins by placing a jack o'lantern.");
					}
				} else {
					list.add(uuid);
					if (plugin.isToggleDefault()) {
						sender.sendMessage("§6You are no longer building flickering pumpkins by placing a jack o'lantern.");
					} else {
						sender.sendMessage("§6You are now building flickering pumpkins by placing a jack o'lantern.");
					}
				}
				break;
			}
			case "reload": {
				if (!sender.hasPermission(PERMISSION_CMD_PREFIX + ".reload")) {
					sender.sendMessage("§cYou do not have permission to use this sub-command!");
					return true;
				}
				plugin.reload(true);
				sender.sendMessage("§6Plugin configuration reloaded!");
				break;
			}
			case "options": {
				if (!sender.hasPermission(PERMISSION_CMD_PREFIX + ".options")) {
					sender.sendMessage("§cYou do not have permission to use this sub-command!");
					return true;
				}
				if (args.length == 1) {
					sendSetHelp(sender, alias);
					break;
				}
				if (args.length == 2) {
					switch (args[1].toLowerCase(Locale.ENGLISH)) {
						case "interval": {
							sender.sendMessage("§6Value of the option§e interval §6is: §e" + plugin.getInterval());
							break;
						}
						case "probabilityon":
						case "probability-on": {
							sender.sendMessage("§6Value of the option§e probability-on §6is: §e" + plugin.getOnProbability());
							break;
						}
						case "probabilityoff":
						case "probability-off": {
							sender.sendMessage("§6Value of the option§e probability-off §6is: §e" + plugin.getOffProbability());
							break;
						}
						case "soundprobability":
						case "sound-probability": {
							sender.sendMessage("§6Value of the option§e sound-probability §6is: §e" + plugin.getSoundProbability());
							break;
						}
						case "spawnbats":
						case "spawn-bats": {
							sender.sendMessage("§6Value of the option§e spawn-bats §6is: §e" + plugin.isBats());
							break;
						}
						case "toggledefault":
						case "toggle-default": {
							sender.sendMessage("§6Value of the option§e toggle-default §6is: §e" + plugin.isBats());
							break;
						}
						case "playsound":
						case "play-sound": {
							sender.sendMessage("§6Value of the option§e play-sound §6is: §e" + plugin.isPlaySound());
							break;
						}
//						case "scary": {
//							sender.sendMessage("§6Value of the option§e scary §6is: §e" + plugin.isScary());
//							break;
//						}
						case "sound": {
							sender.sendMessage("§6Value of the option§e sound §6is: §e" + plugin.getSound());
							break;
						}
						default: {
							sender.sendMessage("§cUnknown option " + args[0]);
							break;
						}
					}
					break;
				}
				// args.length == 3
				switch (args[1].toLowerCase(Locale.ENGLISH)) {
					case "spawnbats":
					case "spawn-bats": {
						boolean bool = Boolean.parseBoolean(args[2]);
						plugin.setBats(bool);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e spawn-bats §6is now: §e" + plugin.isBats());
						return true;
					}
					case "toggledefault":
					case "toggle-default": {
						boolean bool = Boolean.parseBoolean(args[2]);
						plugin.setToggleDefault(bool);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e toggle-default §6is now: §e" + plugin.isToggleDefault());
						return true;
					}
					case "playsound":
					case "play-sound": {
						boolean bool = Boolean.parseBoolean(args[2]);
						plugin.setPlaySound(bool);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e play-sound §6is now: §e" + plugin.isPlaySound());
						return true;
					}
//					case "scary": {
//						boolean bool = Boolean.parseBoolean(args[2]);
//						plugin.setScary(bool);
//						plugin.saveConfigChanges();
//						sender.sendMessage("§6Value of the option§e scary §6is now: §e" + plugin.isScary());
//						return true;
//					}
					case "sound": {
						try {
							Sound.valueOf(args[2]);

							plugin.setSound(args[2]);
							plugin.saveConfigChanges();
							sender.sendMessage("§6Value of the option§e sound §6is now: §e" + plugin.getSound());
						} catch (IllegalArgumentException ex) {
							sender.sendMessage("§cNo sound with the name §6" + plugin.getSound() + "§c found, option not changed.");
							sender.sendMessage("§6You can list all available sounds with §e/" + alias + " list-sounds");
						}
						return true;
					}
				}
				int val;
				try {
					val = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex) {
					sender.sendMessage("§cThis is no (valid) number: §6" + args[2]);
					break;
				}
				if (val < 0) {
					sender.sendMessage("§cYou may only use positive numbers.");
					break;
				}
				switch (args[1].toLowerCase(Locale.ENGLISH)) {
					case "interval": {
						if (val > 1000) {
							sender.sendMessage("§cInterval too high. §7However if you really want it, set it in the configuration file.");
							break;
						}
						plugin.setInterval(val);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e interval §6is now: §e" + plugin.getInterval());
						break;
					}
					case "probabilityon":
					case "probability-on": {
						if (val > 100) {
							sender.sendMessage("§cProbability too high. Needs to be between§6 0 §cand§6 100§c.");
						}
						plugin.setOnProbability(val);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e probability-on §6is now: §e" + plugin.getOnProbability());
						break;
					}
					case "probabilityoff":
					case "probability-off": {
						if (val > 100) {
							sender.sendMessage("§cProbability too high. Needs to be between§6 0 §cand§6 100§c.");
						}
						plugin.setOffProbability(val);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e probability-off §6is now: §e" + plugin.getOffProbability());
						break;
					}
					case "soundprobability":
					case "sound-probability": {
						if (val > 100) {
							sender.sendMessage("§cProbability too high. Needs to be between§6 0 §cand§6 100§c.");
						}
						plugin.setSoundProbability(val);
						plugin.saveConfigChanges();
						sender.sendMessage("§6Value of the option§e sound-probability §6is now: §e" + plugin.getSoundProbability());
						break;
					}
					default: {
						sender.sendMessage("§cUnknown option " + args[0]);
						sendSetHelp(sender, alias);
						break;
					}
				}
				break;
			}
			case "import": {
				if (!sender.hasPermission(PERMISSION_CMD_PREFIX + ".import")) {
					sender.sendMessage("§cYou do not have permission to use this sub-command!");
					return true;
				}
				if (!plugin.getFlickeringPumpkinsJsonFile().exists()) {
					sender.sendMessage("§cFile not found: plugins/FlickeringPumpkins/pumpkins.json");
					return true;
				}
				PumpkinConfiguration pumpkinConfig = plugin.getPumpkinConfiguration();
				JsonPumpkinConfiguration jsonConfig = new JsonPumpkinConfiguration(plugin.getFlickeringPumpkinsJsonFile());
				jsonConfig.reload();
				if (jsonConfig.getPumpkinLocations().isEmpty()) {
					sender.sendMessage("§6Did not copied pumpkins from plugin FlickeringPumpkins because there were no locations specified.");
				} else {
					pumpkinConfig.copyFrom(jsonConfig);
					pumpkinConfig.save();
					sender.sendMessage("§6Copied pumpkins from plugin FlickeringPumkins.");
				}
				break;
			}
			case "listsounds":
			case "list-sounds": {
				if (!sender.hasPermission(PERMISSION_CMD_PREFIX + ".options")) {
					sender.sendMessage("§cYou do not have permission to use this sub-command!");
					return true;
				}
				StringBuilder sb = new StringBuilder();
				Sound[] values = Sound.values();
				if (values.length > 0) {
					sb.append("§e").append(values[0].name());
					for (int i = 1, valuesLength = values.length; i < valuesLength; i++) {
						Sound s = values[i];
						sb.append("§6, §e").append(s.name());
					}
					sender.sendMessage("§6Available sounds: " + sb.toString());
				}
				break;
			}
			default: {
				sender.sendMessage("§cUnknown action " + args[0]);
			}
			//noinspection fallthrough //intended fall-through, if unknown action, ppl should see the help
			case "?":
			case "help": {
				sendHelp(sender, alias);
			}
		}

		return true;
	}

	private void sendHelp(CommandSender sender, String alias) {
		sender.sendMessage("§5 §m============§6 FlickeringPumpkinsLite §eHelp §5§m============");
		sender.sendMessage("§c/" + alias + " get§7 - §6Get a special pumpkin which turns into a flickering one (regardless of the toggle)");
		sender.sendMessage("§c/" + alias + " toggle§7 - §6Toggle any pumpkin turns into a flickering one on placement for you");
		sender.sendMessage("§c/" + alias + " reload§7 - §6Reload the configuration from the file");
		sender.sendMessage("§c/" + alias + " options <option> [value]§7 - §6See or write configuration options");
		sender.sendMessage("§c/" + alias + " import§7 - §6Import data from §eFlickeringPumpkins §6plugin");
		sender.sendMessage("§c/" + alias + " list-sounds§7 - §6List available sounds");
	}

	private void sendSetHelp(CommandSender sender, String alias) {
		sender.sendMessage("§6You can modify these options:\n§e" + COMMA_JOINER.join(OPTIONS_OPTIONS));
		sender.sendMessage("§6To see the options value, use this command:\n§c/" + alias + " options <option>");
		sender.sendMessage("§6To modify the options value, use this command:\n§c/" + alias + " options <option> <newvalue>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 0) {
			plugin.getLogger().warning("Unexpected tab complete by " + sender + " cmd: " + cmd + " alias: " + alias + " args: " + Arrays.toString(args));
			plugin.getLogger().warning("The plugin is still working, but you should send these two warning messages to the author of this plugin to indicate that its not unexpected :)");
			List<String> r = new ArrayList<>();
			for (String subCmd : SUBCOMMAND_LIST) {
				if (sender.hasPermission(PERMISSION_CMD_PREFIX + "." + subCmd)) {
					r.add(subCmd);
				}
			}
			return r;
		}
		if (args.length == 1) {
			String args0 = args[0].toLowerCase(Locale.ENGLISH);
			List<String> r = new ArrayList<>();
			for (String subCmd : SUBCOMMAND_LIST) {
				if (subCmd.startsWith(args0) && sender.hasPermission(PERMISSION_CMD_PREFIX + "." + subCmd)) {
					r.add(subCmd);
				}
			}
			return r;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("options") && sender.hasPermission(PERMISSION_CMD_PREFIX + ".options")) {
				String args1 = args[1].toLowerCase(Locale.ENGLISH);
				List<String> r = new ArrayList<>();
				for (String subCmd : OPTIONS_OPTIONS) {
					if (subCmd.startsWith(args1)) {
						r.add(subCmd);
					}
				}
				return r;
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("options") && sender.hasPermission(PERMISSION_CMD_PREFIX + ".options")) {
				final String args1 = args[1];
				if (args1.equalsIgnoreCase("spawn-bats") || args1.equalsIgnoreCase("spawnbats") || args1.equalsIgnoreCase("toggle-default") || args1.equalsIgnoreCase("toggledefault") || args1.equalsIgnoreCase("play-sound") || args1.equalsIgnoreCase("playsound")/* || args[1].equalsIgnoreCase("scary")*/) {
					String args2 = args[2].toLowerCase(Locale.ENGLISH);
					List<String> r = new ArrayList<>();
					for (String subCmd : TRUE_AND_FALSE) {
						if (subCmd.startsWith(args2)) {
							r.add(subCmd);
						}
					}
					return r;
				} else if (args1.equalsIgnoreCase("sound")) {
					String args2 = args[2].toUpperCase(Locale.ENGLISH);
					List<String> r = new ArrayList<>();
					for (String subCmd : SOUNDS) {
						if (subCmd.startsWith(args2)) {
							r.add(subCmd);
						}
					}
					return r;
				}
			}
		}
		return Collections.emptyList();
	}
}
