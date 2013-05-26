
package org.wargamer2010.capturetheportal.portals;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.capturetheportal.utils.Util;

public class WormholePortal implements IPortal {
    public boolean init() {
        return Util.isPluginEnabled("WormholeXTreme");
    }

    public String getName() {
        return "Wormhole";
    }

    public boolean isPortalNear(int radius, Block origin) {
        BlockFace dailerOrientation;
        World world = origin.getWorld();

        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                if(x == 0 && z == 0) continue;
                Block requireWool = world.getBlockAt(origin.getX()+x, origin.getY(), origin.getZ()+z);
                if(requireWool.getType().equals(Material.OBSIDIAN)
                        && requireWool.getRelative(BlockFace.UP).getType().equals(Material.OBSIDIAN)) {

                    dailerOrientation = Util.getFaceWithMaterial(Material.WOOL, requireWool);
                    if(dailerOrientation == BlockFace.SELF) return false;

                    BlockFace forward = Util.getOrientation(dailerOrientation, "cont");
                    BlockFace backward = Util.getOrientation(forward, "opp");
                    BlockFace otherside = Util.getOrientation(dailerOrientation, "opp");

                    Block requireLever = world.getBlockAt(origin.getX()+x, origin.getY()+1, origin.getZ()+z);
                    Block requireObsidian = world.getBlockAt(origin.getX()+x, origin.getY(), origin.getZ()+z);

                    if((requireLever.getRelative(forward).getType() == Material.LEVER
                            || requireLever.getRelative(backward).getType() == Material.LEVER)
                        && requireObsidian.getRelative(otherside).getType() == Material.OBSIDIAN
                        && requireObsidian.getRelative(otherside).getRelative(BlockFace.UP).getType() == Material.OBSIDIAN
                        && (requireObsidian.getRelative(otherside).getRelative(BlockFace.UP).getRelative(forward).getType() == Material.WALL_SIGN
                            || requireObsidian.getRelative(otherside).getRelative(BlockFace.UP).getRelative(backward).getType() == Material.WALL_SIGN))
                        return true;
                    else
                        continue;
                }
            }
        }
        return false;
    }
}
