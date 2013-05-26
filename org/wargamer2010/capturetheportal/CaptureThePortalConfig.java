
package org.wargamer2010.capturetheportal;

import com.massivecraft.creativegates.CreativeGates;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.wargamer2010.capturetheportal.portals.CreativePortal;
import org.wargamer2010.capturetheportal.utils.Util;
import org.wargamer2010.capturetheportal.portals.EndPortal;
import org.wargamer2010.capturetheportal.portals.IPortal;
import org.wargamer2010.capturetheportal.portals.MultiversePortal;
import org.wargamer2010.capturetheportal.portals.NetherPortal;
import org.wargamer2010.capturetheportal.portals.StargatePortal;
import org.wargamer2010.capturetheportal.portals.WormholePortal;

public class CaptureThePortalConfig {
    private static int capturedelay = 6;                                                              // How long it takes in seconds
    private static int cooldown_time = 120;                                                           // How long the cooldown is before a Portal can be recaptured in seconds
    private static Boolean fullgroupnames = false;                                                     // Whether or not to use the full group name where possible
    private static Boolean usenations = false;                                                     // Whether or not to use Nations in stead of Towns for Towny
    private static int cooldown_message_timeleft_increments = 0;                                       // How much time passes before the cooldown_message is printed, thus it's printer every x increments (0 is disabled)
    private static int cooldown_message_timeleft = 20;                                                 // The amount of cooldowntime that is left before the cooldown_message is printed in seconds
    private static int squareSize = 5;                                                                 // The size of each side of the square + 1
    private static double rewardaftercooldown = 0;                                                     // Amount of money all members of a group should get after the cooldown is over
    private static boolean persistcapture = true;                                                      // Whether or not to store and load captured points
    private static boolean dieorbounce = false;                                                        // Whether the player that attempts to use an uncaptured portal dies (true) or bounces off (false)
    private static boolean enablekickfromworld = true;                                          // Whether players from team A get kicked when a player from team B captures and portals
    private static boolean enablebouncing = true;                                               // Whether or not to bounce people from the Nether portal
    private static boolean enablewormholes = false;                                                    // Whether or not Wormholes should be supported
    private static boolean enablemvportals = false;                                                    // Whether or not MVPortals should be supported
    private static boolean enablestargates = false;                                                    // Whether or not Stargates should be supported
    private static boolean enablecreativegates = false;                                                 // Whether or not Creative Gates should be supported
    private static boolean enablefactions = false;                                                     // Enables factions and disables default way of creating teams (via Permissions)
    private static boolean enableEnder = false;                                                       // Support for Ender portals, is disabled by default
    private static boolean enablenether = true;                                          // Support for Nether portals, enabled by default (of course)
    private static boolean enabletowny = false;                                                        // Enables towny support
    private static boolean enablesimpleclans = false;                                                  // Enables simpleclans support
    private static boolean enablesimpleclans2 = false;                                                  // Enables simpleclans 2 support
    private static boolean enablegods = false;                                                  // Enables gods support
    private static boolean enablepermissions = false;                                                  // Enables permissions support
    private static boolean allowneutraltoportal = false;                                                  // Whether to allow Neutral players to use the portal

    private static Map<String, Boolean> supportedHooks;
    private static Map<IPortal, Integer> supportedPortals;
    private static Map<String, Integer> Colors;                                                  // Stores all the Permissions with their respective color codes

    private CaptureThePortalConfig() {

    }

    public static void init() {
        initConfig();
        initAllowedHooks();
        initAllowedPortals();
        Colors = new HashMap<String, Integer>();
        initColors();
    }

    public static int getColor(Player player, ChatColor color) {
        if(color == null || !Colors.containsKey(color.name().toLowerCase()))
            return Colors.get("default");
        else
            return Colors.get(color.name().toLowerCase());
    }

    public static int getCapturedelay() {
        return capturedelay;
    }

    public static int getCooldown() {
        return cooldown_time;
    }

    public static int getSquareSize() {
        return squareSize;
    }

    public static boolean getDieFromUncapturedPortal() {
        return dieorbounce;
    }

    public static boolean getWormholeSupport() {
        return enablewormholes;
    }

    public static boolean getEnderSupport() {
        return enableEnder;
    }

    public static boolean getNetherSupport() {
        return enablenether;
    }

    public static int getCooldownInterval() {
        return cooldown_message_timeleft_increments;
    }

    public static double getRewardAmount() {
        return rewardaftercooldown;
    }

    public static int getCoolMessageTime() {
        return cooldown_message_timeleft;
    }

    public static boolean getFullgroupnames() {
        return fullgroupnames;
    }

    public static boolean getEnableKickFromWorld() {
        return enablekickfromworld;
    }

    public static boolean getAllowNeutralToPortal() {
        return allowneutraltoportal;
    }

    public static boolean getUseNations() {
        return usenations;
    }

    public static boolean getEnablebouncing() {
        return enablebouncing;
    }

    public static boolean getPersistCapture() {
        return persistcapture;
    }

    public static Map<String, Boolean> getSupportedHooks() {
        return Collections.unmodifiableMap(supportedHooks);
    }

    public static Map<IPortal, Integer> getSupportedPortals() {
        return Collections.unmodifiableMap(supportedPortals);
    }

    public static Map<String, Integer> getColors() {
        return Collections.unmodifiableMap(Colors);
    }

    private static void initConfig() {
        CaptureThePortal.get().reloadConfig();
        FileConfiguration config = CaptureThePortal.get().getConfig();
        File configFile = new File("plugins/CaptureThePortal", "config.yml");
        if(!configFile.exists())
            CaptureThePortal.get().saveDefaultConfig();
        config.options().copyDefaults(true);
        capturedelay = Util.getTimeFromString(config.getString("CaptureThePortal.delay", ""), capturedelay);
        cooldown_time = Util.getTimeFromString(config.getString("CaptureThePortal.cooldown", ""), cooldown_time);
        cooldown_message_timeleft = Util.getTimeFromString(config.getString("CaptureThePortal.cooldown_message_timeleft", ""), cooldown_message_timeleft);
        cooldown_message_timeleft_increments = Util.getTimeFromString(config.getString("CaptureThePortal.cooldown_message_timeleft_increments", ""), cooldown_message_timeleft_increments);
        fullgroupnames = (config.getBoolean("CaptureThePortal.fullgroupnames", fullgroupnames));
        enablekickfromworld = (config.getBoolean("CaptureThePortal.enablekickfromworld", enablekickfromworld));
        usenations = (config.getBoolean("CaptureThePortal.usenations", usenations));
        dieorbounce = (config.getBoolean("CaptureThePortal.dieorbounce", dieorbounce));
        enablebouncing = (config.getBoolean("CaptureThePortal.enablebouncing", enablebouncing));
        persistcapture = (config.getBoolean("CaptureThePortal.persistcapture", persistcapture));
        enablewormholes = (config.getBoolean("CaptureThePortal.enablewormholesupport", enablewormholes));
        enablestargates = (config.getBoolean("CaptureThePortal.enablestargatesupport", enablestargates));
        enablecreativegates = (config.getBoolean("CaptureThePortal.enablecreativegatessupport", enablecreativegates));
        enablefactions = (config.getBoolean("CaptureThePortal.enablefactionsupport", enablefactions));
        enablepermissions = (config.getBoolean("CaptureThePortal.enablepermissionssupport", enablepermissions));
        enabletowny = (config.getBoolean("CaptureThePortal.enabletownysupport", enabletowny));
        enablesimpleclans = (config.getBoolean("CaptureThePortal.enablesimpleclanssupport", enablesimpleclans));
        enablesimpleclans2 = (config.getBoolean("CaptureThePortal.enablesimpleclans2support", enablesimpleclans2));
        enablegods = (config.getBoolean("CaptureThePortal.enablegodssupport", enablegods));
        enableEnder = (config.getBoolean("CaptureThePortal.enableEndersupport", enableEnder));
        enablenether = (config.getBoolean("CaptureThePortal.enablenethersupport", enablenether));
        enablemvportals = (config.getBoolean("CaptureThePortal.enableMVPortals", enablemvportals));
        allowneutraltoportal = (config.getBoolean("CaptureThePortal.allow_neutral_to_portal", allowneutraltoportal));
        rewardaftercooldown = (config.getDouble("CaptureThePortal.rewardaftercooldown", rewardaftercooldown));

        CaptureThePortal.Log("Configuration loaded succesfully", Level.INFO);
        CaptureThePortal.get().saveConfig();
    }

    private static void allowedPortalHelper(IPortal portal, boolean setting, int radius) {
        if(setting && portal.init())
            supportedPortals.put(portal, radius);
    }

    private static void initAllowedPortals() {
        supportedPortals = new LinkedHashMap<IPortal, Integer>();
        allowedPortalHelper(new NetherPortal(), enablenether, squareSize);
        allowedPortalHelper(new EndPortal(), enableEnder, 3);
        allowedPortalHelper(new MultiversePortal(), enablemvportals, 4);
        allowedPortalHelper(new StargatePortal(), enablestargates, 0);
        allowedPortalHelper(new WormholePortal(), enablewormholes, 0);
        allowedPortalHelper(new CreativePortal(), enablecreativegates, 0);
    }

    private static void initAllowedHooks() {
        supportedHooks = new HashMap<String, Boolean>();
        supportedHooks.put("Factions", enablefactions);
        supportedHooks.put("Towny", enabletowny);
        supportedHooks.put("SimpleClans", enablesimpleclans);
        supportedHooks.put("SimpleClans2", enablesimpleclans2);
        supportedHooks.put("Gods", enablegods);
        supportedHooks.put("Vault", enablepermissions);
    }

    private static void initColors() {
        /*
         * 1 = Orange
         * 2 = (light)Purple
         * 3 = (light)Blue
         * 4 = Yellow
         * 5 = (light)Green
         * 6 = (light)Pink
         * 7 = Grey
         * 8 = (light)Grey
         * 9 = (light)Blue
         * 10 = Purple
         * 11 = Blue
         * 12 = Brown
         * 13 = Green
         * 14 = Red
         * 15 = Black
         * 16 = White
         */
        Colors.put("gold", 1);
        Colors.put("light_purple", 2);
        Colors.put("blue", 3);
        Colors.put("yellow", 4);
        Colors.put("green", 5);
        Colors.put("dark_gray", 7);
        Colors.put("gray", 8);
        Colors.put("dark_purple", 10);
        Colors.put("dark_blue", 11);
        Colors.put("dark_green", 13);
        Colors.put("red", 14);
        Colors.put("black", 15);

        Colors.put("default", 14);
    }
}
