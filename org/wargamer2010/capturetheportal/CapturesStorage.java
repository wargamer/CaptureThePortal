package org.wargamer2010.capturetheportal;

import org.wargamer2010.capturetheportal.Utils.configUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.logging.Level;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.wargamer2010.capturetheportal.Utils.Util;

public class CapturesStorage {
    private static Map<Location, CaptureInformation> CapturedPoints = new HashMap<Location, CaptureInformation>();
    private YamlConfiguration yml = null;
    private File ymlfile = null;
    private boolean persist;
    
    public CapturesStorage(File ymlFile, Boolean bpersist) {        
        persist = bpersist;        
        if(persist) {
            if(!ymlFile.exists()) {
                try {
                    ymlFile.createNewFile();
                } catch(IOException ex) {
                    CaptureThePortal.Log("Could not create " + ymlFile.getName(), Level.WARNING);
                }
            }        
            yml = YamlConfiguration.loadConfiguration(ymlFile);
            ymlfile = ymlFile;
            loadCaptures();
        }
        
    }
    
    public String getCapture(Location checkLoc) {        
        if(CapturedPoints.containsKey(checkLoc))
            return CapturedPoints.get(checkLoc).group;
        return "";
    }
    
    public void deleteCapture(Location delLoc) {
        if(CapturedPoints.containsKey(delLoc))
            CapturedPoints.remove(delLoc);
    }
    
    public Map<Location, CaptureInformation> getAllCaptures() {        
        return CapturedPoints;
    }
    
    public void setCapture(Location capLoc, String capturingentity, Integer cooldowntime) {        
        CapturedPoints.put(capLoc, new CaptureInformation(capturingentity, cooldowntime));
        if(persist)
            saveCaptures();
    }
    
    public void clear() {
        CapturedPoints.clear();
    }
    
    private void saveToFile() {        
        try {
            yml.save(ymlfile);
        } catch(IOException IO) {
            CaptureThePortal.Log("Failed to save " + ymlfile.getName(), Level.WARNING);
        }
    }
    
    public void saveCaptures() {
        Map<String,Object> tempPoints = new HashMap<String,Object>();
        Map<String,Object> temp;
        for(Map.Entry<Location,CaptureInformation> settings : CapturedPoints.entrySet()) {
            temp = new HashMap<String,Object>();
            temp.put("location", Util.convertLocationToString(settings.getKey()));
            temp.put("group", settings.getValue().group);
            temp.put("world", settings.getKey().getWorld().getName());
            temp.put("cooldownleft", Integer.toString(settings.getValue().cooldownleft));
            String key = (settings.getKey().getWorld().getName() + Util.convertLocationToString(settings.getKey()));
            tempPoints.put(key, temp);
        }
        yml.set("capturedpoints", tempPoints);
        saveToFile();
    }
    
    private void loadCaptures() {
        HashMap<String,HashMap<String,String>> points = configUtil.fetchHasmapInHashmap("capturedpoints", yml);
        for(Map.Entry<String,HashMap<String,String>> pointsSettings : points.entrySet()) {
            HashMap<String,String> settings = pointsSettings.getValue();
            if(settings.containsKey("group") && settings.containsKey("location") && settings.containsKey("world") && settings.containsKey("cooldownleft")) {
                try {                    
                    World world = Bukkit.getServer().getWorld(settings.get("world"));
                    if(world != null)
                        CapturedPoints.put(Util.convertStringToLocation(settings.get("location"), world), new CaptureInformation(settings.get("group"), Integer.parseInt(settings.get("cooldownleft"))));
                } catch(NumberFormatException ex) {                    
                    continue;
                }
                
            }
        }
    }
    
    public static class CaptureInformation {
        public String group = "";
        public int cooldownleft = 0;
        
        CaptureInformation(String pGroup, int pCooldown) {
            group = pGroup;
            cooldownleft = pCooldown;
        }
    }
}
