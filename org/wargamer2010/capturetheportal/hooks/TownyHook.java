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
            Location spawnlocation;
            try {
                spawnlocation = instance.getTownyUniverse().getTownSpawnLocation(CapturingPlayer);
            } catch(TownyException TE) {
                return false;
            }
            String townName = TownyUniverse.getTownName(spawnlocation);
            return CombatUtil.isAlly(townName, tag);
        } else {
            // No point in checking whether Nations are allies. Towns in the same Nation are allies anyway
            return false;
        }
    }

    public String getGroupByPlayer(Player player) {
        Location spawnlocation;
        try {
            spawnlocation = instance.getTownyUniverse().getTownSpawnLocation(player);
        } catch(TownyException TE) {
            return "";
        }
        if(!CaptureThePortal.getUseNations()) {
            return TownyUniverse.getTownName(spawnlocation);
        } else {
            String town_name = TownyUniverse.getTownName(spawnlocation);
            Town town = instance.getTownyUniverse().getTownsMap().get(town_name);
            Nation nation;
            try {
                if(town == null || town.getNation() == null)
                    return "";
                nation = town.getNation();
                return nation.getName();
            } catch(NotRegisteredException x) {
                return "";
            }
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
