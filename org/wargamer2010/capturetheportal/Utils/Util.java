package org.wargamer2010.capturetheportal.Utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.wargamer2010.capturetheportal.CaptureThePortal;

public class Util {
    private static Map<Player, Timestamp> lastSent = new LinkedHashMap<Player, Timestamp>();

    private Util() {

    }

    public static BlockFace getOrientation(BlockFace face, String par) {
        switch(face) {
            case EAST:
                if(par.equals("opp"))
                    return BlockFace.WEST;
                else if(par.equals("cont"))
                    return BlockFace.NORTH;
                break;
            case WEST:
                if(par.equals("opp"))
                    return BlockFace.EAST;
                else if(par.equals("cont"))
                    return BlockFace.SOUTH;
                break;
            case NORTH:
                if(par.equals("opp"))
                    return BlockFace.SOUTH;
                else if(par.equals("cont"))
                    return BlockFace.EAST;
                break;
            case SOUTH:
                if(par.equals("opp"))
                    return BlockFace.NORTH;
                else if(par.equals("cont"))
                    return BlockFace.WEST;
                break;
        }
        return BlockFace.SELF;
    }

    public static BlockFace getFaceWithMaterial(Material matchMaterial, Block matchBlock) {
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
        for(int i = 0; i < faces.length; i++)
            if(matchBlock.getRelative(faces[i]).getType() == matchMaterial)
                return faces[i];
        return BlockFace.SELF;
    }

    public static String parseTime(int time) {
        String timeString = "";
        timeUnit[] timeUnits = { (new timeUnit(60, "Second", time)), (new timeUnit(60, "Minute")), (new timeUnit(24, "Hour")), (new timeUnit(365, "Day")) };
        for(int i = 0; (i+1) < timeUnits.length; i++)
            while(timeUnits[i].decrement())
                timeUnits[i+1].increment();
        int temp;
        Boolean first = true;
        for(int i = (timeUnits.length-1); i >= 0; i--) {
            temp = timeUnits[i].getAmount();
            if(temp > 0) {
                if(!first && i >= 0)
                    timeString += ", ";
                else
                    first = false;
                timeString += (temp + " " + timeUnits[i].getName());
                if(temp > 1)
                    timeString += "s";
            }
        }
        int pos = timeString.lastIndexOf(',');
        if (pos >= 0)
            timeString = timeString.substring(0,pos) + " and" + timeString.substring(pos+1);
        return timeString;
    }

    public static int getTimeFromString(String time) {
        return getTimeFromString(time, -1);
    }

    public static int getTimeFromString(String time, int defaultValue) {
        timeUnit[] timeUnits = { (new timeUnit(60, "Second")), (new timeUnit(60, "Minute")), (new timeUnit(24, "Hour")), (new timeUnit(365, "Day")) };
        char[] singleUnits = { 's', 'm', 'h', 'd' };
        StringBuilder number = new StringBuilder(time.length());
        StringBuilder unit = new StringBuilder(time.length());

        for(char tempc : time.toLowerCase().replace(" ", "").toCharArray()) {
            if(Character.isDigit(tempc))
                number.append(tempc);
            else
                unit.append(tempc);
        }

        if(number.length() == 0)
            return defaultValue;

        char cUnit = '-';
        if(unit.length() > 0)
            cUnit = unit.toString().charAt(0);
        String sNumber = number.toString();
        Integer iNumber;
        try {
            iNumber = Integer.parseInt(sNumber);
        } catch(NumberFormatException ex) {
            return defaultValue;
        }

        if(cUnit == '-')
            return iNumber;

        boolean found = false;
        for(int i = 0; i < singleUnits.length; i++) {
            char timeunit = singleUnits[i];
            if(timeunit == cUnit) {
                timeUnits[i].setAmount(iNumber);
                found = true;
                break;
            }
        }

        if(!found)
            return defaultValue;

        for(int i = (timeUnits.length -1); i > 0; i--) {
            while(timeUnits[i].singleDecrement())
                timeUnits[i-1].fullIncrement();
        }

        return timeUnits[0].currentAmount;
    }

    public static void sendMessagePlayer(String message, Player player) {
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

        if(lastSent.containsKey(player)) {
            // Attempt to throttle messaging, let's try not to spam the player
            long diff = (currentTimestamp.getTime() - lastSent.get(player).getTime());
            if(diff < 1000)
                return;
            else
                lastSent.remove(player);
        }
        lastSent.put(player, currentTimestamp);
        if(message.isEmpty())
            return;
        player.sendMessage(message);
    }

    public static void broadcastMessage(String message) {
        if(message.isEmpty())
            return;
        Bukkit.getServer().broadcastMessage(message);
    }

    public static String convertLocationToString(Location loc) {
        return (loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ());
    }

    public static Location convertStringToLocation(String sLoc, World world) {
        String[] sCoords = sLoc.split("/");
        if(sCoords.length < 3)
            return null;
        try {
            Location loc = new Location(world, Double.parseDouble(sCoords[0]), Double.parseDouble(sCoords[1]), Double.parseDouble(sCoords[2]));
            return loc;
        } catch(NumberFormatException ex) {
            return null;
        }
    }

    public static ChatColor getColorFromString(String input, Integer index) {
        ChatColor firstColor = null;
        int colorCount = 0;
        for (int i = 0; i < input.length(); i++) {
            char section = input.charAt(i);
            if (section == ChatColor.COLOR_CHAR && i < input.length() - 1) {
                char c = input.charAt(i + 1);
                ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                    colorCount++;
                    if(colorCount == index)
                        return color;
                }

            }
        }
        return firstColor;
    }

    public static void sendNotAllowedMessage(Player player, int mode) {
        if(mode == 2 || mode == 3) {
            if(!CaptureThePortal.getDieFromUncapturedPortal())
                Util.sendMessagePlayer(CaptureThePortal.getMessage("player_not_allowed_to_use"), player);
            else
                Util.sendMessagePlayer(CaptureThePortal.getMessage("player_not_allowed_die_use"), player);
        } else {
            Util.sendMessagePlayer(CaptureThePortal.getMessage("player_not_allowed_to_use_neutral"), player);
        }
    }

    public static void bouncePlayer(int mode, Player player, Location loc, Vector velocity) {
        if(!CaptureThePortal.getEnablebouncing())
            return;
        int delta = 1;

        boolean x_or_z = (Math.abs(velocity.getX()) > Math.abs(velocity.getZ())) ? true : false;
        boolean negativediff = (x_or_z ? (velocity.getX() < 0) : (velocity.getZ() < 0));

        sendNotAllowedMessage(player, mode);
        if(mode == 2 || mode == 3) {
            if(CaptureThePortal.getDieFromUncapturedPortal()) {
                player.damage(1000);
                delta = 3;
            }
        }

        if(x_or_z) {
            if(!negativediff) {
                loc.subtract(delta, 0, 0);
            } else {
                loc.add(delta, 0, 0);
            }
        } else {
            if(!negativediff) {
                loc.subtract(0, 0, delta);
            } else {
                loc.add(0, 0, delta);
            }
        }
        player.teleport(loc);
    }

    public static String locToPrintableString(Location loc) {
        return ("(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
    }

    public static Integer getTicksFromSeconds(Integer seconds) {
        return (seconds * 20);
    }

    private static class timeUnit {
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
            }

            return false;
        }

        Boolean singleDecrement() {
            if(currentAmount > 0) {
                currentAmount--;
                return true;
            }

            return false;
        }

        void increment() {
            currentAmount++;
        }

        void fullIncrement() {
            currentAmount += maxAmount;
        }

        String getName() {
            return name;
        }

        int getAmount() {
            return currentAmount;
        }

        void setAmount(int amount) {
            currentAmount = amount;
        }
    }
}
