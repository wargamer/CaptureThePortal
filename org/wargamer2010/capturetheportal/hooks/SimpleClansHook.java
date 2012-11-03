package org.wargamer2010.capturetheportal.hooks;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.Utils.Util;

public class SimpleClansHook implements Hook {
    SimpleClans instance = null;
    
    public void setPlugin(Plugin pl) {
        instance = (SimpleClans)pl;
    }
    
    public String getName() {
        return "SimpleClans";
    }
    
    public String getGroupType() {
        return "Clan";
    }
    
    public ChatColor getGroupColor(Player player) {
        Clan CP = instance.getClanManager().getClanByPlayerName(player.getName());
        if(CP == null)
            return null;
        if(CP.getTagLabel().length() < 3)
            return null;            
        return Util.getColorFromString(CP.getTagLabel(), 3);
    }
    
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        if(instance == null)
            return false;
        Clan CP = instance.getClanManager().getClanByPlayerName(CapturingPlayer.getName());        
        if(CP == null)
            return false;
        if(CP.getTagLabel().equals(tag) || CP.getName().equals(tag))
            return true;
        return CP.isAlly(tag);
    }
    
    public String getGroupByPlayer(Player player) {
        if(instance == null)
            return "";
        Clan CP = instance.getClanManager().getClanByPlayerName(player.getName());
        if(CP == null)
            return "";
        if(!CaptureThePortal.getFullgroupnames())
            return CP.getTagLabel();
        else
            return CP.getName();
    }
}
