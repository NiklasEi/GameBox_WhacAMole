package me.nikl.gamebox.games.whacamole;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.GameSettings;
import me.nikl.gamebox.games.WhacAMolePlugin;

/**
 * @author Niklas Eicker
 */
public class WhacAMole extends me.nikl.gamebox.game.Game {
    public WhacAMole(GameBox gameBox) {
        super(gameBox, WhacAMolePlugin.WHAC_A_MOLE);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void init() {

    }

    @Override
    public void loadSettings() {
        gameSettings.setGameType(GameSettings.GameType.SINGLE_PLAYER);
        gameSettings.setHandleClicksOnHotbar(false);
        gameSettings.setGameGuiSize(54);
    }

    @Override
    public void loadLanguage() {
        gameLang = new Language(this);
    }

    @Override
    public void loadGameManager() {
        gameManager = new GameManager(this);
    }
}
