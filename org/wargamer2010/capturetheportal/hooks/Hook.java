package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;

public interface Hook {
    public String getName();
    
    public String getGroupType();
    
    public Boolean isAllied(Player CapturingPlayer, String tag);
    
    public String getGroupByPlayer(Player player);
}
