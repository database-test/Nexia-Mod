package com.nexia.minigames.games.duels;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
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
import net.notcoded.codelib.players.AccuratePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelsGame {
    public NexiaPlayer p1;

    public UUID uuid;
    public NexiaPlayer p2;

    public DuelGameMode gameMode;

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
    public NexiaPlayer winner = null;

    public NexiaPlayer loser = null;

    private boolean shouldWait = false;

    public DuelsGame(NexiaPlayer p1, NexiaPlayer p2, DuelGameMode gameMode, DuelsMap map, ServerLevel level, int endTime, int startTime){
        this.p1 = p1;
        this.p2 = p2;
        this.gameMode = gameMode;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public static DuelsGame startGame(NexiaPlayer p1, NexiaPlayer p2, String stringGameMode, @Nullable DuelsMap selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.CLASSIC;
            Main.logger.error(String.format("[Nexia]: Invalid duel gamemode (%s) selected! Using fallback one.", stringGameMode));
            stringGameMode = "CLASSIC";
        }

        PlayerData invitorData = PlayerDataManager.get(p1);
        PlayerData playerData = PlayerDataManager.get(p2);

        if(invitorData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(p1, invitorData.duelOptions.spectatingPlayer, false);
        }
        if(playerData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(p2, playerData.duelOptions.spectatingPlayer, false);
        }

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), gameMode.hasRegen);
        if(selectedMap == null){
            do {
                selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
            } while (!selectedMap.isAdventureSupported && gameMode.gameMode.equals(GameType.ADVENTURE));
        }

        selectedMap.structureMap.pasteMap(duelLevel);

        if(!gameMode.hasSaturation) {
            p1.getFactoryPlayer().addTag(LobbyUtil.NO_SATURATION_TAG);
            p2.getFactoryPlayer().addTag(LobbyUtil.NO_SATURATION_TAG);
        }

        p1.reset(true, GameType.ADVENTURE);
        p2.reset(true, GameType.ADVENTURE);

        selectedMap.p2Pos.teleportPlayer(duelLevel, p1.player().get());
        playerData.inviteOptions.reset();
        playerData.inDuel = true;
        playerData.duelOptions.spectatingPlayer = null;

        selectedMap.p1Pos.teleportPlayer(duelLevel, p1.player().get());
        invitorData.inviteOptions.reset();
        invitorData.inDuel = true;
        invitorData.duelOptions.spectatingPlayer = null;

        removeQueue(p1, null, true);
        removeQueue(p2, null, true);


        p2.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p1.player().name).color(ChatFormat.brandColor2))));

        p1.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p2.player().name).color(ChatFormat.brandColor2))));

        DuelGameHandler.loadInventory(p1, stringGameMode);
        DuelGameHandler.loadInventory(p2, stringGameMode);

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;

        DuelsGame game = new DuelsGame(p1, p2, gameMode, selectedMap, duelLevel, 5, 5);

        playerData.gameOptions = new DuelOptions.GameOptions(game, p1);
        invitorData.gameOptions = new DuelOptions.GameOptions(game, p2);

        DuelGameHandler.duelsGames.add(game);

        game.uuid = gameUUID;

        return game;
    }

    public void duelSecond() {
        if(this.isEnding) {
            int color = 160 * 65536 + 248;
            // r * 65536 + g * 256 + b;
            DuelGameHandler.winnerRockets(this.winner, this.level, color);
            this.currentEndTime++;
            if(this.currentEndTime >= this.endTime || !this.shouldWait) {
                NexiaPlayer attacker = this.winner;
                NexiaPlayer victim = this.loser;

                PlayerData victimData = PlayerDataManager.get(victim);
                PlayerData attackerData = PlayerDataManager.get(attacker);

                attacker.safeReset(false, this.gameMode.gameMode);

                for(NexiaPlayer spectator : this.spectators) {
                    spectator.getFactoryPlayer().runCommand("/hub", 0, false);
                }

                victimData.gameOptions = null;
                victimData.inDuel = false;
                removeQueue(victim, null, true);
                victimData.gameMode = DuelGameMode.LOBBY;
                victimData.inviteOptions.reset();
                victimData.duelOptions.spectatingPlayer = null;

                attackerData.gameOptions = null;
                attackerData.inDuel = false;
                removeQueue(attacker, null, true);
                attackerData.gameMode = DuelGameMode.LOBBY;
                attackerData.inviteOptions.reset();
                attackerData.duelOptions.spectatingPlayer = null;

                attackerData.savedData.wins++;
                victimData.savedData.loss++;

                this.isEnding = false;

                if(victim.player().get() != null) {
                    victim.getFactoryPlayer().runCommand("/hub", 0, false);
                }

                if(attacker.player().get() != null) {
                    attacker.getFactoryPlayer().runCommand("/hub", 0, false);
                }

                for(ServerPlayer spectator : this.level.players()) {
                    PlayerUtil.getFactoryPlayer(spectator).runCommand("/hub", 0, false);
                    spectator.kill();
                }

                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));
                DuelGameHandler.duelsGames.remove(this);
                return;
            }
        }
        if(!this.hasStarted) {

            this.currentStartTime--;

            this.map.p1Pos.teleportPlayer(this.level, this.p1.player().get());
            this.map.p2Pos.teleportPlayer(this.level, this.p2.player().get());

            if (this.startTime - this.currentStartTime >= this.startTime) {
                this.p1.sendSound(new EntityPos(this.p1.player().get()), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                this.p1.player().get().setGameMode((this.gameMode != null) ? this.gameMode.gameMode : GameType.SURVIVAL);
                this.p1.getFactoryPlayer().removeTag(LobbyUtil.NO_DAMAGE_TAG);
                this.p1.getFactoryPlayer().removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

                this.p2.sendSound(new EntityPos(this.p2.player().get()), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                this.p2.player().get().setGameMode((this.gameMode != null) ? this.gameMode.gameMode : GameType.SURVIVAL);
                this.p2.getFactoryPlayer().removeTag(LobbyUtil.NO_DAMAGE_TAG);
                this.p2.getFactoryPlayer().removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

                this.hasStarted = true;
                return;
            }

            Title title;
            TextColor color = NamedTextColor.GREEN;

            if(this.currentStartTime <= 3 && this.currentStartTime > 1) {
                color = NamedTextColor.YELLOW;
            } else if(this.currentStartTime <= 1) {
                color = NamedTextColor.RED;
            }

            title = Title.title(Component.text(this.currentStartTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));

            this.p1.sendTitle(title);
            this.p2.sendTitle(title);


            this.p1.sendSound(new EntityPos(this.p1.player().get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
            this.p2.sendSound(new EntityPos(this.p2.player().get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
        }
    }

    public void endGame(@NotNull NexiaPlayer victim, @Nullable NexiaPlayer attacker, boolean wait) {
        this.loser = victim;
        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;

        boolean attackerNull = attacker == null || attacker.player().get() == null;

        if (!attackerNull) {
            this.winner = attacker;
        }

        Component win = Component.text("The game was a ")
                .color(ChatFormat.normalColor)
                .append(Component.text("draw").color(ChatFormat.brandColor2))
                .append(Component.text("!").color(ChatFormat.normalColor)
                );

        Component titleLose = Component.text("Draw")
                .color(ChatFormat.brandColor2);
        Component subtitleLose = win;

        Component titleWin;
        Component subtitleWin;


        if (!attackerNull) {
            win = Component.text(attacker.player().name).color(ChatFormat.brandColor2)
                    .append(Component.text(" has won the duel!").color(ChatFormat.normalColor)
                            .append(Component.text(" [")
                                    .color(ChatFormat.lineColor))
                            .append(Component.text(FfaUtil.calculateHealth(attacker.getFactoryPlayer().getHealth()) + "❤").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                    );

            titleLose = Component.text("You lost!").color(ChatFormat.brandColor2);
            subtitleLose = Component.text("You have lost against ")
                    .color(ChatFormat.normalColor)
                    .append(Component.text(attacker.player().name)
                            .color(ChatFormat.brandColor2)
                            .append(Component.text(" [")
                                    .color(ChatFormat.lineColor)
                            .append(Component.text(FfaUtil.calculateHealth(attacker.getFactoryPlayer().getHealth()) + "❤").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            )
                    );

            titleWin = Component.text("You won!").color(ChatFormat.brandColor2);
            subtitleWin = Component.text("You have won against ")
                    .color(ChatFormat.normalColor)
                    .append(Component.text(victim.player().name)
                            .color(ChatFormat.brandColor2)
                    );

            attacker.sendMessage(win);
            attacker.sendTitle(Title.title(titleWin, subtitleWin));
        }
        victim.sendMessage(win);
        victim.sendTitle(Title.title(titleLose, subtitleLose));
    }

    public void death(@NotNull NexiaPlayer victim, @Nullable DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(victimData.gameOptions == null || victimData.gameOptions.duelsGame == null || victimData.gameOptions.duelsGame.isEnding) return;

        victim.player().get().destroyVanishingCursedItems();
        victim.player().get().inventory.dropAll();

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim.player().get());

        if(attacker != null){

            NexiaPlayer nexiaAttacker = new NexiaPlayer(new AccuratePlayer(attacker));

            PlayerData attackerData = PlayerDataManager.get(nexiaAttacker);
            if((victimData.inDuel && attackerData.inDuel) && victimData.gameOptions.duelsGame == attackerData.gameOptions.duelsGame){
                this.endGame(victim, nexiaAttacker, true);
                return;
            }
        }
        if(victimData.gameOptions.duelPlayer != null) {
            NexiaPlayer accurateAttacker = victimData.gameOptions.duelPlayer;
            PlayerData attackerData = PlayerDataManager.get(accurateAttacker);

            if ((victimData.inDuel && attackerData.inDuel) && accurateAttacker.equals(victimData.gameOptions.duelPlayer)) {
                this.endGame(victim, accurateAttacker, true);
                return;
            }
        }

        if(victimData.inDuel) {
            this.endGame(victim, null, false);
        }
    }
}