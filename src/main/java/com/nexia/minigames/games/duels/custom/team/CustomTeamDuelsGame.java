package com.nexia.minigames.games.duels.custom.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class CustomTeamDuelsGame { // implements Runnable{
    public DuelsTeam team1;

    public DuelsTeam team2;

    public UUID uuid;

    public String kitID;

    public DuelsMap map;

    public boolean isEnding = false;

    public boolean hasStarted = false;

    public int startTime;

    private int currentStartTime = 5;

    public int endTime;

    private int currentEndTime = 0;

    public ServerLevel level;

    public ArrayList<NexiaPlayer> spectators = new ArrayList<>();

    // Winner thingie
    public DuelsTeam winner = null;

    public DuelsTeam loser = null;

    private boolean shouldWait = false;


    public String perCustomKitID;

    public boolean perCustomDuel;

    public CustomTeamDuelsGame(DuelsTeam team1, DuelsTeam team2, String kitID, DuelsMap map, ServerLevel level, int endTime, int startTime) {
        this.team1 = team1;
        this.team2 = team2;
        this.kitID = kitID;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public CustomTeamDuelsGame(DuelsTeam team1, DuelsTeam team2, String perCustomKitID, String perCustomKitID2, DuelsMap map, ServerLevel level, int endTime, int startTime) {
        this.team1 = team1;
        this.team2 = team2;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;

        this.kitID = perCustomKitID;
        this.perCustomKitID = perCustomKitID2;

        this.perCustomDuel = true;
    }

    public String detectBrokenGame() {
        // return (this.team1 == null || this.team1.leader == null || this.team2 ==
        // null || this.team2.leader == null) || (this.isEnding && ((this.winner ==
        // null || this.winner.leader == null) || (this.loser == null ||
        // this.loser.leader == null)));

        if (this.team1 == null)
            return "Team 1 is not set [NULL]";
        if (this.team1.getLeader() == null || this.team1.getLeader().player().get() == null)
            return "Team 1 Leader is not set [NULL]";

        if (this.team2 == null)
            return "Team 2 is not set [NULL]";
        if (this.team2.getLeader() == null || this.team2.getLeader().player().get() == null)
            return "Team 2 Leader is not set [NULL]";

        if (this.isEnding) {
            if (this.winner == null)
                return "Winner Team is not set [NULL]";
            if (this.winner.getLeader() == null || this.winner.getLeader().player().get() == null)
                return "Winner Team Leader is not set [NULL]";

            if (this.loser == null)
                return "Loser Team is not set [NULL]";
            if (this.loser.getLeader() == null || this.loser.getLeader().player().get() == null)
                return "Loser Team Leader is not set [NULL]";
        }

        return null;

    }

    public static CustomTeamDuelsGame startGame(@NotNull DuelsTeam team1, @NotNull DuelsTeam team2, String kitID, @Nullable DuelsMap selectedMap) {

        String perCustomKitID = null;

        if(!DuelGameHandler.validCustomKit(team1.getLeader(), kitID)){
            Main.logger.error(String.format("[Nexia]: Invalid custom duel kit (%s) selected!", kitID));
            kitID = "";
        }

        PlayerData team1LeaderData = PlayerDataManager.get(team1.getLeader());
        if(team1LeaderData.inviteOptions.perCustomDuel && !DuelGameHandler.validCustomKit(team2.getLeader(), team1LeaderData.inviteOptions.inviteKit2)) {
            Main.logger.error(String.format("[Nexia]: Invalid per-custom (team 2) duel kit (%s) selected!", team1LeaderData.inviteOptions.inviteKit2));
        } else {
            perCustomKitID = team1LeaderData.inviteOptions.inviteKit2;
        }

        team1.alive.clear();
        team1.alive.addAll(team1.all);

        team2.alive.clear();
        team2.alive.addAll(team2.all);

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), true);
        if (selectedMap == null) {
            selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
        }

        selectedMap.structureMap.pasteMap(duelLevel);

        File kitFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + team1.getLeader().player().uuid, kitID.toLowerCase() + ".txt");
        File p2File = null;

        if(perCustomKitID != null && !perCustomKitID.trim().isEmpty()) {
            p2File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + team2.getLeader().player().uuid, perCustomKitID.toLowerCase() + ".txt");
        }

        CustomTeamDuelsGame game;


        if(p2File != null && p2File.exists()) game = new CustomTeamDuelsGame(team1, team2, kitID, perCustomKitID, selectedMap, duelLevel, 5, 5);
        else game = new CustomTeamDuelsGame(team1, team2, kitID, selectedMap, duelLevel, 5, 5);

        DuelGameHandler.customTeamDuelsGames.add(game);

        for (NexiaPlayer player : team1.all) {
            ServerPlayer serverPlayer = player.player().get();
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(serverPlayer);

            DuelGameHandler.leave(player, false);

            data.gameMode = DuelGameMode.CLASSIC;
            data.gameOptions = new DuelOptions.GameOptions(game, team2);
            data.inviteOptions.reset();
            data.inDuel = true;

            selectedMap.p1Pos.teleportPlayer(duelLevel, serverPlayer);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                            .append(Component.text(team2.getLeader().player().name + "'s Team")
                                    .color(ChatFormat.brandColor2))));

            if(kitFile.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + team1.getLeader().player().uuid, kitID.toLowerCase());
            else InventoryUtil.loadInventory(player, "duels", "classic");

            factoryPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            player.reset(true, GameType.ADVENTURE);
        }

        for (NexiaPlayer player : team2.all) {
            ServerPlayer serverPlayer = player.player().get();
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(serverPlayer);

            DuelGameHandler.leave(player, false);

            data.gameMode = DuelGameMode.CLASSIC;
            data.gameOptions = new DuelOptions.GameOptions(game, team1);
            data.inviteOptions.reset();
            data.inDuel = true;

            selectedMap.p2Pos.teleportPlayer(duelLevel, serverPlayer);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                            .append(Component.text(team1.getLeader().player().name + "'s Team")
                                    .color(ChatFormat.brandColor2))));


            if(game.perCustomDuel) {
                if(p2File != null && p2File.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + team2.getLeader().player().uuid, perCustomKitID.toLowerCase());
                else InventoryUtil.loadInventory(player, "duels", "classic");
            } else {
                if(kitFile.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + team1.getLeader().player().uuid, kitID.toLowerCase());
                else InventoryUtil.loadInventory(player, "duels", "classic");
            }

            factoryPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

           player.reset(true, GameType.ADVENTURE);
        }

        game.uuid = gameUUID;

        return game;
    }

    public void duelSecond() {
        String isBroken = this.detectBrokenGame();
        if (isBroken != null) {
            Component error = ChatFormat.nexiaMessage
                    .append(Component.text(
                                    "The game you were in was identified as broken, please contact a developer with a video of the last 30 seconds.")
                            .color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false));

            Component errormsg = Component.text("Cause: " + isBroken);

            for (NexiaPlayer spectator : this.spectators) {
                spectator.sendMessage(error);
                spectator.sendMessage(errormsg);
            }

            for (ServerPlayer player : this.level.players()) {
                Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
                factoryPlayer.sendMessage(error);
                factoryPlayer.sendMessage(errormsg);
            }

            this.hasStarted = true;
            this.isEnding = true;
            this.shouldWait = false;

            DuelsTeam notNullTeam = this.team1;

            if(notNullTeam != null) notNullTeam = this.team2;
            if(notNullTeam != null) this.endGame(notNullTeam, null, false);
        }
        if (this.isEnding) {
            int color = 160 * 65536 + 248;
            // r * 65536 + g * 256 + b;
            DuelGameHandler.winnerRockets(this.winner.alive.get(new Random().nextInt(this.winner.alive.size())),
                    this.level, color);
            this.currentEndTime++;
            if (this.currentEndTime >= this.endTime || !this.shouldWait) {
                DuelsTeam winnerTeam = this.winner;
                DuelsTeam loserTeam = this.loser;

                for (NexiaPlayer spectator : this.spectators) {
                    spectator.getFactoryPlayer().runCommand("/hub", 0, false);
                }

                this.isEnding = false;

                for (NexiaPlayer player : loserTeam.all) {
                    PlayerDataManager.get(player).gameOptions = null;
                    player.getFactoryPlayer().runCommand("/hub", 0, false);
                }
                for (NexiaPlayer player : winnerTeam.all) {
                    PlayerDataManager.get(player).gameOptions = null;
                    player.getFactoryPlayer().runCommand("/hub", 0, false);
                }

                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));
                this.team1.refreshTeam();
                this.team2.refreshTeam();
                DuelGameHandler.customTeamDuelsGames.remove(this);
                return;
            }
        }
        if (!this.hasStarted) {

            this.currentStartTime--;

            for (NexiaPlayer player : this.team1.alive) {
                this.map.p1Pos.teleportPlayer(this.level, player.player().get());
            }
            for (NexiaPlayer player : this.team2.alive) {
                this.map.p2Pos.teleportPlayer(this.level, player.player().get());
            }

            if (this.startTime - this.currentStartTime >= this.startTime) {

                for (NexiaPlayer player : this.team1.alive) {
                    player.sendSound(new EntityPos(player.player().get()), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS,
                            10, 2);
                    player.player().get().setGameMode(GameType.SURVIVAL);
                    player.player().get().removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.player().get().removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                for (NexiaPlayer player : this.team2.alive) {
                    player.sendSound(new EntityPos(player.player().get()), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS,
                            10, 2);
                    player.player().get().setGameMode(GameType.SURVIVAL);
                    player.player().get().removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.player().get().removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                this.hasStarted = true;
                return;
            }

            Title title = getTitle();

            for (NexiaPlayer player : this.team1.alive) {
                player.sendTitle(title);
                player.sendSound(new EntityPos(player.player().get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10,
                        1);
            }
            for (NexiaPlayer player : this.team2.alive) {
                player.sendTitle(title);
                player.sendSound(new EntityPos(player.player().get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10,
                        1);
            }
        }
    }

    @NotNull
    private Title getTitle() {
        Title title;
        TextColor color = NamedTextColor.GREEN;

        if (this.currentStartTime <= 3 && this.currentStartTime > 1) {
            color = NamedTextColor.YELLOW;
        } else if (this.currentStartTime <= 1) {
            color = NamedTextColor.RED;
        }

        title = Title.title(Component.text(this.currentStartTime).color(color), Component.text(""),
                Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
        return title;
    }

    public void endGame(@NotNull DuelsTeam loserTeam, @Nullable DuelsTeam winnerTeam, boolean wait) {

        this.winner = winnerTeam;
        this.loser = loserTeam;
        if (winnerTeam == null) {
            this.winner = this.team1;
            if (loserTeam == this.team1) {
                this.winner = this.team2;
            }
        }

        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;

        Component win = Component.text("The game was a ")
                .color(ChatFormat.normalColor)
                .append(Component.text("draw").color(ChatFormat.brandColor2))
                .append(Component.text("!").color(ChatFormat.normalColor));

        Component titleLose = Component.text("Draw")
                .color(ChatFormat.brandColor2);
        Component subtitleLose;

        Component titleWin = titleLose;
        Component subtitleWin = win;

        if ((winnerTeam == null || winnerTeam.getLeader() == null || winnerTeam.getLeader().player().get() == null)) {
            for (NexiaPlayer player : loserTeam.all) {
                player.sendTitle(Title.title(titleWin, subtitleWin));
                player.sendMessage(win);
            }
            return;
        }

        win = Component.text(winnerTeam.getLeader().player().name + "'s Team").color(ChatFormat.brandColor2)
                .append(Component.text(" has won the duel!").color(ChatFormat.normalColor));

        titleLose = Component.text("You lost!").color(ChatFormat.brandColor2);
        subtitleLose = Component.text("You have lost against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(winnerTeam.getLeader().player().name + "'s Team")
                        .color(ChatFormat.brandColor2));

        titleWin = Component.text("You won!").color(ChatFormat.brandColor2);
        subtitleWin = Component.text("You have won against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(loserTeam.getLeader().player().name + "'s Team")
                        .color(ChatFormat.brandColor2));

        for (NexiaPlayer player : loserTeam.all) {
            PlayerDataManager.get(player).savedData.loss++;
            player.sendTitle(Title.title(titleLose, subtitleLose));
            player.sendMessage(win);
        }

        for (NexiaPlayer player : winnerTeam.all) {
            PlayerDataManager.get(player).savedData.wins++;
            player.sendTitle(Title.title(titleWin, subtitleWin));
            player.sendMessage(win);
        }
    }

    public void death(@NotNull NexiaPlayer victim, @Nullable DamageSource source) {
        PlayerData victimData = PlayerDataManager.get(victim);
        DuelsTeam victimTeam = victimData.duelOptions.duelsTeam;

        if (victimTeam == null || this.isEnding) return;

        victim.player().get().destroyVanishingCursedItems();
        victim.player().get().inventory.dropAll();
        victimTeam.alive.remove(victim);

        boolean isVictimTeamDead = victimTeam.alive.isEmpty();

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim.player().get());

        if (attacker != null) {
            PlayerData attackerData = PlayerDataManager.get(attacker.getUUID());
            if (attackerData.gameOptions.customTeamDuelsGame != null && attackerData.gameOptions.customTeamDuelsGame.equals(this) && isVictimTeamDead) {
                this.endGame(victimTeam, attackerData.duelOptions.duelsTeam, true);
            }
            return;
        }
        if (isVictimTeamDead) {
            if (this.team1 == victimTeam)
                this.endGame(victimTeam, this.team2, true);
            else if (this.team2 == victimTeam)
                this.endGame(victimTeam, this.team1, true);

            return;
        }

        /*
         * if(isVictimTeamDead){
         * this.endGame(victimTeam, null, false);
         * }
         *
         */
    }
}