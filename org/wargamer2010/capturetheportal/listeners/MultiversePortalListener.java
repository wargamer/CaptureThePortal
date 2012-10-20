package org.wargamer2010.capturetheportal.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.Utils.Util;

public class MultiversePortalListener implements Listener {
    CaptureThePortal capture;

    public MultiversePortalListener() {
        capture = CaptureThePortal.instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void MVPortalListener(MVPortalEvent event) {
        if(event.isCancelled())
            return;
        Block block = event.getFrom().getBlock();
        Player player = event.getTeleportee();
        int isAllowed = capture.isAllowedToPortal(block, player, Material.AIR);        
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setCancelled(true);
        }        
    }
}
