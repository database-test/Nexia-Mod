package com.nexia.core.commands.player.duels;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.misc.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class AcceptDuelCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        register(dispatcher, "acceptduel");
        register(dispatcher, "acceptchallenge");
    }

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher, String string) {
        dispatcher.register(CommandUtils.literal(string)
                .requires(commandSourceInfo -> {
                    try {
                        if(CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(commandSourceInfo));

                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
                        PlayerData playerData1 = PlayerDataManager.get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> AcceptDuelCommand.accept(context, context.getArgument("player", ServerPlayer.class)))
                )
        );
    }


    public static int accept(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        GamemodeHandler.acceptDuel(executor, new NexiaPlayer(player));
        return 1;
    }
}
