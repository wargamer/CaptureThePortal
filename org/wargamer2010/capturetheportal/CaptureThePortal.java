package org.wargamer2010.capturetheportal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.wargamer2010.capturetheportal.utils.Util;
import org.wargamer2010.capturetheportal.utils.Vault;
import org.wargamer2010.capturetheportal.hooks.*;
import org.wargamer2010.capturetheportal.listeners.*;
import org.wargamer2010.capturetheportal.metrics.setupMetrics;
import org.wargamer2010.capturetheportal.portals.IPortal;
import org.wargamer2010.capturetheportal.timers.*;

public class CaptureThePortal extends JavaPlugin {
    private static CapturesStorage Storage;
    private static CaptureThePortal instance;
    private static Map<Location, Timer> Timers;                                    // Stores all the timing classes (CapturePortal) for the various locations

    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final String pluginName = "CaptureThePortal";
    private IHook groupPlugin;
    private setupMetrics metricsSetup = null;

    @Override
    public void onDisable()
    {
        if(Storage != null) {
            if(CaptureThePortalConfig.getPersistCapture())
                Storage.saveCaptures();
            else {
                if(Storage.getAllCaptures() != null)
                    for (Location key : Storage.getAllCaptures().keySet()) {
                        Block center = key.getWorld().getBlockAt(key);
                        Block woolCenter = key.getWorld().getBlockAt(center.getX(), (center.getY()-1), center.getZ());
                        colorSquare(woolCenter, key.getWorld(), 0);
                        updateControlledSigns(woolCenter, "");
                    }
                Storage.clear();
            }
        }

        log("Disabled", Level.INFO);

    }

    @Override
    public void onEnable()
    {
        instance = this;
        PluginManager pm = getServer().getPluginManager();
        CaptureThePortalConfig.init();

        Vault vault = new Vault();
        if(Vault.isVaultFound())
            vault.setupEconomy();

        int groupplugins = 0;
        groupPlugin = null;
        for(Map.Entry<String, Boolean> supported : CaptureThePortalConfig.getSupportedHooks().entrySet())
        {
            if(pm.getPlugin(supported.getKey()) == null && supported.getValue())
                log(supported.getKey() + " support enabled in config but " + supported.getKey() + " plugin is not enabled!", Level.WARNING);
            else if(supported.getValue()) {
                try {
                    Class<?> fc = Class.forName("org.wargamer2010.capturetheportal.hooks."+supported.getKey()+"Hook");
                    groupplugins++;
                    if(groupPlugin == null) {
                        groupPlugin = (IHook)fc.newInstance();
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
        } else if(groupPlugin == null) {
            log("None of the Group plugins that were enabled in the config.yml could be found running! Please make sure you have the right one enabled in the config.yml!", Level.SEVERE);
        } else {
            metricsSetup = new setupMetrics(this);
            if(!metricsSetup.isOptOut()) {
                if(metricsSetup.setup())
                    log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
                else
                    log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
            }

            Storage = new CapturesStorage(new File(this.getDataFolder(),"capturedpoints.yml"), CaptureThePortalConfig.getPersistCapture());
            Timers = new HashMap<Location, Timer>();
            if(CaptureThePortalConfig.getPersistCapture())
                spinCooldowns();
            pm.registerEvents(new CaptureThePortalListener(), this);
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

    public static CaptureThePortal get() {
        return instance;
    }

    public static CapturesStorage getStorage() {
        return Storage;
    }

    public void spinCooldowns() {
        PortalCooldown pc;
        for (Map.Entry<Location, CaptureInformation> entry : Storage.getAllCaptures().entrySet()) {
            if(entry.getValue().getCooldownleft() > 0) {
                int decremented = 0;
                int interval = CaptureThePortalConfig.getCooldownInterval();
                if(interval > 0 && entry.getValue().getCooldownleft() > interval)
                    decremented = (interval - (entry.getValue().getCooldownleft() % interval));
                pc = new PortalCooldown(this, entry.getKey().getBlock(), entry.getValue().getCooldownleft(), entry.getValue().getGroup(), decremented, null);
                addTimer(entry.getKey().getBlock().getLocation(), pc);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, pc, Util.getTicksFromSeconds(1));
            }
        }
    }

    public String getGroupType() {
        return groupPlugin.getGroupType();
    }

    public void log(String message, Level lvl) {
        PluginDescriptionFile pdfFile = this.getDescription();
        if(!Util.stringIsEmpty(message))
            logger.log(lvl, ("["+pdfFile.getName()+" v"+pdfFile.getVersion()+"] " + message));
    }

    public static void Log(String message, Level lvl) {
        if(!Util.stringIsEmpty(message))
            logger.log(lvl, ("["+pluginName+"] " + message));
    }

    public static String getMessage(String message) {
        String result = CaptureThePortal.instance.getConfig().getString(("CaptureThePortal.messages." + message));
        if(!Util.stringIsEmpty(result))
            return (ChatColor.DARK_AQUA + "[CTP]: " + ChatColor.WHITE + result);
        return "";
    }

    public String getTeamOfPlayer(Player player) {
        return groupPlugin.getGroupByPlayer(player);
    }

    public int getColor(Player player) {
        ChatColor color = groupPlugin.getGroupColor(player);
        return CaptureThePortalConfig.getColor(player, color);
    }

    public void colorSquare(Block center, World world, int color) {
        int squareSize = CaptureThePortalConfig.getSquareSize();
        if(Util.checkSquare(center, world, squareSize)) {
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
        return validCapture(block, player, false);
    }

    private String validCapture(Block block, Player player, boolean ignoreTeam) {
        String captureType = "";
        Block woolCenter = player.getWorld().getBlockAt(block.getX(), (block.getY()-1), block.getZ());
        for(Map.Entry<IPortal, Integer> entry : CaptureThePortalConfig.getSupportedPortals().entrySet()) {
            if(entry.getKey().isPortalNear(entry.getValue(), woolCenter)) {
                captureType = entry.getKey().getName();
                break;
            }
        }

        if(ignoreTeam)
            return captureType;
        if(!(getTeamOfPlayer(player).isEmpty()) && !Storage.getCapture(block.getLocation()).equals(getTeamOfPlayer(player)))
            return captureType;
        return "";
    }

    private String getColoredTeamName(Player player) {
        String team = getTeamOfPlayer(player);
        ChatColor chat;
        if(groupPlugin.getGroupColor(player) != null)
            chat = groupPlugin.getGroupColor(player);
        else
            chat = ChatColor.WHITE;
        return (chat + team);
    }

    private void broadcastCapture(Player player, String captureType) {
        if(!getMessage("capture_message").isEmpty()) {
            Util.broadcastMessage(ChatColor.GREEN
                                    + getMessage("capture_message")
                                        .replace("[team]", getColoredTeamName(player)+ChatColor.GREEN)
                                        .replace("[type]", captureType)
                                        .replace("[group]", getGroupType()));
        }
    }

    public boolean capturePortal(Block block, Player player) {
        String captureType = validCapture(block, player);
        if(!captureType.isEmpty()) {
            if(Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getTimeLeft() > 0 && Timers.get(block.getLocation()).getType().equals("cooldown")) {
                Util.sendMessagePlayer(getMessage("player_portal_is_cooldown").replace("[cooldown]", Util.parseTime(Timers.get(block.getLocation()).getTimeLeft())+ "."), player);
                return false;
            } else if (Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getTimeLeft()> 0 && Timers.get(block.getLocation()).getType().equals("delay")) {
                if(Timers.get(block.getLocation()).getCapturer() != player)
                    Util.sendMessagePlayer(getMessage("player_someone_else_capturing"), player);
                return false;
            } else if(Timers.containsKey(block.getLocation()) && Timers.get(block.getLocation()).getTimeLeft() == 0) {
                Timers.remove(block.getLocation());
            }
            broadcastCapture(player, captureType);
            Util.sendMessagePlayer(getMessage("player_capturing_portal_holdit").replace("[capturetime]",
                    Util.parseTime(CaptureThePortalConfig.getCapturedelay())), player);
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                    new CapturePortal(this, player, block, CaptureThePortalConfig.getCooldown(), CaptureThePortalConfig.getCapturedelay()),
                        Util.getTicksFromSeconds(1));
            return true;
        } else
            return false;
    }

    public void addCaptureLocation(Block block, String group, Integer cooldownleft) {
        Storage.setCapture(block.getLocation(), group, cooldownleft);
    }

    public void rewardTeam(Block block, String group, Player player) {
        if(CaptureThePortalConfig.getRewardAmount() <= 0)
            return;
        if(groupPlugin.giveMoneyToPlayers(group, block.getWorld(), CaptureThePortalConfig.getRewardAmount())) {
            Util.broadcastMessage(ChatColor.GREEN
                    + getMessage("reward_message")
                        .replace("[team]", getColoredTeamName(player) +ChatColor.GREEN)
                        .replace("[group]", getGroupType())
                        .replace("[reward]", Vault.getEconomy().format(CaptureThePortalConfig.getRewardAmount())));
        }

    }

    public void updateControlledSigns(Block block, String group) {
        int checkradius = 6;
        World world = block.getWorld();

        for(int x = -checkradius; x <= checkradius; x++) {
            for(int z = -checkradius; z <= checkradius; z++) {
                for(int y = -checkradius; y <= checkradius; y++) {
                    Block temp = world.getBlockAt(block.getX()+x, block.getY()+y, block.getZ()+z);
                    if(temp.getType() == Material.SIGN || temp.getType() == Material.WALL_SIGN || temp.getType() == Material.SIGN_POST) {
                        Sign sign = (Sign) temp.getState();
                        if(sign.getLine(0).equalsIgnoreCase("[Controlled by]")) {
                            sign.setLine(1, group);
                            sign.update();
                        }
                    }
                }
            }
        }
    }

    public void addTimer(Location loc, Timer tim) {
        if(Timers.containsKey(loc))
            Timers.remove(loc);
        Timers.put(loc, tim);
    }

    public void removeTimer(Location loc) {
        if(Timers.containsKey(loc))
            Timers.remove(loc);
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
                        if(validCapture(checkBlock, player, true).isEmpty())
                            continue;

                        if(!CaptureThePortalConfig.getAllowNeutralToPortal() && getTeamOfPlayer(player).isEmpty())
                            return 1;

                        if(Storage.getCapture(checkBlock.getLocation()).isEmpty())
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
}
