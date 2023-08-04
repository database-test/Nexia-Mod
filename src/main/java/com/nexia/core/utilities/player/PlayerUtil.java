package com.nexia.core.utilities.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.bossevents.CustomBossEvent;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PlayerUtil {

    public static HashMap<Player, ServerPlayer> cachedServerPlayer = new HashMap<>();

    public static HashMap<ServerPlayer, Player> cachedFactoryPlayers = new HashMap<>();

    public static void broadcast(List<ServerPlayer> players, String string) {
        broadcast(players, new TextComponent(string));
    }

    public static void broadcast(List<ServerPlayer> players, Component component) {
        for (ServerPlayer player : players) {
            player.sendMessage(component, Util.NIL_UUID);
        }
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

    public static void broadcastSound(List<ServerPlayer> players, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        for (ServerPlayer player : players) {
            sendSound(player, soundEvent, soundSource, volume, pitch);
        }
    }

    public static boolean sendBossbar(CustomBossEvent customBossEvent, @Nullable Collection<ServerPlayer> collection) {
        if(collection == null) {
            customBossEvent.removeAllPlayers();
            return true;
        }
        return customBossEvent.setPlayers(collection);
    }

    public static boolean sendBossbar(CustomBossEvent customBossEvent, ServerPlayer player, boolean remove) {
        if(remove) {
            customBossEvent.removePlayer(player);
            return true;
        }
        if(customBossEvent.getPlayers().contains(player)) return false;
        customBossEvent.addPlayer(player);
        return true;
    }

    public static void resetHealthStatus(@NotNull net.minecraft.world.entity.player.Player player) {
        player.setInvulnerable(false);
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
    }


    public static Player getFactoryPlayer(@NotNull ServerPlayer minecraftPlayer) {
        Player fPlayer = cachedFactoryPlayers.get(minecraftPlayer);
        if(fPlayer == null) {
            fPlayer = ServerTime.factoryServer.getPlayer(minecraftPlayer.getUUID());
            cachedFactoryPlayers.put(minecraftPlayer, fPlayer);
        }
        return fPlayer;
    }

    private static void sendDefaultTitleLength(ServerPlayer player) {
        player.connection.send(new ClientboundSetTitlesPacket(10, 60, 20));
    }

    public static void sendActionbar(ServerPlayer player, String string){
        player.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.ACTIONBAR,
                LegacyChatFormat.format(string)));
    }

    public static Player getFactoryPlayerFromName(@NotNull String player) {
        if(player.trim().length() == 0) return null;
        return ServerTime.factoryServer.getPlayer(player);
    }

    public static void sendTitle(ServerPlayer player, String title, String sub, int in, int stay, int out) {
        player.connection.send(new ClientboundSetTitlesPacket(in, stay, out));
        player.connection.send(new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title)));
        player.connection.send(new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(sub)));
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

    public static ServerPlayer getMinecraftPlayer(@NotNull Player player){

        ServerPlayer sPlayer = cachedServerPlayer.get(player);
        if(sPlayer == null) {
            sPlayer = ServerTime.minecraftServer.getPlayerList().getPlayer(player.getUUID());
            cachedServerPlayer.put(player, sPlayer);
        }
        return sPlayer;
    }

    public static ServerPlayer getMinecraftPlayerFromName(@NotNull String player){
        return ServerTime.minecraftServer.getPlayerList().getPlayerByName(player);
    }

    public static void resetHealthStatus(@NotNull Player player) {
        player.setInvulnerabilityTime(0);
        player.clearEffects();
        player.setHealth(20);
        player.setFoodLevel(20);
    }
    public static boolean hasPermission(@NotNull CommandSourceStack permission, @NotNull String command, int level) {
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

    public static void sendSound(ServerPlayer player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        sendSound(player, new EntityPos(player), soundEvent, soundSource, volume, pitch);
    }

    public static ServerPlayer getFixedPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer == null) return null;
        return ServerTime.minecraftServer.getPlayerList().getPlayer(serverPlayer.getUUID());
    }

    public static void sendSound(ServerPlayer player, EntityPos position, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(soundEvent, soundSource,
                position.x, position.y, position.z, 16f * volume, pitch));
    }

}
