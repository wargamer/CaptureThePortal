package org.wargamer2010.capturetheportal.hooks;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public interface IHook {
    public void setPlugin(Plugin pl);

    public String getName();

    public String getGroupType();

    public ChatColor getGroupColor(Player player);

    public Boolean isAllied(Player CapturingPlayer, String tag);

    public String getGroupByPlayer(Player player);

    public Boolean giveMoneyToPlayers(String group, World world, double amount);
}
