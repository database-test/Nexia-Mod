package com.nexia.core.mixin.entity;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.notcoded.codelib.players.AccuratePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends ThrowableItemProjectile {

    public ThrownEnderpearlMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(method = "onHit", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float pearlDamage(float damage) {
        if (getOwner() instanceof ServerPlayer thrower) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(thrower));

            if (BwUtil.isInBedWars(nexiaPlayer)) {
                return BwUtil.getPearlDamage();
            }
            if (FfaSkyUtil.isFfaPlayer(nexiaPlayer)) {
                return 0;
            }

        }
        return damage;
    }


}
