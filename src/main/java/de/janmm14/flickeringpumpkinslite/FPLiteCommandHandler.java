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
	private static final List<String> OPTIONS_OPTIONS = ImmutableList.of("interval", "probability");
	private static final Joiner COMMA_JOINER = Joiner.on(", ");

	private final FlickeringPumpkinsLite plugin;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (!sender.hasPermission("flickeringpumpkinslite.command.use")) {
			return false;
		}
		if (args.length == 0) {
			//TODO send help
			return true;
		}
		switch (args[0].toLowerCase()) {
			case "get": {
				if (!(sender instanceof Player)) {
					sender.sendMessage("§cThis subcommand is only available as a player.");
					return true;
				}
				Player plr = (Player) sender;
				plr.getInventory().addItem(plugin.getPumpkinItem());

				break;
			}
			case "toggle": {
				if (!(sender instanceof Player)) {
					sender.sendMessage("§cThis subcommand is only available as a player.");
					return true;
				}
				List<UUID> list = plugin.getSpecialPumpkinCreators();
				Player plr = (Player) sender;
				UUID uuid = plr.getUniqueId();
				if (list.remove(uuid)) {
					sender.sendMessage("§6You are no longer building flickering pumpkins by any pumpkin.");
				} else {
					list.add(uuid);
					sender.sendMessage("§6You are now building flickering pumpkins by placing any pumpkin.");
				}
				break;
			}
			case "reload": {
				plugin.reload(true);
				break;
			}
			case "options": {
				if (args.length == 1) {
					sendSetHelp(sender, alias);
					break;
				}
				if (args.length == 2) {
					switch (args[1].toLowerCase()) {
						case "interval": {
							sender.sendMessage("§6Value of the option §einterval §6is: §e" + plugin.getInterval());
							break;
						}
						case "probability": {
							sender.sendMessage("§6Value of the option §eprobability §6is: §e" + plugin.getProbability());
							break;
						}
						default: {
							sender.sendMessage("§cUnknown option " + args[0]);
							break;
						}
					}
					break;
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
						sender.sendMessage("§6Value of the option §einterval §6is: §e" + plugin.getInterval());
						break;
					}
					case "probability": {
						if (val > 100) {
							sender.sendMessage("§cProbability too high. Needs to be between §60 §cand §6100§c.");
						}
						sender.sendMessage("§6Value of the option §eprobability §6is: §e" + plugin.getProbability());
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
				if (!plugin.getFlickeringPumpkinsJsonFile().exists()) {
					sender.sendMessage("§cFile not found: plugins/FlickeringPumpkins/pumpkins.json");
				}
				PumpkinConfiguration pumpkinConfig = plugin.getPumpkinConfiguration();
				JsonPumpkinConfiguration jsonConfig = new JsonPumpkinConfiguration(plugin.getFlickeringPumpkinsJsonFile());
				if (!jsonConfig.getPumpkinLocations().isEmpty()) {
					pumpkinConfig.copyFrom(jsonConfig);
					pumpkinConfig.save();
					sender.sendMessage("§6Copied pumpkins from plugin FlickeringPumkins.");
				} else {
					sender.sendMessage("§6Did not copied pumpkins from plugin FlickeringPumpkins because there were no locations specified.");
				}
				break;
			}
			default: {
				sender.sendMessage("§cUnknown action " + args[0]);
			}
			//noinspection fallthrough //intended fall-through, if unknown action, ppl should see the help
			case "?":
			case "help": {
				//TODO send help
			}
		}

		return true;
	}

	private void sendSetHelp(CommandSender sender, String alias) {
		sender.sendMessage("§6You can modify these options: " + COMMA_JOINER.join(OPTIONS_OPTIONS));
		sender.sendMessage("§6To see the options value, use this command: §c/" + alias + " options <option>");
		sender.sendMessage("§6To modify the options value, use this command: §c/" + alias + " options <option> <newvalue>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 0) {
			return SUBCOMMAND_LIST;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("options")) {
				return OPTIONS_OPTIONS;
			}
			String args0 = args[0].toLowerCase();
			return SUBCOMMAND_LIST.stream()
				.filter(subCmd -> subCmd.startsWith(args0))
				.collect(Collectors.toList());
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("options")) {
				String args1 = args[1].toLowerCase();
				return OPTIONS_OPTIONS.stream()
					.filter(subCmd -> subCmd.startsWith(args1))
					.collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}
}
