package com.nexia.core.mixin.item;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.sky.utilities.FfaAreas;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.notcoded.codelib.players.AccuratePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderpearlItem.class)
public class EnderPearlItemMixin extends Item {

    @Unique
    private static ServerPlayer thrower;

    public EnderPearlItemMixin(Item.Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void setPlayer(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (player instanceof ServerPlayer serverPlayer) {
            thrower = serverPlayer;
            NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(serverPlayer));

            if ((FfaAreas.isFfaWorld(serverPlayer.getLevel()) && FfaSkyUtil.wasInSpawn.contains(serverPlayer.getUUID())) || (com.nexia.core.utilities.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(PlayerGameMode.LOBBY) && com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(nexiaPlayer).gameMode.equals(DuelGameMode.LOBBY))) {
                cir.setReturnValue(InteractionResultHolder.pass(serverPlayer.getItemInHand(interactionHand)));
                nexiaPlayer.refreshInventory();
                return;
            }

        }

    }

    @ModifyArg(method = "use", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"))
    private int setPearlCooldown(int original) {
        int time = original;
        if (thrower == null) return time;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(thrower));

        DuelGameMode duelGameMode = PlayerDataManager.get(nexiaPlayer).gameMode;

        if(duelGameMode.equals(DuelGameMode.POT) || duelGameMode.equals(DuelGameMode.NETH_POT)) time = 300;
        if (FfaSkyUtil.isFfaPlayer(nexiaPlayer)) time = 10;

        return time;
    }
}
