package org.wargamer2010.capturetheportal.hooks;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.Utils.Vault;

public class TownyHook implements Hook {

    public void setPlugin(Plugin pl) {

    }

    public String getName() {
        return "Towny";
    }

    public String getGroupType() {
        if(!CaptureThePortal.getUseNations())
            return "Town";
        else
            return "Nation";
    }

    public ChatColor getGroupColor(Player player) {
        return null;
    }

    public Boolean isAllied(Player CapturingPlayer, String tag) {
        if(!CaptureThePortal.getUseNations()) {
            String townName = getGroupByPlayer(CapturingPlayer);
            if(townName.isEmpty())
                return false;
            return CombatUtil.isAlly(townName, tag);
        } else {
            // No point in checking whether Nations are allies. Towns in the same Nation are allies anyway
            return false;
        }
    }

    public String getGroupByPlayer(Player player) {
        try {
            Resident res = TownyUniverse.getDataSource().getResident(player.getName());
            Town town = res.getTown();
            if(!CaptureThePortal.getUseNations()) {
                return town.getName();
            } else {
                return town.getNation().getName();
            }
        } catch(NotRegisteredException ex) {
            // Logging this exception might be worth something to SA's but it will probably
            // spam console too much so let's assume this behavior is expected and return an empty string
            return "";
        }
    }

    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if(!Vault.isVaultFound() || Vault.getEconomy() == null)
            return false;

        List<Resident> residents;
        if(CaptureThePortal.getUseNations()) {
            try {
                residents = TownyUniverse.getDataSource().getNation(group).getResidents();
            } catch(NotRegisteredException ex) {
                return false;
            }
        } else {
            try {
                residents = TownyUniverse.getDataSource().getTown(group).getResidents();
            } catch(NotRegisteredException ex) {
                return false;
            }
        }

        for(Resident player : residents) {
            Vault.getEconomy().depositPlayer(player.getName(), amount);
        }

        return true;
    }
}
