package com.nexia.minigames.games.duels;

import com.google.gson.Gson;
import com.nexia.core.Main;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.DimensionType;
import net.notcoded.codelib.players.AccuratePlayer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelGameHandler {


    public static List<DuelsGame> duelsGames = new ArrayList<>();
    public static List<TeamDuelsGame> teamDuelsGames = new ArrayList<>();

    public static List<CustomDuelsGame> customDuelsGames = new ArrayList<>();
    public static List<CustomTeamDuelsGame> customTeamDuelsGames = new ArrayList<>();

    public static boolean validCustomKit(ServerPlayer player, String kitID) {
        if(kitID.trim().isEmpty()) return false;

        File file = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID(), kitID + ".txt");
        return file.exists();
    }

    public static void loadInventory(ServerPlayer player, String gameMode) {
        String file = InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID() + File.separator + "layout" + File.separator + gameMode.toLowerCase() + ".json";

        SavableInventory defaultInventory = null;
        SavableInventory layout = null;

        try {
            String defaultJson = Files.readString(Path.of(InventoryUtil.dirpath + "/duels/default-layouts/" + gameMode.toLowerCase() + ".json"));
            Gson gson = new Gson();
            defaultInventory = gson.fromJson(defaultJson, SavableInventory.class);

            if(new File(file).exists()) {
                String layoutJson = Files.readString(Path.of(file));
                layout = gson.fromJson(layoutJson, SavableInventory.class);
            }
        } catch (Exception var4) {
            InventoryUtil.loadInventory(player, "duels", gameMode.toLowerCase());
            var4.printStackTrace();
        }

        if(defaultInventory == null) {
            InventoryUtil.loadInventory(player, "duels", gameMode.toLowerCase());
            return;
        }

        if(layout != null) {
            InventoryMerger.mergeSafe(player, layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            InventoryUtil.loadInventory(player, "duels", gameMode.toLowerCase());
        }

        ItemStackUtil.sendInventoryRefreshPacket(player);
    }

    public static void leave(ServerPlayer player, boolean leaveTeam) {
        PlayerData data = PlayerDataManager.get(player);
        if (data.gameOptions != null && data.gameOptions.duelsGame != null) {
            data.gameOptions.duelsGame.death(player, player.getLastDamageSource());
        }
        if (data.gameOptions != null && data.gameOptions.teamDuelsGame != null) {
            data.gameOptions.teamDuelsGame.death(player, player.getLastDamageSource());
        }
        if (data.gameOptions != null && data.gameOptions.customDuelsGame != null) {
            data.gameOptions.customDuelsGame.death(player, player.getLastDamageSource());
        }
        if (data.gameOptions != null && data.gameOptions.customTeamDuelsGame != null) {
            data.gameOptions.customTeamDuelsGame.death(player, player.getLastDamageSource());
        }
        if (data.gameMode == DuelGameMode.SPECTATING) {
            GamemodeHandler.unspectatePlayer(AccuratePlayer.create(player), data.duelOptions.spectatingPlayer, false);
        }
        data.inDuel = false;
        data.inviteOptions.reset();
        removeQueue(player, null, true);
        data.gameMode = DuelGameMode.LOBBY;
        data.editingLayout = "";
        data.editingKit = "";
        if (leaveTeam) {
            if (data.duelOptions.duelsTeam != null) {
                data.duelOptions.duelsTeam.leaveTeam(AccuratePlayer.create(player), true);
            }
            data.duelOptions.duelsTeam = null;
        }
        data.gameOptions = null;
        data.duelOptions.spectatingPlayer = null;

        if(Main.config.debugMode) Main.logger.info(String.format("[DEBUG]: Player %s left Duels.", player.getScoreboardName()));
    }

    public static void winnerRockets(@NotNull ServerPlayer winner, @NotNull ServerLevel level,
            @NotNull Integer winnerColor) {

        Random random = level.getRandom();
        EntityPos pos = new EntityPos(winner).add(random.nextInt(9) - 4, 2, random.nextInt(9) - 4);

        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
        try {
            itemStack.setTag(TagParser.parseTag("{Fireworks:{Explosions:[{Type:0,Flicker:1b,Trail:1b,Colors:[I;" +
                    winnerColor + "]}]}}"));
        } catch (Exception ignored) {
        }

        FireworkRocketEntity rocket = new FireworkRocketEntity(level, pos.x, pos.y, pos.z, itemStack);
        level.addFreshEntity(rocket);
    }

    public static void starting() {
        for (DuelGameMode duelGameMode : DuelGameMode.duelGameModes) {
            duelGameMode.queue.clear();
        }

        DuelGameHandler.duelsGames.clear();
        DuelGameHandler.teamDuelsGames.clear();

        DuelGameHandler.customDuelsGames.clear();
        DuelGameHandler.customTeamDuelsGames.clear();
    }

    public static ServerLevel createWorld(String uuid, boolean doRegeneration) {
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(DimensionType.OVERWORLD_LOCATION)
                .setGenerator(new VoidChunkGenerator(BuiltinRegistries.BIOME))
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                .setGameRule(GameRules.RULE_DAYLIGHT, false)
                .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                .setGameRule(GameRules.RULE_NATURAL_REGENERATION, doRegeneration)
                .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0)
                .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
                .setTimeOfDay(6000);


        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Created world: duels:" + uuid);

        return ServerTime.fantasy.openTemporaryWorld(config, new ResourceLocation("duels", uuid)).asWorld();
        //return ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", uuid)).location(), config).asWorld();
    }

    public static void deleteWorld(String id) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", id)).location(),
                    new RuntimeWorldConfig());
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/duels", id));
            ServerTime.factoryServer.unloadWorld("duels:" + id, false);
        } catch (Exception e) {
            Main.logger.error("Error occurred while deleting world: duels:" + id);
            try {
                ServerTime.factoryServer.unloadWorld("duels:" + id, false);
            } catch (Exception ignored2) {
                if(Main.config.debugMode) e.printStackTrace();
            }

            if(Main.config.debugMode) e.printStackTrace();

            return;
        }
        worldHandle.delete();
    }
}