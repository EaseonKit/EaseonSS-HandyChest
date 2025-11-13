package com.easeon.ss.handychest;

import com.easeon.ss.core.helper.ItemHelper;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class EaseonItemUseHandler {
    public static ActionResult onUseItem(EaseonWorld world, EaseonPlayer player, Hand hand) {
        var item = player.getStackInHand(hand);

        if (item.of(Items.ENDER_CHEST)) {
            var enderChest = player.getEnderChest();
            enderChest.onOpen(player.get());

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, p) -> {
                    var handler = GenericContainerScreenHandler.createGeneric9x3(syncId, inv, enderChest);
                    return new GenericContainerScreenHandler(handler.getType(), syncId, inv, enderChest, 3) {
                        @Override
                        public void onClosed(PlayerEntity player) {
                            super.onClosed(player);
                            enderChest.onClose(player);
                            world.playSound(player.getEntityPos(), SoundEvents.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 1.0f);
                        }
                    };
                }, item.getName()
            ));

            player.incrementStat(Stats.OPEN_ENDERCHEST);
            player.swingHand(hand);
            world.playSound(player.getPos(), SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f);

            return ActionResult.SUCCESS;

        } else if (item.isIn(ItemTags.SHULKER_BOXES)) {
            var inventory = ItemHelper.getShulkerBoxInventory(item);
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, p) ->
                new ShulkerBoxScreenHandler(syncId, inv, inventory) {
                    @Override
                    public void onClosed(PlayerEntity player) {
                        super.onClosed(player);
                        world.playSound(player.getEntityPos(), SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 1.0f);
                        ItemHelper.saveShulkerBoxInventory(item, inventory);
                    }
                },
                item.getName()
            ));

            player.incrementStat(Stats.OPEN_SHULKER_BOX);
            player.swingHand(hand);
            world.playSound(player.getPos(), SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5f);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}