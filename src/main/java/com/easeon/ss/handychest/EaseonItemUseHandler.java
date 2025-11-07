package com.easeon.ss.handychest;

import com.easeon.ss.core.game.EaseonSound;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EaseonItemUseHandler {
    public static ActionResult onUseItem(ServerPlayerEntity mcPlayer, World mcWorld, Hand hand) {
        var world = new EaseonWorld(mcWorld);
        var player = new EaseonPlayer(mcPlayer);
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
                            EaseonSound.play(player, SoundEvents.BLOCK_ENDER_CHEST_CLOSE);
                        }
                    };
                }, item.getName()
            ));

            player.incrementStat(Stats.OPEN_ENDERCHEST);
            player.swingHand(hand);
            world.playSound(player, SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f);

            return ActionResult.SUCCESS;
        } else if (item.isIn(ItemTags.SHULKER_BOXES)) {
            var inventory = getShulkerBoxInventory(item.get());
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, p) ->
                new ShulkerBoxScreenHandler(syncId, inv, inventory) {
                    @Override
                    public void onClosed(PlayerEntity player) {
                        super.onClosed(player);
                        EaseonSound.play(player, SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
                        saveShulkerBoxInventory(item.get(), inventory);
                    }
                },
                item.getName()
            ));

            player.incrementStat(Stats.OPEN_SHULKER_BOX);
            player.swingHand(hand);
            world.playSound(player, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5f);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static SimpleInventory getShulkerBoxInventory(ItemStack stack) {
        ContainerComponent container = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        SimpleInventory inventory = new SimpleInventory(27);
        container.copyTo(inventory.getHeldStacks());
        return inventory;
    }

    private static void saveShulkerBoxInventory(ItemStack stack, SimpleInventory inventory) {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            // 빈 슬롯도 포함 (isEmpty 체크 제거)
            items.add(slotStack.copy());
        }
        stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(items));
    }
}