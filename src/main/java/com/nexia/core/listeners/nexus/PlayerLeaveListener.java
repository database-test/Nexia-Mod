package com.nexia.core.listeners.nexus;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.nexus.api.event.player.PlayerDisconnectEvent;

public class PlayerLeaveListener {
    public void registerListener() {
        PlayerDisconnectEvent.BACKEND.register(playerDisconnectEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerDisconnectEvent.getPlayer());
            processDisconnect(player);

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



    private static void processDisconnect(NexiaPlayer player){
        /*
        if (BwUtil.isInBedWars(player)) BwPlayerEvents.leaveInBedWars(player);
        else if (FfaUtil.isFfaPlayer(player)) {
            FfaUtil.leaveOrDie(player, player.unwrap().getLastDamageSource(), true);
        }
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY) DuelGameHandler.leave(player, true);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.SKYWARS) SkywarsGame.leave(player);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC) OitcGame.leave(player);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.FOOTBALL) FootballGame.leave(player);
        else if (player.hasTag("duels")) DuelGameHandler.leave(player, true);
         */

        player.leaveAllGames();

        com.nexia.ffa.classic.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.kits.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.uhc.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.sky.utilities.player.PlayerDataManager.removePlayerData(player);

        com.nexia.discord.utilities.player.PlayerDataManager.removePlayerData(player.getUUID());
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.oitc.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.football.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.skywars.util.player.PlayerDataManager.removePlayerData(player);

        PlayerDataManager.removePlayerData(player);
    }
}