
package org.wargamer2010.capturetheportal.portals;

import org.bukkit.block.Block;

public interface IPortal {
    public boolean init();

    public String getName();

    public boolean isPortalNear(int radius, Block origin);
}
