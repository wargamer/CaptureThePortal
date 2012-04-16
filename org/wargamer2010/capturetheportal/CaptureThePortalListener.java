package org.wargamer2010.capturetheportal;

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

public class CaptureThePortalListener implements Listener {
    CaptureThePortal capture;

    CaptureThePortalListener(CaptureThePortal pl) {
        capture = pl;
    }
    
    private Material isPortalMaterial(Block checkthis) {
        if(checkthis.getType() == Material.PORTAL)
            return Material.PORTAL;
        else if(capture.getEnderSupport() && checkthis.getType() == Material.ENDER_PORTAL)
            return Material.ENDER_PORTAL;
        else if(capture.getWormholeSupport() && checkthis.getType() == Material.STATIONARY_WATER)
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
        if(event.getPlayer() != null) {

            Player player = event.getPlayer();
            double xdif = event.getTo().getX() - event.getFrom().getX();
            double zdif = event.getTo().getZ() - event.getFrom().getZ();
            double ydif = 0;
            if(zdif == 0.0 && xdif == 0.0 && !capture.getEnderSupport())
                return;
            else if(zdif == 0.0 && xdif == 0.0 && capture.getEnderSupport()) {
                ydif = event.getTo().getY() - event.getFrom().getY();
                if(ydif == 0.0)
                    return;
            }

            boolean x_or_z = (Math.abs(xdif) > Math.abs(zdif)) ? true : false;
            boolean touchingPortal = false;
            Material touchingMaterial = null;
            
            if((touchingMaterial = isPortalMaterial(event.getTo().getBlock())) != null)            
                touchingPortal = true;
            else {
                Location getTo = new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
                double radius = 0.8;
                double precision = 0.1;
                
                for(double x = -radius; x <= radius && touchingMaterial == null; x += precision) {
                    for(double z = -radius; z <= radius && touchingMaterial == null; z += precision) {
                        for(double y = -radius; y <= radius && touchingMaterial == null; y += precision) {
                            getTo = new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());                            
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
            
            if(touchingPortal && isAllowed != 2) {
                int delta = 1;
                if(!capture.getDieFromUncapturedPortal()) {
                    player.sendMessage("Your team has not captured this portal yet so,");
                    player.sendMessage("you're not allowed to use it!");
                } else {
                    player.sendMessage("Your body was ripped apart while trying to cross the portal.");
                    player.sendMessage("The portal does not belong to your faction.");
                    player.damage(1000);
                    delta = 3;
                }
                
                Location loc = event.getTo();
                if(x_or_z) {
                    if(xdif > 0) {
                        loc.subtract(delta, 0, 0);
                    } else {
                        loc.add(delta, 0, 0);                        
                    }
                } else {
                    if(zdif > 0) {
                        loc.subtract(0, 0, delta);
                    } else {
                        loc.add(0, 0, delta);
                    }
                }
                player.teleport(loc);                
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(final PlayerPortalEvent event) {
        if(event.getPlayer() != null && event.getFrom() != null && event.getTo() != null) {
            if(event.getTo().getWorld().getEnvironment() != Environment.NETHER || event.getFrom().getWorld().getEnvironment() != Environment.NORMAL || event.isCancelled())
                return;
            Player player = event.getPlayer();
            // Admins shouldn't force a respawn
            if(capture.getTeamOfPlayer(player).equals(""))
                return;
            Player[] online = event.getPlayer().getServer().getOnlinePlayers();
            for (int i = 0; i < online.length; ++i) {
                Player p = online[i];
                if(p == null)
                    continue;
                if(p.getWorld().getEnvironment() == event.getTo().getWorld().getEnvironment() 
                        && !capture.getTeamOfPlayer(player).equals(capture.getTeamOfPlayer(p))
                        && !capture.isAllied(player, p.getName())) {
                    p.sendMessage("Another team captured the portal!");
                    p.sendMessage("You are now forced to respawn!");
                    p.teleport(player.getWorld().getSpawnLocation());
                }
            }
        }
    }
}
