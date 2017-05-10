package me.nikl.whacamole;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Random;

/**
 * Created by Niklas on 14.04.2017.
 *
 * Game
 */
public class Game {

    private Main plugin;

    private GameRules rule;
    private boolean playSounds;
    private Language lang;

    private Inventory inventory;

    private Player player;

    private Random random;

    private Sounds gameOver = Sounds.ANVIL_LAND;

    private float volume = 0.5f, pitch= 1f;

    private int score = 0;


    public Game(GameRules rule, Main plugin, Player player, boolean playSounds){
        this.plugin = plugin;
        this.lang = plugin.lang;
        this.rule = rule;
        this.player = player;
        this.random = new Random(System.currentTimeMillis());

        // only play sounds if the game setting allows to
        this.playSounds = plugin.getPlaySounds() && playSounds;

        // create inventory
        this.inventory = Bukkit.createInventory(null, 54, lang.GAME_TITLE.replace("%score%", String.valueOf(score)));

        player.openInventory(inventory);

    }



}
