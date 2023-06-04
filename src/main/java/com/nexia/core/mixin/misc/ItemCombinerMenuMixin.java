package com.nexia.core.mixin.misc;

import com.combatreforged.factory.builder.mixin_interfaces.LevelAccessOwner;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * factory bug fix
 */
@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin implements LevelAccessOwner {
    ContainerLevelAccess access;

    @Unique
    @Override
    public void setContainerLevelAccess(ContainerLevelAccess access) {
        this.access = access;
    }

    @Unique
    @Override
    public ContainerLevelAccess getContainerLevelAccess() {
        return access;
    }
}