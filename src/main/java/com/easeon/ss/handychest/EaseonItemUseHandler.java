package com.easeon.ss.handychest;

import com.easeon.ss.core.helper.ItemHelper;
import com.easeon.ss.core.wrapper.EaseonItem;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public class EaseonItemUseHandler {
    public static InteractionResult onUseItem(EaseonWorld world, EaseonPlayer player, InteractionHand hand) {
        var item = player.getStackInHand(hand);

        if (item.of(Items.ENDER_CHEST)) {
            var enderChest = player.getEnderChest();
            enderChest.startOpen(player.get());

            player.openHandledScreen(new SimpleMenuProvider((syncId, inv, p) -> {
                var handler = ChestMenu.threeRows(syncId, inv, enderChest);
                return new ChestMenu(handler.getType(), syncId, inv, enderChest, 3) {
                    @Override
                    public void removed(@NotNull Player player) {
                        super.removed(player);
                        enderChest.stopOpen(player);
                        world.playSound(player.position(), SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 1.0f);
                    }
                };
            }, item.getItemName()
            ));

            player.incrementStat(Stats.OPEN_ENDERCHEST);
            player.swingHand(hand);
            world.playSound(player.getPos(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5f);

            return InteractionResult.SUCCESS;

        } else if (item.isIn(ItemTags.SHULKER_BOXES)) {
            var inventory = ItemHelper.getShulkerBoxInventory(item);
            var container = new SimpleContainer(inventory.toArray(new net.minecraft.world.item.ItemStack[0]));
            player.openHandledScreen(new SimpleMenuProvider((syncId, inv, p) ->
                    new ShulkerBoxMenu(syncId, inv, container) {
                        @Override
                        public void removed(@NotNull Player player) {
                            super.removed(player);
                            world.playSound(player.position(), SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 1.0f);

                            // 현재 플레이어가 손에 들고 있는 셜커박스를 다시 가져옴
                            var currentItem = new EaseonItem(player.getItemInHand(hand));
                            if (currentItem.isIn(ItemTags.SHULKER_BOXES)) {
                                // Container의 내용을 다시 NonNullList로 변환하여 저장
                                for (int i = 0; i < container.getContainerSize(); i++) {
                                    inventory.set(i, container.getItem(i));
                                }
                                ItemHelper.saveShulkerBoxInventory(currentItem, inventory);
                            }
                        }
                    },
                    item.getItemName()
            ));

            player.incrementStat(Stats.OPEN_SHULKER_BOX);
            player.swingHand(hand);
            world.playSound(player.getPos(), SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5f);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}