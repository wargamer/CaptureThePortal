package org.wargamer2010.capturetheportal;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class CapturePortal implements Runnable {
    private CaptureThePortal plugin;
    private Player capturer;
    private Block button;
    private Location standing;
    private int cooldown_time;        
    private Server server;
    private int capturedelay_left;
    private String cooldown_message;
    private int cooldown_message_timeleft;   

    CapturePortal(CaptureThePortal CTP, Player pl, Block block, int time, Server serv, int left, Location stand, String cdm, int cdm_time) {
        plugin = CTP;
        capturer = pl;
        button = block;
        standing = stand;
        cooldown_time = time;
        server = serv;
        capturedelay_left = left;
        cooldown_message = cdm;
        cooldown_message_timeleft = cdm_time;
    }

    private boolean isMoved(Location loc1, Location loc2, double threshold) {
        double xdif = loc1.getX() - loc2.getX();
        double ydif = loc1.getY() - loc2.getY();
        double zdif = loc1.getZ() - loc2.getZ();            
        if( (xdif > threshold || xdif < -threshold) || (ydif > threshold || ydif < -threshold) || (zdif > threshold || zdif < -threshold) )
            return true;
        else
            return false;
    }

    public void run() {
        if(!isMoved(button.getLocation(), capturer.getLocation(), 1.5)) {
            if(capturedelay_left != 0) {
                capturedelay_left -= 10; // Decrementing by a second (10 deciseconds)
                server.getScheduler().scheduleSyncDelayedTask(plugin, new CapturePortal(plugin, capturer, button, cooldown_time, server, capturedelay_left, standing, cooldown_message, cooldown_message_timeleft), 10);                    
                PortalCooldown pc = new PortalCooldown(plugin, button, plugin.getCapturedelay(), capturer, server, cooldown_message, cooldown_message_timeleft, "delay", 0);
                plugin.addTimer(button.getLocation(), pc);
                server.getScheduler().scheduleSyncDelayedTask(plugin, pc, 10);
            } else {
                plugin.addCaptureLocation(button, capturer);
                Block woolCenter = capturer.getWorld().getBlockAt(button.getX(), (button.getY()-1), button.getZ());
                plugin.colorSquare(woolCenter, capturer.getWorld(), (int)plugin.getColor(plugin.getTeamOfPlayer(capturer)));
                capturer.sendMessage("Succesfully captured the portal!");
                PortalCooldown pc = new PortalCooldown(plugin, button, cooldown_time, capturer, server, cooldown_message, cooldown_message_timeleft, "cooldown", 0);
                plugin.addTimer(button.getLocation(), pc);
                server.getScheduler().scheduleSyncDelayedTask(plugin, pc, 10);
            }
        } else
            capturer.sendMessage("Failed to capture the Portal because you moved.");
    }
}
