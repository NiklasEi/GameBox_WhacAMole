package me.nikl.gamebox.games.whacamole;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.rules.GameRuleMultiRewards;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Niklas
 *
 * Game rules container for Whac a mole
 */
public class GameRules extends GameRuleMultiRewards {
    private int time;
    private GameMode gameMode;
    private boolean gameOverOnHittingHuman = false;
    private int punishmentOnHittingHuman = 5;

    public GameRules(WhacAMole plugin, GameMode gameMode, String key, double cost, int time, boolean saveStats) {
        super(key, saveStats, SaveType.SCORE, cost);
        this.time = time;
        this.gameMode = gameMode;
        loadRewards(plugin);
    }

    private void loadRewards(WhacAMole plugin) {
        if (!plugin.getConfig().isConfigurationSection("gameBox.gameButtons." + key + ".scoreIntervals")) return;
        ConfigurationSection onGameEnd = plugin.getConfig().getConfigurationSection("gameBox.gameButtons." + key + ".scoreIntervals");
        for (String key : onGameEnd.getKeys(false)) {
            int keyInt;
            try {
                keyInt = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                plugin.warn(" NumberFormatException while getting the rewards from config!");
                continue;
            }
            if (onGameEnd.isSet(key + ".money") && (onGameEnd.isDouble(key + ".money") || onGameEnd.isInt(key + ".money"))) {
                addMoneyReward(keyInt, onGameEnd.getDouble(key + ".money"));
            } else {
                addMoneyReward(keyInt, 0.);
            }
            if (onGameEnd.isSet(key + ".tokens") && (onGameEnd.isDouble(key + ".tokens") || onGameEnd.isInt(key + ".tokens"))) {
                addTokenReward(keyInt, onGameEnd.getInt(key + ".tokens"));
            } else {
                addTokenReward(keyInt, 0);
            }
        }
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isGameOverOnHittingHuman() {
        return gameOverOnHittingHuman;
    }

    public void setGameOverOnHittingHuman(boolean gameOverOnHittingHuman) {
        this.gameOverOnHittingHuman = gameOverOnHittingHuman;
    }

    public int getPunishmentOnHittingHuman() {
        return punishmentOnHittingHuman;
    }

    public void setPunishmentOnHittingHuman(int punishmentOnHittingHuman) {
        this.punishmentOnHittingHuman = punishmentOnHittingHuman;
    }

    public int getTime() {
        return time;
    }

    public enum GameMode {
        CLASSIC, FULLINVENTORY;
    }
}
