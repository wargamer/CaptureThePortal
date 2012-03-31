package org.wargamer2010.capturetheportal.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyAdvancedHook implements Hook {
    Towny instance = null;
    
    public TownyAdvancedHook(Plugin TA) {
        instance = (Towny)TA;
    }
    
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        try {
            Location spawnlocation = instance.getTownyUniverse().getTownSpawnLocation(CapturingPlayer);
            String townName = instance.getTownyUniverse().getTownName(spawnlocation);
            return instance.getTownyUniverse().isAlly(townName, tag);
        } catch(TownyException TE) {
            return false;
        }
    }
    
    public String getGroupByName(Player player) {
        try {
            Location spawnlocation = instance.getTownyUniverse().getTownSpawnLocation(player);
            return instance.getTownyUniverse().getTownName(spawnlocation);            
        } catch(TownyException TE) {
            return "";
        }
    }
}
