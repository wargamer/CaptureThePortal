package org.wargamer2010.capturetheportal.hooks;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;
import org.wargamer2010.capturetheportal.utils.Util;
import org.wargamer2010.capturetheportal.utils.Vault;

public class SimpleClansHook implements IHook {
    private SimpleClans instance = null;

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
        if(!CaptureThePortalConfig.getFullgroupnames())
            return CP.getTagLabel();
        else
            return CP.getName();
    }

    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if(!Vault.isVaultFound() || Vault.getEconomy() == null)
            return false;

        Clan CP = null;
        if(!CaptureThePortalConfig.getFullgroupnames()) {
            CP = instance.getClanManager().getClan(group);
        } else {
            for(Clan clan : instance.getClanManager().getClans()) {
                if(clan.getName().equals(group))
                    CP = clan;
            }
        }
        if(CP == null)
            return false;

        for(ClanPlayer player : CP.getAllMembers()) {
            Vault.getEconomy().depositPlayer(player.getName(), amount);
        }

        return true;
    }
}
