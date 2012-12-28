
package org.wargamer2010.capturetheportal.timers;

import org.bukkit.entity.Player;

public abstract class Timer implements Runnable {
    public abstract int getTimeLeft();

    public abstract String getType();

    public abstract Player getCapturer();

    public abstract void run();
}
