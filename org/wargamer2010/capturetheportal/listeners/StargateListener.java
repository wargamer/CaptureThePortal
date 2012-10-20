package org.wargamer2010.capturetheportal.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import net.TheDgtl.Stargate.event.StargateAccessEvent;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.Utils.Util;

public class StargateListener implements Listener {
    CaptureThePortal capture;

    public StargateListener() {
        capture = CaptureThePortal.instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void StargatePortalListener(StargatePortalEvent event) {
        if(event.isCancelled())
            return;
        Block block = event.getPlayer().getLocation().getBlock();
        Player player = event.getPlayer();
        int isAllowed = capture.isAllowedToPortal(block, player, Material.AIR);        
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setCancelled(true);
        }        
    }    
    
    @EventHandler(priority = EventPriority.HIGH)
    public void StargateAccessListener(StargateAccessEvent event) {        
        if(event.isCancelled() && !event.getDeny())
            return;
        Block block = event.getPlayer().getLocation().getBlock();
        Player player = event.getPlayer();
        int isAllowed = capture.isAllowedToPortal(block, player, Material.AIR);        
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);            
            event.setDeny(true);
        }        
    }    
}
