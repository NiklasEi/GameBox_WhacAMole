package me.nikl.whacamole;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.game.IGameManager;
import me.nikl.gamebox.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Niklas.
 *
 * Whac A Mole GameManager
 */

public class GameManager implements IGameManager {
    private Main plugin;

    private Map<UUID, Game> games = new HashMap<>();
    private Language lang;

    private Map<String,GameRules> gameTypes;

    private Map<String, ItemStack> items = new HashMap<>();



    public GameManager(Main plugin){
        this.plugin = plugin;
        this.lang = plugin.lang;

        ItemStack item = ItemStackUtil.getItemStack(plugin.getConfig().getString("items.creeper", "SKULL_ITEM:4"));
        if(item == null){
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
        }
        items.put("creeper", item);

        item = ItemStackUtil.getItemStack(plugin.getConfig().getString("items.human", "SKULL_ITEM:3"));
        if(item == null){
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        }
        items.put("human", item);

        item = ItemStackUtil.getItemStack(plugin.getConfig().getString("items.mole", "LEATHER"));
        if(item == null){
            item = new ItemStack(Material.LEATHER, 1);
        }
        items.put("mole", item);

        item = ItemStackUtil.getItemStack(plugin.getConfig().getString("items.cover", "STAINED_GLASS_PANE"));
        if(item == null){
            item = new MaterialData(Material.STAINED_GLASS_PANE).toItemStack(1);
        }
        items.put("cover", item);

        item = ItemStackUtil.getItemStack(plugin.getConfig().getString("items.grass", "LONG_GRASS"));
        if(item == null){
            item = new MaterialData(Material.LONG_GRASS).toItemStack(1);
        }
        items.put("grass", item);
    }


    @Override
    public boolean onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if(!games.keySet().contains(inventoryClickEvent.getWhoClicked().getUniqueId())) return false;

        Game game = games.get(inventoryClickEvent.getWhoClicked().getUniqueId());

        game.onClick(inventoryClickEvent);
        return true;
    }


    @Override
    public boolean onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        if(!games.keySet().contains(inventoryCloseEvent.getPlayer().getUniqueId())) return false;

        // do same stuff as on removeFromGame()
        removeFromGame(inventoryCloseEvent.getPlayer().getUniqueId());
        return true;
    }


    @Override
    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }


    @Override
    public int startGame(Player[] players, boolean playSounds, String... strings) {
        if (strings.length != 1) {
            Bukkit.getLogger().log(Level.WARNING, " unknown number of arguments to start a game: " + Arrays.asList(strings));
            return GameBox.GAME_NOT_STARTED_ERROR;
        }

        GameRules rule = gameTypes.get(strings[0]);

        if (rule == null) {
            Bukkit.getLogger().log(Level.WARNING, " unknown gametype: " + Arrays.asList(strings));
            return GameBox.GAME_NOT_STARTED_ERROR;
        }

        if (!pay(players, rule.getCost())) {
            return GameBox.GAME_NOT_ENOUGH_MONEY;
        }

        games.put(players[0].getUniqueId(), new Game(rule, plugin, players[0], playSounds,items));
        return GameBox.GAME_STARTED;
    }


    @Override
    public void removeFromGame(UUID uuid) {

        Game game = games.get(uuid);

        if(game == null) return;

        game.onGameEnd();
        game.cancel();

        games.remove(uuid);
    }


    public void setGameTypes(Map<String, GameRules> gameTypes) {
        this.gameTypes = gameTypes;
    }


    private boolean pay(Player[] player, double cost) {
        if (plugin.isEconEnabled() && !player[0].hasPermission(Permissions.BYPASS_ALL.getPermission()) && !player[0].hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && cost > 0.0) {
            if (Main.econ.getBalance(player[0]) >= cost) {
                Main.econ.withdrawPlayer(player[0], cost);
                player[0].sendMessage(GameBox.chatColor(lang.PREFIX + plugin.lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
                return true;
            } else {
                player[0].sendMessage(GameBox.chatColor(lang.PREFIX + plugin.lang.GAME_NOT_ENOUGH_MONEY));
                return false;
            }
        } else {
            return true;
        }
    }
}
