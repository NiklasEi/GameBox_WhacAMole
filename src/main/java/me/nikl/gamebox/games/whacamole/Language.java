package me.nikl.gamebox.games.whacamole;

import me.nikl.gamebox.game.GameLanguage;


public class Language extends GameLanguage {
    public String GAME_TITLE, GAME_TITLE_LOST, GAME_TITLE_START, GAME_WON_MONEY, GAME_WON, GAME_MOLE_NAME, GAME_CREEPER_NAME, GAME_HUMAN_NAME;
    public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY;

    public Language(WhacAMole whacAMole) {
        super(whacAMole);
    }

    @Override
    protected void loadMessages() {
        getGameMessages();
    }

    private void getGameMessages() {
        this.GAME_TITLE = getString("game.inventoryTitles.gameTitle");
        this.GAME_TITLE_LOST = getString("game.inventoryTitles.gameOver");
        this.GAME_TITLE_START = getString("game.inventoryTitles.start");
        this.GAME_MOLE_NAME = getString("game.moleDisplayName");
        this.GAME_CREEPER_NAME = getString("game.creeperDisplayName");
        this.GAME_HUMAN_NAME = getString("game.humanDisplayName");
        this.GAME_PAYED = getString("game.econ.payed");
        this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");
        this.GAME_WON_MONEY = getString("game.econ.wonMoney");
        this.GAME_WON = getString("game.gameOverNoPay");
    }
}

