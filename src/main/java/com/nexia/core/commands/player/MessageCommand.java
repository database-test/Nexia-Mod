package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class MessageCommand {

    public static void registerMsg(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
        commandDispatcher.register(Commands.literal("msg")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::msgCommand)
                        )
                ));
        commandDispatcher.register(Commands.literal("mesaage")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::msgCommand)
                        )
                )
        );
    }

    public static void registerReply(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
        commandDispatcher.register(Commands.literal("r")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommand::replyCommand)
                ));
        commandDispatcher.register(Commands.literal("reply")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommand::replyCommand)
                )
        );
    }

    private static int msgCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer receiver = EntityArgument.getPlayer(context, "player");
        String message = StringArgumentType.getString(context, "message");

        sendMessage(sender, receiver, message);

        return 1;
    }

    private static int replyCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        PlayerData senderData = PlayerDataManager.get(sender);
        ServerPlayer receiver = PlayerUtil.getFixedPlayer(senderData.lastMessageSender);

        if (receiver == null) {
            sender.sendMessage(ChatFormat.format("{s}Nobody to reply to"), Util.NIL_UUID);
            return 1;
        }

        String message = StringArgumentType.getString(context, "message");

        sendMessage(sender, receiver, message);
        return 1;
    }

    private static void sendMessage(ServerPlayer sender, ServerPlayer receiver, String message) {
        if (PlayerMutes.muted(sender) || sender == receiver) return;

        sender.sendMessage(ChatFormat.format("{b1}To {} {s}» {b1}{}", receiver.getScoreboardName(), message),
                Util.NIL_UUID);
        PlayerDataManager.get(sender).lastMessageSender = receiver;

        receiver.sendMessage(ChatFormat.format("{b1}From {} {s}» {b1}{}", sender.getScoreboardName(), message),
                Util.NIL_UUID);
        PlayerDataManager.get(receiver).lastMessageSender = sender;
    }

}