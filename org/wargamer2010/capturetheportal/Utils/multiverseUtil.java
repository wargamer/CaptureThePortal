package org.wargamer2010.capturetheportal.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class multiverseUtil {
    private multiverseUtil() {
        
    }

    protected static boolean multiversePortalNear(int radius, Block origin) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        if(plugin == null)
            return false;
        MultiversePortals portal = (MultiversePortals)plugin;
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = -radius; y <= radius; y++) {
                    Location loc = new Location(origin.getWorld(), origin.getX()+x, origin.getY(), origin.getZ()+z);
                    if(portal.getPortalManager().getPortal(loc) != null)
                        return true;
                }
            }
        }
        return false;
    }
}
