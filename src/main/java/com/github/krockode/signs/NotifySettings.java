package com.github.krockode.signs;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;

public class NotifySettings {

    private List<String> locations;
    private Pattern fileMask;
    private String colourToken;

    public NotifySettings() {
    }

    /**
     * Construct settings based on given FileConfiguration
     */
    public static NotifySettings getInstance(FileConfiguration config) {
        NotifySettings nc = new NotifySettings();
        nc.locations = config.getStringList("variables.file_locations");
        nc.fileMask = Pattern.compile(config.getString("variables.file_mask"));
        nc.colourToken = config.getString("variables.colour_token", "%%");
        return nc;
    }

    public List<String> getLocations() {
        return locations;
    }
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
    public Pattern getFileMask() {
        return fileMask;
    }
    public void setFileMask(Pattern fileMask) {
        this.fileMask = fileMask;
    }
    public String getColourToken() {
        return colourToken;
    }
    public void setColourToken(String colourToken) {
        this.colourToken = colourToken;
    }
}
