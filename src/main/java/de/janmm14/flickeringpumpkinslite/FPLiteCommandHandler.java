package de.janmm14.flickeringpumpkinslite;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import de.janmm14.flickeringpumpkinslite.pumpkinconfig.JsonPumpkinConfiguration;
import de.janmm14.flickeringpumpkinslite.pumpkinconfig.PumpkinConfiguration;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FPLiteCommandHandler implements TabExecutor {

	private static final List<String> SUBCOMMAND_LIST = ImmutableList.of("get","toggle", "reload", "options", "import");
	private static final List<String> OPTIONS_OPTIONS = ImmutableList.of("interval", "probability", "spawn-bats", "toggle-default");
	private static final Joiner COMMA_JOINER = Joiner.on(", ");
	private static final String PERMISSION_CMD_PREFIX = "flickeringpumpkinslite.command";

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
		switch (args[0].toLowerCase()) {
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
					sender.sendMessage("§6You are no longer building flickering pumpkins by placing any pumpkin.");
				} else {
					list.add(uuid);
					sender.sendMessage("§6You are now building flickering pumpkins by placing any pumpkin.");
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
					switch (args[1].toLowerCase()) {
						case "interval": {
							sender.sendMessage("§6Value of the option§e interval §6is: §e" + plugin.getInterval());
							break;
						}
						case "probability": {
							sender.sendMessage("§6Value of the option§e probability §6is: §e" + plugin.getProbability());
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
						default: {
							sender.sendMessage("§cUnknown option " + args[0]);
							break;
						}
					}
					break;
				}
				switch (args[1].toLowerCase()) {
					case "spawnbats":
					case "spawn-bats": {
						boolean bool = Boolean.parseBoolean(args[2]);
						plugin.setBats(bool);
						sender.sendMessage("§6Value of the option§e spawn-bats §6is now: §e" + plugin.isBats());
						return true;
					}
					case "toggledefault":
					case "toggle-default": {
						boolean bool = Boolean.parseBoolean(args[2]);
						plugin.setToggleDefault(bool);
						sender.sendMessage("§6Value of the option§e toggle-default §6is now: §e" + plugin.isToggleDefault());
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
				}
				switch (args[1].toLowerCase()) {
					case "interval": {
						if (val > 1000) {
							sender.sendMessage("§cInterval too high. §7However if you really want it, set it in the configuration file.");
							break;
						}
						plugin.setInterval(val);
						sender.sendMessage("§6Value of the option§e interval §6is now: §e" + plugin.getInterval());
						break;
					}
					case "probability": {
						if (val > 100) {
							sender.sendMessage("§cProbability too high. Needs to be between §60 §cand §6100§c.");
						}
						plugin.setProbability(val);
						sender.sendMessage("§6Value of the option§e probability §6is now: §e" + plugin.getProbability());
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
				}
				PumpkinConfiguration pumpkinConfig = plugin.getPumpkinConfiguration();
				JsonPumpkinConfiguration jsonConfig = new JsonPumpkinConfiguration(plugin.getFlickeringPumpkinsJsonFile());
				if (jsonConfig.getPumpkinLocations().isEmpty()) {
					sender.sendMessage("§6Did not copied pumpkins from plugin FlickeringPumpkins because there were no locations specified.");
				} else {
					pumpkinConfig.copyFrom(jsonConfig);
					pumpkinConfig.save();
					sender.sendMessage("§6Copied pumpkins from plugin FlickeringPumkins.");
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
		sender.sendMessage("§c/" + alias + " get&7 - &6Get a special pumpkin which turns into a flickering one (regardless of the toggle)");
		sender.sendMessage("§c/" + alias + " toggle&7 - &6Toggle any pumpkin turns into a flickering one on placement for you");
		sender.sendMessage("§c/" + alias + " reload&7 - &6Reload the configuration from the file");
		sender.sendMessage("§c/" + alias + " options <option> [value]&7 - &6See or write configuration options");
		sender.sendMessage("§c/" + alias + " import&7 - &6Import data from &eFlickeringPumpkins &6plugin");
	}

	private void sendSetHelp(CommandSender sender, String alias) {
		sender.sendMessage("§6You can modify these options: " + COMMA_JOINER.join(OPTIONS_OPTIONS));
		sender.sendMessage("§6To see the options value, use this command: §c/" + alias + " options <option>");
		sender.sendMessage("§6To modify the options value, use this command: §c/" + alias + " options <option> <newvalue>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 0) {
			return SUBCOMMAND_LIST.stream()
				.filter(subCmd -> sender.hasPermission(PERMISSION_CMD_PREFIX + "." + subCmd))
				.collect(Collectors.toList());
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("options")) {
				if (!sender.hasPermission(PERMISSION_CMD_PREFIX + ".options")) {
					return Collections.emptyList();
				}
				return OPTIONS_OPTIONS;
			}
			String args0 = args[0].toLowerCase();
			return SUBCOMMAND_LIST.stream()
				.filter(subCmd -> subCmd.startsWith(args0))
				.filter(subCmd -> sender.hasPermission(PERMISSION_CMD_PREFIX + "." + subCmd))
				.collect(Collectors.toList());
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("options") && sender.hasPermission(PERMISSION_CMD_PREFIX + ".options")) {
				String args1 = args[1].toLowerCase();
				return OPTIONS_OPTIONS.stream()
					.filter(subCmd -> subCmd.startsWith(args1))
					.collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}
}
