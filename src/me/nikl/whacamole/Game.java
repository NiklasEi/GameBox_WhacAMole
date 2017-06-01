package me.nikl.whacamole;

import me.nikl.gamebox.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import sun.plugin2.main.server.Plugin;

import java.util.Random;

/**
 * Created by Niklas on 14.04.2017.
 *
 * Game
 */
public class Game extends BukkitRunnable {

    private final long intervall = 10;

    private Main plugin;

    private GameRules rule;
    private boolean playSounds;
    private Language lang;

    private Inventory inventory;

    private Player player;

    private Random random;

    private int creeper = -99, human = -99;

    private Sounds gameOver = Sounds.ANVIL_LAND;

    private float volume = 0.5f, pitch= 1f;

    private int score = 0;

    private ItemStack skull, cover, head;


    public Game(GameRules rule, Main plugin, Player player, boolean playSounds){
        this.plugin = plugin;
        this.lang = plugin.lang;
        this.rule = rule;
        this.player = player;
        this.random = new Random(System.currentTimeMillis());

        skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Creeper");
        skull.setItemMeta(meta);

        head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        meta = head.getItemMeta();
        meta.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Human");
        head.setItemMeta(meta);

        cover = new MaterialData(Material.LONG_GRASS).toItemStack(1);
        meta = cover.getItemMeta();
        meta.setDisplayName(" ");
        cover.setItemMeta(meta);

        // only play sounds if the game setting allows to
        this.playSounds = plugin.getPlaySounds() && playSounds;

        // create inventory
        this.inventory = Bukkit.createInventory(null, 54, lang.GAME_TITLE.replace("%score%", String.valueOf(score)));

        buildInv();

        player.openInventory(inventory);


        runTaskTimer(plugin, 0, intervall);
    }

    private void buildInv() {
        for(int i = 0 ; i < inventory.getSize(); i++){
            inventory.setItem(i, cover);
        }
    }



    public void onClick(InventoryClickEvent inventoryClickEvent) {
        if(inventoryClickEvent.getRawSlot() != creeper) return;

        score++;
        inventory.setItem(creeper, cover);
        plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)));
        creeper = -99;
    }

    @Override
    public void run() {
        Bukkit.getConsoleSender().sendMessage("running...");
        if(creeper < 0){
            if(random.nextDouble() > 0.5){
                creeper = random.nextInt(inventory.getSize());
                inventory.setItem(creeper, skull);
            }
        } else {
            if(random.nextDouble() > 0.5){
                inventory.setItem(creeper, cover);
                creeper = - 99;
            }
        }

        if(human < 0){
            if(random.nextDouble() > 0.9){
                human = random.nextInt(inventory.getSize());
                while (human == creeper){
                    human = random.nextInt(inventory.getSize());
                }
                inventory.setItem(human, head);
            }
        } else {
            if(random.nextDouble() > 0.3){
                inventory.setItem(human, cover);
                human = - 99;
            }
        }
    }
}
