package org.wargamer2010.capturetheportal;

import org.wargamer2010.capturetheportal.Utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;

public class PortalCooldown implements Runnable {
    private CaptureThePortal plugin;
    private Block button;
    private int cooldown_left;
    private String group;
    private String type;    
    private int decremented;    
    private Player capturer;

    PortalCooldown(CaptureThePortal CTP, Block block, int time, String g, String t, int pDecremented, Player pCapturer) {
        cooldown_left = time;
        group = g;        
        plugin = CTP;
        button = block;
        type = t;
        decremented = pDecremented;        
        capturer = pCapturer;
    }

    public int getCooldown() {
        return cooldown_left;
    }

    public String getType() {
        return type;
    }

    public Player getCapturer() {
        return capturer;
    }

    @Override
    public void run() {
        cooldown_left -= 10;
        decremented += 1;               
        if(cooldown_left != 0) {
            if(type.equals("cooldown") && cooldown_left == plugin.getCoolMessageTime())                
                Util.broadcastMessage(ChatColor.GREEN+plugin.getMessage("cooldown_message").replace("[cooldown]", (ChatColor.BLUE+Util.parseTime(cooldown_left/10)+ChatColor.GREEN)));
            else if(type.equals("cooldown") && decremented == plugin.getCooldownInterval()) {
                Util.broadcastMessage(ChatColor.GREEN+plugin.getMessage("cooldown_message").replace("[cooldown]", (ChatColor.BLUE+Util.parseTime(cooldown_left/10)+ChatColor.GREEN)));
                decremented = 0;
            }
            PortalCooldown pc = new PortalCooldown(plugin, button, cooldown_left, group, type, decremented, capturer);
            plugin.addTimer(button.getLocation(), pc);
            if(type.equals("cooldown"))
                plugin.addCaptureLocation(button, group, (cooldown_left/10));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, pc, 10);
        } else if(type.equals("cooldown")) {
            plugin.addCaptureLocation(button, group, 0);
            Util.broadcastMessage(plugin.getMessage("available_message"));
        }
    }
}