package me.nikl.whacamole;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.Sounds;
import me.nikl.gamebox.data.SaveType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.logging.Level;

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

    private int moleSlot = -99, humanSlot = -99;

    private Sounds gameOver = Sounds.ANVIL_LAND, hitMole = Sounds.DONKEY_HIT, hitHuman = Sounds.VILLAGER_DEATH, hitCreeper = Sounds.CREEPER_DEATH;

    private float volume = 0.5f, pitch= 1f;

    private int score = 0;

    private int time;

    private boolean decreaseTime = false;

    private ItemStack mole, grass, human, cover;

    private boolean spawnHuman = false;

    private int[] spawnLocations;

    private GameState gameState = GameState.START;


    public Game(GameRules rule, Main plugin, Player player, boolean playSounds){
        this.plugin = plugin;
        this.lang = plugin.lang;
        this.rule = rule;
        this.player = player;
        this.random = new Random(System.currentTimeMillis());

        this.time = rule.getTime();

        ItemMeta meta;

        switch (rule.getGameMode()){
            case FULLINVENTORY:
                mole = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
                meta = mole.getItemMeta();
                meta.setDisplayName(lang.GAME_CREEPER_NAME);
                mole.setItemMeta(meta);
                break;

            case CLASSIC:
            default:
                mole = new ItemStack(Material.LEATHER, 1);
                meta = mole.getItemMeta();
                meta.setDisplayName(lang.GAME_MOLE_NAME);
                mole.setItemMeta(meta);
                break;
        }

        human = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        meta = human.getItemMeta();
        meta.setDisplayName(lang.GAME_HUMAN_NAME);
        human.setItemMeta(meta);

        grass = new MaterialData(Material.LONG_GRASS).toItemStack(1);
        grass.setDurability((short) 1);
        meta = grass.getItemMeta();
        meta.setDisplayName(" ");
        grass.setItemMeta(meta);

        cover = new MaterialData(Material.STAINED_GLASS_PANE).toItemStack(1);
        meta = cover.getItemMeta();
        meta.setDisplayName(" ");
        cover.setItemMeta(meta);

        // only play sounds if the game setting allows to
        this.playSounds = plugin.getPlaySounds() && playSounds;

        // create inventory
        this.inventory = Bukkit.createInventory(null, 54, lang.GAME_TITLE_START.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));

        if(rule.getGameMode() == GameRules.GameMode.FULLINVENTORY) spawnHuman = true;

        buildInv();

        player.openInventory(inventory);


        runTaskTimer(plugin, 0, intervall);
    }

    private void buildInv() {
        switch (rule.getGameMode()) {
            case FULLINVENTORY:
                spawnLocations = new int[inventory.getSize()];
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, grass);
                    spawnLocations[i] = i;
                }
                break;

            case CLASSIC:
                spawnLocations = new int[4];
                int counter = 0;
                for (int i = 0; i < inventory.getSize(); i++) {
                    if(i == 12 || i == 30 || i == 14 || i == 32){
                        spawnLocations[counter] = i;
                        counter++;
                    } else {
                        inventory.setItem(i, cover);
                    }
                }
                break;

            default:
                plugin.getLogger().log(Level.SEVERE, " Unknown WAM GameMode");
                break;
        }
    }



    public void onClick(InventoryClickEvent inventoryClickEvent) {
        if(this.gameState == GameState.GAME_OVER) return;

        if(this.gameState == GameState.START){
            gameState = GameState.PLAY;
            plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
            return;
        }

        if(inventoryClickEvent.getRawSlot() != moleSlot && inventoryClickEvent.getRawSlot() != humanSlot) return;

        switch (rule.getGameMode()){
            case FULLINVENTORY:
                if(inventoryClickEvent.getRawSlot() == moleSlot) {
                    score++;
                    inventory.setItem(moleSlot, grass);
                    if(playSounds)player.playSound(player.getLocation(), hitCreeper.bukkitSound(), volume, pitch);
                    plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                    moleSlot = -99;
                } else {
                    // human was hit
                    if(rule.isGameOverOnHittingHuman()){
                        onGameEnd();
                        if(playSounds)player.playSound(player.getLocation(), gameOver.bukkitSound(), volume, pitch);
                        plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE_LOST.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                    } else {
                        score = Math.max(score - rule.getPunishmentOnHittingHuman(), 0);
                        if(playSounds)player.playSound(player.getLocation(), hitHuman.bukkitSound(), volume, pitch);
                        plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                    }
                    inventory.setItem(humanSlot, grass);
                    humanSlot = -99;
                }

                return;

            case CLASSIC:
                score++;
                inventory.setItem(moleSlot, null);
                if(playSounds)player.playSound(player.getLocation(), hitMole.bukkitSound(), volume, pitch);
                plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                moleSlot = -99;

                return;
        }
    }

    @Override
    public void run() {
        if(this.gameState != GameState.PLAY) return;

        if(decreaseTime) time --;
        decreaseTime = !decreaseTime;
        if(time < 1){
            onGameEnd();
            if(playSounds)player.playSound(player.getLocation(), gameOver.bukkitSound(), volume, pitch);
            plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE_LOST.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
            return;
        }
        plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));


        if (moleSlot < 0) {
            if (random.nextDouble() < 0.25) {
                moleSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                while (humanSlot == moleSlot) {
                    moleSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                }
                inventory.setItem(moleSlot, mole);
            }
        } else {
            if (random.nextDouble() < 0.45) {
                inventory.setItem(moleSlot, rule.getGameMode() == GameRules.GameMode.FULLINVENTORY? grass : null);
                moleSlot = -99;
            }
        }

        if (spawnHuman) {
            if (humanSlot < 0) {
                if (random.nextDouble() < 0.1) {
                    humanSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                    while (humanSlot == moleSlot) {
                        humanSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                    }
                    inventory.setItem(humanSlot, human);
                }
            } else {
                if (random.nextDouble() < 0.3) {
                    inventory.setItem(humanSlot, rule.getGameMode() == GameRules.GameMode.FULLINVENTORY? grass : null);
                    humanSlot = -99;
                }
            }
        }
    }

    public void onGameEnd() {
        if(gameState == GameState.GAME_OVER) return;

        int key = getKey();

        double reward = key >= 0 ? rule.getMoneyRewards().get(key) : 0.;
        int token = key >= 0 ? rule.getTokenRewards().get(key) : 0;

        if(plugin.isEconEnabled() && reward > 0 && !player.hasPermission(Permissions.BYPASS_ALL.getPermission()) && !player.hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID))){
            Main.econ.depositPlayer(player, reward);
            player.sendMessage(lang.PREFIX + lang.GAME_WON_MONEY.replace("%reward%", String.valueOf(reward)).replace("%score%", String.valueOf(score)));
        } else {
            player.sendMessage(lang.PREFIX + lang.GAME_WON.replace("%score%", String.valueOf(score)));
        }
        if(rule.isSaveStats()){
            plugin.gameBox.getStatistics().addStatistics(player.getUniqueId(), Main.gameID, rule.getKey(), score, SaveType.SCORE);
        }
        if(token > 0){
            plugin.gameBox.wonTokens(player.getUniqueId(), token, Main.gameID);
        }

        gameState = GameState.GAME_OVER;
    }

    private int getKey(){
        int distance = -1;
        for(int key : rule.getMoneyRewards().keySet()) {
            if((score - key) >= 0 && (distance < 0 || distance > (score - key))){
                distance = score - key;
            }
        }
        if(distance > -1)
            return score - distance;
        return -1;
    }
}
