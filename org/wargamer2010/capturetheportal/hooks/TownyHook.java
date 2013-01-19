package org.wargamer2010.capturetheportal.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.Utils.Vault;

public class TownyHook implements Hook {
    private Towny instance = null;

    public void setPlugin(Plugin pl) {
        instance = (Towny)pl;
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
        Resident res = instance.getTownyUniverse().getResidentMap().get(player.getName());

        try {
            Town town = res.getTown();
            if(town == null)
                return "";
            if(!CaptureThePortal.getUseNations()) {
                return town.getName();
            } else {
                return (town.getNation() == null ? "" : town.getNation().getName());
            }
        } catch(NotRegisteredException ex) {
            return "";
        }
    }

    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if(!Vault.vaultFound || Vault.economy == null)
            return false;

        List<Resident> residents;
        if(CaptureThePortal.getUseNations()) {
            Nation nation = instance.getTownyUniverse().getNationsMap().get(group);
            if(nation == null)
                return false;

            residents = nation.getResidents();
        } else {
            Town town = instance.getTownyUniverse().getTownsMap().get(group);
            if(town == null)
                return false;

            residents = town.getResidents();
        }
        if(residents != null) {
            for(Resident player : residents) {
                Vault.economy.depositPlayer(player.getName(), amount);
            }
        }

        return true;
    }
}
