
package org.wargamer2010.capturetheportal.portals;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;

public class EndPortal implements IPortal {
    public boolean init() {
        return CaptureThePortalConfig.getEnderSupport();
    }

    public String getName() {
        return "End";
    }

    public boolean isPortalNear(int radius, Block origin) {
        World world = origin.getWorld();
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = -radius; y <= radius; y++) {
                    if(world.getBlockAt(origin.getX()+x, origin.getY(), origin.getZ()+z).getType() == Material.ENDER_PORTAL)
                        return true;
                }
            }
        }
        return false;
    }
}
