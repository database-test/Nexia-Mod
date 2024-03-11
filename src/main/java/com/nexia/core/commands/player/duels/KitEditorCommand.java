package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective_fabric.functions.PlayerFunctions;
import com.natamus.collective_fabric.functions.StringFunctions;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KitEditorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("kiteditor")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(Commands.argument("argument", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"save", "edit", "reset"}), builder)))
                                .then(Commands.argument("inventory", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(InventoryUtil.getListOfInventories("duels"), builder)))
                                        .executes(context -> run(context, StringArgumentType.getString(context, "argument"), StringArgumentType.getString(context, "inventory")))
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context, @NotNull String argument, @NotNull String inventory) throws CommandSyntaxException {

        ServerPlayer player = context.getSource().getPlayerOrException();

        if(inventory.trim().isEmpty() || !InventoryUtil.getListOfInventories("duels").contains(inventory.toLowerCase())) {
            context.getSource().sendFailure(LegacyChatFormat.format("Invalid inventory name!"));
            return 0;
        }

        if(argument.equalsIgnoreCase("save")) {
            String gearstring = PlayerFunctions.getPlayerGearString(player);

            Path playerPath = Path.of(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID());
            File playerDir = playerPath.toFile();
            try {
                if(!playerDir.exists()) Files.createDirectory(playerPath);
            } catch (IOException ignored) { }


            if (StringFunctions.sequenceCount(gearstring, "\n") < 40 || !playerDir.exists()) {
                context.getSource().sendFailure(LegacyChatFormat.format("Something went wrong while generating the save file content for your inventory."));

                ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                return 0;
            } else if (!InventoryUtil.writeGearStringToFile("duels/custom/" + player.getStringUUID(), inventory, gearstring)) {
                context.getSource().sendFailure(LegacyChatFormat.format("Something went wrong while saving the content of your inventory as '{}'.", inventory));

                ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                return 0;
            } else {
                context.getSource().sendFailure(LegacyChatFormat.format("Successfully saved your inventory as '{}'.", inventory));

                ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                return 1;
            }
        }

        if (argument.equalsIgnoreCase("edit")) {

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID(), inventory + ".txt");
            if(playerFile.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + player.getStringUUID(), inventory);
            else InventoryUtil.loadInventory(player, "duels", inventory);

            return 1;
        }

        if(argument.equalsIgnoreCase("reset")) {

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID(), inventory + ".txt");
            if(playerFile.exists() && playerFile.delete()) {
                context.getSource().sendSuccess(LegacyChatFormat.format("{b1}Successfully deleted saved kit '{}'!", inventory), false);
            } else {
                context.getSource().sendFailure(LegacyChatFormat.format("Saved Kit does not exist!"));
            }

            return 1;
        }

        context.getSource().sendFailure(LegacyChatFormat.format("Invalid argument!"));
        return 1;
    }
}