package org.wargamer2010.capturetheportal.hooks;

import com.p000ison.dev.simpleclans2.api.SCCore;
import com.p000ison.dev.simpleclans2.api.clan.Clan;
import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;
import org.wargamer2010.capturetheportal.utils.Util;
import org.wargamer2010.capturetheportal.utils.Vault;

public class SimpleClans2Hook implements IHook {
    private SCCore core = null;

    @Override
    public void setPlugin(Plugin pl) {
        core = (SCCore) pl;
    }

    @Override
    public String getName() {
        return "SimpleClans2";
    }

    @Override
    public String getGroupType() {
        return "Clan";
    }

    @Override
    public ChatColor getGroupColor(Player player) {
        Clan CP = getClanByPlayer(player.getName());
        if (CP == null) {
            return null;
        }
        if (CP.getTag().length() < 3) {
            return null;
        }
        return Util.getColorFromString(CP.getTag(), 3);
    }

    @Override
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        if (core == null) {
            return false;
        }

        Clan cp = getClanByPlayer(CapturingPlayer.getName());
        if (cp == null) {
            return false;
        }

        if (cp.getTag().equals(tag) || cp.getName().equals(tag)) {
            return true;
        }

        Clan ally = core.getClanManager().getClanExact(tag);
        return ally != null && cp.isAlly(ally);
    }

    private Clan getClanByPlayer(String name) {
        ClanPlayer cp = core.getClanPlayerManager().getClanPlayerExact(name);
        if (cp == null) {
            return null;
        }

        return cp.getClan();
    }

    @Override
    public String getGroupByPlayer(Player player) {
        if (core == null) {
            return "";
        }
        Clan cp = getClanByPlayer(player.getName());
        if (cp == null) {
            return "";
        }
        return !CaptureThePortalConfig.getFullgroupnames() ? cp.getTag() : cp.getName();
    }

    @Override
    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if (!Vault.isVaultFound() || Vault.getEconomy() == null) {
            return false;
        }

        Clan cp = null;
        if (!CaptureThePortalConfig.getFullgroupnames()) {
            cp = core.getClanManager().getClan(group);
        } else {
            for (Clan clan : core.getClanManager().getClans()) {
                if (clan.getName().equals(group)) {
                    cp = clan;
                }
            }
        }

        if (cp == null) {
            return false;
        }

        for (ClanPlayer player : cp.getAllMembers()) {
            player.deposit(amount);
        }

        return true;
    }
}
