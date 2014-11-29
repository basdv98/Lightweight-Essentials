package com.basdv98.plugins.lightweightessentials;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.basdv98.plugins.lightweightessentials.Updater.ReleaseType;
import com.basdv98.plugins.lightweightessentials.Updater.UpdateResult;

public class main extends JavaPlugin implements Listener {	

	protected Logger log;
	public static boolean update = false;
	public static String name = "";
	public static ReleaseType type = null;
	public static String version = "";
	public static String link = "";

	SettingsManager settings = SettingsManager.getInstance();

	//Show MOTD on join
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (settings.getConfig().getString("onjoinmotd") == "true") {
			p.sendMessage(ChatColor.GREEN + settings.getConfig().getString("motd"));
		}
		if (e.getPlayer().hasPermission("lightweightessentials.updatenotify") && settings.getConfig().getString("update") == "true") {
			Updater updater = new Updater(this, 50175, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
			name = updater.getLatestName();
			version = updater.getLatestGameVersion();
			type = updater.getLatestType();
			link = updater.getLatestFileLink();
			e.getPlayer().sendMessage(ChatColor.GREEN + "An update is available: " + name + ", a " + type + " for " + version + " available at " + link);
			e.getPlayer().sendMessage(ChatColor.GREEN + "Type /essentials update if you would like to automatically update.");
		}
	}


	public void onEnable() {
		//Create / Save Files
		settings.setup(this);
		settings.saveData();
		settings.saveConfig();

		//PluginMetrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			//Cannot submit stats to mcstats.org
		}

		//Enable message
		getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getLogger().info("Lightweight Essentials Enabled!");
	}

	public void onDisable() {
		//Disable message
		Bukkit.getServer().getLogger().info("Lightweight Essentials Disabled!");
		//Save data
		settings.saveData();
		settings.saveConfig();
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		//Kick
		if (settings.getConfig().getString("enable.kick") == "true") {
			if (cmd.getName().equalsIgnoreCase("kick")) {
				if (!sender.hasPermission("lightweightessentials.kick")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length < 1) {
					sender.sendMessage(ChatColor.RED + "Please specify a player!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Could not find player " + args[0] + "!");
					return true;
				}
				target.kickPlayer(ChatColor.RED + "You have been kicked!");
				Bukkit.getServer().getPluginManager().callEvent(new KickEvent(target, KickType.KICK));
				Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Player " + target.getName() + " has been kicked by " + sender.getName() + "!");
			}
		}

		//Ban
		if (settings.getConfig().getString("enable.ban") == "true") {
			if (cmd.getName().equalsIgnoreCase("ban")) {
				if (!sender.hasPermission("lightweightessentials.ban")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "Please specify a player!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Could not find player " + args[0] + "!");
					return true;
				}
				target.kickPlayer(ChatColor.RED + "You have been banned!");
				target.setBanned(true);
				Bukkit.getServer().getPluginManager().callEvent(new KickEvent(target, KickType.BAN));
				Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Player " + target.getName() + " has been banned by " + sender.getName() + "!");
			}
		}

		//Prevent console to send Lightweight Essentials commands
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You can only use this command from ingame!");
			return true;
		}
		Player player = (Player) sender;

		//LIghtweight Essentials Basic Command
		if (cmd.getName().equalsIgnoreCase("essentials")) {

			//No Subcommand
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Use one of our subcommands:");
				player.sendMessage(ChatColor.DARK_RED + "/" + cmd.getName() + " help" + ChatColor.RED + ", " + ChatColor.DARK_RED + "/" + cmd.getName() + " reload" + ChatColor.RED + ", " + ChatColor.DARK_RED + "/" + cmd.getName() + " update" + ChatColor.RED + ".");
				return true;
			}

			//Reload
			if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("lightweightessentials.reload")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				settings.reloadConfig();
				settings.reloadData();
				player.sendMessage(ChatColor.GREEN + "[Lightweight Essentials] Reload Complete!");
			}

			//Update
			if (args[0].equalsIgnoreCase("update")) {
				if (!sender.hasPermission("lightweightessentials.update")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if(settings.getConfig().getString("update") == "true") {
					Updater updater = new Updater(this, 50175, this.getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
					if (updater.getResult().equals(UpdateResult.NO_UPDATE)) {
						sender.sendMessage(ChatColor.GREEN + "Lightweight Essentials is already up to date.");
						return true;
					}
					if (updater.getResult().equals(UpdateResult.DISABLED)) {
						sender.sendMessage(ChatColor.RED + "Updating is disabled in it's configuration file.");
						return true;
					}
					if (updater.getResult().equals(UpdateResult.SUCCESS)) {
						sender.sendMessage(ChatColor.GREEN + "Lightweight Essentials has been updated successfully!");
						return true;
					}
					if (!updater.getResult().equals(UpdateResult.SUCCESS)) {
						sender.sendMessage(ChatColor.RED + "An error occurred while updating Lightweight Essentials.");
						return true;
					}
					return true;
				}
				sender.sendMessage(ChatColor.RED + "Updating is disabled in the Lightweight Essentials configuration file.");
			}

			//Help
			if (args[0].equalsIgnoreCase("help")) {
				if (!sender.hasPermission("lightweightessentials.help")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				player.sendMessage(ChatColor.WHITE + "--={" + ChatColor.GREEN + "Lightweight Essentials Help!" + ChatColor.WHITE + "}=--");
				player.sendMessage(ChatColor.GREEN + "/help lightweight essentials" + ChatColor.WHITE + " View all Lightweight Essentials Commands");
			}
		}

		//ClearChat	
		if (settings.getConfig().getString("enable.clearchat") == "true") {
			if (cmd.getName().equalsIgnoreCase("clearchat")) {

				if (!sender.hasPermission("lightweightessentials.clearchat")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				player.sendMessage( "                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 ");
			}
		}

		//Heal    
		if (settings.getConfig().getString("enable.heal") == "true") {
			if (cmd.getName().equalsIgnoreCase("heal")) {
				if (!sender.hasPermission("lightweightessentials.heal")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					player.setHealth(20);
					player.sendMessage(ChatColor.GREEN + "You have been healed!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Could not find player!");
					return true;
				}
				target.setHealth(20);
				target.sendMessage(ChatColor.GREEN + "You have been healed by " + sender.getName());
				player.sendMessage(ChatColor.GREEN + target.getName() + " has been healed!");
			}
		}

		//kill
		if (settings.getConfig().getString("enable.kill") == "true") {
			if (cmd.getName().equalsIgnoreCase("kill")) {

				if (!sender.hasPermission("lightweightessentials.kill")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					player.setHealth(0);
					player.sendMessage(ChatColor.GREEN + "You have been killed!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Could not find player!");
					return true;
				}
				target.setHealth(0);
				target.sendMessage(ChatColor.GREEN + "You are killed by " + sender.getName());
				player.sendMessage(ChatColor.GREEN + target.getName() + " has been killed");
			}
		}

		//feed
		if (settings.getConfig().getString("enable.feed") == "true") {
			if (cmd.getName().equalsIgnoreCase("feed")) {
				if (!sender.hasPermission("lightweightessentials.feed")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					player.setFoodLevel(20);
					player.sendMessage(ChatColor.GREEN + "You have been fed!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Could not find player!");
					return true;
				}
				target.setFoodLevel(20);
				target.sendMessage(ChatColor.GREEN + "You have been fed by " + sender.getName());
				player.sendMessage(ChatColor.GREEN + target.getName() + " has been fed!");
			}
		}

		//tp
		Player p = (Player) sender;
		if (settings.getConfig().getString("enable.tp") == "true") {
			if (cmd.getName().equalsIgnoreCase("tp")) {
				if (!sender.hasPermission("lightweightessentials.tp")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					p.sendMessage(ChatColor.RED + "Please specify a player.");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					p.sendMessage(ChatColor.RED + "Could not find player " + args[0] + "!");
					return true;
				}
				p.teleport(target.getLocation());
				p.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName());
			}
		}

		//tphere
		if (settings.getConfig().getString("enable.tphere") == "true") {
			if (cmd.getName().equalsIgnoreCase("tphere")) {
				if (!sender.hasPermission("lightweightessentials.tphere")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					p.sendMessage(ChatColor.RED + "Please specify a player.");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					p.sendMessage(ChatColor.RED + "Could not find player " + args[0] + "!");
					return true;
				}
				target.teleport(p.getLocation());
				p.sendMessage(ChatColor.GREEN + "Teleported " + target.getName() + " to you");
			}
		}

		//setspawn
		if (settings.getConfig().getString("enable.setspawn") == "true") {
			if (cmd.getName().equalsIgnoreCase("setspawn")) {
				if (!sender.hasPermission("lightweightessentials.setspawn")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				settings.getData().set("spawn.world", p.getLocation().getWorld().getName());
				settings.getData().set("spawn.x", p.getLocation().getX());
				settings.getData().set("spawn.y", p.getLocation().getY());
				settings.getData().set("spawn.z", p.getLocation().getZ());
				settings.getData().set("spawn.yaw", p.getLocation().getYaw());
				settings.getData().set("spawn.pitch", p.getLocation().getPitch());
				settings.saveData();
				p.sendMessage(ChatColor.GREEN + "Spawn set!");
			}
		}

		//spawn  
		if (settings.getConfig().getString("enable.spawn") == "true") {
			if (cmd.getName().equalsIgnoreCase("spawn")) {
				if (!sender.hasPermission("lightweightessentials.spawn")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (settings.getData().getConfigurationSection("spawn") == null) {
					p.sendMessage(ChatColor.RED + "The spawn has not been set!");
					return true;
				}
				World w = Bukkit.getServer().getWorld(settings.getData().getString("spawn.world"));
				double x = settings.getData().getDouble("spawn.x");
				double y = settings.getData().getDouble("spawn.y");
				double z = settings.getData().getDouble("spawn.z");
				float yaw = (float) settings.getData().getDouble("spawn.yaw");
				float pitch = (float) settings.getData().getDouble("spawn.pitch");
				p.teleport(new Location(w, x, y, z, yaw, pitch));
				p.sendMessage(ChatColor.GREEN + "Teleported to Spawn");
			}
		}

		//clearinventory
		if (settings.getConfig().getString("enable.clearinventory") == "true") {
			if (cmd.getName().equalsIgnoreCase("clearinventory")) {
				if (!sender.hasPermission("lightweightessentials.clearinventory")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					PlayerInventory pi = p.getInventory(); 
					pi.clear();
					player.sendMessage(ChatColor.GREEN + "Inventory Cleared!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Could not find player!");
					return true;
				}
				PlayerInventory tar = target.getInventory(); 
				tar.clear();
				target.sendMessage(ChatColor.GREEN + "Your inventory is cleared by " + sender.getName());
				player.sendMessage(ChatColor.GREEN + "You cleared the inventory of" + target.getName());
			}
		}

		//motd
		if (settings.getConfig().getString("enable.motd") == "true") {
			if (cmd.getName().equalsIgnoreCase("motd")) {
				if (!sender.hasPermission("lightweightessentials.motd")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				sender.sendMessage(ChatColor.GREEN + "MOTD: " + settings.getConfig().getString("motd"));
			}
		}

		//setmotd
		if (settings.getConfig().getString("enable.setmotd") == "true") {
			if (cmd.getName().equalsIgnoreCase("setmotd")) {
				if (!sender.hasPermission("lightweightessentials.setmotd")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "Please specify a message!");
					return true;
				}
				StringBuilder str = new StringBuilder();
				for (int i = 0; i < args.length; i++) {
					str.append(args[i] + " ");
				}
				String motd = str.toString();
				settings.getConfig().set("motd", motd);
				settings.saveConfig();
				sender.sendMessage(ChatColor.GREEN + "MOTD set to: " + motd);
			}
		}

		//setwarp
		if (settings.getConfig().getString("enable.setwarp") == "true") {
			if (cmd.getName().equalsIgnoreCase("setwarp")) {
				if (!sender.hasPermission("lightweightessentials.setwarp")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					p.sendMessage(ChatColor.RED + "Please specify a name!");
					return true;
				}
				settings.getData().set("warps." + args[0] + ".world", p.getLocation().getWorld().getName());
				settings.getData().set("warps." + args[0] + ".x", p.getLocation().getX());
				settings.getData().set("warps." + args[0] + ".y", p.getLocation().getY());
				settings.getData().set("warps." + args[0] + ".z", p.getLocation().getZ());
				settings.getData().set("warps." + args[0] + ".yaw", p.getLocation().getYaw());
				settings.getData().set("warps." + args[0] + ".pitch", p.getLocation().getPitch());
				settings.saveData();
				p.sendMessage(ChatColor.GREEN + "Set warp " + args[0] + "!");
			}
		}

		//warp
		if (settings.getConfig().getString("enable.warp") == "true") {
			if (cmd.getName().equalsIgnoreCase("warp")) {
				if (!sender.hasPermission("lightweightessentials.warp")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					p.sendMessage(ChatColor.RED + "Please specify a name!");
					return true;
				}
				if (settings.getData().getConfigurationSection("warps." + args[0]) == null) {
					p.sendMessage(ChatColor.RED + "Warp " + args[0] + " does not exist!");
					return true;
				}
				World w = Bukkit.getServer().getWorld(settings.getData().getString("warps." + args[0] + ".world"));
				double x = settings.getData().getDouble("warps." + args[0] + ".x");
				double y = settings.getData().getDouble("warps." + args[0] + ".y");
				double z = settings.getData().getDouble("warps." + args[0] + ".z");
				float yaw = (float) settings.getData().getDouble("warps." + args[0] + ".yaw");
				float pitch = (float) settings.getData().getDouble("warps." + args[0] + ".pitch");
				p.teleport(new Location(w, x, y, z, yaw, pitch));
				p.sendMessage(ChatColor.GREEN + "Teleported to " + args[0] + "!");
			}
		}

		//delwarp
		if (settings.getConfig().getString("enable.delwarp") == "true") {
			if (cmd.getName().equalsIgnoreCase("delwarp")) {
				if (!sender.hasPermission("lightweightessentials.delwarp")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					p.sendMessage(ChatColor.RED + "Please specify a name!");
					return true;
				}
				if (settings.getData().getConfigurationSection("warps." + args[0]) == null) {
					p.sendMessage(ChatColor.RED + "Warp " + args[0] + " does not exist!");
					return true;
				}
				settings.getData().set("warps." + args[0], null);
				settings.saveData();
				p.sendMessage(ChatColor.GREEN + "Removed warp " + args[0] + "!");
			}
		}

		//sethome
		if (settings.getConfig().getString("enable.sethome") == "true") {
			if (cmd.getName().equalsIgnoreCase("sethome")) {
				if (!sender.hasPermission("lightweightessentials.sethome")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				settings.getData().set("homes." + player.getName() + ".world", p.getLocation().getWorld().getName());
				settings.getData().set("homes." + player.getName() + ".x", p.getLocation().getX());
				settings.getData().set("homes." + player.getName() + ".y", p.getLocation().getY());
				settings.getData().set("homes." + player.getName() + ".z", p.getLocation().getZ());
				settings.getData().set("homes." + player.getName() + ".yaw", p.getLocation().getYaw());
				settings.getData().set("homes." + player.getName() + ".pitch", p.getLocation().getPitch());
				settings.saveData();
				p.sendMessage(ChatColor.GREEN + "Set your home!");
			}
		}

		//home
		if (settings.getConfig().getString("enable.home") == "true") {
			if (cmd.getName().equalsIgnoreCase("home")) {
				if (!sender.hasPermission("lightweightessentials.home")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (settings.getData().getConfigurationSection("homes." + player.getName()) == null) {
					p.sendMessage(ChatColor.RED + "Your home is not set!");
					return true;
				}
				World w = Bukkit.getServer().getWorld(settings.getData().getString("homes." + player.getName() + ".world"));
				double x = settings.getData().getDouble("homes." + player.getName() + ".x");
				double y = settings.getData().getDouble("homes." + player.getName() + ".y");
				double z = settings.getData().getDouble("homes." + player.getName() + ".z");
				float yaw = (float) settings.getData().getDouble("homes." + player.getName() + ".yaw");
				float pitch = (float) settings.getData().getDouble("homes." + player.getName() + ".pitch");
				p.teleport(new Location(w, x, y, z, yaw, pitch));
				p.sendMessage(ChatColor.GREEN + "Teleported to your home!");
			}
		}

		//delhome
		if (settings.getConfig().getString("enable.delhome") == "true") {
			if (cmd.getName().equalsIgnoreCase("delhome")) {
				if (!sender.hasPermission("lightweightessentials.delhome")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (settings.getData().getConfigurationSection("homes." + player.getName()) == null) {
					p.sendMessage(ChatColor.RED + "Your home is not set!");
					return true;
				}
				settings.getData().set("homes." + player.getName(), null);
				settings.saveData();
				p.sendMessage(ChatColor.GREEN + "Removed your home!");
			}
		}

		//Burn
		if (settings.getConfig().getString("enable.burn") == "true") {
			if (cmd.getName().equalsIgnoreCase("burn")) {
				if (!sender.hasPermission("lightweightessentials.burn")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length == 0) {
					player.setFireTicks(10000);
					player.sendMessage(ChatColor.GREEN + "You are ignited!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Could not find player!");
					return true;
				}
				target.setFireTicks(10000);
				target.sendMessage(ChatColor.GREEN + "You have been ignited by " + sender.getName());
				player.sendMessage(ChatColor.GREEN + target.getName() + " has been ignited!");
			}
		}

		//msg
		if (settings.getConfig().getString("enable.msg") == "true") {
			if (cmd.getName().equalsIgnoreCase("msg")) {
				if (!sender.hasPermission("lightweightessentials.msg")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + "Please Specify a player and a message!");
					return true;
				}
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Could not find player!");
					return true;
				}
				target.sendMessage(ChatColor.GREEN + "[" + ChatColor.RED + player.getName() + ChatColor.GREEN + " -> me] " + ChatColor.WHITE + args[1]);
				player.sendMessage(ChatColor.GREEN + "[me -> " + ChatColor.RED + target.getName() + ChatColor.GREEN + "] " + ChatColor.WHITE + args[1]);
			}
		}

		//broadcast
		if (settings.getConfig().getString("enable.broadcast") == "true") {
			if (cmd.getName().equalsIgnoreCase("broadcast")) {
				if (!sender.hasPermission("lightweightessentials.broadcast")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Please Specify a message");
					return true;
				}
				Bukkit.broadcastMessage(ChatColor.RED + "[" + ChatColor.DARK_RED + "Broadcast" + ChatColor.RED + "] " + ChatColor.GREEN + args[0]);
			}
		}

		//ping
		if (settings.getConfig().getString("enable.ping") == "true") {
			if (cmd.getName().equalsIgnoreCase("ping")) {
				if (!sender.hasPermission("lightweightessentials.ping")) {
					sender.sendMessage(ChatColor.RED + "You are not permitted to use that command!");
					return true;
				}
				player.sendMessage(ChatColor.GREEN + "Pong!");
			}
		}

		//Stop the code
		return true;
	}	
}