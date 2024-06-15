package com.nexia.core.listeners.nexus;

import com.nexia.nexus.api.event.player.PlayerDisconnectEvent;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.server.level.ServerPlayer;

public class PlayerLeaveListener {
    public static void registerListener() {
        PlayerDisconnectEvent.BACKEND.register(playerDisconnectEvent -> {

            Player player = playerDisconnectEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            processDisconnect(player, minecraftPlayer);

            /*
            if(Main.config.events.statusMessages){
                playerDisconnectEvent.setLeaveMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("-").color(ChatFormat.failColor)
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getRawName()).color(ChatFormat.failColor)))
                );
            }

             */
        });
    }



    private static void processDisconnect(Player player, ServerPlayer minecraftPlayer){

        if (BwUtil.isInBedWars(minecraftPlayer)) BwPlayerEvents.leaveInBedWars(minecraftPlayer);
        else if (FfaUtil.isFfaPlayer(minecraftPlayer)) {
            FfaUtil.leaveOrDie(minecraftPlayer, minecraftPlayer.getLastDamageSource(), true);
        }
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.LOBBY) DuelGameHandler.leave(minecraftPlayer, true);
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.SKYWARS) SkywarsGame.leave(minecraftPlayer);
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.OITC) OitcGame.leave(minecraftPlayer);
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.FOOTBALL) FootballGame.leave(minecraftPlayer);
        else if (minecraftPlayer.getTags().contains("duels")) DuelGameHandler.leave(minecraftPlayer, true);

        com.nexia.ffa.classic.utilities.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.ffa.kits.utilities.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.ffa.uhc.utilities.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.ffa.sky.utilities.player.PlayerDataManager.removePlayerData(minecraftPlayer);

        com.nexia.discord.utilities.player.PlayerDataManager.removePlayerData(minecraftPlayer.getUUID());
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.minigames.games.oitc.util.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.minigames.games.football.util.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.minigames.games.skywars.util.player.PlayerDataManager.removePlayerData(minecraftPlayer);


        //LobbyUtil.leaveAllGames(minecraftPlayer, true);

        PlayerDataManager.removePlayerData(minecraftPlayer);
    }
}