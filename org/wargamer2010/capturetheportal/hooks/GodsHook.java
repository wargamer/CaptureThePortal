package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import com.dogonfire.gods.Gods;

public class GodsHook implements Hook {
    Gods instance = null;    
    
    public void setPlugin(Plugin pl) {
        instance = ((Gods)pl);
    }
    
    public String getName() {
        return "Gods";
    }
    
    public String getGroupType() {
        return "God";
    }
    
    public ChatColor getGroupColor(Player player) {
        return null;
    }
    
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        return false;
    }
    
    public String getGroupByPlayer(Player player) {
        String god = instance.getBelieverManager().getGodForBeliever(player.getName());
        if(god == null) god = "";
        return god;
    }    
}
