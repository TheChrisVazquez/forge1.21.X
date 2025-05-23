package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends ComplexItem {
    public EmptyMapItem(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (pLevel.isClientSide) {
            return InteractionResultHolder.success(itemstack);
        } else {
            itemstack.consume(1, pPlayer);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            pPlayer.level().playSound(null, pPlayer, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, pPlayer.getSoundSource(), 1.0F, 1.0F);
            ItemStack itemstack1 = MapItem.create(pLevel, pPlayer.getBlockX(), pPlayer.getBlockZ(), (byte)0, true, false);
            if (itemstack.isEmpty()) {
                return InteractionResultHolder.consume(itemstack1);
            } else {
                if (!pPlayer.getInventory().add(itemstack1.copy())) {
                    pPlayer.drop(itemstack1, false);
                }

                return InteractionResultHolder.consume(itemstack);
            }
        }
    }
}