package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LeaveCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        register(dispatcher, "leave");
        register(dispatcher, "lobby");
        register(dispatcher, "l");
        register(dispatcher, "hub");
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, String string) {
        dispatcher.register(Commands.literal(string).executes(LeaveCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.leaveAllGames(player, true);
        return 1;
    }

}