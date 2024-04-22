package com.nexia.core.mixin.block;

import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockMixin extends BlockEntity {
    @Unique
    CompoundTag compoundTag = new CompoundTag();

    @Shadow public abstract CompoundTag save(CompoundTag compoundTag);

    @Shadow public abstract void load(BlockState blockState, CompoundTag compoundTag);

    public ChestBlockMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Inject(method = "startOpen", at = @At("HEAD"))
    public void onOpen(Player player, CallbackInfo ci) {
        if (KitRoom.isInKitRoom(player)) {
            this.save(compoundTag);
        }
    }

    @Inject(method = "stopOpen", at = @At("HEAD"))
    public void onClose(Player player, CallbackInfo ci) {
        if (KitRoom.isInKitRoom(player)) {
            this.load(this.getBlockState(), compoundTag);
            compoundTag = new CompoundTag();
        }
    }
}
