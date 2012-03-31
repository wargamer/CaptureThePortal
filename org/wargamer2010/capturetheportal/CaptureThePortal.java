package org.wargamer2010.capturetheportal;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import org.bukkit.block.BlockFace;
import org.wargamer2010.capturetheportal.hooks.*;

public class CaptureThePortal extends JavaPlugin {
    private int capturedelay = 60;                                                              // How long it takes in deciseconds (1/10th of a second)
    private int cooldown_time = 1200;                                                           // How long the cooldown is before a Portal can be recaptured in deciseconds
    private String capture_message = "[team] [group] is attempting to capture the [type].";             // The message that is displayed when a team attempts to capture the portal
    private String cooldown_message = "The portal will be available for capture again within [cooldown].";   // The message that is displayed when the portal is close to being available for capture
    private int cooldown_message_timeleft_increments = 0;                                       // How much time passes before the cooldown_message is printed, thus it's printer every x increments (0 is disabled)
    private int cooldown_message_timeleft = 20;                                                 // The amount of cooldowntime that is left before the cooldown_message is printed    
    private int squareSize = 5;                                                                 // The size of each side of the square + 1
    private boolean dieorbounce = false;                                                        // Whether the player that attempts to use an uncaptured portal dies (true) or bounces off (false)
    private boolean enablewormholes = false;                                                    // Whether or not Wormholes should be supported together with regular nether portals
    private boolean enablefactions = false;                                                     // Enables factions and disables default way of creating teams (via Permissions)
    private boolean enable_ender = false;                                                       // Support for Ender portals, is disabled by default    
    private boolean enabletowny = false;                                                        // Enables towny support and disables default way of creating teams (via Permissions)
    private boolean enablesimpleclans = false;                                                  // Enables simpleclans support and disables default way of creating teams (via Permissions)
    private boolean usepermissions = false;                                                     // Whether or not to use the Permissions plugin
    private String neutralPermission = "CaptureThePortal.neutral";                              // The permissions that can assigned the a group/player that is not allowed to participate
    private static HashMap<Location, String> CapturedPortals = null;                            // Stores all the Captured Portal with their respective permission (i.e. .red when red captured it)
    private static HashMap<Location, World> WorldLocations = null;                              // Stores all the Locations with their respective World, may not work well in a multi-world enviroment
    private static HashMap<Location, PortalCooldown> Timers;                                    // Stores all the timing classes (CapturePortal) for the various locations
    private static Map<String, Integer> Colors;                                                 // Stores all the Permissions with their respective color codes
    private static PermissionHandler permissionHandler;
    private static CaptureThePortalListener PlayerListener;
    private static final Logger logger = Logger.getLogger("Minecraft");
    private Hook groupPlugin;

    @Override
    public void onDisable()
    {
        if(CapturedPortals != null && WorldLocations != null)
            for (Location key : CapturedPortals.keySet()) {
                if(WorldLocations.containsKey(key)) {
                    World world = WorldLocations.get(key);
                    Block center = world.getBlockAt(key);
                    Block woolCenter = world.getBlockAt(center.getX(), (center.getY()-1), center.getZ());
                    this.colorSquare(woolCenter, world, 0);
                }
            }
        log("disabled", Level.INFO);
    }

    @Override
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        initConfig();
        usepermissions = setupPermissions();
        if(pm.getPlugin("Factions") == null && enablefactions) {
            enablefactions = false;
            log("Faction support enabled in config but Factions plugin is not enabled!", Level.WARNING);
        } 
        if(pm.getPlugin("Towny") == null && enabletowny) {
            enabletowny = false;
            log("Towny support enabled in config but Towny plugin is not enabled!", Level.WARNING);
        }
        if(pm.getPlugin("SimpleClans") == null && enablesimpleclans) {
            enablesimpleclans = false;
            log("SimpleClans support enabled in config but SimpleClans plugin is not enabled!", Level.WARNING);
        }
        if(usepermissions || enablefactions || enabletowny || enablesimpleclans) {
            int groupplugins = 0;            
            String chosenplugin = "";
            if(enablefactions) {
                groupPlugin = new FactionsHook();
                chosenplugin = "Factions";
                groupplugins++;
            }
            if(enabletowny) {
                groupPlugin = new TownyAdvancedHook(pm.getPlugin("Towny"));
                chosenplugin = "Towny";
                groupplugins++;
            }
            if(enablesimpleclans) {
                groupPlugin = new SimpleclansHook(pm.getPlugin("SimpleClans"));
                chosenplugin = "SimpleClans";
                groupplugins++;
                
            }
            if(groupplugins > 1) {
                log("Please only enable one Group plugin, so Factions OR Towny OR Simpleclans!", Level.SEVERE);                
                return;
            } else if(groupplugins == 1)
                usepermissions = false;
            
            if(!usepermissions)
                log("Using " + chosenplugin +  " as Group plugin", Level.INFO);                
            else
                log("No Group plugin enabled so using Permissions.", Level.INFO);            
            PlayerListener = new CaptureThePortalListener(this);
            CapturedPortals = new HashMap();
            WorldLocations = new HashMap();
            Colors = new HashMap();
            Timers = new HashMap();
            initColors();
            pm.registerEvents(PlayerListener, this);
            log("enabled", Level.INFO);
        } else {
            // Permission system could not be hooked, running plugin passively..
            log("Permission system not detected and no other Group plugin found or not enabled in config. Plugin can't run", Level.SEVERE);            
            return;
        }
    }
    
    public String getGroupType() {
        if(enablefactions)
            return "Faction";        
        else if(enabletowny)
            return "Town";        
        else if(enablesimpleclans)
            return "Clan";        
        return "";
    }
    
    public void log(String message, Level lvl) {
        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(lvl, ("["+pdfFile.getName()+" v"+pdfFile.getVersion()+"] " + message));
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
        cooldown_message = (config.getString("CaptureThePortal.cooldown_message"));
        cooldown_message_timeleft = (config.getInt("CaptureThePortal.cooldown_message_timeleft", 0)*10);
        cooldown_message_timeleft_increments = config.getInt("CaptureThePortal.cooldown_message_timeleft_increments", 0);
        capture_message = (config.getString("CaptureThePortal.capture_message"));
        dieorbounce = (config.getBoolean("CaptureThePortal.dieorbounce", dieorbounce));
        enablewormholes = (config.getBoolean("CaptureThePortal.enablewormholesupport", enablewormholes));
        enablefactions = (config.getBoolean("CaptureThePortal.enablefactionsupport", enablefactions));
        enabletowny = (config.getBoolean("CaptureThePortal.enabletownysupport", enabletowny));
        enablesimpleclans = (config.getBoolean("CaptureThePortal.enablesimpleclanssupport", enablesimpleclans));
        enable_ender = (config.getBoolean("CaptureThePortal.enableEndersupport", enable_ender));
        
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
        Colors.put("CaptureThePortal.gold", 1);
        Colors.put("CaptureThePortal.light_purple", 2);
        Colors.put("CaptureThePortal.blue", 3);
        Colors.put("CaptureThePortal.yellow", 4);
        Colors.put("CaptureThePortal.green", 5);
        Colors.put("CaptureThePortal.dark_gray", 7);
        Colors.put("CaptureThePortal.gray", 8);
        Colors.put("CaptureThePortal.dark_purple", 10);
        Colors.put("CaptureThePortal.dark_blue", 11);        
        Colors.put("CaptureThePortal.dark_green", 13);
        Colors.put("CaptureThePortal.red", 14);
        Colors.put("CaptureThePortal.black", 15);
        
        Colors.put("Factions.captured", 14);
    }

    private boolean hasRights(Player player, String Permission) {
        return ((permissionHandler != null) ? permissionHandler.has(player, Permission) : false);
    }
    
    private boolean setupPermissions()
    {
        Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
        if(permissionHandler == null)
            if(permissionsPlugin != null) {
                permissionHandler = ((Permissions)permissionsPlugin).getHandler();                
                return true;
            }               
        return false;
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
    
    
    private boolean checkWormHoleDailer(Block block, World world) {
        BlockFace dailerOrientation = BlockFace.SELF;        
        
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
    
    private boolean checkEndPortal(Block block, World world) {
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

    /**
     * Returns a CaptureThePortal.color if faction support is disabled
     * and returns the faction name if it's enabled
     * 
     * @param player
     * @return String
     */
    public String getTeamOfPlayer(Player player) {        
        if(usepermissions) {
            if(hasRights(player, neutralPermission))
                return "";

            int amount = 0;
            String returnPerm = "";
            Iterator it = Colors.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                if(hasRights(player, (String)pairs.getKey())) {
                    returnPerm = (String)pairs.getKey();
                    amount++;
                }
            }

            if(amount > 1)
                return "";
            else
                return returnPerm;
        } else
            return groupPlugin.getGroupByName(player);        
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
        if(getEnderSupport() && checkEndPortal(woolCenter, player.getWorld()))
            captureType = "End";
        else if(checkWormHoleDailer(woolCenter, player.getWorld()) && getWormholeSupport())
            captureType = "Wormhole";
        else if(checkSquare(woolCenter, player.getWorld()))
            captureType = "Nether";        
        if(!getTeamOfPlayer(player).equals(""))
            if(!CapturedPortals.containsKey(block.getLocation()) || (CapturedPortals.containsKey(block.getLocation()) && !CapturedPortals.get(block.getLocation()).equals(getTeamOfPlayer(player))))
                return captureType;
        return "";
    }
    
    private void broadcastCapture(Player player, String captureType) {
        if(!capture_message.equals("")) {
            String team = "";
            ChatColor chat;
            if(usepermissions) {
                team = getTeamOfPlayer(player);
                team = team.replace("CaptureThePortal.", "");                
                chat = ChatColor.valueOf(team.toUpperCase());
                team = team.replace("_", " ");
            } else {
                team = groupPlugin.getGroupByName(player);
                chat = ChatColor.WHITE;
            }
            getServer().broadcastMessage(ChatColor.GREEN+capture_message.replace("[team]", chat+team+ChatColor.GREEN).replace("[type]", captureType).replace("[group]", getGroupType()));
        }
    }
    
    public boolean capturePortal(Block block, Player player) {
        String captureType;
        if(!(captureType = validCapture(block, player)).equals("")) {
            if(Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getCooldown() > 0 && Timers.get(block.getLocation()).getType().equals("cooldown")) {
                player.sendMessage("This Portal is on Cooldown, total cooldown time is "+Util.parseTime(Timers.get(block.getLocation()).getCooldown()/10)+ ".");
                return false;
            } else if (Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getCooldown() > 0 && Timers.get(block.getLocation()).getType().equals("delay")) {
                if(Timers.get(block.getLocation()).getCapturer() != player)
                    player.sendMessage("Someone else is currently capturing this portal!");
                return false;
            } else if(Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getCooldown() == 0) {
                Timers.remove(block.getLocation());
            }
            broadcastCapture(player, captureType);
            player.sendMessage("Capturing Portal! Hold it for "+Util.parseTime(capturedelay/10)+" to capture it!");            
            getServer().getScheduler().scheduleSyncDelayedTask(this, new CapturePortal(this, player, block, cooldown_time, getServer(), capturedelay, player.getLocation(), cooldown_message, cooldown_message_timeleft), 1);
            return true;
        } else
            return false;
    }
    
    public int getCapturedelay() {
        return capturedelay;
    }

    public void addCaptureLocation(Block block, Player player) {
        if(CapturedPortals.containsKey(block.getLocation()))
            CapturedPortals.remove(block.getLocation());
        if(WorldLocations.containsKey(block.getLocation()))
            WorldLocations.remove(block.getLocation());
        CapturedPortals.put(block.getLocation(), getTeamOfPlayer(player));
        WorldLocations.put(block.getLocation(), player.getWorld());
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
                        if((CapturedPortals.containsKey(checkBlock.getLocation())
                            && !CapturedPortals.get(checkBlock.getLocation()).equals(getTeamOfPlayer(player))
                            && !getTeamOfPlayer(player).equals(""))
                            && !groupPlugin.isAllied(player, CapturedPortals.get(checkBlock.getLocation())))
                            return 0;
                        
                        if(!CapturedPortals.containsKey(checkBlock.getLocation()))
                            return 1;
                    }
                }
            }
        }
        return 2;
    }

    public int getColor(String Permission) {
        if(usepermissions) {
            if(Colors.containsKey(Permission))
                return Colors.get(Permission);
            else
                return 0;
        } else
            return Colors.get("Factions.captured");
    }
    
    public boolean getDieFromUncapturedPortal() {
        return dieorbounce;
    }
    
    public boolean getWormholeSupport() {
        return enablewormholes;
    }
    
    public boolean getEnderSupport() {
        return enable_ender;
    }
    
    public int getCooldownInterval() {
        return cooldown_message_timeleft_increments;
    }
}
