package com.github.krockode.signs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.sl.API.TickMode;
import com.bergerkiller.bukkit.sl.API.Variable;
import com.bergerkiller.bukkit.sl.API.Variables;

public class SignLinkNotify extends JavaPlugin {

    private static final int ONE_SECOND_TICKS = 20;

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
        NotifySettings settings = NotifySettings.getInstance(getConfig());
        if (settings.getLocations() != null && !settings.getLocations().isEmpty()) {
            VariableChecker checker = new VariableChecker(settings);
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, checker, ONE_SECOND_TICKS, ONE_SECOND_TICKS);
        }
    }
}

class VariableChecker implements Runnable {

    private Plugin plugin = SignLinkNotify.getPlugin();
    private Map<String, String> knownVariables = new HashMap<String, String>();
    private NotifySettings settings;

    public VariableChecker(NotifySettings settings) {
        this.settings = settings;
    }

    public void run() {
        Map<String, VariableUpdateDetails> updated = new HashMap<String, VariableUpdateDetails>();
        for (String location : settings.getLocations()) {
            updated.putAll(readVariableNotificationFiles(new File(location)));
        }
        if (!updated.isEmpty()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new VariableUpdater(updated));
        }
    }

    private Map<String, VariableUpdateDetails> readVariableNotificationFiles(File location) {
        Map<String, VariableUpdateDetails> updates = new HashMap<String, VariableUpdateDetails>();
        if (!location.canRead()) {
            return updates;
        } else if (location.isDirectory()) {
            for (File file : location.listFiles()) {
                updates.putAll(readVariableNotificationFiles(file));
            }
        } else if (settings.getFileMask().matcher(location.getName()).matches()) {
            plugin.getLogger().log(Level.FINEST, "reading variables from: " + location.getAbsolutePath());
            updates.putAll(readVariables(location));
        }
        return updates;
    }

    private Map<String, VariableUpdateDetails> readVariables(File file) {
        Scanner s = null;
        Map<String, VariableUpdateDetails> updated = new HashMap<String, VariableUpdateDetails>();
        try {
            s = new Scanner(file);
            while (s.hasNextLine()) {
                String var = s.nextLine();
                int colonIndex = var.indexOf(":");
                if (colonIndex > 0) {
                    VariableUpdateDetails details = new VariableUpdateDetails();
                    String name = var.substring(0, colonIndex);
                    if (name.contains("-")) {
                        TickMode tickMode = TickMode.valueOf(name.substring(name.indexOf("-") + 1));
                        name = name.substring(0, name.indexOf("-"));
                        details.setTickMode(tickMode);
                    }
                    String message = formatMessage(var.substring(colonIndex + 1));
                    details.setMessage(message);
                    if (!knownVariables.containsKey(name) || !message.equals(knownVariables.get(name))) {
                        updated.put(name, details);
                    }
                    knownVariables.put(name, message);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "could not read variable file", e);
            if (s != null)
                s.close();
        }
        return updated;
    }

    private String formatMessage(final String rawMessage) {
        String message = rawMessage;
        for (ChatColor color : ChatColor.values()) {
            String token = settings.getColourToken() + color.name() + settings.getColourToken();
            message = message.replace(token, color.toString());
        }
        if (!message.equals(rawMessage)) {
            message += ChatColor.RESET;
        }
        return message;
    }
}

class VariableUpdater implements Runnable {

    private Map<String, VariableUpdateDetails> variables;
    public VariableUpdater(Map<String, VariableUpdateDetails> variables) {
        this.variables = variables;
    }

    public void run() {
        for (String name : variables.keySet()) {
            VariableUpdateDetails details = variables.get(name);
            Variable v = Variables.get(name);
            v.set(details.getMessage());
            v.getTicker().interval = details.getInterval();
            v.getTicker().mode = details.getTickMode();
        }
    }
}

class VariableUpdateDetails {
    private TickMode tickMode;
    private int interval = 10;
    private String message;

    public TickMode getTickMode() {
        if (tickMode != null) {
            return tickMode;
        } else {
            return message.length() > 12 ? TickMode.LEFT : TickMode.NONE;
        }
    }
    public void setTickMode(TickMode tickMode) {
        this.tickMode = tickMode;
    }
    public int getInterval() {
        return interval;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
