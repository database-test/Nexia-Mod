package com.nexia.minigames.games.duels.gamemodes;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GamemodeHandler {

    public static DuelGameMode identifyGamemode(@NotNull String gameMode) {

        if (gameMode.equalsIgnoreCase("axe")) {
            return DuelGameMode.AXE;
        }

        if (gameMode.equalsIgnoreCase("bow_only")) {
            return DuelGameMode.BOW_ONLY;
        }

        if (gameMode.equalsIgnoreCase("shield")) {
            return DuelGameMode.SHIELD;
        }

        if (gameMode.equalsIgnoreCase("pot")) {
            return DuelGameMode.POT;
        }

        if (gameMode.equalsIgnoreCase("neth_pot")) {
            return DuelGameMode.NETH_POT;
        }

        if (gameMode.equalsIgnoreCase("uhc_shield")) {
            return DuelGameMode.UHC_SHIELD;
        }

        if (gameMode.equalsIgnoreCase("hsg")) {
            return DuelGameMode.HSG;
        }

        if (gameMode.equalsIgnoreCase("skywars")) {
            return DuelGameMode.SKYWARS;
        }

        if (gameMode.equalsIgnoreCase("classic_crystal")) {
            return DuelGameMode.CLASSIC_CRYSTAL;
        }

        if (gameMode.equalsIgnoreCase("vanilla")) {
            return DuelGameMode.VANILLA;
        }

        if (gameMode.equalsIgnoreCase("smp")) {
            return DuelGameMode.SMP;
        }

        if (gameMode.equalsIgnoreCase("sword_only")) {
            return DuelGameMode.SWORD_ONLY;
        }

        if (gameMode.equalsIgnoreCase("classic")) {
            return DuelGameMode.CLASSIC;
        }

        if (gameMode.equalsIgnoreCase("hoe_only")) {
            return DuelGameMode.HOE_ONLY;
        }

        if (gameMode.equalsIgnoreCase("uhc")) {
            return DuelGameMode.UHC;
        }

        if (gameMode.equalsIgnoreCase("trident_only")) {
            return DuelGameMode.TRIDENT_ONLY;
        }

        return null;
    }

    public static boolean isInQueue(@NotNull ServerPlayer player, @NotNull DuelGameMode gameMode) {
        return gameMode.queue.contains(player);
    }

    public static void joinQueue(ServerPlayer minecraftPlayer, String stringGameMode, boolean silent) {
        if (stringGameMode.equalsIgnoreCase("lobby") || stringGameMode.equalsIgnoreCase("leave")) {
            LobbyUtil.leaveAllGames(minecraftPlayer, true);
            return;
        }

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        if (gameMode == null) {
            if (!silent) player.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            return;
        }

        PlayerData data = PlayerDataManager.get(minecraftPlayer);

        if (data.duelsTeam != null) {
            if (!silent) player.sendMessage(Component.text("You are in a team!").color(ChatFormat.failColor));
            return;
        }


        if (!silent) {
            player.sendMessage(
                    Component.text("You have queued up for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".").decoration(ChatFormat.bold, false))
            );

        }

        removeQueue(minecraftPlayer, stringGameMode, true);

        gameMode.queue.add(minecraftPlayer);
        if (gameMode.queue.size() >= 2) {
            GamemodeHandler.joinGamemode(minecraftPlayer, gameMode.queue.get(0), stringGameMode, null, false);
        }

    }

    public static void removeQueue(ServerPlayer minecraftPlayer, @Nullable String stringGameMode, boolean silent) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        if (stringGameMode != null) {
            DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
            if (gameMode == null) {
                if (!silent) player.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
                return;
            }

            gameMode.queue.remove(minecraftPlayer);

            if (!silent) {
                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("You have left the queue for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                        .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                ));
            }
        } else {
            for(DuelGameMode gameMode : DuelGameMode.duelGameModes) {
                gameMode.queue.remove(minecraftPlayer);
            }
        }
    }


    public static void spectatePlayer(@NotNull ServerPlayer executor, @NotNull ServerPlayer player) {
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor);
        if (executor == player) {
            factoryExecutor.sendMessage(Component.text("You may not spectate yourself!").color(ChatFormat.failColor));
            return;
        }

        PlayerData playerData = PlayerDataManager.get(player);

        if (!playerData.inDuel && playerData.teamDuelsGame == null && playerData.duelsGame == null) {
            factoryExecutor.sendMessage(Component.text("That player is not in a duel!").color(ChatFormat.failColor));
            return;
        }


        PlayerData executorData = PlayerDataManager.get(executor);

        if (executorData.gameMode == DuelGameMode.SPECTATING) {
            unspectatePlayer(executor, player, false);
        }

        if(playerData.teamDuelsGame != null) {
            factoryExecutor.sendMessage(Component.text("Spectating Team Duels is currently not available. We are sorry for the inconvenience.").color(ChatFormat.failColor));
            return;
        }

        factoryExecutor.setGameMode(Minecraft.GameMode.SPECTATOR);
        executor.teleportTo(player.getLevel(), player.getX(), player.getY(), player.getZ(), 0, 0);

        DuelsGame duelsGame = playerData.duelsGame;
        TeamDuelsGame teamDuelsGame = playerData.teamDuelsGame;

        TextComponent spectateMSG = new TextComponent("§7§o(" + factoryExecutor.getRawName() + " started spectating)");

        if (teamDuelsGame != null) {
            teamDuelsGame.spectators.add(executor);
            List<ServerPlayer> everyTeamMember = teamDuelsGame.team1.all;
            everyTeamMember.addAll(teamDuelsGame.team2.all);
            for (ServerPlayer players : everyTeamMember) {
                players.sendMessage(spectateMSG, Util.NIL_UUID);
            }
        } else if (duelsGame != null) {
            duelsGame.spectators.add(executor);
            duelsGame.p1.sendMessage(spectateMSG, Util.NIL_UUID);
            duelsGame.p2.sendMessage(spectateMSG, Util.NIL_UUID);
        }


        factoryExecutor.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You are now spectating ")
                                .color(ChatFormat.normalColor)
                                .decoration(ChatFormat.bold, false)
                                .append(Component.text(player.getScoreboardName())
                                        .color(ChatFormat.brandColor1)
                                        .decoration(ChatFormat.bold, true)
                                )
                        )
        );

        executorData.spectatingPlayer = player;
        executorData.gameMode = DuelGameMode.SPECTATING;
    }

    public static void unspectatePlayer(@NotNull ServerPlayer executor, @Nullable ServerPlayer player, boolean teleport) {
        PlayerData playerData = null;

        if (player != null) {
            playerData = PlayerDataManager.get(player);
        }

        DuelsGame duelsGame = null;
        TeamDuelsGame teamDuelsGame = null;

        if (player != null && playerData.inDuel && playerData.duelsGame != null) {
            duelsGame = playerData.duelsGame;
        } else if (player != null && playerData.inDuel && playerData.teamDuelsGame != null) {
            teamDuelsGame = playerData.teamDuelsGame;
        }

        PlayerData executorData = PlayerDataManager.get(executor);
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor);

        TextComponent spectateMSG = new TextComponent("§7§o(" + factoryExecutor.getRawName() + " has stopped spectating)");
        if (duelsGame != null || teamDuelsGame != null) {
            factoryExecutor.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You have stopped spectating ")
                                    .color(ChatFormat.normalColor)
                                    .decoration(ChatFormat.bold, false)
                                    .append(Component.text(player.getScoreboardName())
                                            .color(ChatFormat.brandColor1)
                                            .decoration(ChatFormat.bold, true)
                                    )
                            )
            );
        }


        if (duelsGame != null) {
            duelsGame.spectators.remove(executor);

            duelsGame.p1.sendMessage(spectateMSG, Util.NIL_UUID);
            duelsGame.p2.sendMessage(spectateMSG, Util.NIL_UUID);
        } else if (teamDuelsGame != null) {
            teamDuelsGame.spectators.remove(executor);

            List<ServerPlayer> everyTeamPlayer = teamDuelsGame.team1.all;
            everyTeamPlayer.addAll(teamDuelsGame.team2.all);

            for (ServerPlayer players : everyTeamPlayer) {
                players.sendMessage(spectateMSG, Util.NIL_UUID);
            }
        }
        executorData.gameMode = DuelGameMode.LOBBY;
        executorData.spectatingPlayer = null;
        LobbyUtil.leaveAllGames(executor, teleport);
    }
    public static void joinGamemode(ServerPlayer invitor, ServerPlayer player, String stringGameMode, @Nullable DuelsMap selectedmap, boolean silent) {
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            }
            return;
        }
        PlayerData data = PlayerDataManager.get(invitor);
        PlayerData playerData = PlayerDataManager.get(player);

        if (data.duelsTeam != null && data.duelsTeam.people.contains(player)) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("You cannot duel people on your team!").color(ChatFormat.failColor));
            }
            return;
        }

        if (data.duelsTeam != null && !data.duelsTeam.refreshLeader(invitor)) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            }
            return;
        }

        if (data.duelsTeam == null && playerData.duelsTeam != null) {
            DuelsTeam.createTeam(invitor, false);
        }

        if (data.duelsTeam != null && playerData.duelsTeam != null) {
            TeamDuelsGame.startGame(data.duelsTeam, playerData.duelsTeam, stringGameMode, selectedmap);
            return;
        }

        DuelsGame.startGame(invitor, player, stringGameMode, selectedmap);

    }

    public static void acceptDuel(@NotNull ServerPlayer minecraftExecutor, @NotNull ServerPlayer minecraftPlayer) {
        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);
        //Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftExecutor).gameMode != PlayerGameMode.LOBBY || com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if (!playerData.inviting || !playerData.invitingPlayer.getUUID().equals(executor.getUUID())) {
            executor.sendMessage(Component.text("That player has not challenged you to a duel!").color(ChatFormat.failColor));
            return;
        }

        GamemodeHandler.joinGamemode(minecraftExecutor, minecraftPlayer, playerData.inviteKit, playerData.inviteMap, true);
    }

    public static void declineDuel(@NotNull ServerPlayer minecraftExecutor, @NotNull ServerPlayer minecraftPlayer) {
        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        //PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftExecutor).gameMode != PlayerGameMode.LOBBY || com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if (!playerData.inviting || !playerData.invitingPlayer.getUUID().equals(executor.getUUID())) {
            executor.sendMessage(Component.text("That player has not challenged you to a duel!").color(ChatFormat.failColor));
            return;
        }

        /*
        if (!executorData.inviteMap.equalsIgnoreCase(map)) {
            executorData.inviteMap = map;
        }

        if (!executorData.inviteKit.equalsIgnoreCase(stringGameMode.toUpperCase())) {
            executorData.inviteKit = stringGameMode.toUpperCase();
        }

        if (!executorData.inviting) {
            executorData.inviting = true;
        }

        if (executorData.invitingPlayer != minecraftPlayer) {
            executorData.invitingPlayer = minecraftPlayer;
        }
         */

        playerData.inviteMap = DuelsMap.CITY;
        playerData.inviteKit = "";
        playerData.inviting = false;
        playerData.invitingPlayer = null;


        player.sendMessage(ChatFormat.nexiaMessage.append(Component.text(executor.getRawName() + " has declined your duel.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        executor.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("You have declined ")
                        .color(ChatFormat.normalColor)
                        .decoration(ChatFormat.bold, false))
                .append(Component.text(player.getRawName())
                        .color(ChatFormat.brandColor1)
                        .decoration(ChatFormat.bold, true))
                .append(Component.text("'s duel.")
                        .color(ChatFormat.normalColor)
                        .decoration(ChatFormat.bold, false)
                )
        );


        GamemodeHandler.joinGamemode(minecraftExecutor, minecraftPlayer, playerData.inviteKit, playerData.inviteMap, true);
    }

    public static void challengePlayer(ServerPlayer minecraftExecutor, ServerPlayer minecraftPlayer, String stringGameMode, @Nullable DuelsMap selectedmap) {

        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            executor.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            return;
        }
        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if (executorData.duelsTeam != null && !executorData.duelsTeam.refreshLeader(minecraftExecutor)) {
            executor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        DuelsMap map = selectedmap;
        if (map == null) {
            map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
        } else {
            if (!DuelsMap.duelsMaps.contains(map)) {
                executor.sendMessage(Component.text("Invalid map!").color(ChatFormat.failColor));
                return;
            }
        }
        if(map != null && gameMode.gameMode == GameType.ADVENTURE && !map.isAdventureSupported) {
            executor.sendMessage(Component.text("This map is not supported for this gamemode!").color(ChatFormat.failColor));
            return;
        }

        if (!executorData.inviteMap.equals(map)) {
            executorData.inviteMap = map;
        }

        if (!executorData.inviteKit.equalsIgnoreCase(stringGameMode.toUpperCase())) {
            executorData.inviteKit = stringGameMode.toUpperCase();
        }

        if (!executorData.inviting) {
            executorData.inviting = true;
        }

        if (executorData.invitingPlayer != minecraftPlayer) {
            executorData.invitingPlayer = minecraftPlayer;
        }

        // } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(minecraftExecutor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){

        Component message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                .append(Component.text(" has challenged you to a duel!").color(ChatFormat.normalColor)
                );

        if (executorData.duelsTeam == null) {
            executor.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Sending a duel request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" with kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(stringGameMode).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        } else {
            executor.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Sending a ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text("team duel").color(ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                            .append(Component.text(" request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" with kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(stringGameMode).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                    .append(Component.text(" has challenged you to a ").color(ChatFormat.normalColor))
                    .append(Component.text("team duel").color(ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                    .append(Component.text("!").color(ChatFormat.normalColor));
        }


        Component kit = Component.text("Kit: ").color(ChatFormat.brandColor1)
                .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.normalColor)
                );

        Component mapName = Component.text("Map: ").color(ChatFormat.brandColor1)
                .append(Component.text(map.id.toUpperCase()).color(ChatFormat.normalColor)
                );

        Component yes = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/acceptduel " + executor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );

        Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/declineduel " + executor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );


        player.sendMessage(message);
        player.sendMessage(kit);
        player.sendMessage(mapName);
        player.sendMessage(yes.append(no));
    }
}