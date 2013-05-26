
package org.wargamer2010.capturetheportal.portals;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.wargamer2010.capturetheportal.CaptureThePortal;
import org.wargamer2010.capturetheportal.utils.Util;
import com.massivecraft.creativegates.event.CreativeGatesTeleportEvent;
import com.massivecraft.creativegates.Gates;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;

public class CreativePortal implements IPortal, Listener {
    public boolean init() {
        if(!CaptureThePortalConfig.getCreativeGatesSupport())
            return false;
        if(Util.isPluginEnabled("CreativeGates"))
            Bukkit.getServer().getPluginManager().registerEvents(this, CaptureThePortal.get());
        return Util.isPluginEnabled("CreativeGates");
    }

    public String getName() {
        return "Gate";
    }

    public boolean isPortalNear(int radius, Block origin) {
        List<BlockFace> faces = Util.getHorizontalBlockFaces();
        for(BlockFace face : faces) {
            if(Gates.i.findFromFrame(origin.getRelative(face)) != null)
                return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void CreativeGatesTeleportListener(CreativeGatesTeleportEvent event) {
        if(event.isCancelled())
            return;
        Block block = event.getPlayerMoveEvent().getFrom().getBlock();
        Player player = event.getPlayerMoveEvent().getPlayer();
        int isAllowed = CaptureThePortal.get().isAllowedToPortal(block, player, Material.AIR);
        if(isAllowed != 0) {
            Util.sendNotAllowedMessage(player, isAllowed);
            event.setCancelled(true);
        }
    }
}
