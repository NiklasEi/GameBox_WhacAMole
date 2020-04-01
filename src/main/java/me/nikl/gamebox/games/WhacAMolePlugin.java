package me.nikl.gamebox.games;

import me.nikl.gamebox.games.whacamole.WhacAMole;
import me.nikl.gamebox.module.GameBoxModule;

/**
 * @author Niklas Eicker
 */
public class WhacAMolePlugin extends GameBoxModule {
    public static final String WHAC_A_MOLE = "whacamole";

    @Override
    public void onEnable() {
        registerGame(WHAC_A_MOLE, WhacAMole.class, "wam");
    }

    @Override
    public void onDisable() {

    }
}
