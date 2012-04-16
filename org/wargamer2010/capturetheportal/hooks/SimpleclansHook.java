package org.wargamer2010.capturetheportal.hooks;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SimpleclansHook implements Hook {
    SimpleClans instance = null;
    
    public SimpleclansHook(Plugin SC) {
        instance = (SimpleClans)SC;
    }
    
    public String getName() {
        return "SimpleClans";
    }
    
    public String getGroupType() {
        return "Clan";
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
        return CP.getTag();
    }
}
