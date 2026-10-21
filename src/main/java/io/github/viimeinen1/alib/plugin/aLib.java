package io.github.viimeinen1.alib.plugin;

import io.github.viimeinen1.alib.module.region.RegionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class aLib extends JavaPlugin {

    public static JavaPlugin instance;

    @Override
    public void onEnable() {
        super.onEnable();
        aLib.instance = this;
        RegionManager.enable(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        RegionManager.disable();
    }

}
