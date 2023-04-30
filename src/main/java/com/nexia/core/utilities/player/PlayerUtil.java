package com.nexia.core.utilities.player;

import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerUtil {

    public static void broadcast(List<ServerPlayer> players, String string) {
        broadcast(players, new TextComponent(string));
    }

    public static void sendActionbar(ServerPlayer player, String string){
        player.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.ACTIONBAR,
                ChatFormat.format(string)));
    }

    public static void broadcast(List<ServerPlayer> players, Component component) {
        for (ServerPlayer player : players) {
            player.sendMessage(component, Util.NIL_UUID);
        }
    }

    public static void sendTitle(ServerPlayer player, String title, String sub, int in, int stay, int out) {
        player.connection.send(new ClientboundSetTitlesPacket(in, stay, out));
        player.connection.send(new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title)));
        player.connection.send(new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(sub)));
    }

    public static void broadcastTitle(List<ServerPlayer> players, String title, String subtitle, int in, int stay, int out) {
        ClientboundSetTitlesPacket titlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title));
        ClientboundSetTitlesPacket subtitlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(subtitle));

        for (ServerPlayer player : players) {
            player.connection.send(new ClientboundSetTitlesPacket(in, stay, out));
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
    }

    public static void broadcastTitle(List<ServerPlayer> players, String title, String subtitle) {
        ClientboundSetTitlesPacket titlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title));
        ClientboundSetTitlesPacket subtitlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(subtitle));

        for (ServerPlayer player : players) {
            sendDefaultTitleLength(player);
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
    }

    private static void sendDefaultTitleLength(ServerPlayer player) {
        player.connection.send(new ClientboundSetTitlesPacket(10, 60, 20));
    }

    public static void broadcastSound(List<ServerPlayer> players, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        for (ServerPlayer player : players) {
            sendSound(player, soundEvent, soundSource, volume, pitch);
        }
    }

    public static void sendSound(ServerPlayer player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        sendSound(player, new EntityPos(player), soundEvent, soundSource, volume, pitch);
    }

    public static void sendSound(ServerPlayer player, EntityPos position, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(soundEvent, soundSource,
                position.x, position.y, position.z, 16f * volume, pitch));
    }

    public static ServerPlayer getFixedPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer == null) return null;
        return ServerTime.minecraftServer.getPlayerList().getPlayer(serverPlayer.getUUID());
    }

    public static void resetHealthStatus(ServerPlayer player) {
        player.setInvulnerable(false);
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
    }

    public static Object[] getTags(@NotNull ServerPlayer player){
        return player.getTags().toArray();
    }

    public static boolean hasTag(@NotNull ServerPlayer player, @NotNull String tag){
        Object[] result = getTags(player);
        if(result.length > 0) {
            for (Object s : result) {
                if (String.valueOf(s).equalsIgnoreCase(tag)) {
                    return true;
                }
            }
            return String.valueOf(result[0]).equalsIgnoreCase(tag);
        } else {
            return false;
        }
    }

    public static boolean doesPlayerExist(String player){
        return (getPlayerFromName(player) != null);
    }

    public static ServerPlayer getPlayerFromName(String player){
        return Main.server.getPlayerList().getPlayerByName(player);
    }

    public static String returnSetPlayer(ServerPlayer player, String command){
        return command.replaceAll("%player%", String.valueOf(player.getScoreboardName()));
    }

    public static void executeServerCommand(String command, ServerPlayer player){
        if(player != null){
            Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), returnSetPlayer(player, command));
        } else {
            Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), command);
        }
    }

    public static void executePlayerCommand(ServerPlayer player, String command) {
        if(player != null){
            Main.server.getCommands().performCommand(player.createCommandSourceStack(), returnSetPlayer(player, command));
        }
    }

    public static boolean hasPermission(@NotNull CommandSourceStack permission, @Nullable String command, @Nullable int level) {
        return me.lucko.fabric.api.permissions.v0.Permissions.check(permission, command, level);
    }

    public static ServerPlayer getPlayerAttacker(Entity attackerEntity) {
        if (attackerEntity == null) return null;

        if (attackerEntity instanceof ServerPlayer) {
            return (ServerPlayer) attackerEntity;

        } else if (attackerEntity instanceof Projectile) {
            Entity projectileOwner = ((Projectile) attackerEntity).getOwner();
            if (!(projectileOwner instanceof ServerPlayer)) return null;
            return (ServerPlayer) projectileOwner;
        }
        return null;
    }

    public static boolean couldCrit(ServerPlayer player) {
        return !player.isOnGround() && player.fallDistance > 0.0f && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.onClimbable() && !player.isInWater() && !player.isPassenger();
    }

    public static void removeItem(ServerPlayer player, Item item, int count) {
        for (ItemStack itemStack : ItemStackUtil.getInvItems(player)) {
            if (itemStack.getItem() != item) continue;

            if (itemStack.getCount() >= count) {
                itemStack.shrink(count);
                break;
            } else {
                count -= itemStack.getCount();
                itemStack.shrink(itemStack.getCount());
            }
        }
    }

    public static boolean containsUuid(List<ServerPlayer> playerList, ServerPlayer player) {
        for (ServerPlayer listPlayer : playerList) {
            if (listPlayer.getUUID().equals(player.getUUID())) return true;
        }
        return false;
    }

}