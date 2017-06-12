package me.nikl.whacamole;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niklas
 *
 * Game rules container for Whac a mole
 */
public class GameRules {

    private double cost;
    private boolean saveStats;
    private String key;

    private Map<Integer, Double> moneyRewards;
    private Map<Integer, Integer> tokenRewards;

    private int time;

    private GameMode gameMode;

    // gamemode full inventory rules
    private boolean gameOverOnHittingHuman = false;
    private int punishmentOnHittingHuman = 5;

    public GameRules(Main plugin, GameMode gameMode, String key, double cost, int time, boolean saveStats){
        this.cost = cost;
        this.saveStats = saveStats;
        this.key = key;
        this.time = time;

        this.gameMode = gameMode;

        loadRewards(plugin);
    }


    private void loadRewards(Main plugin) {
        moneyRewards = new HashMap<>();
        tokenRewards = new HashMap<>();

        if(!plugin.getConfig().isConfigurationSection("gameBox.gameButtons." + key + ".scoreIntervals")) return;

        ConfigurationSection onGameEnd = plugin.getConfig().getConfigurationSection("gameBox.gameButtons." + key + ".scoreIntervals");
        for (String key : onGameEnd.getKeys(false)) {
            int keyInt;
            try {
                keyInt = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning(plugin.lang.PREFIX + " NumberFormatException while getting the rewards from config!");
                continue;
            }
            if (onGameEnd.isSet(key + ".money") && (onGameEnd.isDouble(key + ".money") || onGameEnd.isInt(key + ".money"))) {
                moneyRewards.put(keyInt, onGameEnd.getDouble(key + ".money"));
            } else {
                moneyRewards.put(keyInt, 0.);
            }
            if (onGameEnd.isSet(key + ".tokens") && (onGameEnd.isDouble(key + ".tokens") || onGameEnd.isInt(key + ".tokens"))) {
                tokenRewards.put(keyInt, onGameEnd.getInt(key + ".tokens"));
            } else {
                tokenRewards.put(keyInt, 0);
            }
        }
    }

    public double getCost() {
        return cost;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return key;
    }

    public Map<Integer,Double> getMoneyRewards() {
        return moneyRewards;
    }

    public Map<Integer,Integer> getTokenRewards() {
        return tokenRewards;
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

    public enum GameMode{
        CLASSIC, FULLINVENTORY;
    }
}
