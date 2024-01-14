package com.nexia.ffa.kits;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class FfaKit {
    public static ArrayList<FfaKit> ffaKits = new ArrayList<>();
    public static ArrayList<String> stringFfaKits = new ArrayList<>();


    public String id;
    public ItemStack item;

    public static final FfaKit KNIGHT = new FfaKit("knight", new ItemStack(Items.DIAMOND_SWORD));
    public static final FfaKit POSEIDON = new FfaKit("poseidon", new ItemStack(Items.TRIDENT));
    public static final FfaKit BRUTE = new FfaKit("brute", new ItemStack(Items.NETHERITE_CHESTPLATE));
    public static final FfaKit HUNTER = new FfaKit("HUNTER", new ItemStack(Items.CROSSBOW));
    public static final FfaKit NINJA = new FfaKit("NINJA", new ItemStack(Items.SUGAR));

    public static final FfaKit KIT6 = new FfaKit("kit_6", new ItemStack(Items.BARRIER));

    public static final FfaKit KIT7 = new FfaKit("kit_7", new ItemStack(Items.BARRIER));

    public FfaKit(String id, ItemStack item) {
        this.id = id;
        this.item = item;

        FfaKit.ffaKits.add(this);
        FfaKit.stringFfaKits.add(id);
    }

    public static FfaKit identifyKit(String name) {
        if(name.equalsIgnoreCase("knight")) return FfaKit.KNIGHT;
        if(name.equalsIgnoreCase("poseidon")) return FfaKit.POSEIDON;
        if(name.equalsIgnoreCase("brute")) return FfaKit.BRUTE;
        if(name.equalsIgnoreCase("hunter")) return FfaKit.HUNTER;
        if(name.equalsIgnoreCase("ninja")) return FfaKit.NINJA;
        if(name.equalsIgnoreCase("kit6")) return FfaKit.KIT6;
        if(name.equalsIgnoreCase("kit7")) return FfaKit.KIT7;
        return null;
    }
    public void giveKit(ServerPlayer player, boolean clearEffect) {
        Player fPlayer = PlayerUtil.getFactoryPlayer(player);
        PlayerDataManager.get(player).kit = this;

        if(clearEffect) fPlayer.clearEffects();
        fPlayer.runCommand("/loadinventory " + "ffa_kits-" + this.id, 4, false);
    }
}
