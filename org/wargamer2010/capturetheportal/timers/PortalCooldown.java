package org.wargamer2010.capturetheportal.timers;

import org.wargamer2010.capturetheportal.Utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.wargamer2010.capturetheportal.CaptureThePortal;

public class PortalCooldown extends Timer {
    private CaptureThePortal plugin;
    private Block button;
    private int cooldown_left;
    private String group;
    private int decremented;
    private Player capturer;

    public PortalCooldown(CaptureThePortal CTP, Block block, int time, String g, int pDecremented, Player pCapturer) {
        cooldown_left = time;
        group = g;
        plugin = CTP;
        button = block;
        decremented = pDecremented;
        capturer = pCapturer;
    }

    public int getTimeLeft() {
        return cooldown_left;
    }

    public String getType() {
        return "cooldown";
    }

    public Player getCapturer() {
        return capturer;
    }

    @Override
    public void run() {
        cooldown_left -= 1;
        decremented += 1;
        if(cooldown_left != 0) {
            if(cooldown_left == CaptureThePortal.getCoolMessageTime())
                Util.broadcastMessage(ChatColor.GREEN+CaptureThePortal.getMessage("cooldown_message").replace("[cooldown]", (ChatColor.BLUE+Util.parseTime(cooldown_left)+ChatColor.GREEN)));
            else if(decremented == CaptureThePortal.getCooldownInterval()) {
                Util.broadcastMessage(ChatColor.GREEN+CaptureThePortal.getMessage("cooldown_message").replace("[cooldown]", (ChatColor.BLUE+Util.parseTime(cooldown_left)+ChatColor.GREEN)));
                decremented = 0;
            }
            plugin.addTimer(button.getLocation(), this);
            plugin.addCaptureLocation(button, group, (cooldown_left));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, Util.getTicksFromSeconds(1));
        } else {
            plugin.addCaptureLocation(button, group, 0);
            Util.broadcastMessage(CaptureThePortal.getMessage("available_message").replace("[location]", Util.locToPrintableString(button.getLocation())));
            plugin.removeTimer(button.getLocation());
        }
    }
}