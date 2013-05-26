
package org.wargamer2010.capturetheportal.portals;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import net.TheDgtl.Stargate.event.StargateAccessEvent;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;
import org.wargamer2010.capturetheportal.utils.Util;

public class MultiversePortal implements IPortal, Listener {
    private MultiversePortals plugin;

    public boolean init() {
        if(!CaptureThePortalConfig.getMVPSupport())
            return false;
        Plugin temp = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        if(temp == null)
            return false;
        plugin = (MultiversePortals)temp;
        Bukkit.getServer().getPluginManager().registerEvents(this, CaptureThePortal.get());
        return true;
    }

    public String getName() {
        return "Portal";
    }

    public boolean isPortalNear(int radius, Block origin) {
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = -radius; y <= radius; y++) {
                    Location loc = new Location(origin.getWorld(), origin.getX()+x, origin.getY(), origin.getZ()+z);
                    if(plugin.getPortalManager().getPortal(loc) != null)
                        return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void MVPortalListener(MVPortalEvent event) {
        if(event.isCancelled())
            return;
        Block block = event.getFrom().getBlock();
        Player player = event.getTeleportee();
        int isAllowed = CaptureThePortal.get().isAllowedToPortal(block, player, Material.AIR);
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setCancelled(true);
        }
    }
}
