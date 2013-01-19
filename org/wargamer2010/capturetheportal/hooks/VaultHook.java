package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import java.util.Map;
import java.util.logging.Level;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.Utils.Vault;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.World;

public class VaultHook implements Hook {
    private Permission instance = null;
    private String neutralPermission = "CaptureThePortal.neutral"; // The permissions that can be assigned to a group/player that is not allowed to participate

    public void setPlugin(Plugin pl) {
        if(Vault.vaultFound) {
            Vault vault = new Vault();
            if(vault.setupPermissions()) {
                instance = Vault.permission;
            } else {
                CaptureThePortal.Log("Vault could not find a Permission plugin. Either load a compatible Permission plugin or choose a different Group Plugin!", Level.SEVERE);
            }
        }
    }

    public String getName() {
        return "Permissions";
    }

    public String getGroupType() {
        return "Team";
    }

    public ChatColor getGroupColor(Player player) {
        String shortName = getGroupByPlayer(player, true).replace("CaptureThePortal.", "");
        if(CaptureThePortal.getColors().containsKey(shortName))
            return ChatColor.valueOf(shortName.toUpperCase());
        else
            return null;
    }

    public Boolean isAllied(Player CapturingPlayer, String tag) {
        return (getGroupByPlayer(CapturingPlayer, false).equals(tag));
    }

    public String getGroupByPlayer(Player player) {
        return this.getGroupByPlayer(player, false);
    }

    public String getGroupByPlayer(Player player, Boolean bNeedUnderscore) {
        if(hasRights(player, neutralPermission))
            return "";

        int amount = 0;
        String returnPerm = "";
        for(Map.Entry<String, Integer> color : CaptureThePortal.getColors().entrySet()) {
            if(hasRights(player, ("CaptureThePortal." + color.getKey()))) {
                returnPerm = color.getKey();
                amount++;
            }
        }
        if(!bNeedUnderscore)
            returnPerm = returnPerm.replace("_", "");

        if(amount > 1)
            return "";
        else
            return returnPerm;
    }

    private boolean hasRights(Player player, String Permission) {
        Boolean isOP = player.isOp();
        if(isOP) player.setOp(false);
        Boolean bHasIt = ((instance != null) ? instance.has(player, Permission) : false);
        if(isOP) player.setOp(true);
        return bHasIt;
    }

    public Boolean giveMoneyToPlayers(String group, World world, double amount) {
        if(!Vault.vaultFound || Vault.economy == null)
            return false;

        for(Player player : world.getPlayers()) {
            if(getGroupByPlayer(player, false).equals(group)) {
                Vault.economy.depositPlayer(player.getName(), amount);
            }
        }

        return true;
    }
}
