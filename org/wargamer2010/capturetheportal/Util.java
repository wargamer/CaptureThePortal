package org.wargamer2010.capturetheportal;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.Material;

class Util {
    public static BlockFace getOrientation(BlockFace face, String par) {
        switch(face) {            
            case EAST:
                if(par.equals("opp"))
                    return BlockFace.WEST;
                else if(par.equals("cont"))
                    return BlockFace.NORTH;
            case WEST:
                if(par.equals("opp"))
                    return BlockFace.EAST;
                else if(par.equals("cont"))
                    return BlockFace.SOUTH;
            case NORTH:
                if(par.equals("opp"))
                    return BlockFace.SOUTH;
                else if(par.equals("cont"))
                    return BlockFace.EAST;
            case SOUTH:
                if(par.equals("opp"))
                    return BlockFace.NORTH;
                else if(par.equals("cont"))
                    return BlockFace.WEST;
            default:
                return BlockFace.SELF;
        }        
    }
    
    public static BlockFace getFaceWithMaterial(Material matchMaterial, Block matchBlock) {        
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN }; 
        for(int i = 0; i < faces.length; i++)
            if(matchBlock.getRelative(faces[i]).getType() == matchMaterial)
                return faces[i];
        return BlockFace.SELF;
    }
    
    static private class timeUnit {
        int maxAmount;
        int currentAmount = 0;
        String name;
        
        timeUnit(int pMaxAmount, String pName) {
            maxAmount = pMaxAmount;
            name = pName;
        }
        
        timeUnit(int pMaxAmount, String pName, int pCurrentAmount) {
            maxAmount = pMaxAmount;
            name = pName;
            currentAmount = pCurrentAmount;
        }
        
        Boolean decrement() {
            if(currentAmount >= maxAmount) {
                currentAmount -= maxAmount;
                return true;
            } else
                return false;
        }
        
        void increment() {
            currentAmount++;
        }
        
        String getName() {
            return name;
        }
        
        int getAmount() {
            return currentAmount;
        }
    }
    
    public static String parseTime(int time) {
        String timeString = "";
        timeUnit[] timeUnits = { (new timeUnit(60, "Second", time)), (new timeUnit(60, "Minute")), (new timeUnit(24, "Hour")), (new timeUnit(365, "Day")) };
        for(int i = 0; (i+1) < timeUnits.length; i++)
            while(timeUnits[i].decrement())
                timeUnits[i+1].increment();
        int temp = 0;
        Boolean first = true;
        for(int i = (timeUnits.length-1); i >= 0; i--) {
            if((temp = timeUnits[i].getAmount()) > 0) {
                if(!first && i >= 0)
                    timeString += ", ";
                else
                    first = false;
                timeString += (temp + " " + timeUnits[i].getName());
                if(temp > 1)
                    timeString += "s";
            }
        }
        int pos = timeString.lastIndexOf(",");
        if (pos >= 0)
            timeString = timeString.substring(0,pos) + " and" + timeString.substring(pos+1);
        return timeString;
    }
}
