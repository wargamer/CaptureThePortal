package org.wargamer2010.capturetheportal.hooks;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.wargamer2010.capturetheportal.CaptureThePortal;

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
        if(ChatColor.getLastColors(CP.getTagLabel()).length() == 2) {
            ChatColor color = ChatColor.getByChar(ChatColor.getLastColors(CP.getTagLabel()).charAt(1));            
            return color;        
        }
        return null;
        
    }
    
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        if(instance == null)
            return false;
        Clan CP = instance.getClanManager().getClanByPlayerName(CapturingPlayer.getName());
        if(CP == null)
            return false;
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
