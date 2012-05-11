package org.wargamer2010.capturetheportal.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyAdvancedHook implements Hook {
    Towny instance = null;
    
    public TownyAdvancedHook(Plugin TA) {
        instance = (Towny)TA;
    }
    
    public String getName() {
        return "Towny";
    }
    
    public String getGroupType() {
        return "Town";
    }
    
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        try {
            Location spawnlocation = instance.getTownyUniverse().getTownSpawnLocation(CapturingPlayer);            
            String townName = TownyUniverse.getTownName(spawnlocation);                        
            return CombatUtil.isAlly(townName, tag);
        } catch(TownyException TE) {
            return false;
        }
    }
    
    public String getGroupByPlayer(Player player) {
        try {
            Location spawnlocation = instance.getTownyUniverse().getTownSpawnLocation(player);
            return TownyUniverse.getTownName(spawnlocation);            
        } catch(TownyException TE) {
            return "";
        }
    }
}
