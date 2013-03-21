package org.wargamer2010.capturetheportal.hooks;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.capturetheportal.Utils.Vault;

public class FactionsHook implements Hook {
    public void setPlugin(Plugin pl) {

    }

    public String getGroupType() {
        return "Faction";
    }

    public String getName() {
        return "Factions";
    }

    public ChatColor getGroupColor(Player player) {
        return null;
    }

    public Boolean isAllied(Player CapturingPlayer, String tag) {
        FPlayer FP = FPlayers.i.get(CapturingPlayer);
        Faction capturing_faction = FP.getFaction();
        if(capturing_faction == null || FP.getFactionId().equals("0"))
            return false;
        Faction captured_faction = Factions.i.getByTag(tag);
        if(captured_faction == null)
            return false;
        Relation rel;
        if(capturing_faction.getRelationWish(captured_faction).value >= captured_faction.getRelationWish(capturing_faction).value)
            rel = captured_faction.getRelationWish(capturing_faction);
        else
            rel = capturing_faction.getRelationWish(captured_faction);
        if(rel == Relation.ALLY)
            return true;
        else
            return false;
    }

    public String getGroupByPlayer(Player player) {
        FPlayer FP = FPlayers.i.get(player);
        Faction Fac = FP.getFaction();

        if(Fac == null || FP.getFactionId().equals("0"))
            return "";
        else
            return Fac.getTag();
    }

    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if(!Vault.isVaultFound() || Vault.getEconomy() == null)
            return false;
        Faction fac = Factions.i.getByTag(group);
        if(fac == null)
            return false;
        else {
            for(FPlayer player : fac.getFPlayers()) {
                Vault.getEconomy().depositPlayer(player.getName(), amount);
            }
        }
        return true;
    }
}
