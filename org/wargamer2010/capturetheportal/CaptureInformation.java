
package org.wargamer2010.capturetheportal;

public class CaptureInformation {
    private String group = "";
    private int cooldownleft = 0;

    CaptureInformation(String pGroup, int pCooldown) {
        group = pGroup;
        cooldownleft = pCooldown;
    }

    public String getGroup() {
        return group;
    }

    public int getCooldownleft() {
        return cooldownleft;
    }
}
