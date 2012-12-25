package org.wargamer2010.capturetheportal.Utils;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.World;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class portalUtil {
    private portalUtil() {

    }

    public static boolean checkWormHoleDailer(Block block, World world) {
        BlockFace dailerOrientation;

        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                if(x == 0 && z == 0) continue;
                Block requireWool = world.getBlockAt(block.getX()+x, block.getY(), block.getZ()+z);
                if(requireWool.getType().equals(Material.OBSIDIAN)
                        && requireWool.getRelative(BlockFace.UP).getType().equals(Material.OBSIDIAN)) {

                    dailerOrientation = Util.getFaceWithMaterial(Material.WOOL, requireWool);
                    if(dailerOrientation == BlockFace.SELF) return false;

                    BlockFace forward = Util.getOrientation(dailerOrientation, "cont");
                    BlockFace backward = Util.getOrientation(forward, "opp");
                    BlockFace otherside = Util.getOrientation(dailerOrientation, "opp");

                    Block requireLever = world.getBlockAt(block.getX()+x, block.getY()+1, block.getZ()+z);
                    Block requireObsidian = world.getBlockAt(block.getX()+x, block.getY(), block.getZ()+z);

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

    public static boolean checkEndPortal(Block block, World world) {
        int radius = 3;
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = -radius; y <= radius; y++) {
                    if(world.getBlockAt(block.getX()+x, block.getY(), block.getZ()+z).getType() == Material.ENDER_PORTAL)
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean checkStargatePortal(Block woolblock, World world) {
        Block button = world.getBlockAt(woolblock.getX(), woolblock.getY()+1, woolblock.getZ());
        List<BlockFace> faces = new ArrayList();
        faces.add(BlockFace.NORTH);
        faces.add(BlockFace.EAST);
        faces.add(BlockFace.SOUTH);
        faces.add(BlockFace.WEST);
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

    public static boolean checkMultiversePortal(Block woolBlock, World world) {
        if(Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals") == null)
            return false;
        return multiverseUtil.multiversePortalNear(4, woolBlock);
    }
}
