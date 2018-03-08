package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Niklas Eicker
 */
public class WhacAMolePlugin extends JavaPlugin {
    public static final String WHAC_A_MOLE = "whacamole";
    private GameBox gameBox;

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("GameBox");
        if (plugin == null || !plugin.isEnabled()) {
            getLogger().warning(" GameBox was not found! Disabling WhacAMole...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        gameBox = (GameBox) plugin;
        new Module(gameBox, WHAC_A_MOLE
                , "me.nikl.gamebox.games.whacamole.WhacAMole"
                , this, WHAC_A_MOLE, "wam");
    }
}
