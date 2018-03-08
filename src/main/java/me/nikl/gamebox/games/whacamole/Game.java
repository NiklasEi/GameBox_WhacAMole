package me.nikl.gamebox.games.whacamole;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.WhacAMolePlugin;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;

/**
 * Created by Niklas on 14.04.2017.
 *
 * Game
 */
public class Game extends BukkitRunnable {
    private final long intervall = 5;
    private WhacAMole plugin;
    private GameRules rule;
    private boolean playSounds;
    private Language lang;
    private Inventory inventory;
    private Player player;
    private Random random;
    private int moleSlot = -99, humanSlot = -99;
    private Sound gameOver = Sound.ANVIL_LAND, hitMole = Sound.DONKEY_HIT, hitHuman = Sound.VILLAGER_DEATH, hitCreeper = Sound.CREEPER_HISS;
    private float volume = 0.5f, pitch = 1f;
    private int score = 0;
    private int time;
    private int counter = 0;
    private ItemStack mole, grass, human, cover;
    private boolean spawnHuman = false;
    private int[] spawnLocations;
    private GameState gameState = GameState.START;

    public Game(GameRules rule, WhacAMole plugin, Player player, boolean playSounds, Map<String, ItemStack> items) {
        this.plugin = plugin;
        this.lang = (Language) plugin.getGameLang();
        this.rule = rule;
        this.player = player;
        this.random = new Random(System.currentTimeMillis());
        this.time = rule.getTime();
        ItemMeta meta;
        switch (rule.getGameMode()) {
            case FULLINVENTORY:
                mole = items.get("creeper");
                meta = mole.getItemMeta();
                meta.setDisplayName(lang.GAME_CREEPER_NAME);
                mole.setItemMeta(meta);
                break;
            case CLASSIC:
            default:
                mole = items.get("mole");
                meta = mole.getItemMeta();
                meta.setDisplayName(lang.GAME_MOLE_NAME);
                mole.setItemMeta(meta);
                break;
        }
        human = items.get("human");
        meta = human.getItemMeta();
        meta.setDisplayName(lang.GAME_HUMAN_NAME);
        human.setItemMeta(meta);

        grass = items.get("grass");
        grass.setDurability((short) 1);
        meta = grass.getItemMeta();
        meta.setDisplayName(" ");
        grass.setItemMeta(meta);

        cover = items.get("cover");
        meta = cover.getItemMeta();
        meta.setDisplayName(" ");
        cover.setItemMeta(meta);
        this.playSounds = plugin.getSettings().isPlaySounds() && playSounds;
        String title = lang.GAME_TITLE_START.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time));
        this.inventory = plugin.createInventory(54, title);
        if (rule.getGameMode() == GameRules.GameMode.FULLINVENTORY) spawnHuman = true;
        buildInv();
        player.openInventory(inventory);
        runTaskTimer(plugin.getGameBox(), 0, intervall);
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
                    if (i == 19 || i == 21 || i == 23 || i == 25) {
                        spawnLocations[counter] = i;
                        counter++;
                    } else {
                        inventory.setItem(i, cover);
                    }
                }
                break;
            default:
                plugin.warn(" Unknown WAM GameMode");
                break;
        }
    }


    public void onClick(InventoryClickEvent inventoryClickEvent) {
        if (this.gameState == GameState.GAME_OVER) return;
        if (this.gameState == GameState.START) {
            gameState = GameState.PLAY;
            NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
            return;
        }
        if (inventoryClickEvent.getRawSlot() != moleSlot && inventoryClickEvent.getRawSlot() != humanSlot) return;
        switch (rule.getGameMode()) {
            case FULLINVENTORY:
                if (inventoryClickEvent.getRawSlot() == moleSlot) {
                    score++;
                    inventory.setItem(moleSlot, grass);
                    if (playSounds) player.playSound(player.getLocation(), hitCreeper.bukkitSound(), volume, pitch);
                    NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                    moleSlot = -99;
                } else {
                    // human was hit
                    if (rule.isGameOverOnHittingHuman()) {
                        onGameEnd();
                        if (playSounds) player.playSound(player.getLocation(), gameOver.bukkitSound(), volume, pitch);
                        NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE_LOST.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                    } else {
                        score = Math.max(score - rule.getPunishmentOnHittingHuman(), 0);
                        if (playSounds) player.playSound(player.getLocation(), hitHuman.bukkitSound(), volume, pitch);
                        NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                    }
                    inventory.setItem(humanSlot, grass);
                    humanSlot = -99;
                }
                return;
            case CLASSIC:
                score++;
                inventory.setItem(moleSlot, null);
                if (playSounds) player.playSound(player.getLocation(), hitMole.bukkitSound(), volume, pitch);
                NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
                moleSlot = -99;

                return;
        }
    }

    @Override
    public void run() {
        if (this.gameState != GameState.PLAY) return;
        if (counter == 3) {
            time--;
            NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
            counter = 0;
        } else {
            counter++;
        }
        if (time < 1) {
            onGameEnd();
            if (playSounds) player.playSound(player.getLocation(), gameOver.bukkitSound(), volume, pitch);
            NmsFactory.getNmsUtility().updateInventoryTitle(player, lang.GAME_TITLE_LOST.replace("%score%", String.valueOf(score)).replace("%time%", String.valueOf(time)));
            return;
        }
        if (moleSlot < 0) {
            if (random.nextDouble() < 0.18) {
                moleSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                while (humanSlot == moleSlot) {
                    moleSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                }
                inventory.setItem(moleSlot, mole);
            }
        } else {
            if (random.nextDouble() < 0.225) {
                inventory.setItem(moleSlot, rule.getGameMode() == GameRules.GameMode.FULLINVENTORY ? grass : null);
                moleSlot = -99;
            }
        }
        if (spawnHuman) {
            if (humanSlot < 0) {
                if (random.nextDouble() < 0.05) {
                    humanSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                    while (humanSlot == moleSlot) {
                        humanSlot = spawnLocations[random.nextInt(spawnLocations.length)];
                    }
                    inventory.setItem(humanSlot, human);
                }
            } else {
                if (random.nextDouble() < 0.15) {
                    inventory.setItem(humanSlot, rule.getGameMode() == GameRules.GameMode.FULLINVENTORY ? grass : null);
                    humanSlot = -99;
                }
            }
        }
    }

    public void onGameEnd() {
        if (gameState == GameState.GAME_OVER) return;
        double reward = rule.getMoneyToWin(score);
        int token = rule.getTokenToWin(score);
        if (plugin.getSettings().isEconEnabled() && reward > 0 && !Permission.BYPASS_GAME.hasPermission(player, WhacAMolePlugin.WHAC_A_MOLE)) {
            GameBox.econ.depositPlayer(player, reward);
            player.sendMessage(lang.PREFIX + lang.GAME_WON_MONEY.replace("%reward%", String.valueOf(reward)).replace("%score%", String.valueOf(score)));
        } else {
            player.sendMessage(lang.PREFIX + lang.GAME_WON.replace("%score%", String.valueOf(score)));
        }
        if (rule.isSaveStats()) {
            plugin.getGameBox().getDataBase().addStatistics(player.getUniqueId(), WhacAMolePlugin.WHAC_A_MOLE, rule.getKey(), score, SaveType.SCORE);
        }
        if (token > 0) {
            plugin.getGameBox().wonTokens(player.getUniqueId(), token, WhacAMolePlugin.WHAC_A_MOLE);
        }
        gameState = GameState.GAME_OVER;
    }
}
