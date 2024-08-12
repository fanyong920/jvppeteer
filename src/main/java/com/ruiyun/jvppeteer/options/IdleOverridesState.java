package com.ruiyun.jvppeteer.options;

public class IdleOverridesState extends ActiveProperty {

    public Overrides overrides;

    public IdleOverridesState(boolean active) {
        super(active);
    }

    public IdleOverridesState(boolean active, Overrides overrides) {
        super(active);
        this.overrides = overrides;
    }

    public Overrides getOverrides() {
        return overrides;
    }

    public void setOverrides(Overrides overrides) {
        this.overrides = overrides;
    }

    public static class Overrides{

        public boolean isUserActive = false;

        public boolean  isScreenUnlocked = false;

        public Overrides() {}

        public Overrides(boolean isUserActive, boolean isScreenUnlocked) {
            this.isUserActive = isUserActive;
            this.isScreenUnlocked = isScreenUnlocked;
        }

        public boolean getIsUserActive() {
            return isUserActive;
        }

        public void setIsUserActive(boolean userActive) {
            isUserActive = userActive;
        }

        public boolean getIsScreenUnlocked() {
            return isScreenUnlocked;
        }

        public void setIsScreenUnlocked(boolean screenUnlocked) {
            isScreenUnlocked = screenUnlocked;
        }
    }
}
