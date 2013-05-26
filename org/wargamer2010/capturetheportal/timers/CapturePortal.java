package org.wargamer2010.capturetheportal.timers;

import org.wargamer2010.capturetheportal.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.capturetheportal.CaptureThePortal;

public class CapturePortal extends Timer {
    private CaptureThePortal plugin;
    private Player capturer;
    private Block button;
    private int cooldown_time;
    private int capturedelay_left;

    public CapturePortal(CaptureThePortal CTP, Player pl, Block block, int time, int left) {
        plugin = CTP;
        capturer = pl;
        button = block;
        cooldown_time = time;
        capturedelay_left = left;
    }

    public String getType() {
        return "delay";
    }

    public int getTimeLeft() {
        return capturedelay_left;
    }

    public Player getCapturer() {
        return capturer;
    }

    private boolean isMoved(Location loc1, Location loc2, double threshold) {
        double xdif = Math.abs(loc1.getX() - loc2.getX());
        double ydif = Math.abs(loc1.getY() - loc2.getY());
        double zdif = Math.abs(loc1.getZ() - loc2.getZ());
        if(xdif > threshold || ydif > threshold || zdif > threshold)
            return true;
        else
            return false;
    }

    private int findCustomCooldown(Block center) {
        int radius = 2;
        int custom = -1;

        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = -radius; y <= radius; y++) {
                    Location loc = new Location(center.getWorld(), center.getX()+x, center.getY()+y, center.getZ()+z);
                    if(loc.getBlock().getType() == Material.SIGN || loc.getBlock().getType() == Material.SIGN_POST || loc.getBlock().getType() == Material.WALL_SIGN) {
                        Sign sign = (Sign) loc.getBlock().getState();
                        String lastline = sign.getLine(3);
                        int time = Util.getTimeFromString(lastline);
                        if(time > -1)
                            return time;
                    }
                }
            }
        }
        return custom;
    }

    public void run() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        if(!isMoved(button.getLocation(), capturer.getLocation(), 1.5)) {
            if(capturedelay_left != 0) {
                capturedelay_left -= 1;
                scheduler.scheduleSyncDelayedTask(plugin, this, Util.getTicksFromSeconds(1));
                plugin.addTimer(button.getLocation(), this);
            } else {
                plugin.addCaptureLocation(button, plugin.getTeamOfPlayer(capturer), 0);
                plugin.updateControlledSigns(button, plugin.getTeamOfPlayer(capturer));
                Block woolCenter = capturer.getWorld().getBlockAt(button.getX(), (button.getY()-1), button.getZ());
                plugin.colorSquare(woolCenter, capturer.getWorld(), plugin.getColor(capturer));
                Util.sendMessagePlayer(CaptureThePortal.getMessage("player_captured_it"), capturer);

                Integer customcooldown = findCustomCooldown(button);
                if(customcooldown == -1)
                    customcooldown = cooldown_time;

                PortalCooldown pc = new PortalCooldown(plugin, button, customcooldown, plugin.getTeamOfPlayer(capturer), 0, capturer);
                plugin.addTimer(button.getLocation(), pc);
                scheduler.scheduleSyncDelayedTask(plugin, pc, Util.getTicksFromSeconds(1));
            }
        } else {
            CaptureThePortal.getStorage().deleteCapture(button.getLocation());
            Util.sendMessagePlayer(CaptureThePortal.getMessage("player_failed_capture"), capturer);
            plugin.removeTimer(button.getLocation());
        }
    }
}
