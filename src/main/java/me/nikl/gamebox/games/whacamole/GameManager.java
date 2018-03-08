package me.nikl.gamebox.games.whacamole;

import me.nikl.gamebox.game.exceptions.GameStartException;
import me.nikl.gamebox.game.manager.EasyManager;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.utility.ItemStackUtility;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Niklas.
 *
 * Whac A Mole GameManager
 */
public class GameManager extends EasyManager {
    private WhacAMole whacAMole;
    private Map<UUID, Game> games = new HashMap<>();
    private Language lang;
    private Map<String, GameRules> gameTypes = new HashMap<>();
    private Map<String, ItemStack> items = new HashMap<>();

    public GameManager(WhacAMole whacAMole) {
        this.whacAMole = whacAMole;
        this.lang = (Language) whacAMole.getGameLang();
        loadItems();
    }

    private void loadItems() {
        ItemStack item = ItemStackUtility.getItemStack(whacAMole.getConfig().getString("items.creeper", "SKULL_ITEM:4"));
        if (item == null) {
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
        }
        items.put("creeper", item);
        item = ItemStackUtility.getItemStack(whacAMole.getConfig().getString("items.human", "SKULL_ITEM:3"));
        if (item == null) {
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        }
        items.put("human", item);
        item = ItemStackUtility.getItemStack(whacAMole.getConfig().getString("items.mole", "LEATHER"));
        if (item == null) {
            item = new ItemStack(Material.LEATHER, 1);
        }
        items.put("mole", item);
        item = ItemStackUtility.getItemStack(whacAMole.getConfig().getString("items.cover", "STAINED_GLASS_PANE"));
        if (item == null) {
            item = new MaterialData(Material.STAINED_GLASS_PANE).toItemStack(1);
        }
        items.put("cover", item);
        item = ItemStackUtility.getItemStack(whacAMole.getConfig().getString("items.grass", "LONG_GRASS"));
        if (item == null) {
            item = new MaterialData(Material.LONG_GRASS).toItemStack(1);
        }
        items.put("grass", item);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if (!games.keySet().contains(inventoryClickEvent.getWhoClicked().getUniqueId())) return;
        Game game = games.get(inventoryClickEvent.getWhoClicked().getUniqueId());
        game.onClick(inventoryClickEvent);
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        if (!games.keySet().contains(inventoryCloseEvent.getPlayer().getUniqueId())) return;
        removeFromGame(inventoryCloseEvent.getPlayer().getUniqueId());
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }

    @Override
    public void startGame(Player[] players, boolean playSounds, String... strings) throws GameStartException {
        if (strings.length != 1) {
            whacAMole.warn(" unknown number of arguments to start a game: " + Arrays.asList(strings));
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        GameRules rule = gameTypes.get(strings[0]);
        if (rule == null) {
            whacAMole.warn(" unknown gametype: " + Arrays.asList(strings));
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        if (!whacAMole.payIfNecessary(players[0], rule.getCost())) {
            throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY);
        }
        games.put(players[0].getUniqueId(), new Game(rule, whacAMole, players[0], playSounds, items));
    }

    @Override
    public void removeFromGame(UUID uuid) {
        Game game = games.get(uuid);
        if (game == null) return;
        game.onGameEnd();
        game.cancel();
        games.remove(uuid);
    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        GameRules.GameMode gameMode;
        try {
            gameMode = GameRules.GameMode.valueOf(buttonSec.getString("gameMode", "classic").toUpperCase());
        } catch (IllegalArgumentException exception) {
            gameMode = GameRules.GameMode.CLASSIC;
        }
        int time = buttonSec.getInt("time", 60);
        if (time < 1) time = 60;
        double cost = buttonSec.getDouble("cost", 0.);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        GameRules rule = new GameRules(whacAMole, gameMode, buttonID, cost, time, saveStats);
        if (gameMode == GameRules.GameMode.FULLINVENTORY) {
            rule.setGameOverOnHittingHuman(buttonSec.getBoolean("gameOverOnHittingHuman", false));
            rule.setPunishmentOnHittingHuman(buttonSec.getInt("punishmentOnHittingHuman", 5));
        }
        gameTypes.put(buttonID, rule);
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameTypes;
    }
}
