package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import de.themoep.minedown.adventure.MineDown;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
import org.json.simple.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.nexia.core.utilities.player.BanHandler.banTimeToText;
import static com.nexia.core.utilities.player.BanHandler.getBanTime;
import static com.nexia.core.utilities.time.ServerTime.leavePlayer;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Unique
    private ServerPlayer joinPlayer = null;

    @ModifyArgs(method = "broadcastMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundChatPacket;<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void handleChat(Args args) {
        try {
            Component component = args.get(0);
            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(args.get(2));
            String key = ((TranslatableComponent) component).getKey();

            if (key.contains("multiplayer.player.left")) args.set(0, leaveFormat(component, leavePlayer));
            if (key.contains("multiplayer.player.join")) args.set(0, joinFormat(component, joinPlayer));

            assert player != null;
            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
            if(!PlayerMutes.muted(nexiaPlayer)){
                args.set(0, chatFormat(nexiaPlayer, component));
            }

        } catch (Exception ignored) {}
    }

    @Inject(method = "broadcastMessage", at = @At("HEAD"), cancellable = true)
    private void handleBotMessages(Component component, ChatType chatType, UUID uUID, CallbackInfo ci) {
        String key = ((TranslatableComponent) component).getKey();

        if (key.contains("multiplayer.player.left")) {
            if(leavePlayer.getTags().contains("bot")) ci.cancel();
            if(leavePlayer.getTags().contains("viafabricplus")) {
                leavePlayer.removeTag("viafabricplus");
                ci.cancel();
            }
            return;
        }

        if(key.contains("multiplayer.player.join")) {
            if(joinPlayer.getTags().contains("bot")) ci.cancel();
            if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(joinPlayer.getUUID())).clientType.equals(CorePlayerData.ClientType.VIAFABRICPLUS)) {
                joinPlayer.addTag("viafabricplus");
                ci.cancel();
            }
        }
    }

    @Unique
    private static Component joinFormat(Component original, ServerPlayer joinPlayer) {
        String name = joinPlayer.getScoreboardName();
        if(name.isEmpty()) { return original; }

        if(joinPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) {
            return ObjectMappings.convertComponent(
                    net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                            .append(net.kyori.adventure.text.Component.text("!").color(ChatFormat.goldColor)
                                    .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                                    .append(net.kyori.adventure.text.Component.text(name).color(ChatFormat.goldColor)))
            );
        } else {
            return ObjectMappings.convertComponent(
                    net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                            .append(net.kyori.adventure.text.Component.text("+").color(ChatFormat.greenColor))
                            .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                            .append(net.kyori.adventure.text.Component.text(name).color(ChatFormat.greenColor))
            );
        }
    }

    @Unique
    private static Component leaveFormat(Component original, ServerPlayer leavePlayer) {
        String name = leavePlayer.getScoreboardName();
        if(name.isEmpty()) { return original; }

        return ObjectMappings.convertComponent(
                net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                        .append(net.kyori.adventure.text.Component.text("-", ChatFormat.failColor)
                                .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                                .append(net.kyori.adventure.text.Component.text(name, ChatFormat.failColor)))
        );
    }

    @Inject(method = "canPlayerLogin", at = @At("TAIL"), cancellable = true)
    private void checkIfBanned(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir){
        if (socketAddress == null || gameProfile == null) {
            return;
        }

        JSONObject banJSON = BanHandler.getBanList(gameProfile.getId().toString());

        if(banJSON != null) {
            LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
            String reason = (String) banJSON.get("reason");

            String textBanTime = banTimeToText(banTime);

            if(LocalDateTime.now().isBefore(banTime)){
                cir.setReturnValue(ObjectMappings.convertComponent(
                        net.kyori.adventure.text.Component.text("You have been banned.", ChatFormat.failColor)
                                .append(net.kyori.adventure.text.Component.text("\nDuration: ", ChatFormat.systemColor))
                                .append(net.kyori.adventure.text.Component.text(textBanTime, ChatFormat.brandColor2))
                                .append(net.kyori.adventure.text.Component.text("\nReason: ", ChatFormat.systemColor))
                                .append(net.kyori.adventure.text.Component.text(reason, ChatFormat.brandColor2))
                                .append(net.kyori.adventure.text.Component.text("\nYou can appeal your ban at ", ChatFormat.systemColor))
                                .append(net.kyori.adventure.text.Component.text(NexiaDiscord.config.discordLink, ChatFormat.brandColor2))
                ));

            } else {
                BanHandler.removeBanFromList(gameProfile);
            }
        }
    }

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void setJoinMessage(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci){
        joinPlayer = serverPlayer;
    }

    @Unique
    private static Component chatFormat(NexiaPlayer player, Component original) {
        try {
            TranslatableComponent component = (TranslatableComponent) original;
            Object[] args = component.getArgs();

            MutableComponent name = (MutableComponent) args[0];
            MutableComponent suffix = new TextComponent(" » ").withStyle(ChatFormatting.GRAY);

            String messageString = (String) args[1];
            MutableComponent message = new TextComponent(messageString).withStyle(ChatFormatting.WHITE);

            if(player.hasPermission("nexia.chat.formatting", 4)) {
                message = (MutableComponent) ObjectMappings.convertComponent(MineDown.parse("&white&" + messageString));
            }

            return name.append(suffix).append(message);

        } catch (Exception e) {
            return original;
        }
    }
}
