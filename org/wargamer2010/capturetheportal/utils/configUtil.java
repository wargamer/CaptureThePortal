package org.wargamer2010.capturetheportal.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.MemorySection;
import java.util.HashMap;
import java.util.Map;

public class configUtil {
    private configUtil() {
        
    }

    public static HashMap<String,HashMap<String,String>> fetchHasmapInHashmap(String path, FileConfiguration config) {
        HashMap<String,HashMap<String,String>> tempHasinHash = new HashMap<String,HashMap<String,String>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempHasinHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                MemorySection memsec = (MemorySection)entry.getValue();
                HashMap<String,String> tempmap = new HashMap<String, String>();
                for(Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet())
                    tempmap.put(subentry.getKey(), (String)subentry.getValue());
                tempHasinHash.put(entry.getKey(), tempmap);
            }
        } catch(ClassCastException ex) {

        }
        return tempHasinHash;
    }
}
