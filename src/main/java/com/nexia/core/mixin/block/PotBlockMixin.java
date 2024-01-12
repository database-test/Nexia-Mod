package com.nexia.core.mixin.block;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public class PotBlockMixin {
    @Inject(method = "use", cancellable = true, at = @At("HEAD"))
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        PlayerData playerData = PlayerDataManager.get(serverPlayer);

        if (BwUtil.isInBedWars(serverPlayer) || (playerData.duelsGame != null || playerData.teamDuelsGame != null) || LobbyUtil.isLobbyWorld(serverPlayer.getLevel()) || (FfaAreas.isFfaWorld(serverPlayer.getLevel()) || com.nexia.ffa.kits.utilities.FfaAreas.isFfaWorld(serverPlayer.getLevel())) && !serverPlayer.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
    }
}