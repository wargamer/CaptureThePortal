package org.wargamer2010.capturetheportal;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;

public class PortalCooldown implements Runnable {
    private CaptureThePortal plugin;
    private Block button;
    private int cooldown_left;
    private Player capturer;
    private Server server;        
    private String cooldown_message;
    private String type;
    private int cooldown_message_timeleft;
    private int decremented;

    PortalCooldown(CaptureThePortal CTP, Block block, int time, Player capt, Server serv, String cdm, int cdm_time, String t, int pDecremented) {
        cooldown_left = time;
        capturer = capt;
        server = serv;
        cooldown_message = cdm;
        cooldown_message_timeleft = cdm_time;
        plugin = CTP;
        button = block;
        type = t;
        decremented = pDecremented;
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

    public void run() {
        cooldown_left -= 10;
        decremented += 1;
        if(cooldown_left != 0) {
            if(!cooldown_message.equals("") && type.equals("cooldown") && cooldown_left == cooldown_message_timeleft)
                server.broadcastMessage(ChatColor.GREEN+cooldown_message.replace("[cooldown]", (ChatColor.BLUE+Util.parseTime(cooldown_left/10)+ChatColor.GREEN)));
            else if(!cooldown_message.equals("") && type.equals("cooldown") && decremented == plugin.getCooldownInterval()) {
                server.broadcastMessage(ChatColor.GREEN+cooldown_message.replace("[cooldown]", (ChatColor.BLUE+Util.parseTime(cooldown_left/10)+ChatColor.GREEN)));
                decremented = 0;
            }
            PortalCooldown pc = new PortalCooldown(plugin, button, cooldown_left, capturer, server, cooldown_message, cooldown_message_timeleft, type, decremented);
            plugin.addTimer(button.getLocation(), pc);
            server.getScheduler().scheduleSyncDelayedTask(plugin, pc, 10);
        }


    }
}