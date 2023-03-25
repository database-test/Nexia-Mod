package com.nexia.minigames.games.bedwars.upgrades;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.RomanNumbers;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import com.nexia.minigames.games.bedwars.shop.BwShop;
import com.nexia.minigames.games.bedwars.util.BwGen;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;

public class BwUpgradeShop extends SimpleGui {

    final static TextComponent title = new TextComponent("Upgrade Shop");

    public BwUpgradeShop(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    public static void openShopGui(ServerPlayer player) {
        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team == null) return;

        BwUpgradeShop shop = new BwUpgradeShop(MenuType.GENERIC_9x5, player, false);
        shop.setTitle(title);
        shop.resetLayout(team);
        shop.open();
    }

    private void resetLayout(BwTeam team) {
        // Upgrades
        HashMap<String, BwUpgrade> upgrades = team.upgrades;
        for (String key : upgrades.keySet()) {
            BwUpgrade upgrade = upgrades.get(key);
            ItemStack displayItem = upgrade.displayItem.copy();
            addUpgradeCostLore(displayItem, upgrade);
            addStatusLore(displayItem, upgrade);
            if (key.equals(BwUpgrade.UPGRADE_KEY_GENERATOR)) addGenUpgradeText(displayItem, team);
            this.setSlot(rowColumnToGuiSlot(upgrade.displayRow, upgrade.displayColumn), displayItem);
        }
        // Traps
        HashMap<String, BwTrap> traps = team.traps;
        for (String key : traps.keySet()) {
            BwTrap trap = traps.get(key);
            ItemStack displayItem = trap.displayItem.copy();
            addTrapCostLore(displayItem, team, trap);
            this.setSlot(rowColumnToGuiSlot(trap.displayRow, trap.displayColumn), displayItem);
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            if (clickType != ClickType.MOUSE_DOUBLE_CLICK) {

                ItemStack itemStack = element.getItemStack();
                CompoundTag tag = itemStack.getOrCreateTag();

                if (tag.contains(BwUpgrade.UPGRADE_TAG_KEY)) {
                    purchaseUpgrade(player, itemStack.copy());
                } else if (tag.contains(BwTrap.TRAP_TAG_KEY)) {
                    purchaseTrap(player, itemStack.copy());
                }

            }
        }
        return super.click(index, clickType, action);
    }

    private void purchaseUpgrade(ServerPlayer player, ItemStack upgradeItem) {
        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team == null) return;

        CompoundTag tag = upgradeItem.getOrCreateTag();
        String upgradesMapKey = tag.getString(BwUpgrade.UPGRADE_TAG_KEY);
        BwUpgrade upgrade = team.upgrades.get(upgradesMapKey);
        if (upgrade == null) return;

        if (upgrade.level >= upgrade.costs.length) {
            BwShop.sendFail(player, "You have reached the maximum level of this upgrade");
            return;
        }
        if (player.inventory.countItem(Items.DIAMOND) < upgrade.costs[upgrade.level]) {
            BwShop.sendFail(player, "You can't afford this upgrade");
            return;
        }

        PlayerUtil.removeItem(player, Items.DIAMOND, upgrade.costs[upgrade.level]);
        upgrade.level++;
        this.resetLayout(team);
        BwShop.playPurchaseSound(player, false);
        PlayerUtil.broadcast(team.players, ChatFormat.brandColor1 + player.getScoreboardName() +
                ChatFormat.brandColor2 + " has purchased " +
                ChatFormat.brandColor1 + ChatFormat.removeColors(upgradeItem.getHoverName().getString()) +
                " " + RomanNumbers.intToRoman(upgrade.level));
    }

    private void purchaseTrap(ServerPlayer player, ItemStack trapItem) {
        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team == null) return;

        CompoundTag tag = trapItem.getOrCreateTag();
        String trapsMapKey = tag.getString(BwTrap.TRAP_TAG_KEY);
        BwTrap trap = team.traps.get(trapsMapKey);
        if (trap == null) return;

        if (trap.bought) {
            BwShop.sendFail(player, "You have already purchased this trap.");
            return;
        }
        int cost = getTrapCost(team);
        if (player.inventory.countItem(Items.DIAMOND) < cost) {
            BwShop.sendFail(player, "You can't afford this trap.");
            return;
        }

        PlayerUtil.removeItem(player, Items.DIAMOND, cost);
        trap.bought = true;
        this.resetLayout(team);
        BwShop.playPurchaseSound(player, false);
        PlayerUtil.broadcast(team.players, ChatFormat.brandColor1 + player.getScoreboardName() +
                ChatFormat.brandColor2 + " has purchased " +
                ChatFormat.brandColor1 + ChatFormat.removeColors(trapItem.getHoverName().getString()));
    }

    static int rowColumnToGuiSlot(int row, int column) {
        return (1 + row) * 9 + 1 + column;
    }

    static void addUpgradeCostLore(ItemStack itemStack, BwUpgrade upgrade) {
        if (upgrade.level >= upgrade.costs.length) {
            ItemDisplayUtil.addLore(itemStack, "\247aMaximum Level Reached", 0);
            ItemDisplayUtil.addGlint(itemStack);
        } else {
            int cost = upgrade.costs[upgrade.level];
            String lore = "\2477Cost:\247b " + cost + " Diamond";
            if (cost != 1) lore += "s";
            ItemDisplayUtil.addLore(itemStack, lore, 0);
        }
    }

    static void addTrapCostLore(ItemStack itemStack, BwTeam team, BwTrap trap) {
        if (trap.bought) {
            ItemDisplayUtil.addLore(itemStack, "\247aPurchased!", 0);
            ItemDisplayUtil.addGlint(itemStack);
        } else {
            int cost = getTrapCost(team);
            String lore = "\2477Cost:\247b " + cost + " Diamond";
            if (cost != 1) lore += "s";
            ItemDisplayUtil.addLore(itemStack, lore, 0);
        }
    }

    static void addStatusLore(ItemStack itemStack, BwUpgrade upgrade) {
        String lore = "\2477Status:\247f Level " + upgrade.level + "/" + upgrade.costs.length;
        ItemDisplayUtil.addLore(itemStack, lore, 1);
    }

    static void addGenUpgradeText(ItemStack itemStack, BwTeam team) {
        if (team == null || team.upgrades.get(BwUpgrade.UPGRADE_KEY_GENERATOR) == null) return;

        int currentLevel = team.upgrades.get(BwUpgrade.UPGRADE_KEY_GENERATOR).level;

        ItemDisplayUtil.addLore(itemStack, "", -1);
        addGenUpgradeLore(itemStack, currentLevel, BwGen.ironDelays, "\247fIron");
        addGenUpgradeLore(itemStack, currentLevel, BwGen.goldDelays, "\2476Gold");
        addGenUpgradeLore(itemStack, currentLevel, BwGen.emeraldDelays, "\2472Emeralds");
        ItemDisplayUtil.addLore(itemStack, "", -1);
    }

    private static void addGenUpgradeLore(ItemStack itemStack, int currentLevel, int[] delays, String currency) {
        if (currentLevel < 0 || currentLevel + 1 >= delays.length) return;

        float originalDelay = delays[0];
        for (int delay : delays) {
            if (delay != 0) {
                originalDelay = delay;
                break;
            }
        }
        float currentDelay = delays[currentLevel];
        float nextDelay = delays[currentLevel+1];
        float extraSpeed;

        if (currentDelay == nextDelay) return;
        if (currentDelay == 0) extraSpeed = 1;
        else if (nextDelay == 0) extraSpeed = originalDelay / currentDelay;
        else extraSpeed = (currentDelay / nextDelay - 1) * originalDelay / currentDelay;

        int extraPercentage = Math.round(extraSpeed * 100);
        String line = "% " + currency;
        if (extraPercentage < 0) line = "\n247e" + extraPercentage + line;
        else line = "\247e+" + extraPercentage + line;

        ItemDisplayUtil.addLore(itemStack, line, -1);
    }

    private static int getTrapCost(BwTeam team) {
        int price = 1;
        for (BwTrap trap : team.traps.values()) {
            if (trap.bought) price *= 2;
        }
        return price;
    }

}
