package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;

public interface Hook {
    public Boolean isAllied(Player CapturingPlayer, String tag);
    
    public String getGroupByName(Player player);
}
