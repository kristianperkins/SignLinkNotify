package com.github.krockode.signs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.sl.API.TickMode;
import com.bergerkiller.bukkit.sl.API.Variable;
import com.bergerkiller.bukkit.sl.API.Variables;

public class SignLinkNotify extends JavaPlugin {

    private static SignLinkNotify plugin;
    public static SignLinkNotify getPlugin() {
        return plugin;
    }

    public void onDisable() {
    }

    public void onEnable() {
        plugin = this;
        getConfig().options().copyDefaults(true);
        this.saveConfig();
        List<String> locations = getConfig().getStringList("variables.file_locations");
        if (locations != null && !locations.isEmpty()) {
            VariableChecker updater = new VariableChecker(locations);
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, updater, 10, 20);
        }
    }
}

class VariableChecker implements Runnable {

    private Plugin plugin = SignLinkNotify.getPlugin();
    private List<String> configLocations;
    private Map<String, String> knownVariables = new HashMap<String, String>();

    public VariableChecker(List<String> fileLocations) {
        this.configLocations = fileLocations;
    }

    public void run() {
        Map<String, String> updated = new HashMap<String, String>();
        for (String location : configLocations) {
            updated.putAll(readVariableNotificationFiles(new File(location)));
        }
        if (!updated.isEmpty()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new VariableUpdater(updated));
        }
    }

    private Map<String, String> readVariableNotificationFiles(File location) {
        Map<String, String> updates = new HashMap<String, String>();
        if (!location.canRead()) {
            return updates;
        } else if (location.isDirectory()) {
            for (File file : location.listFiles()) {
                updates.putAll(readVariables(file));
            }
        } else {
            readVariables(location);
        }
        return updates;
    }

    private Map<String, String> readVariables(File file) {
        Scanner s = null;
        Map<String, String> updated = new HashMap<String, String>();
        try {
            s = new Scanner(file);
            while (s.hasNextLine()) {
                String var = s.nextLine();
                String name = var.substring(0, var.indexOf(":"));
                String message = var.substring(var.indexOf(":") + 1);
                if (!knownVariables.containsKey(name) || !message.equals(knownVariables.get(name))) {
                    updated.put(name, message);
                }
                knownVariables.put(name, message);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "could not read variable file", e);
            if (s != null)
                s.close();
        }
        return updated;
    }
}

class VariableUpdater implements Runnable {

    private Map<String, String> variables;
    public VariableUpdater(Map<String, String> variables) {
        this.variables = variables;
    }

    public void run() {
        for (String name : variables.keySet()) {
            String message = variables.get(name);
            System.out.println("updating variable: " + name + " with value " + message);
            Variable v = Variables.get(name);
            v.set(message);
            v.getTicker().interval = 10;
            v.getTicker().mode = message.length() > 12 ? TickMode.LEFT : TickMode.NONE;
        }
    }
}
