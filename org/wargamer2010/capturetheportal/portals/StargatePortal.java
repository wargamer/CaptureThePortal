
package org.wargamer2010.capturetheportal.portals;

import java.util.List;
import net.TheDgtl.Stargate.event.StargateAccessEvent;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;
import org.wargamer2010.capturetheportal.utils.Util;

public class StargatePortal implements IPortal, Listener {
    public boolean init() {
        if(!CaptureThePortalConfig.getStargatesSupport())
            return false;
        if(Util.isPluginEnabled("Stargate"))
            Bukkit.getServer().getPluginManager().registerEvents(this, CaptureThePortal.get());
        return Util.isPluginEnabled("Stargate");
    }

    public String getName() {
        return "Stargate";
    }

    public boolean isPortalNear(int radius, Block origin) {
        World world = origin.getWorld();
        Block button = world.getBlockAt(origin.getX(), origin.getY()+1, origin.getZ());
        List<BlockFace> faces = Util.getHorizontalBlockFaces();
        for(BlockFace face : faces) {
            if(button.getRelative(face).getType() == Material.OBSIDIAN) {
                for(BlockFace sface : faces) {
                    Block attached = button.getRelative(face).getRelative(sface);
                    if(attached.getType() == Material.STONE_BUTTON || attached.getType() == Material.WALL_SIGN || attached.getType() == Material.SIGN)
                        return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void StargatePortalListener(StargatePortalEvent event) {
        if(event.isCancelled())
            return;
        Block block = event.getPlayer().getLocation().getBlock();
        Player player = event.getPlayer();
        int isAllowed = CaptureThePortal.get().isAllowedToPortal(block, player, Material.AIR);
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void StargateAccessListener(StargateAccessEvent event) {
        if(event.isCancelled() && !event.getDeny())
            return;
        Block block = event.getPlayer().getLocation().getBlock();
        Player player = event.getPlayer();
        int isAllowed = CaptureThePortal.get().isAllowedToPortal(block, player, Material.AIR);
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setDeny(true);
        }
    }
}
