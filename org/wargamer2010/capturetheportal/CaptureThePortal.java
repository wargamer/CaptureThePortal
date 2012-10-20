package org.wargamer2010.capturetheportal;

import org.wargamer2010.capturetheportal.listeners.*;
import org.wargamer2010.capturetheportal.Utils.Util;
import java.util.HashMap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.Bukkit;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.wargamer2010.capturetheportal.Utils.portalUtil;
import org.wargamer2010.capturetheportal.hooks.*;
import org.wargamer2010.capturetheportal.metrics.setupMetrics;
import org.wargamer2010.capturetheportal.timers.*;

public class CaptureThePortal extends JavaPlugin {
    private int capturedelay = 60;                                                              // How long it takes in deciseconds (1/10th of a second)
    private int cooldown_time = 1200;                                                           // How long the cooldown is before a Portal can be recaptured in deciseconds    
    private static Boolean fullgroupnames = false;                                                     // Whether or not to use the full group name where possible
    private static Boolean usenations = false;                                                     // Whether or not to use Nations in stead of Towns for Towny
    private static int cooldown_message_timeleft_increments = 0;                                       // How much time passes before the cooldown_message is printed, thus it's printer every x increments (0 is disabled)
    private static int cooldown_message_timeleft = 20;                                                 // The amount of cooldowntime that is left before the cooldown_message is printed    
    private int squareSize = 5;                                                                 // The size of each side of the square + 1
    private boolean persistcapture = true;                                                      // Whether or not to store and load captured points
    private static boolean dieorbounce = false;                                                        // Whether the player that attempts to use an uncaptured portal dies (true) or bounces off (false)
    private static boolean enablebouncing = true;                                               // Whether or not to bounce people from the Nether portal
    private static boolean enablewormholes = false;                                                    // Whether or not Wormholes should be supported together with regular nether portals
    private boolean enablemvportals = false;                                                    // Whether or not MVPortals should be supported together with regular nether portals
    private boolean enablestargates = false;                                                    // Whether or not Stargates should be supported together with regular nether portals
    private boolean enablefactions = false;                                                     // Enables factions and disables default way of creating teams (via Permissions)
    private static boolean enable_ender = false;                                                       // Support for Ender portals, is disabled by default    
    private boolean enabletowny = false;                                                        // Enables towny support
    private boolean enablesimpleclans = false;                                                  // Enables simpleclans support
    private boolean enablegods = false;                                                  // Enables gods support
    private boolean enablepermissions = false;                                                  // Enables permissions support    
    private boolean allowneutraltoportal = false;                                                  // Whether to allow Neutral players to use the portal
    
    private static HashMap<String, Boolean> supportedHooks;
    private static HashMap<Location, PortalCooldown> Timers;                                    // Stores all the timing classes (CapturePortal) for the various locations
    public static Map<String, Integer> Colors;                                                  // Stores all the Permissions with their respective color codes        
    public static CapturesStorage Storage;
    
    public static CaptureThePortal instance;
    
    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final String pluginName = "CaptureThePortal";
    private Hook groupPlugin;
    private setupMetrics metricsSetup = null;

    @Override
    public void onDisable()
    {
        if(persistcapture)
            Storage.saveCaptures();
        else {
            if(Storage.getAllCaptures() != null)
                for (Location key : Storage.getAllCaptures().keySet()) {
                    Block center = key.getWorld().getBlockAt(key);
                    Block woolCenter = key.getWorld().getBlockAt(center.getX(), (center.getY()-1), center.getZ());
                    this.colorSquare(woolCenter, key.getWorld(), 0);                
                }
            Storage.clear();
        }
        log("Disabled", Level.INFO);
        
    }

    @Override
    public void onEnable()
    {
        instance = this;
        PluginManager pm = getServer().getPluginManager();
        initConfig();   
        initAllowedHooks();
        
        int groupplugins = 0;
        groupPlugin = null;
        for(Map.Entry<String, Boolean> supported : supportedHooks.entrySet())
        {
            if(pm.getPlugin(supported.getKey()) == null && supported.getValue())
                log(supported.getKey() + " support enabled in config but " + supported.getKey() + " plugin is not enabled!", Level.WARNING);
            else if(supported.getValue()) {
                try {
                    Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.capturetheportal.hooks."+supported.getKey()+"Hook");
                    groupplugins++;
                    if(groupPlugin == null) {
                        groupPlugin = (Hook)fc.newInstance();
                        groupPlugin.setPlugin(pm.getPlugin(supported.getKey()));
                    } else {
                        log("Please only enable one Group plugin at a time!", Level.SEVERE);
                        return;
                    }
                }
                catch(ClassNotFoundException notfoundex) {  }
                catch(InstantiationException instex) {  }
                catch(IllegalAccessException illex) {  }
            }
        }    
        if(groupplugins == 0 && groupPlugin == null) {
            log("Please enable 1 Group plugin in the config.yml!", Level.SEVERE);
            return;
        } else if(groupPlugin == null) {
            log("None of the Group plugins that were enabled in the config.yml could be found running! Please make sure you have the right one enabled in the config.yml!", Level.SEVERE);
            return;
        } else {
            metricsSetup = new setupMetrics();
            if(metricsSetup.setup(this))
                log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
            else
                log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
            
            Storage = new CapturesStorage(new File(this.getDataFolder(),"capturedpoints.yml"), persistcapture);            
            Colors = new HashMap();
            Timers = new HashMap();
            initColors();
            if(persistcapture)
                spinCooldowns();
            pm.registerEvents(new CaptureThePortalListener(), this);
            if(pm.getPlugin("Multiverse-Portals") != null)
                pm.registerEvents(new MultiversePortalListener(), this);
            if(pm.getPlugin("Stargate") != null)
                pm.registerEvents(new StargateListener(), this);
            log("Enabled with " + groupPlugin.getName() + " support", Level.INFO);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        String commandName = cmd.getName().toLowerCase();        
        if(!commandName.equalsIgnoreCase("capturetheportal") && !commandName.equalsIgnoreCase("ctp"))
            return true;
        if(args.length != 1)
            return false;
        if((sender instanceof Player) && !((Player)sender).isOp()) {
            ((Player)sender).sendMessage(ChatColor.RED + "You are not allowed to use that command. OP only.");
            return true;
        }
        if(args[0].equals("reload")) {
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            Bukkit.getServer().getPluginManager().enablePlugin(this);
            log("Reloaded", Level.INFO);
            if((sender instanceof Player))
                Util.sendMessagePlayer(getMessage("Reloaded"), ((Player)sender));
        } else if(args[0].equals("info")) {
            PluginDescriptionFile pdfFile = this.getDescription();
            String message = "\nVersion: " + pdfFile.getVersion() + 
                                "\n" + "Author: " + pdfFile.getAuthors().toString().replace("[", "").replace("]", "") +
                                "\nHome: http://dev.bukkit.org/server-mods/capturetheportal/ \n";
            if((sender instanceof Player))
                Util.sendMessagePlayer(getMessage(message), ((Player)sender));                
            else
                log(message, Level.INFO);
        } else
            return false;
        
        return true;
    }
    
    public void spinCooldowns() {
        PortalCooldown pc;
        for (Map.Entry<Location, CapturesStorage.CaptureInformation> entry : Storage.getAllCaptures().entrySet()) {                     
            if(entry.getValue().cooldownleft > 0) {
                int decremented = 0;
                if(cooldown_message_timeleft_increments > 0 && entry.getValue().cooldownleft > cooldown_message_timeleft_increments)
                    decremented = (cooldown_message_timeleft_increments - (entry.getValue().cooldownleft % cooldown_message_timeleft_increments));
                pc = new PortalCooldown(this, entry.getKey().getBlock(), (entry.getValue().cooldownleft*10), entry.getValue().group, "cooldown", decremented, null);
                addTimer(entry.getKey().getBlock().getLocation(), pc);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, pc, 10);
            }
        }
    }
    
    public void initAllowedHooks() {
        supportedHooks = new HashMap<String, Boolean>();
        supportedHooks.put("Factions", enablefactions);
        supportedHooks.put("Towny", enabletowny);
        supportedHooks.put("SimpleClans", enablesimpleclans);
        supportedHooks.put("Gods", enablegods);
        supportedHooks.put("Vault", enablepermissions);
    }
    
    public String getGroupType() {        
        return groupPlugin.getGroupType();        
    }
    
    public void log(String message, Level lvl) {
        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(lvl, ("["+pdfFile.getName()+" v"+pdfFile.getVersion()+"] " + message));
    }
    
    public static void Log(String message, Level lvl) {        
        logger.log(lvl, ("["+pluginName+"] " + message));
    }
    
    public static String getMessage(String message) {
        return (ChatColor.DARK_AQUA + "[CTP]: " + ChatColor.WHITE + CaptureThePortal.instance.getConfig().getString(("CaptureThePortal.messages." + message), message));
    }
    
    private void initConfig() {
        this.reloadConfig();
        FileConfiguration config = this.getConfig();
        File configFile = new File("plugins/CaptureThePortal", "config.yml");
        if(!configFile.exists())
            this.saveDefaultConfig();
        config.options().copyDefaults(true);
        capturedelay = (config.getInt("CaptureThePortal.delay", 0)*10);         // Convert to deciseconds
        cooldown_time = (config.getInt("CaptureThePortal.cooldown", 0)*10);        
        cooldown_message_timeleft = (config.getInt("CaptureThePortal.cooldown_message_timeleft", 0)*10);
        cooldown_message_timeleft_increments = config.getInt("CaptureThePortal.cooldown_message_timeleft_increments", 0);        
        fullgroupnames = (config.getBoolean("CaptureThePortal.fullgroupnames", fullgroupnames));
        usenations = (config.getBoolean("CaptureThePortal.usenations", usenations));
        dieorbounce = (config.getBoolean("CaptureThePortal.dieorbounce", dieorbounce));
        enablebouncing = (config.getBoolean("CaptureThePortal.enablebouncing", enablebouncing));
        persistcapture = (config.getBoolean("CaptureThePortal.persistcapture", persistcapture));        
        enablewormholes = (config.getBoolean("CaptureThePortal.enablewormholesupport", enablewormholes));
        enablestargates = (config.getBoolean("CaptureThePortal.enablestargatesupport", enablestargates));
        enablefactions = (config.getBoolean("CaptureThePortal.enablefactionsupport", enablefactions));
        enablepermissions = (config.getBoolean("CaptureThePortal.enablepermissionssupport", enablepermissions));
        enabletowny = (config.getBoolean("CaptureThePortal.enabletownysupport", enabletowny));
        enablesimpleclans = (config.getBoolean("CaptureThePortal.enablesimpleclanssupport", enablesimpleclans));
        enablegods = (config.getBoolean("CaptureThePortal.enablegodssupport", enablegods));
        enable_ender = (config.getBoolean("CaptureThePortal.enableEndersupport", enable_ender));
        enablemvportals = (config.getBoolean("CaptureThePortal.enableMVPortals", enablemvportals));
        allowneutraltoportal = (config.getBoolean("CaptureThePortal.allow_neutral_to_portal", allowneutraltoportal));
        
                
        log("Configuration loaded succesfully", Level.INFO);
        this.saveConfig();
    }

    private void initColors() {
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

    private boolean checkSquare(Block block, World world) {
        boolean isWoolSquare = true;
        
        for(int x = -((squareSize-1)/2); x <= ((squareSize-1)/2); x++) {
            for(int z = -((squareSize-1)/2); z <= ((squareSize-1)/2); z++) {
                if(world.getBlockAt(block.getX() + x, block.getY(), block.getZ() + z).getType() != Material.WOOL)
                    isWoolSquare = false;
            }
        }
        
        return isWoolSquare;
    }
    
    public String getTeamOfPlayer(Player player) {
        return groupPlugin.getGroupByPlayer(player);
    }
    
    public void colorSquare(Block center, World world, int color) {        
        if(checkSquare(center, world)) {
            for(int x = -((squareSize-1)/2); x <= ((squareSize-1)/2); x++) {
                for(int z = -((squareSize-1)/2); z <= ((squareSize-1)/2); z++) {
                    if(world.getBlockAt(center.getX() + x, center.getY(), center.getZ() + z).getType() == Material.WOOL)
                        world.getBlockAt(center.getX() + x, center.getY(), center.getZ() + z).setData((byte)(color));
                }
            }
        } else
            center.setData((byte)(color));
    }
    
    private String validCapture(Block block, Player player) {
        String captureType = "";
        Block woolCenter = player.getWorld().getBlockAt(block.getX(), (block.getY()-1), block.getZ());
        if(enable_ender && portalUtil.checkEndPortal(woolCenter, player.getWorld()))
            captureType = "End";
        else if(enablewormholes && portalUtil.checkWormHoleDailer(woolCenter, player.getWorld()))
            captureType = "Wormhole";
        else if(enablestargates && portalUtil.checkStargatePortal(woolCenter, player.getWorld()))
            captureType = "Startgate";
        else if(enablemvportals && portalUtil.checkMultiversePortal(woolCenter, player.getWorld()))
            captureType = "Portal";
        else if(checkSquare(woolCenter, player.getWorld()))
            captureType = "Nether";                        
        if(!getTeamOfPlayer(player).equals(""))
            if(!Storage.getCapture(block.getLocation()).equals(getTeamOfPlayer(player)))
                return captureType;
        return "";
    }
    
    private void broadcastCapture(Player player, String captureType) {
        if(!getMessage("capture_message").equals("")) {
            String team = "";
            ChatColor chat;
            team = getTeamOfPlayer(player);
            if(enablepermissions)            
                chat = groupPlugin.getGroupColor(player);
            else
                chat = ChatColor.WHITE;
            Util.broadcastMessage(ChatColor.GREEN+getMessage("capture_message").replace("[team]", chat+team+ChatColor.GREEN).replace("[type]", captureType).replace("[group]", getGroupType()));
        }
    }
    
    public boolean capturePortal(Block block, Player player) {
        String captureType;
        if(!(captureType = validCapture(block, player)).equals("")) {
            if(Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getCooldown() > 0 && Timers.get(block.getLocation()).getType().equals("cooldown")) {
                Util.sendMessagePlayer(getMessage("player_portal_is_cooldown").replace("[cooldown]", Util.parseTime(Timers.get(block.getLocation()).getCooldown()/10)+ "."), player);
                return false;
            } else if (Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getCooldown() > 0 && Timers.get(block.getLocation()).getType().equals("delay")) {
                if(Timers.get(block.getLocation()).getCapturer() != player)
                    Util.sendMessagePlayer(getMessage("player_someone_else_capturing"), player);
                return false;
            } else if(Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getCooldown() == 0) {
                Timers.remove(block.getLocation());
            }
            broadcastCapture(player, captureType);
            Util.sendMessagePlayer(getMessage("player_capturing_portal_holdit").replace("[capturetime]", Util.parseTime(capturedelay/10)), player);
            getServer().getScheduler().scheduleSyncDelayedTask(this, new CapturePortal(this, player, block, cooldown_time, capturedelay, player.getLocation()), 1);
            return true;
        } else
            return false;
    }
    
    public int getCapturedelay() {
        return capturedelay;
    }

    public void addCaptureLocation(Block block, String group, Integer cooldownleft) {        
        Storage.setCapture(block.getLocation(), group, cooldownleft);
    }

    public void addTimer(Location loc, PortalCooldown pc) {
        if(Timers.containsKey(loc))
            Timers.remove(loc);
        Timers.put(loc, pc);
    }

    public int isAllowedToPortal(Block block, Player player, Material portalMaterial) {
        Block checkBlock;
        int xradius, yradius, zradius;        
        if(portalMaterial == Material.STATIONARY_WATER) {
            xradius = 5;
            zradius = 5;
            yradius = 6;
        } else {
            xradius = 3;
            zradius = 3;
            yradius = 5;
        }
        for(int x = -xradius; x <= xradius; x++) {
            for(int z = -zradius; z <= zradius; z++) {
                for(int y = -yradius; y <= yradius; y++) {
                    checkBlock = player.getWorld().getBlockAt((int)(player.getLocation().getX() + x), (int)(player.getLocation().getY() + y), (int)(player.getLocation().getZ() + z));                    
                    if(checkBlock.getType() == Material.STONE_PLATE) {
                        
                        if(!allowneutraltoportal && getTeamOfPlayer(player).equals(""))
                            return 1;
                        
                        if(Storage.getCapture(checkBlock.getLocation()).equals(""))
                            return 2;
                        
                        if((!Storage.getCapture(checkBlock.getLocation()).equals(getTeamOfPlayer(player))
                            && !groupPlugin.isAllied(player, Storage.getCapture(checkBlock.getLocation()))))
                            return 3;
                        
                        
                    }
                }
            }
        }
        return 0;
    }
    
    public Boolean isAllied(Player Player, String sOtherPlayer) {
        return groupPlugin.isAllied(Player, sOtherPlayer);
    }

    public int getColor(Player player) {
        ChatColor color = groupPlugin.getGroupColor(player);        
        if(color == null || !Colors.containsKey(color.name().toLowerCase()))
            return Colors.get("default");
        else
            return Colors.get(color.name().toLowerCase());
    }
    
    public static boolean getDieFromUncapturedPortal() {
        return dieorbounce;
    }
    
    public static boolean getWormholeSupport() {
        return enablewormholes;
    }
    
    public static boolean getEnderSupport() {
        return enable_ender;
    }
    
    public static int getCooldownInterval() {
        return cooldown_message_timeleft_increments;
    }
    
    public static int getCoolMessageTime() {
        return cooldown_message_timeleft;
    }
    
    public static Boolean getFullgroupnames() {
        return fullgroupnames;
    }
    
    public static Boolean getUseNations() {
        return usenations;
    }
    
    public static Boolean getEnablebouncing() {
        return enablebouncing;
    }
}
