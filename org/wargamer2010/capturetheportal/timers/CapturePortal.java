package org.wargamer2010.capturetheportal.timers;

import org.wargamer2010.capturetheportal.Utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.wargamer2010.capturetheportal.CaptureThePortal;

public class CapturePortal implements Runnable {
    private CaptureThePortal plugin;
    private Player capturer;
    private Block button;
    private Location standing;
    private int cooldown_time;            
    private int capturedelay_left;        

    public CapturePortal(CaptureThePortal CTP, Player pl, Block block, int time, int left, Location stand) {
        plugin = CTP;
        capturer = pl;
        button = block;
        standing = stand;
        cooldown_time = time;        
        capturedelay_left = left;                
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
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        if(!isMoved(button.getLocation(), capturer.getLocation(), 1.5)) {
            if(capturedelay_left != 0) {
                capturedelay_left -= 10; // Decrementing by a second (10 deciseconds)
                scheduler.scheduleSyncDelayedTask(plugin, new CapturePortal(plugin, capturer, button, cooldown_time, capturedelay_left, standing), 10);                    
                PortalCooldown pc = new PortalCooldown(plugin, button, plugin.getCapturedelay(), plugin.getTeamOfPlayer(capturer), "delay", 0, capturer);
                plugin.addTimer(button.getLocation(), pc);
                scheduler.scheduleSyncDelayedTask(plugin, pc, 10);
            } else {                
                plugin.addCaptureLocation(button, plugin.getTeamOfPlayer(capturer), 0);
                Block woolCenter = capturer.getWorld().getBlockAt(button.getX(), (button.getY()-1), button.getZ());
                plugin.colorSquare(woolCenter, capturer.getWorld(), (int)plugin.getColor(capturer));
                Util.sendMessagePlayer(plugin.getMessage("player_captured_it"), capturer);
                PortalCooldown pc = new PortalCooldown(plugin, button, cooldown_time, plugin.getTeamOfPlayer(capturer), "cooldown", 0, capturer);
                plugin.addTimer(button.getLocation(), pc);
                scheduler.scheduleSyncDelayedTask(plugin, pc, 10);
            }
        } else {
            CaptureThePortal.Storage.deleteCapture(button.getLocation());
            Util.sendMessagePlayer(plugin.getMessage("player_failed_capture"), capturer);            
        }
    }
}
