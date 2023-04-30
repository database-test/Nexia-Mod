package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsSpawn;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.stream.Stream;

public class DevExperimentalMapCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("devexperimentalmap")
                .requires(commandSourceStack -> {
                    try {
                        return Permissions.check(commandSourceStack, "nexia.dev.experimentalmap");
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .then(Commands.argument("argument", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"cffa"}), builder))))
                        .executes(DevExperimentalMapCommand::run)
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String argument = StringArgumentType.getString(context, "argument");

        if(argument.equalsIgnoreCase("cffa")){
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ffa", "map")).location(), (
                    new RuntimeWorldConfig()
                            .setDimensionType(DuelsSpawn.duelWorld.dimensionType())
                            .setGenerator(DuelsSpawn.duelWorld.getChunkSource().getGenerator())
                            .setDifficulty(Difficulty.HARD)
                            .setGameRule(GameRules.RULE_KEEPINVENTORY, true)
                            .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                            .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                            .setGameRule(GameRules.RULE_DAYLIGHT, false)
                            .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, true)
                            .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                            .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                            .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
                            .setGameRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK, true)
                            .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0))).asWorld();

            player.teleportTo(level, 0, 80, 0, 0, 0);
        }

        return 1;
    }
}