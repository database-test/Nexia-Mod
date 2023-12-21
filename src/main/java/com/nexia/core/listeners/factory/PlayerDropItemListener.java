package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerHotbarDropItemEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDropItemListener {
    public static void registerListener(){
        PlayerHotbarDropItemEvent.BACKEND.register(playerDropItemEvent -> {

            Player player = playerDropItemEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            if (FfaUtil.isFfaPlayer(minecraftPlayer)) {
                playerDropItemEvent.setCancelled(true);
                return;
            }

            if (BwUtil.isBedWarsPlayer(minecraftPlayer)) {
                if (!BwUtil.canDropItem(playerDropItemEvent.getItemStack())) {
                    ItemStackUtil.sendInventoryRefreshPacket(minecraftPlayer);
                    playerDropItemEvent.setCancelled(true);
                    return;
                }
            }

            if(LobbyUtil.isLobbyWorld(minecraftPlayer.getLevel())){
                playerDropItemEvent.setCancelled(true);
                return;
            }
        });
    }
}
