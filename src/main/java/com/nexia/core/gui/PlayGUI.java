package com.nexia.core.gui;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import com.nexia.minigames.games.skywars.SkywarsGame;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayGUI extends SimpleGui {

    static final TextComponent title = new TextComponent("Game Menu");
    public PlayGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 9; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){

        int players = FfaAreas.ffaWorld.players().size();
        players = players + com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld.players().size();
        players = players + com.nexia.ffa.pot.utilities.FfaAreas.ffaWorld.players().size();
        players = players + com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld.players().size();

        ItemStack ffa = new ItemStack(Items.NETHERITE_SWORD, 1);
        ffa.setHoverName(new TextComponent("§3FFA"));
        ItemDisplayUtil.addGlint(ffa);
        ffa.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(ffa, "§5", 0);
        ItemDisplayUtil.addLore(ffa, "§7Fight players in a huge landscape", 1);
        ItemDisplayUtil.addLore(ffa, "§7be the best player.", 2);
        ItemDisplayUtil.addLore(ffa, "§f", 3);
        ItemDisplayUtil.addLore(ffa, "§3◆ There are " + players + " people playing this gamemode.", 4);

        ItemStack hub = new ItemStack(Items.DRAGON_BREATH, 1);
        hub.setHoverName(new TextComponent("§5Hub"));
        hub.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        ItemDisplayUtil.addLore(hub, "§7Return back to the hub.", 0);

        ItemStack bedwars = new ItemStack(Items.RED_BED, 1);
        bedwars.setHoverName(new TextComponent("§cBedwars"));
        ItemDisplayUtil.addGlint(bedwars);
        bedwars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(bedwars, "§5", 0);
        ItemDisplayUtil.addLore(bedwars, "§7Protect your bed and", 1);
        ItemDisplayUtil.addLore(bedwars, "§7destroy other's beds, kill your", 2);
        ItemDisplayUtil.addLore(bedwars, "§7opponents to win!", 3);
        ItemDisplayUtil.addLore(bedwars, "§f", 4);
        ItemDisplayUtil.addLore(bedwars, "§c◆ There are " + BwAreas.bedWarsWorld.players().size() + " people playing this gamemode.", 5);

        ItemStack duels = new ItemStack(Items.NETHERITE_AXE, 1);
        duels.setHoverName(new TextComponent("§9Duels"));
        ItemDisplayUtil.addGlint(duels);
        duels.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        int duelsPlayers = 0;
        for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            DuelGameMode gameMode = PlayerDataManager.get(serverPlayer).gameMode;
            if(gameMode != null && (gameMode != DuelGameMode.LOBBY && gameMode != DuelGameMode.SPECTATING)) duelsPlayers++;
        }

        ItemDisplayUtil.addLore(duels, "§5", 0);
        ItemDisplayUtil.addLore(duels, "§7Duel against other people", 1);
        ItemDisplayUtil.addLore(duels, "§7or play against people in teams", 2);
        ItemDisplayUtil.addLore(duels, "§7with team duels!", 3);
        ItemDisplayUtil.addLore(duels, "§f", 4);
        ItemDisplayUtil.addLore(duels, "§9◆ There are " + (LobbyUtil.lobbyWorld.players().size() + duelsPlayers) + " people playing this gamemode.", 5);

        ItemStack skywars = new ItemStack(Items.GRASS_BLOCK, 1);
        skywars.setHoverName(new TextComponent("§aSkywars"));
        skywars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(skywars, "§5", 0);
        ItemDisplayUtil.addLore(skywars, "§7Battle against others on", 1);
        ItemDisplayUtil.addLore(skywars, "§7sky islands and be the", 2);
        ItemDisplayUtil.addLore(skywars, "§7last one standing to win!", 3);
        ItemDisplayUtil.addLore(skywars, "§f", 4);
        ItemDisplayUtil.addLore(skywars, "§a◆ There are " + SkywarsGame.world.players().size() + " people playing this gamemode.", 5);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        this.setSlot(0, skywars);
        this.setSlot(2, ffa);
        this.setSlot(4, hub);
        this.setSlot(6, duels);
        this.setSlot(8, bedwars);
    }

    private void setFFALayout(){
        ItemStack classic = new ItemStack(Items.NETHERITE_SWORD, 1);
        classic.setHoverName(new TextComponent("§cClassic FFA"));
        ItemDisplayUtil.addGlint(classic);
        classic.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(classic, "§5", 0);
        ItemDisplayUtil.addLore(classic, "§7The classic snapshot", 1);
        ItemDisplayUtil.addLore(classic, "§7Free For All gamemode.", 2);
        ItemDisplayUtil.addLore(classic, "§f", 3);
        ItemDisplayUtil.addLore(classic, "§c◆ There are " + FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);

        ItemStack kit = new ItemStack(Items.DIAMOND_SWORD, 1);
        kit.setHoverName(new TextComponent("§bKit FFA"));
        ItemDisplayUtil.addGlint(classic);
        kit.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(kit, "§5", 0);
        ItemDisplayUtil.addLore(kit, "§7Fight against players", 1);
        ItemDisplayUtil.addLore(kit, "§7with various kits!", 2);
        ItemDisplayUtil.addLore(kit, "§f", 3);
        ItemDisplayUtil.addLore(kit, "§b◆ There are " + com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);



        ItemStack pot = new ItemStack(Items.POTION, 1);
        pot.setHoverName(new TextComponent("§dPot FFA"));
        ItemDisplayUtil.addGlint(pot);
        pot.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(pot, "§5", 0);
        ItemDisplayUtil.addLore(pot, "§7The classic snapshot", 1);
        ItemDisplayUtil.addLore(pot, "§7Free For All gamemodes.", 2);
        ItemDisplayUtil.addLore(pot, "§5", 3);
        ItemDisplayUtil.addLore(pot, "§d◆ There are " + com.nexia.ffa.pot.utilities.FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);



        ItemStack uhc = new ItemStack(Items.GOLDEN_APPLE, 1);
        uhc.setHoverName(new TextComponent("§6UHC FFA"));
        ItemDisplayUtil.addGlint(classic);
        uhc.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(uhc, "§5", 0);
        ItemDisplayUtil.addLore(uhc, "§7The classic snapshot", 1);
        ItemDisplayUtil.addLore(uhc, "§7Free For All gamemodes.", 2);
        ItemDisplayUtil.addLore(uhc, "§f", 3);
        ItemDisplayUtil.addLore(uhc, "§6◆ There are " + com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        this.setSlot(1, classic);
        this.setSlot(3, uhc);
        this.setSlot(5, pot);
        this.setSlot(7, kit);
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();
            if(name.getString().equalsIgnoreCase("§cClassic FFA")){
                LobbyUtil.sendGame(this.player, "classic ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§bKit FFA")){
                LobbyUtil.sendGame(this.player, "kits ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§dPot FFA")){
                LobbyUtil.sendGame(this.player, "pot ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§6UHC FFA")){
                LobbyUtil.sendGame(this.player, "uhc ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§9Duels")) {
                LobbyUtil.sendGame(this.player, "duels", true, true);
            }

            if(name.getString().equalsIgnoreCase("§cBedwars")){
                LobbyUtil.sendGame(this.player, "bedwars", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§aSkywars")){
                LobbyUtil.sendGame(this.player, "skywars", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§3FFA")){
                this.setFFALayout();
            }

            if(name.getString().toLowerCase().equalsIgnoreCase("§5Hub")){
                LobbyUtil.leaveAllGames(this.player, true);
            }

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR && itemStack.getItem() != Items.NETHERITE_SWORD && itemStack.getItem() != Items.COMPASS){
                this.close();
            }
        }
        return super.click(index, clickType, action);
    }
    public static void openMainGUI(ServerPlayer player) {
        PlayGUI shop = new PlayGUI(MenuType.GENERIC_9x1, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
    }
}