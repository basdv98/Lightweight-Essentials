package com.basdv98.plugins.lightweightessentials;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class SettingsManager {

	private SettingsManager() { }

	static SettingsManager instance = new SettingsManager();

	public static SettingsManager getInstance() {
		return instance;
	}

	Plugin p;

	FileConfiguration config;
	File cfile;

	FileConfiguration data;
	File dfile;

	public void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		
		cfile = new File(p.getDataFolder(), "config.yml");

		if (!cfile.exists()) {
			try {
				cfile.createNewFile();
			}
			catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create config.yml!");
			}
		}

		config = YamlConfiguration.loadConfiguration(cfile);
		config.addDefault("enable.ban", true);
		config.addDefault("enable.kick", true);
		config.addDefault("enable.clearchat", true);
		config.addDefault("enable.heal", true);
		config.addDefault("enable.feed", true);
		config.addDefault("enable.tp", true);
		config.addDefault("enable.tphere", true);
		config.addDefault("enable.setspawn", true);
		config.addDefault("enable.spawn", true);
		config.addDefault("enable.clearinventory", true);
		config.addDefault("enable.motd", true);
		config.addDefault("enable.setmotd", true);
		config.addDefault("enable.setwarp", true);
		config.addDefault("enable.warp", true);
		config.addDefault("enable.delwarp", true);
		config.addDefault("enable.kill", true);
		config.addDefault("enable.burn", true);
		config.addDefault("enable.msg", true);
		config.addDefault("enable.broadcast", true);
		config.addDefault("enable.sethome", true);
		config.addDefault("enable.home", true);
		config.addDefault("enable.delhome", true);
		config.addDefault("enable.ping", true);
		config.addDefault("onjoinmotd", true);
		config.addDefault("motd", "Welcome to the Server!");
		config.addDefault("update", true);
		config.options().copyDefaults(true);

		dfile = new File(p.getDataFolder(), "data.yml");

		if (!dfile.exists()) {
			try {
				dfile.createNewFile();
			}
			catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create data.yml!");
			}
		}

		data = YamlConfiguration.loadConfiguration(dfile);
		data.options().copyDefaults(true);
	}

	public FileConfiguration getData() {
		return data;
	}

	public void saveData() {
		try {
			data.save(dfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save data.yml!");
		}
	}

	public void reloadData() {
		data = YamlConfiguration.loadConfiguration(dfile);
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public void saveConfig() {
		try {
			config.save(cfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save config.yml!");
		}
	}

	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(cfile);
	}

	public PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}