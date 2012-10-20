package org.wargamer2010.capturetheportal.listeners;

import org.wargamer2010.capturetheportal.Utils.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.Location;
import org.bukkit.World.Environment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.wargamer2010.capturetheportal.CaptureThePortal;

public class CaptureThePortalListener implements Listener {
    CaptureThePortal capture;

    public CaptureThePortalListener() {
        capture = CaptureThePortal.instance;
    }
    
    private Material isPortalMaterial(Block checkthis) {
        if(checkthis.getType() == Material.PORTAL)
            return Material.PORTAL;
        else if(CaptureThePortal.getEnderSupport() && checkthis.getType() == Material.ENDER_PORTAL)
            return Material.ENDER_PORTAL;
        else if(CaptureThePortal.getWormholeSupport() && checkthis.getType() == Material.STATIONARY_WATER)
            return Material.STATIONARY_WATER;
        return null;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {        
        if(event.isCancelled())
            return;
        if(event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.STONE_PLATE)
            capture.capturePortal(event.getClickedBlock(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if(event.getPlayer() != null && !event.isCancelled()) {
            Player player = event.getPlayer();
            // getVelocity() is buggy, shows movement on the Y axis when the player's moving in a X/Z direction
            Vector velocity = new Vector(
                    (event.getTo().getX() - event.getFrom().getX()),
                    (event.getTo().getY() - event.getFrom().getY()), 
                    (event.getTo().getZ() - event.getFrom().getZ()));
            if(velocity.getZ() == 0.0d && velocity.getX() == 0.0d && velocity.getY() == 0.0d)
                return;            

            boolean touchingPortal = false;
            Material touchingMaterial = null;
            
            if((touchingMaterial = isPortalMaterial(event.getTo().getBlock())) != null)            
                touchingPortal = true;
            else {
                Location getTo = new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
                double radius = 0.8;
                double precision = 0.3;
                
                for(double x = -radius; x <= radius && touchingMaterial == null; x += precision) {
                    for(double z = -radius; z <= radius && touchingMaterial == null; z += precision) {
                        for(double y = -radius; y <= radius && touchingMaterial == null; y += precision) {                            
                            getTo.setX(event.getFrom().getX());
                            getTo.setY(event.getFrom().getY());
                            getTo.setZ(event.getFrom().getZ());
                            if((touchingMaterial = isPortalMaterial(getTo.add(x, y, z).getBlock())) != null) {
                                touchingPortal = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            if(!touchingPortal)
                return;
            
            Block block = event.getTo().getBlock();
            int isAllowed = capture.isAllowedToPortal(block, player, touchingMaterial);            
            
            if(touchingPortal && isAllowed != 0)
                Util.bouncePlayer(isAllowed, player, event.getTo(), velocity);            
        }
    } 
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(final PlayerPortalEvent event) {
        if(event.isCancelled())
            return;
        Player player = event.getPlayer();
        Location from = event.getFrom();
        int isAllowed = capture.isAllowedToPortal(from.getBlock(), player, from.getBlock().getType());            
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setCancelled(true);
            return;
        }
        else if(event.getPlayer() != null && event.getFrom() != null && event.getTo() != null) {
            if(event.getTo().getWorld().getEnvironment() != Environment.NETHER || from.getWorld().getEnvironment() != Environment.NORMAL || event.isCancelled())
                return;            
            // Admins shouldn't force a respawn
            if(capture.getTeamOfPlayer(player).equals(""))
                return;
            Player[] online = player.getServer().getOnlinePlayers();
            for (int i = 0; i < online.length; ++i) {
                Player p = online[i];
                if(p == null)
                    continue;
                if(p.getWorld().getEnvironment() == event.getTo().getWorld().getEnvironment() 
                        && !capture.getTeamOfPlayer(player).equals(capture.getTeamOfPlayer(p))
                        && !capture.isAllied(player, p.getName())) {
                    Util.sendMessagePlayer(CaptureThePortal.getMessage("player_forced_respawn"), p);                    
                    p.teleport(player.getWorld().getSpawnLocation());
                }
            }
        }
    }
}
