package com.nexia.core.listeners.factory;

import com.combatreforged.metis.api.event.player.PlayerUseItemEvent;
import com.combatreforged.metis.api.world.item.ItemStack;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerDataManager;
import net.kyori.adventure.text.Component;

public class UseItemListener {
    public static void registerListener() {
        PlayerUseItemEvent.BACKEND.register(playerUseItemEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerUseItemEvent.getPlayer());

            ItemStack itemStack = playerUseItemEvent.getItemStack();

            Component name = itemStack.getDisplayName();

            PlayerGameMode gameMode = PlayerDataManager.get(player).gameMode;
            String sName = name.toString().toLowerCase();

            if(gameMode == PlayerGameMode.LOBBY) {
                if(sName.contains("gamemode selector")){
                    //PlayGUI.openMainGUI(minecraftPlayer);
                    player.runCommand("/play", 0, false);
                    return;
                }

                if(sName.contains("prefix selector")){
                    //PrefixGUI.openRankGUI(minecraftPlayer);
                    player.runCommand("/prefix", 0, false);
                    return;
                }

                if(sName.contains("duel sword") && !sName.contains("custom duel sword")) {
                    //QueueGUI.openQueueGUI(minecraftPlayer);
                    player.runCommand("/queue", 0, false);
                    return;
                }

                if(sName.contains("team axe")) {
                    player.runCommand("/party list");
                }
            }
        });
    }
}
