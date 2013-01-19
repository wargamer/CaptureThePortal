package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import com.dogonfire.gods.Gods;
import org.bukkit.World;
import org.wargamer2010.capturetheportal.Utils.Vault;

public class GodsHook implements Hook {
    private Gods instance = null;

    public void setPlugin(Plugin pl) {
        instance = ((Gods)pl);
    }

    public String getName() {
        return "Gods";
    }

    public String getGroupType() {
        return "God";
    }

    public ChatColor getGroupColor(Player player) {
        return null;
    }

    public Boolean isAllied(Player CapturingPlayer, String tag) {
        return false;
    }

    public String getGroupByPlayer(Player player) {
        String god = instance.getBelieverManager().getGodForBeliever(player.getName());
        if(god == null) god = "";
        return god;
    }

    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if(!Vault.vaultFound || Vault.economy == null)
            return false;
        
        if(!instance.getGodManager().getGods().contains(group))
            return false;
        for(String player : instance.getBelieverManager().getBelieversForGod(group)) {
            Vault.economy.depositPlayer(player, amount);
        }
        return true;
    }
}
