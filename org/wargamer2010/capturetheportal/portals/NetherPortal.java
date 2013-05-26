
package org.wargamer2010.capturetheportal.portals;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.wargamer2010.capturetheportal.CaptureThePortalConfig;

public class NetherPortal implements IPortal {
    public boolean init() {
        return CaptureThePortalConfig.getNetherSupport();
    }

    public String getName() {
        return "Nether";
    }

    public boolean isPortalNear(int radius, Block origin) {
        boolean isWoolSquare = true;
        World world = origin.getWorld();

        for(int x = -((radius-1)/2); x <= ((radius-1)/2); x++) {
            for(int z = -((radius-1)/2); z <= ((radius-1)/2); z++) {
                if(world.getBlockAt(origin.getX() + x, origin.getY(), origin.getZ() + z).getType() != Material.WOOL)
                    isWoolSquare = false;
            }
        }

        return isWoolSquare;
    }
}
