package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.Group;

public class PermissionsHook implements Hook {
    PermissionHandler instance = null;
    private String neutralPermission = "CaptureThePortal.neutral"; // The permissions that can be assigned to a group/player that is not allowed to participate
    
    public PermissionsHook(Plugin SC) {
        instance = ((Permissions)SC).getHandler();
    }
    
    public String getName() {
        return "Permissions";
    }
    
    public String getGroupType() {
        return "Team";
    }
    
    public ChatColor getGroupColor(Player player) {
        String shortName = getGroupByPlayer(player).replace("CaptureThePortal.", "");
        if(CaptureThePortal.Colors.containsKey(shortName))
            return ChatColor.valueOf(shortName.toUpperCase());
        else
            return null;
    }
    
    public Boolean isAllied(Player CapturingPlayer, String tag) {
        Collection<Group> cpGroups = instance.getGroups(CapturingPlayer.getName());
        Collection<Group> owGroups = instance.getGroups(tag);
        for(Iterator iter = cpGroups.iterator(); iter.hasNext();) {
            if(owGroups.contains((Group)iter.next()))
                return true;
        }
        return false;
    }
    
    public String getGroupByPlayer(Player player) {
        if(hasRights(player, neutralPermission))
            return "";

        int amount = 0;
        String returnPerm = "";
        Iterator it = CaptureThePortal.Colors.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            if(hasRights(player, ("CaptureThePortal." + (String)pairs.getKey()))) {
                returnPerm = (String)pairs.getKey();
                amount++;                
            }
        }
        returnPerm = returnPerm.replace("_", "");
        
        if(amount > 1)
            return "";
        else
            return returnPerm;
    }
    
    private boolean hasRights(Player player, String Permission) {
        return ((instance != null) ? instance.has(player, Permission) : false);
    }
}
